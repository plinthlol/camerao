package cam.rao.mixin;

import cam.rao.Camerao;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    /** No view bobbing while the camera is detached (it is parked, not moving). */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void camerao$noBobWhenDetached(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (Camerao.isCamDetached) {
            ci.cancel();
        }
    }
}
