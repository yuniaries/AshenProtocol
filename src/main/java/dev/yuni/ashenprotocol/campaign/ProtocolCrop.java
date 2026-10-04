package dev.yuni.ashenprotocol.campaign;
import dev.yuni.ashenprotocol.expansion.ExpansionContent;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.ItemLike;
public final class ProtocolCrop extends CropBlock {
    private final String crop;
    public ProtocolCrop(Properties p,String crop){super(p);this.crop=crop;}
    @Override protected ItemLike getBaseSeedId(){return ExpansionContent.item(crop+"_seeds");}
}
