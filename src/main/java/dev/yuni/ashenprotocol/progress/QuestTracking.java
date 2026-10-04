package dev.yuni.ashenprotocol.progress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
public final class QuestTracking {
 public static int selected(ServerPlayer p){var tag=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);int id=tag.contains("AshenTrackedQuest")?tag.getInt("AshenTrackedQuest"):-2;if(id==-1)return -1;if(id<0||id>=Progression.TITLES.length||Progression.completed(p,id))return QuestCatalog.recommend(i->Progression.completed(p,i));return id;}
 public static boolean select(ServerPlayer p,int id){if(id< -1||id>=Progression.TITLES.length)return false;var tag=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);tag.putInt("AshenTrackedQuest",id);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,tag);return true;}
}
