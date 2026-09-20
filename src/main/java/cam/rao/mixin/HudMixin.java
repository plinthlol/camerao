package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public class HudMixin {
    /** Makes the HUD (hotbar, health, etc.) correspond to the player, not the free cam entity. */
    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    public void camerao$onGetCameraPlayer(CallbackInfoReturnable<Player> cir) {
        if (Camerao.isFreeCam) {
            cir.setReturnValue(Minecraft.getInstance().player);
        }
    }

    /** Don't render equipped-item overlays (pumpkin, powder snow...) while in free cam. */
    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
    public void camerao$onExtractTextureOverlay(GuiGraphicsExtractor graphics, Identifier texture,
                                                float alpha, CallbackInfo ci) {
        if (Camerao.isFreeCam) {
            ci.cancel();
        }
    }

    /** Keeps the crosshair on screen while in free cam. Enabling free cam switches the camera
     *  to third person (so you can see your own body), and vanilla only draws the crosshair in
     *  first person - this hook is the early return that makes it vanish. */
    @Redirect(method = "extractCrosshair", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean camerao$showCrosshairInFreeCam(CameraType cameraType) {
        return Camerao.isFreeCam || cameraType.isFirstPerson();
    }
}
