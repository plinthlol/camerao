package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.Minecraft.class)
public class MinecraftMixin {
    /** Prevents attacking while in free cam (the crosshair aims from the camera). */
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    public void camerao$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (Camerao.isFreeCam) {
            cir.cancel();
        }
    }

    /** Prevents middle-click item picking while in free cam. */
    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
    public void camerao$onPickBlock(CallbackInfo ci) {
        if (Camerao.isFreeCam) {
            ci.cancel();
        }
    }

    /** Prevents block breaking while in free cam. */
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    public void camerao$onContinueAttack(boolean leftClick, CallbackInfo ci) {
        if (Camerao.isFreeCam) {
            ci.cancel();
        }
    }
}
