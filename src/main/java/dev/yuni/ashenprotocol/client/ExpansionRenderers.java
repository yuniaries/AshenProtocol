package dev.yuni.ashenprotocol.client;
import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.expansion.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
@Mod.EventBusSubscriber(modid=AshenProtocol.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ExpansionRenderers {
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e) {
        for(String id:ExpansionContent.ENEMIES) e.registerEntityRenderer(ExpansionContent.MOBS.get(id).get(),ctx->new Renderer(ctx,id));
    }
    private static final class Renderer extends MobRenderer<ProtocolMob,HumanoidModel<ProtocolMob>> {
        private final ResourceLocation texture;
        Renderer(EntityRendererProvider.Context ctx,String id) { super(ctx,new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)),.5f); texture=new ResourceLocation("ashenprotocol","textures/entity/"+id+".png"); }
        @Override public ResourceLocation getTextureLocation(ProtocolMob m) { return texture; }
        @Override protected void scale(ProtocolMob m,PoseStack pose,float partial) { if(m.isBoss()) pose.scale(1.3f,1.3f,1.3f); }
    }
}
