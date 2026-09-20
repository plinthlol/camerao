package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.freecam.FreeCamEntity;
import cam.rao.freecam.Freecam;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes the first person hand follow the free cam rotation instead of the player's.
 * 1.21.11 passes the real player to submitHandsWithItems (so swing/item state comes
 * along for free); we redirect only the view rotation and walk-bob reads.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    private static FreeCamEntity camerao$camera() {
        return Camerao.isFreeCam ? Freecam.getFreeCam() : null;
    }

    @Redirect(method = "submitHandsWithItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot(F)F"))
    public float camerao$handXRot(LocalPlayer player, float partialTick) {
        FreeCamEntity camera = camerao$camera();
        if (camera != null) {
            return Mth.lerp(partialTick, camera.xRotO, camera.getXRot());
        }
        return player.getXRot(partialTick);
    }

    @Redirect(method = "submitHandsWithItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewXRot(F)F"))
    public float camerao$handViewXRot(LocalPlayer player, float partialTick) {
        FreeCamEntity camera = camerao$camera();
        return camera != null ? camera.getViewXRot(partialTick) : player.getViewXRot(partialTick);
    }

    @Redirect(method = "submitHandsWithItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewYRot(F)F"))
    public float camerao$handViewYRot(LocalPlayer player, float partialTick) {
        FreeCamEntity camera = camerao$camera();
        return camera != null ? camera.getViewYRot(partialTick) : player.getViewYRot(partialTick);
    }

    /** Ordinals 0/1 of Mth.lerp(FFF) in submitHandsWithItems are the xBob/yBob walk-bob lerps. */
    @Redirect(method = "submitHandsWithItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 0))
    public float camerao$handBobX(float delta, float start, float end) {
        FreeCamEntity camera = camerao$camera();
        return camera != null ? Mth.lerp(delta, camera.xBobO, camera.xBob) : Mth.lerp(delta, start, end);
    }

    @Redirect(method = "submitHandsWithItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 1))
    public float camerao$handBobY(float delta, float start, float end) {
        FreeCamEntity camera = camerao$camera();
        return camera != null ? Mth.lerp(delta, camera.yBobO, camera.yBob) : Mth.lerp(delta, start, end);
    }
}
