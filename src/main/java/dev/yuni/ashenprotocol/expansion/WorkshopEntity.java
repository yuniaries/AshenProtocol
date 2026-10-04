package dev.yuni.ashenprotocol.expansion;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WorkshopEntity extends BlockEntity {
    public record Recipe(String input, String output, int count, int ticks) {}
    public static Recipe recipe(String machine) { return switch(machine) {
        case "ash_press" -> new Recipe("raw_ash", "ash_ingot", 1, 80);
        case "crystal_refinery" -> new Recipe("signal_shard", "signal_ingot", 1, 120);
        case "echo_loom" -> new Recipe("echo_residue", "woven_echo", 2, 160);
        case "alloy_forge" -> new Recipe("rift_dust", "rift_ingot", 1, 200);
        case "restoration_array" -> new Recipe("restoration_cell", "", 0, 200);
        case "world_anchor" -> new Recipe("atlas_core", "", 0, 600);
        default -> throw new IllegalArgumentException(machine);
    }; }
    private ItemStack input = ItemStack.EMPTY, output = ItemStack.EMPTY;
    private int fuel, progress;
    public WorkshopEntity(BlockPos p, BlockState s) { super(ExpansionContent.WORKSHOP.get(),p,s); }
    public String machine() { return ((WorkshopBlock)getBlockState().getBlock()).machine; }
    public ItemStack input() { return input; } public ItemStack output() { return output; } public int fuel() { return fuel; } public int progress() { return progress; }
    public void interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand); Recipe r = recipe(machine());
        if (held.isEmpty()) {
            if (player.isShiftKeyDown()) { if (!input.isEmpty()) { player.getInventory().placeItemBackInInventory(input.copy()); input = ItemStack.EMPTY; progress = 0; } }
            else if (!output.isEmpty()) { player.getInventory().placeItemBackInInventory(output.copy()); output = ItemStack.EMPTY; }
            else message(player,"输入 " + input.getCount() + " · 输出 " + output.getCount() + " · 能量 " + fuel + " · 进度 " + progress + "/" + r.ticks() + "；需红石信号。潜行空手取回输入。");
        } else if (held.is(ExpansionContent.item("protocol_fragment"))) {
            if (fuel > 3800) message(player,"能量已满，未扣除材料。");
            else { fuel += 200; held.shrink(1); message(player,"能量 +200。一次加工每秒消耗1点能量。"); }
        } else if (held.is(ExpansionContent.item(r.input()))) {
            int amount = Math.min(held.getCount(),64-input.getCount());
            if (amount > 0) { if (input.isEmpty()) input = new ItemStack(held.getItem(), amount); else input.grow(amount); held.shrink(amount); }
            else message(player,"输入已满，未扣除材料。");
        } else message(player,"需要输入：" + new ItemStack(ExpansionContent.item(r.input())).getHoverName().getString() + "；碎片充能；空手取输出。");
        setChanged();
    }
    private void message(Player p, String s) { p.displayClientMessage(Component.literal(s),true); }
    public void dropInventory() { if (level != null && !level.isClientSide) { Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,input); Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,output); input=ItemStack.EMPTY; output=ItemStack.EMPTY; } }
    public static void tick(Level l, BlockPos pos, BlockState state, WorkshopEntity be) {
        if (!(l instanceof ServerLevel sl) || !l.hasNeighborSignal(pos) || be.fuel <= 0 || be.input.isEmpty()) return;
        Recipe r = recipe(be.machine());
        if (r.count()>0 && be.output.getCount()+r.count()>64) return;
        be.progress++;
        if (be.progress%20==0) be.fuel--;
        if (be.progress>=r.ticks()) {
            be.progress=0; be.input.shrink(1);
            if (r.count()>0) { if (be.output.isEmpty()) be.output=new ItemStack(ExpansionContent.item(r.output()),r.count()); else be.output.grow(r.count()); }
            else { var data=ProtocolSavedData.get(sl.getServer()); boolean anchor=be.machine().equals("world_anchor"); data.addIntegrity(anchor ? 500 : 40); data.addEntropy(anchor ? -1000 : -60); }
            sl.sendParticles(ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+1.1,pos.getZ()+.5,8,.2,.2,.2,.03);
        }
        be.setChanged();
    }
    @Override protected void saveAdditional(CompoundTag t) { super.saveAdditional(t); t.put("Input",input.save(new CompoundTag())); t.put("Output",output.save(new CompoundTag())); t.putInt("Fuel",fuel); t.putInt("Progress",progress); }
    @Override public void load(CompoundTag t) { super.load(t); input=ItemStack.of(t.getCompound("Input")); output=ItemStack.of(t.getCompound("Output")); fuel=Math.max(0,Math.min(4000,t.getInt("Fuel"))); progress=Math.max(0,Math.min(recipe(machine()).ticks()-1,t.getInt("Progress"))); }
}
