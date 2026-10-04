package dev.yuni.ashenprotocol.campaign;
import dev.yuni.ashenprotocol.expansion.ExpansionContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
public final class ContractTerminal extends Block {
    public static final String[] REQUESTS={"ash_ingot","ash_wheat","signal_ingot","signal_berry","memory_shard","frost_root","rift_ingot","void_shard"};
    public static final int[] COUNTS={8,16,6,12,8,12,4,4};
    public ContractTerminal(Properties p){super(p);}
    @Override public InteractionResult use(BlockState state,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;
        if(l.isClientSide)return InteractionResult.SUCCESS;
        var saved=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);int choice=Math.floorMod(saved.getInt("AshenContractChoice"),8);
        if(p.isShiftKeyDown()){choice=(choice+1)%8;saved.putInt("AshenContractChoice",choice);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,saved);}
        var held=p.getItemInHand(hand);var required=ExpansionContent.item(REQUESTS[choice]);
        if(!p.isShiftKeyDown()&&held.is(required)&&held.getCount()>=COUNTS[choice]){
            long ready=saved.getLong("AshenContractReady");
            if(ready>l.getGameTime()){p.displayClientMessage(Component.literal("委托整理中，剩余 "+(ready-l.getGameTime())/20+" 秒。"),true);return InteractionResult.CONSUME;}
            held.shrink(COUNTS[choice]);p.getInventory().placeItemBackInInventory(new ItemStack(ExpansionContent.item("contract_token"),3+choice));
            saved.putLong("AshenContractReady",l.getGameTime()+1200);saved.putInt("AshenContracts",saved.getInt("AshenContracts")+1);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,saved);
            if(p instanceof ServerPlayer sp){sp.giveExperiencePoints(20);dev.yuni.ashenprotocol.progress.Progression.complete(sp,51);}
            p.sendSystemMessage(Component.literal("[灰烬协议] 委托完成，获得 "+(3+choice)+" 枚凭证。"));
        }else if(!p.isShiftKeyDown()&&held.is(ExpansionContent.item("contract_token"))&&held.getCount()>=2){
            held.shrink(2);p.getInventory().placeItemBackInInventory(new ItemStack(ExpansionContent.item(CampaignContent.CROPS[choice]+"_seeds"),4));
            p.sendSystemMessage(Component.literal("[灰烬协议] 换得4份种子："+new ItemStack(ExpansionContent.item(CampaignContent.CROPS[choice]+"_seeds")).getHoverName().getString()));
        }else p.sendSystemMessage(Component.literal("委托 "+(choice+1)+"：手持 "+COUNTS[choice]+" × "+new ItemStack(required).getHoverName().getString()+" 提交，奖励 "+(3+choice)+" 凭证；手持2凭证换4份对应种子。潜行右键切换委托。"));
        return InteractionResult.CONSUME;
    }
}
