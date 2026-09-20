package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While in free cam, the crosshair aims from the camera, which can be very far
 * from the player. Attacks and item/block interactions are suppressed so you
 * cannot hit or use things from a distance.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void camerao$suppressAttack(Player player, Entity target, CallbackInfo ci) {
        if (Camerao.isFreeCam) {
            ci.cancel();
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void camerao$suppressUseItemOn(LocalPlayer player, InteractionHand hand,
                                          BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        if (Camerao.isFreeCam) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    public void camerao$suppressUseItem(Player player, InteractionHand hand,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        if (Camerao.isFreeCam) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void camerao$suppressInteract(Player player, Entity target, EntityHitResult result,
                                         InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (Camerao.isFreeCam) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
