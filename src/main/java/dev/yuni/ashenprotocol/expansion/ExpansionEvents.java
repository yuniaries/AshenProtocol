package dev.yuni.ashenprotocol.expansion;
import dev.yuni.ashenprotocol.AshenProtocol;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
@Mod.EventBusSubscriber(modid=AshenProtocol.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ExpansionEvents {
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e) {
        for(int i=0;i<ExpansionContent.ENEMIES.length;i++) e.put(ExpansionContent.MOBS.get(ExpansionContent.ENEMIES[i]).get(),ProtocolMob.attributes(i).build());
    }
    @SubscribeEvent public static void placements(SpawnPlacementRegisterEvent e) {
        for(int i=0;i<4;i++) e.register(ExpansionContent.MOBS.get(ExpansionContent.ENEMIES[i]).get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Monster::checkMonsterSpawnRules,SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
