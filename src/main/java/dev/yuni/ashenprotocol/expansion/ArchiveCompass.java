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
    public static final String[] SITES={"ash_outpost","tidal_archive","frost_observatory","sunken_workshop","rift_foundry","last_archive","growers_depot","signal_exchange","storm_observatory","root_sanctuary","abyss_cathedral","storm_citadel","root_vault","dawn_spire","memory_lab","echo_quarry","nether_pumpworks","ender_foundry"};
    public static final String[] NAMES={"灰烬哨站","潮汐档案馆","冰原观测站","沙海工坊","裂隙铸造所","终末档案库","培育者补给站","信号交易所","风暴观测所","根系养护站","深渊大教堂","风暴堡垒","根源藏库","曙光高塔","记忆实验室","回声采掘站","下界泵站","终界装配厂"};
    public ArchiveCompass(Properties p) { super(p); }
    @Override public InteractionResultHolder<ItemStack> use(Level l,net.minecraft.world.entity.player.Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand); if(l.isClientSide) return InteractionResultHolder.success(stack);
        if(!(player instanceof ServerPlayer p)) return InteractionResultHolder.pass(stack);
        var nbt=stack.getOrCreateTag(); int selected=Math.floorMod(nbt.getInt("Site"),SITES.length);
        if(p.isShiftKeyDown()) { selected=(selected+1)%SITES.length; nbt.putInt("Site",selected); p.displayClientMessage(Component.literal("遗迹目标："+NAMES[selected]+"。松开潜行后右键定位。"),true); return InteractionResultHolder.consume(stack); }
        if(p.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        String required=(selected==4||selected==16) ? "minecraft:the_nether" : (selected==5||selected==17) ? "minecraft:the_end" : selected>=10&&selected<=15 ? "ashenprotocol:echo_depths" : "minecraft:overworld";
        if(!l.dimension().location().toString().equals(required) && !((selected<4||selected>=6&&selected<=9) && l.dimension().equals(CoordinateGate.REALM))) { p.sendSystemMessage(Component.literal("目标位于 "+required+"，请先前往对应维度。")); return InteractionResultHolder.fail(stack); }
        // Structure placement lookup does not explore/generate arbitrary terrain or consume resources.
        var tag=TagKey.create(Registries.STRUCTURE,new ResourceLocation("ashenprotocol",SITES[selected]));
        var pos=p.serverLevel().findNearestMapStructure(tag,p.blockPosition(),12,false);
        p.getCooldowns().addCooldown(this,200);
        p.sendSystemMessage(Component.literal(pos==null ? "附近未检索到"+NAMES[selected]+"。换一个区域再试；旧存档需要探索新地形。" : NAMES[selected]+"：X "+pos.getX()+" / Z "+pos.getZ()+"。抵达后留意箱子与祭坛。"));
        return InteractionResultHolder.consume(stack);
    }
}
