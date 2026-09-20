package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.freecam.FreeCamEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    /** Hides the free cam entity itself; your own body stays rendered. */
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    public void camerao$shouldRender(Entity entity, Frustum frustum, double camX, double camY,
                                     double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof FreeCamEntity) {
            cir.setReturnValue(false);
        }
    }
}
