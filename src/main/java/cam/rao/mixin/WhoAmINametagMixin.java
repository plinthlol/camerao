package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets your own name tag render in the world, the same way the {@code Who am I?} mod does.
 * The only change to vanilla's name-tag logic is bypassing the {@code entity != cameraEntity}
 * self-exclusion: everything else (distance check, name-visibility setting, sneaking...) runs
 * untouched.
 *
 * <p>Target name {@code shouldShowName} is the official Mojang mapping for this method on
 * 1.21.11 (and later versions), confirmed against the mapping Loom resolves for this branch.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class WhoAmINametagMixin {
    @ModifyExpressionValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            at = @At(value = At.INVOKE, target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private static Entity camerao$showOwnNameTag(Entity cameraEntity, LivingEntity entity) {
        // cameraEntity is the value vanilla just read from Minecraft.getCameraEntity().
        if (cameraEntity != entity) {
            return cameraEntity; // rendering someone else's name tag: vanilla behaviour
        }
        // cameraEntity == entity == the local player. This is the self name tag gate.
        if (!Camerao.config.isWhoAmI()) {
            return cameraEntity; // feature off: vanilla hides your own name tag
        }
        boolean inGui = Minecraft.getInstance().screen != null;
        // In the world: always show it. In an inventory/container: only if toggled on.
        return (!inGui || Camerao.config.isShowInInventory()) ? null : cameraEntity;
    }
}
