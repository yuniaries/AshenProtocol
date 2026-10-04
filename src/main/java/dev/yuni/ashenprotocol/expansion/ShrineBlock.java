package dev.yuni.ashenprotocol.expansion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class ShrineBlock extends Block {
    public final int tier;
    public ShrineBlock(Properties p,int tier) { super(p); this.tier=tier; }
    @Override public InteractionResult use(BlockState s, Level l, BlockPos pos, Player p, InteractionHand hand, BlockHitResult hit) {
        if (hand!=InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (l.isClientSide) return InteractionResult.SUCCESS;
        var key=ExpansionContent.item(ExpansionContent.KEYS[tier]);
        if (!p.getItemInHand(hand).is(key)) { p.displayClientMessage(Component.literal("需要："+new net.minecraft.world.item.ItemStack(key).getHoverName().getString()+"；准备好装备后右键召唤守卫。"),true); return InteractionResult.CONSUME; }
        if (l.getDifficulty()==Difficulty.PEACEFUL) { p.displayClientMessage(Component.literal("和平难度无法召唤守卫，钥匙未消耗。"),true); return InteractionResult.CONSUME; }
        if (!l.getEntitiesOfClass(ProtocolMob.class,new net.minecraft.world.phys.AABB(pos).inflate(64),m -> m.isBoss() && m.isAlive()).isEmpty()) { p.displayClientMessage(Component.literal("附近已有守卫，先完成当前挑战。"),true); return InteractionResult.CONSUME; }
        var mob=ExpansionContent.MOBS.get(ExpansionContent.ENEMIES[ExpansionContent.BOSS_VARIANTS[tier]]).get().create(l);
        BlockPos spawn=null;
        for (int[] offset : new int[][]{{3,0},{-3,0},{0,3},{0,-3}}) {
            var candidate=pos.offset(offset[0],0,offset[1]);
            mob.moveTo(candidate.getX()+.5,candidate.getY(),candidate.getZ()+.5,p.getYRot(),0);
            l.getChunkAt(candidate);
            if (l.getBlockState(candidate.below()).isSolid() && l.noCollision(mob)) { spawn=candidate; break; }
        }
        if (spawn==null) { p.displayClientMessage(Component.literal("祭坛周围需要至少3格高的空地和坚实地面；钥匙未消耗。"),true); return InteractionResult.CONSUME; }
        mob.setPersistenceRequired(); mob.setTarget(p); mob.setHome(pos);
        if (!((ServerLevel)l).addFreshEntity(mob)) return InteractionResult.FAIL;
        if (!p.isCreative()) p.getItemInHand(hand).shrink(1);
        p.sendSystemMessage(Component.literal("[灰烬协议] 坐标守卫已响应。低血量会进入强化阶段，留意蓄力粒子！"));
        return InteractionResult.CONSUME;
    }
}
