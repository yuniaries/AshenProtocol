package dev.yuni.ashenprotocol.campaign;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import dev.yuni.ashenprotocol.expansion.ExpansionContent;
public final class ExpeditionBeacon extends BaseEntityBlock {
    public ExpeditionBeacon(Properties p){super(p);}
    public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ExpeditionArena(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,ExpansionContent.ARENA.get(),ExpeditionArena::tick);}
    @Override public InteractionResult use(BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult h){
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;
        if(!l.isClientSide&&l.getBlockEntity(pos) instanceof ExpeditionArena arena)arena.start(p,hand);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){
        if(!s.is(n.getBlock())&&l.getBlockEntity(p) instanceof ExpeditionArena arena)arena.cancel();super.onRemove(s,l,p,n,moving);
    }
}
