package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.whoami.WhoAmI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla hides the local player's nametag because the entity is the camera.
 * Returning null from that comparison lets the rest of the nametag checks run,
 * which is the same bypass as the Who am I? mod.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Redirect(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity camerao$showOwnName(Minecraft minecraft) {
        Entity camera = minecraft.getCameraEntity();
        if (!Camerao.config.isWhoAmI()) {
            return camera;
        }
        if (WhoAmI.inventoryPreview && !Camerao.config.isWhoAmIInInventory()) {
            return camera;
        }
        return null;
    }
}
