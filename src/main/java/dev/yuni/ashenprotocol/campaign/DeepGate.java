package dev.yuni.ashenprotocol.campaign;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.level.Level;
public final class DeepGate extends dev.yuni.ashenprotocol.expansion.CoordinateGate {
    public static final ResourceKey<Level> REALM=ResourceKey.create(Registries.DIMENSION,new ResourceLocation("ashenprotocol","echo_depths"));
    public DeepGate(Properties p){super(p,REALM);}
}
