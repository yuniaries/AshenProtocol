package dev.yuni.ashenprotocol.expansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class ArchiveCompass extends Item {
    public static final String[] SITES={"ash_outpost","tidal_archive","frost_observatory","sunken_workshop","rift_foundry","last_archive"};
    public static final String[] NAMES={"灰烬哨站","潮汐档案馆","冰原观测站","沙海工坊","裂隙铸造所","终末档案库"};
    public ArchiveCompass(Properties p) { super(p); }
    @Override public InteractionResultHolder<ItemStack> use(Level l,net.minecraft.world.entity.player.Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand); if(l.isClientSide) return InteractionResultHolder.success(stack);
        if(!(player instanceof ServerPlayer p)) return InteractionResultHolder.pass(stack);
        var nbt=stack.getOrCreateTag(); int selected=Math.floorMod(nbt.getInt("Site"),SITES.length);
        if(p.isShiftKeyDown()) { selected=(selected+1)%SITES.length; nbt.putInt("Site",selected); p.displayClientMessage(Component.literal("遗迹目标："+NAMES[selected]+"。松开潜行后右键定位。"),true); return InteractionResultHolder.consume(stack); }
        if(p.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        String required=selected==4 ? "minecraft:the_nether" : selected==5 ? "minecraft:the_end" : "minecraft:overworld";
        if(!l.dimension().location().toString().equals(required) && !(selected<4 && l.dimension().equals(CoordinateGate.REALM))) { p.sendSystemMessage(Component.literal("目标位于 "+required+"，请先前往对应维度。")); return InteractionResultHolder.fail(stack); }
        // Structure placement lookup does not explore/generate arbitrary terrain or consume resources.
        var tag=TagKey.create(Registries.STRUCTURE,new ResourceLocation("ashenprotocol",SITES[selected]));
        var pos=p.serverLevel().findNearestMapStructure(tag,p.blockPosition(),12,false);
        p.getCooldowns().addCooldown(this,200);
        p.sendSystemMessage(Component.literal(pos==null ? "附近未检索到"+NAMES[selected]+"。换一个区域再试；旧存档需要探索新地形。" : NAMES[selected]+"：X "+pos.getX()+" / Z "+pos.getZ()+"。抵达后留意箱子与祭坛。"));
        return InteractionResultHolder.consume(stack);
    }
}
