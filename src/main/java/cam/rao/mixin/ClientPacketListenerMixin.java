package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    /** Disables freecam when the player respawns or switches dimensions. */
    @Inject(method = "handleRespawn", at = @At("TAIL"))
    public void camerao$onRespawn(CallbackInfo ci) {
        if (Camerao.isFreeCam) {
            Camerao.forceStopFreeCam(Minecraft.getInstance());
        }
    }
}
