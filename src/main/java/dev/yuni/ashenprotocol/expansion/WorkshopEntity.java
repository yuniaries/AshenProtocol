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
    public record Recipe(String input, String output, int count, int ticks, String secondary, int secondaryCount) {
        public Recipe(String input,String output,int count,int ticks){this(input,output,count,ticks,"",0);}
    }
    public static Recipe recipe(String machine) { return switch(machine) {
        case "ash_press" -> new Recipe("raw_ash", "ash_ingot", 1, 80);
        case "crystal_refinery" -> new Recipe("signal_shard", "signal_ingot", 1, 120);
        case "echo_loom" -> new Recipe("echo_residue", "woven_echo", 2, 160);
        case "alloy_forge" -> new Recipe("rift_dust", "rift_ingot", 1, 200);
        case "restoration_array" -> new Recipe("restoration_cell", "", 0, 200);
        case "world_anchor" -> new Recipe("atlas_core", "", 0, 600);
        case "carbon_kiln" -> new Recipe("minecraft:coal","carbon_dust",2,120);
        case "wire_drawer" -> new Recipe("minecraft:copper_ingot","copper_wire",4,100);
        case "circuit_assembler" -> new Recipe("signal_ingot","signal_circuit",1,160,"copper_wire",2);
        case "steel_foundry" -> new Recipe("minecraft:iron_ingot","steel_ingot",1,200,"carbon_dust",2);
        case "compost_processor" -> new Recipe("ash_wheat","fertile_compost",2,100,"minecraft:bone_meal",1);
        case "bio_refinery" -> new Recipe("bio_pulp","bio_fuel",2,160,"fertile_compost",1);
        case "field_kitchen" -> new Recipe("tide_rice","expedition_meal",2,140,"sun_pepper",1);
        case "herbal_extractor" -> new Recipe("memory_herb","healing_extract",2,160,"signal_berry",2);
        case "fiber_spinner" -> new Recipe("woven_echo","echo_fiber",4,120,"bio_pulp",1);
        case "matrix_compressor" -> new Recipe("memory_shard","memory_matrix",1,240,"crystal_matrix",2);
        case "archive_decoder" -> new Recipe("sealed_archive","archive_chip",3,240,"signal_circuit",1);
        case "precision_lathe" -> new Recipe("steel_ingot","precision_gear",2,160,"copper_wire",2);
        case "phase_assembler" -> new Recipe("deep_ingot","harmonic_ingot",1,320,"void_matrix",1);
        case "biosphere_restorer" -> new Recipe("biosphere_core","",0,400,"bio_fuel",4);
        default -> throw new IllegalArgumentException(machine);
    }; }
    private ItemStack input = ItemStack.EMPTY, output = ItemStack.EMPTY, secondary = ItemStack.EMPTY, queuedFuel = ItemStack.EMPTY;
    private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> inventory = net.minecraftforge.common.util.LazyOptional.of(() -> new Handler(null));
    private final java.util.EnumMap<net.minecraft.core.Direction,net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>> sided = new java.util.EnumMap<>(net.minecraft.core.Direction.class);
    private ItemStack stack(int slot){return switch(slot){case 0->input;case 1->secondary;case 2->output;case 3->queuedFuel;default->throw new IndexOutOfBoundsException();};}
    private void stack(int slot,ItemStack value){switch(slot){case 0->input=value;case 1->secondary=value;case 2->output=value;case 3->queuedFuel=value;default->throw new IndexOutOfBoundsException();}setChanged();}
    private final class Handler implements net.minecraftforge.items.IItemHandler {
        private final net.minecraft.core.Direction face;
        Handler(net.minecraft.core.Direction face){this.face=face;}
        public int getSlots(){return 4;} public ItemStack getStackInSlot(int slot){return stack(slot).copy();} public int getSlotLimit(int slot){return 64;}
        public boolean isItemValid(int slot,ItemStack value){
            Recipe r=recipe(machine());
            if(face==net.minecraft.core.Direction.DOWN)return false;
            if(face==net.minecraft.core.Direction.UP&&slot!=0)return false;
            if(face!=null&&face!=net.minecraft.core.Direction.UP&&slot==0)return false;
            return slot==0&&value.is(ExpansionContent.item(r.input())) || slot==1&&!r.secondary().isEmpty()&&value.is(ExpansionContent.item(r.secondary())) || slot==3&&value.is(ExpansionContent.item("protocol_fragment"));
        }
        public ItemStack insertItem(int slot,ItemStack value,boolean simulate){
            if(value.isEmpty()||!isItemValid(slot,value))return value;
            ItemStack old=stack(slot);if(!old.isEmpty()&&!ItemStack.isSameItemSameTags(old,value))return value;
            int count=Math.min(value.getCount(),Math.min(64,value.getMaxStackSize())-old.getCount());if(count<=0)return value;
            if(!simulate){ItemStack next=old.isEmpty()?value.copy():old.copy();next.setCount(old.getCount()+count);stack(slot,next);}
            ItemStack remainder=value.copy();remainder.shrink(count);return remainder;
        }
        public ItemStack extractItem(int slot,int amount,boolean simulate){
            if(amount<=0 || face!=null&&(face!=net.minecraft.core.Direction.DOWN||slot!=2))return ItemStack.EMPTY;
            ItemStack old=stack(slot);ItemStack result=old.copy();result.setCount(Math.min(amount,old.getCount()));
            if(!simulate){ItemStack next=old.copy();next.shrink(result.getCount());stack(slot,next);if(face==net.minecraft.core.Direction.DOWN&&slot==2)measure("AshenAutoOutputs",result.getCount());}return result;
        }
    }
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,net.minecraft.core.Direction face){
        if(capability==net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER&&!isRemoved())return (face==null?inventory:sided.computeIfAbsent(face,f->net.minecraftforge.common.util.LazyOptional.of(()->new Handler(f)))).cast();
        return super.getCapability(capability,face);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();inventory.invalidate();sided.values().forEach(net.minecraftforge.common.util.LazyOptional::invalidate);}
    @Override public void reviveCaps(){super.reviveCaps();inventory=net.minecraftforge.common.util.LazyOptional.of(()->new Handler(null));sided.clear();}
    public ItemStack secondary(){return secondary;}
    private int fuel, progress;private java.util.UUID owner;
    public void setOwner(Player p){owner=p.getUUID();setChanged();}
    private void measure(String key,int amount){if(level instanceof ServerLevel sl&&owner!=null){var p=sl.getServer().getPlayerList().getPlayer(owner);if(p!=null)dev.yuni.ashenprotocol.progress.Progression.addCounter(p,key,amount);}}
    public WorkshopEntity(BlockPos p, BlockState s) { super(ExpansionContent.WORKSHOP.get(),p,s); }
    public String machine() { return ((WorkshopBlock)getBlockState().getBlock()).machine; }
    public ItemStack input() { return input; } public ItemStack output() { return output; } public int fuel() { return fuel; } public int progress() { return progress; }
    public void interact(Player player, InteractionHand hand) {
        if(owner==null)setOwner(player);ItemStack held = player.getItemInHand(hand); Recipe r = recipe(machine());
        if (held.isEmpty()) {
            if (player.isShiftKeyDown()) { if (!input.isEmpty()) { player.getInventory().placeItemBackInInventory(input.copy()); input = ItemStack.EMPTY; progress = 0; } if(!secondary.isEmpty()){player.getInventory().placeItemBackInInventory(secondary.copy());secondary=ItemStack.EMPTY; } }
            else if (!output.isEmpty()) { player.getInventory().placeItemBackInInventory(output.copy()); output = ItemStack.EMPTY; }
            else message(player,"输入 " + input.getCount() + " · 辅料 " + secondary.getCount() + " · 输出 " + output.getCount() + " · 能量 " + fuel + " · 进度 " + progress + "/" + r.ticks() + "；需红石信号。潜行空手取回输入。");
        } else if (held.is(ExpansionContent.item("protocol_fragment"))) {
            if (fuel > 3800) message(player,"能量已满，未扣除材料。");
            else { fuel += 200; held.shrink(1); message(player,"能量 +200。一次加工每秒消耗1点能量。"); }
        } else if (held.is(ExpansionContent.item(r.input()))) {
            int amount = Math.min(held.getCount(),64-input.getCount());
            if (amount > 0) { if (input.isEmpty()) input = new ItemStack(held.getItem(), amount); else input.grow(amount); held.shrink(amount); }
            else message(player,"输入已满，未扣除材料。");
        } else if (!r.secondary().isEmpty() && held.is(ExpansionContent.item(r.secondary()))) {
            ItemStack remaining=new Handler(null).insertItem(1,held,false);held.setCount(remaining.getCount());
        } else message(player,"需要输入：" + new ItemStack(ExpansionContent.item(r.input())).getHoverName().getString() + (r.secondary().isEmpty()?"":" + "+r.secondaryCount()+" × "+new ItemStack(ExpansionContent.item(r.secondary())).getHoverName().getString()) + "；碎片充能；空手取输出。");
        setChanged();
    }
    private void message(Player p, String s) { p.displayClientMessage(Component.literal(s),true); }
    public void dropInventory() { if(level!=null&&!level.isClientSide)for(int slot=0;slot<4;slot++){Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,stack(slot));stack(slot,ItemStack.EMPTY);} }
    public static void tick(Level l, BlockPos pos, BlockState state, WorkshopEntity be) {
        if (!(l instanceof ServerLevel sl)) return;
        if(be.fuel<=3800&&!be.queuedFuel.isEmpty()){be.queuedFuel.shrink(1);be.fuel+=200;be.setChanged();}
        if (!l.hasNeighborSignal(pos) || be.fuel <= 0 || be.input.isEmpty()) return;
        Recipe r = recipe(be.machine());
        if(!be.input.is(ExpansionContent.item(r.input())))return;
        if(r.secondaryCount()>0&&(!be.secondary.is(ExpansionContent.item(r.secondary()))||be.secondary.getCount()<r.secondaryCount()))return;
        if(r.count()>0&&(!be.output.isEmpty()&&!be.output.is(ExpansionContent.item(r.output()))||be.output.getCount()+r.count()>64))return;
        be.progress++;
        if (be.progress%20==0) be.fuel--;
        if (be.progress>=r.ticks()) {
            be.progress=0; be.input.shrink(1);be.secondary.shrink(r.secondaryCount());
            if (r.count()>0) { if (be.output.isEmpty()) be.output=new ItemStack(ExpansionContent.item(r.output()),r.count()); else be.output.grow(r.count()); }
            else { var data=ProtocolSavedData.get(sl.getServer()); boolean anchor=be.machine().equals("world_anchor"); boolean bio=be.machine().equals("biosphere_restorer");data.addIntegrity(anchor ? 500 : bio?200:40); data.addEntropy(anchor ? -1000 : bio?-300:-60); }
            be.measure("AshenJobs",1);
            if(be.machine().equals("phase_assembler"))be.measure("AshenPhaseJobs",1);
            if(be.machine().equals("biosphere_restorer"))be.measure("AshenBiosphereJobs",1);
            if(be.machine().equals("world_anchor"))be.measure("AshenAnchorJobs",1);
            sl.sendParticles(ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+1.1,pos.getZ()+.5,8,.2,.2,.2,.03);
        }
        be.setChanged();
    }
    @Override protected void saveAdditional(CompoundTag t) { super.saveAdditional(t);if(owner!=null)t.putUUID("Owner",owner); t.put("Input",input.save(new CompoundTag())); t.put("Output",output.save(new CompoundTag())); t.put("Secondary",secondary.save(new CompoundTag()));t.put("QueuedFuel",queuedFuel.save(new CompoundTag())); t.putInt("Fuel",fuel); t.putInt("Progress",progress); }
    @Override public void load(CompoundTag t) { super.load(t);owner=t.hasUUID("Owner")?t.getUUID("Owner"):null; input=ItemStack.of(t.getCompound("Input")); output=ItemStack.of(t.getCompound("Output")); secondary=ItemStack.of(t.getCompound("Secondary"));queuedFuel=ItemStack.of(t.getCompound("QueuedFuel"));fuel=Math.max(0,Math.min(4000,t.getInt("Fuel"))); progress=Math.max(0,Math.min(recipe(machine()).ticks()-1,t.getInt("Progress"))); }
}
