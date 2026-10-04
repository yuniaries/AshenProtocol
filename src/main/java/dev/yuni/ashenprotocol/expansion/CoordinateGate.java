package dev.yuni.ashenprotocol.expansion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
public final class CoordinateGate extends Item {
    public static final ResourceKey<Level> REALM=ResourceKey.create(Registries.DIMENSION,new ResourceLocation("ashenprotocol","lost_coordinates"));
    public CoordinateGate(Properties p) { super(p); }
    @Override public InteractionResultHolder<ItemStack> use(Level l,net.minecraft.world.entity.player.Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);if(l.isClientSide)return InteractionResultHolder.success(stack);
        if(!(player instanceof ServerPlayer p)||p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(stack);
        var saved=p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
        boolean returning=l.dimension().equals(REALM);
        if(!returning && !l.dimension().equals(Level.OVERWORLD)) { p.sendSystemMessage(Component.literal("坐标门只能在主世界与失序领域之间开启，请先返回主世界。"));return InteractionResultHolder.fail(stack); }
        var key=returning ? ResourceKey.create(Registries.DIMENSION,ResourceLocation.tryParse(saved.getString("AshenOrigin"))==null ? new ResourceLocation("minecraft","overworld") : new ResourceLocation(saved.getString("AshenOrigin"))) : REALM;
        var destination=p.getServer().getLevel(key);
        if(destination==null) { p.sendSystemMessage(Component.literal("目的地维度不可用，请确认完整安装灰烬协议。"));return InteractionResultHolder.fail(stack); }
        double x=returning && saved.contains("AshenOriginX") ? saved.getDouble("AshenOriginX") : p.getX();
        double z=returning && saved.contains("AshenOriginZ") ? saved.getDouble("AshenOriginZ") : p.getZ();
        // Load destination terrain and choose dry headroom above the motion-blocking heightmap.
        var surface=destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,BlockPos.containing(x,0,z));
        BlockPos landing=null;
        for(int radius=0;radius<=8&&landing==null;radius++)for(int dx=-radius;dx<=radius&&landing==null;dx++)for(int dz=-radius;dz<=radius&&landing==null;dz++) {
            var top=destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,surface.offset(dx,0,dz));
            for(int dy=0;dy<=2;dy++) {
                var at=top.above(dy);var support=at.below();
                var body=new net.minecraft.world.phys.AABB(at.getX()+.2,at.getY(),at.getZ()+.2,at.getX()+.8,at.getY()+1.8,at.getZ()+.8);
                if(!destination.getBlockState(support).getCollisionShape(destination,support).isEmpty() && destination.getFluidState(support).isEmpty()
                    && destination.getFluidState(at).isEmpty() && destination.getFluidState(at.above()).isEmpty()
                    && at.getY()<destination.getMaxBuildHeight()-2 && destination.noCollision(p,body)) { landing=at;break; }
            }
        }
        if(landing==null && !returning) {
            // Arrival over water: place a small platform in unoccupied air, never replace a player's solid blocks.
            int platformY=Math.max(destination.getSeaLevel(),surface.getY())+1;
            var base=BlockPos.containing(x,platformY,z);boolean clear=platformY+3<destination.getMaxBuildHeight();
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(int dy=0;dy<=3;dy++)
                if(!destination.getBlockState(base.offset(dx,dy,dz)).isAir()) clear=false;
            if(clear) {
                for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) destination.setBlockAndUpdate(base.offset(dx,0,dz),ExpansionContent.BLOCKS.get("archive_bricks").get().defaultBlockState());
                landing=base.above();
            }
        }
        if(landing==null) { p.sendSystemMessage(Component.literal("附近没有安全落点。换个位置再开启坐标门；未扣除任何物品。"));return InteractionResultHolder.fail(stack); }
        if(!returning) { saved.putString("AshenOrigin",l.dimension().location().toString());saved.putDouble("AshenOriginX",p.getX());saved.putDouble("AshenOriginZ",p.getZ());p.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,saved); }
        p.teleportTo(destination,landing.getX()+.5,landing.getY(),landing.getZ()+.5,p.getYRot(),p.getXRot());p.fallDistance=0;
        p.getCooldowns().addCooldown(this,200);p.sendSystemMessage(Component.literal(returning ? "已返回原维度的地表安全落点。" : "已抵达失序领域。再次使用坐标门可返回；这里禁止床和重生锚，请在主世界设置重生点。"));
        return InteractionResultHolder.consume(stack);
    }
}
