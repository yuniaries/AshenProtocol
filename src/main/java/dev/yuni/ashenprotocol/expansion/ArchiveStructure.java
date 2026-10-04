package dev.yuni.ashenprotocol.expansion;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pools.*;
import java.util.Optional;
/** An island-bound archive: vanilla end biome classification may include empty void columns. */
public final class ArchiveStructure extends Structure {
    public static final Codec<ArchiveStructure> CODEC=RecordCodecBuilder.create(instance -> instance.group(
        settingsCodec(instance), StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s->s.pool)
    ).apply(instance,ArchiveStructure::new));
    private final Holder<StructureTemplatePool> pool;
    public ArchiveStructure(StructureSettings settings,Holder<StructureTemplatePool> pool) { super(settings);this.pool=pool; }
    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        var pos=new BlockPos(context.chunkPos().getMinBlockX(),0,context.chunkPos().getMinBlockZ());
        return JigsawPlacement.addPieces(context,pool,Optional.empty(),1,pos,false,Optional.of(Heightmap.Types.WORLD_SURFACE_WG),80)
            .filter(stub->stub.position().getY()>=32 && stub.position().getY()<context.heightAccessor().getMaxBuildHeight()-12);
    }
    @Override public StructureType<?> type() { return ExpansionContent.ARCHIVE_STRUCTURE.get(); }
}
