package dev.yuni.ashenprotocol.progress;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.network.ProtocolNetwork;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AshenProtocol.MOD_ID)
public final class Progression {
    public static final String[] TITLES = {"协议接入", "读取信号", "网络节点", "力量的代价", "因果回收", "熵的结晶", "主动净化", "世界重建", "地下余灰", "材料压制", "追踪遗迹", "灰烬武装", "余烬守卫", "信号精炼", "潮汐武装", "织潮者", "编织回声", "两极档案", "裂隙锻造", "失谐执政官", "区域复苏", "终末坐标", "最后的测绘者", "复苏新纪元", "耕地与余灰", "信号浆果", "潮汐水田", "霜根储备", "日椒温室", "回声豆田", "裂瓜培育", "记忆药圃", "远征口粮", "现场救护", "毒性净化", "碳化生产", "拉丝生产", "钢铁路线", "信号电路", "堆肥循环", "生物燃料", "提取药剂", "回声纺织", "晶格框架", "钢铁护甲", "装配转子", "精密齿轮", "水流过滤", "密封档案", "记忆矩阵", "契约终端", "完成委托", "远征信标", "相位线圈", "导航阵列", "深层入口", "深矿采集", "深层冶炼", "虚空矩阵", "深渊钥匙", "深渊武装", "深渊咏者", "深渊核心", "风暴晶屑", "风暴连射", "风暴钥匙", "风暴君王", "风暴核心", "根系树脂", "根源钥匙", "根系营养", "根源母体", "根源核心", "曙光碎片", "曙光钥匙", "曙光执政官", "曙光核心", "生命圈核心", "六波远征", "完整生态复苏", "百次生产", "自动运输", "农田丰收", "十次委托", "战地医护", "相位量产", "生命圈启动", "坐标锚定", "跨越深层", "失序全境", "深层全境", "远征老兵", "八阶守卫", "工业基石", "作物齐备", "无熵新世界"};
    private static CompoundTag persisted(ServerPlayer p) { return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG); }
    public static long[] bits(ServerPlayer p){
        CompoundTag tag=persisted(p);long[] saved=tag.getLongArray("AshenMilestoneBits");
        if(saved.length==2)return saved;
        return new long[]{Integer.toUnsignedLong(tag.getInt("AshenMilestones")),0};
    }
    public static int mask(ServerPlayer p){return (int)bits(p)[0];}
    public static boolean completed(ServerPlayer p,int index){long[] b=bits(p);return index>=0&&index<TITLES.length&&(b[index/64]&(1L<<(index%64)))!=0;}
    public static void complete(ServerPlayer p,int index){
        if(index<0||index>=TITLES.length)throw new IllegalArgumentException("milestone index");
        if(completed(p,index))return;
        CompoundTag tag=persisted(p);long[] b=bits(p);b[index/64]|=1L<<(index%64);
        tag.putLongArray("AshenMilestoneBits",b);tag.putInt("AshenMilestones",(int)b[0]);
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG,tag);
        p.sendSystemMessage(Component.literal("[灰烬协议] 已完成："+TITLES[index]));p.giveExperiencePoints(15+Math.min(index,30)*5);
    }
    public static void addCounter(ServerPlayer p,String key,int amount){
        if(amount<=0)return;var tag=persisted(p);tag.putInt(key,Math.min(1000000,tag.getInt(key)+amount));p.getPersistentData().put(Player.PERSISTED_NBT_TAG,tag);
    }
    public static int counter(ServerPlayer p,String key){return persisted(p).getInt(key);}
    public static void markBoss(ServerPlayer p,int tier){var tag=persisted(p);tag.putInt("AshenBossKills",tag.getInt("AshenBossKills")|(1<<tier));p.getPersistentData().put(Player.PERSISTED_NBT_TAG,tag);}
    public static void markCrop(ServerPlayer p,int crop){var tag=persisted(p);tag.putInt("AshenHarvestKinds",tag.getInt("AshenHarvestKinds")|(1<<crop));p.getPersistentData().put(Player.PERSISTED_NBT_TAG,tag);addCounter(p,"AshenHarvests",1);}
    private static boolean has(ServerPlayer p, Item item) { return p.getInventory().contains(new ItemStack(item)); }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p) || p.tickCount % 20 != 0 || !p.isAlive()) return;
        if (has(p, ModItems.PROTOCOL_FRAGMENT.get())) complete(p, 0);
        if (has(p, ModItems.ENTROPY_METER.get())) complete(p, 1);
        if (has(p, ModItems.PROTOCOL_RELAY_ITEM.get())) complete(p, 2);
        if (has(p, ModItems.ECHO_RESIDUE.get())) complete(p, 4);
        if (has(p, ModItems.ENTROPY_CRYSTAL.get())) complete(p, 5);
        var data = ProtocolSavedData.get(p.getServer());
        if ((mask(p) & (1 << 6)) != 0 && data.getIntegrity() >= 3000 && data.getEntropy() < 1000) complete(p, 7);
        String[] goals={"raw_ash","ash_ingot","archive_compass","ash_chestplate","cinder_core","signal_ingot","tide_chestplate","tide_core","woven_echo","sun_disk","rift_ingot","rift_core","restoration_array","void_shard","atlas_core"};
        for(int i=0;i<goals.length;i++) if(has(p, dev.yuni.ashenprotocol.expansion.ExpansionContent.item(goals[i]))) {
            if(i!=9 || has(p,dev.yuni.ashenprotocol.expansion.ExpansionContent.item("frost_lens"))) complete(p,8+i);
        }
        if((mask(p)&(1<<22))!=0 && data.getIntegrity()>=8000 && data.getEntropy()<500) complete(p,23);
        String[] campaignGoals={"ash_wheat","signal_berry","tide_rice","frost_root","sun_pepper","echo_bean","rift_melon","memory_herb","expedition_meal","field_bandage","purity_capsule","carbon_dust","copper_wire","steel_ingot","signal_circuit","fertile_compost","bio_fuel","healing_extract","echo_fiber","lattice_frame","steel_chestplate","rotor","precision_gear","water_filter","sealed_archive","memory_matrix","contract_terminal",null,"expedition_beacon","phase_coil","navigation_array","deep_gate","deep_ore_fragment","deep_ingot","void_matrix","abyss_key","echo_chestplate",null,"abyss_core","storm_shard","storm_repeater","storm_key",null,"storm_core","root_resin","root_key","nutrient_gel",null,"root_core","dawn_fragment","dawn_key",null,"dawn_core",null,null,null};
        for(int i=0;i<campaignGoals.length;i++)if(campaignGoals[i]!=null&&has(p,dev.yuni.ashenprotocol.expansion.ExpansionContent.item(campaignGoals[i])))complete(p,24+i);
        if(has(p,dev.yuni.ashenprotocol.expansion.ExpansionContent.item("biosphere_core")))complete(p,77);
        if(completed(p,75)&&completed(p,78)&&data.getIntegrity()>=9500&&data.getEntropy()<200)complete(p,79);
        String[] measuredKeys={"AshenJobs","AshenAutoOutputs","AshenHarvests","AshenContracts","AshenMedicalUses","AshenPhaseJobs","AshenBiosphereJobs","AshenAnchorJobs"};
        int[] thresholds={100,64,32,10,10,10,1,1};for(int i=0;i<measuredKeys.length;i++)if(counter(p,measuredKeys[i])>=thresholds[i])complete(p,80+i);
        String[] lostBiomes={"ash_steppe","signal_grove","tide_mire","frost_expanse","sun_scar","rift_highlands"};
        String[] deepBiomes={"abyss_shore","storm_plateau","root_canopy","dawn_wastes","memory_fen","harmonic_ridge"};
        String biome=p.serverLevel().getBiome(p.blockPosition()).unwrapKey().map(k->k.location().toString()).orElse("");
        var stats=persisted(p);
        for(int i=0;i<6;i++){
            if(p.level().dimension().equals(dev.yuni.ashenprotocol.expansion.CoordinateGate.REALM)&&biome.equals("ashenprotocol:"+lostBiomes[i]))stats.putInt("AshenLostVisits",stats.getInt("AshenLostVisits")|(1<<i));
            if(p.level().dimension().equals(dev.yuni.ashenprotocol.campaign.DeepGate.REALM)&&biome.equals("ashenprotocol:"+deepBiomes[i]))stats.putInt("AshenDeepVisits",stats.getInt("AshenDeepVisits")|(1<<i));
        }
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG,stats);
        if(p.level().dimension().equals(dev.yuni.ashenprotocol.campaign.DeepGate.REALM))complete(p,88);
        if(stats.getInt("AshenLostVisits")==63)complete(p,89);if(stats.getInt("AshenDeepVisits")==63)complete(p,90);
        if(stats.getInt("AshenArenaWins")>=3)complete(p,91);if(stats.getInt("AshenBossKills")==255)complete(p,92);
        if(stats.getInt("AshenJobs")>=1000)complete(p,93);if(stats.getInt("AshenHarvestKinds")==255)complete(p,94);
        if(completed(p,79)&&data.getIntegrity()==10000&&data.getEntropy()==0)complete(p,95);
        ProtocolNetwork.sync(p);
    }
    private Progression() {}
}
