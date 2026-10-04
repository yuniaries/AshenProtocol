package dev.yuni.ashenprotocol.campaign;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=dev.yuni.ashenprotocol.AshenProtocol.MOD_ID)
public final class FarmingEvents {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void harvest(net.minecraftforge.event.level.BlockEvent.BreakEvent e){
  if(e.getPlayer() instanceof net.minecraft.server.level.ServerPlayer p&&e.getState().getBlock() instanceof ProtocolCrop crop&&crop.isMaxAge(e.getState())){
   String name=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(crop).getPath();
   for(int i=0;i<CampaignContent.CROPS.length;i++)if(name.equals(CampaignContent.CROPS[i]+"_crop"))dev.yuni.ashenprotocol.progress.Progression.markCrop(p,i);
  }
 }
}
