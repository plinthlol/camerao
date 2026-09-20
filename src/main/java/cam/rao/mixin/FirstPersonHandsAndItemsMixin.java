package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.freecam.FreeCam;
import cam.rao.freecam.Freecam;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the first person hand follow the free cam rotation instead of the player's,
 * ported from the Freecam mod's 26.3 handling.
 */
@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void camerao$onExtractRenderState(LocalPlayer player, float partialTicks,
                                             FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        if (Camerao.isFreeCam) {
            FreeCam camera = Freecam.getFreeCam();
            if (camera == null) {
                return;
            }
            state.viewXRot = camera.getViewXRot(partialTicks);
            state.viewYRot = camera.getViewYRot(partialTicks);
            state.xBob = Mth.lerp(partialTicks, camera.xBobO, camera.xBob);
            state.yBob = Mth.lerp(partialTicks, camera.yBobO, camera.yBob);
        }
    }
}
