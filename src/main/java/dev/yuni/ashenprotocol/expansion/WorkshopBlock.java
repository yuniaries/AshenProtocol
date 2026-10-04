package dev.yuni.ashenprotocol.expansion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class WorkshopBlock extends BaseEntityBlock {
    public final String machine;
    public WorkshopBlock(Properties p, String id) { super(p); machine = id; }
    public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new WorkshopEntity(p, s); }
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) { return l.isClientSide ? null : createTickerHelper(t, ExpansionContent.WORKSHOP.get(), WorkshopEntity::tick); }
    @Override public InteractionResult use(BlockState s, Level l, BlockPos pos, Player p, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!l.isClientSide && l.getBlockEntity(pos) instanceof WorkshopEntity be) be.interact(p, hand);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public void onRemove(BlockState s, Level l, BlockPos p, BlockState next, boolean moving) {
        if (!s.is(next.getBlock()) && l.getBlockEntity(p) instanceof WorkshopEntity be) be.dropInventory();
        super.onRemove(s,l,p,next,moving);
    }
}
