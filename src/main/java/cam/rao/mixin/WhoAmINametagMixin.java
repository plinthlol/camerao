package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes your own name tag render in the world, the same way the {@code Who am I?} mod does.
 *
 * <p>Vanilla hides your own name tag with a {@code entity != Minecraft.getCameraEntity()}
 * check. Returning {@code null} from that {@code getCameraEntity()} call breaks the check so
 * the name tag is allowed to render, while every other condition (distance, name-visibility
 * setting, sneaking, ...) is left untouched.
 *
 * <p>Two toggles control it and the inventory case needs to know which entity is about to be
 * drawn, so a HEAD inject remembers whether this is the local player first.
 *
 * <p>{@code shouldShowName} is the official Mojang name for this method on 1.21.11 (and on
 * 1.21.x generally) - same name this mixin targets on the 26.x branches.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class WhoAmINametagMixin {
    @Unique
    private static final ThreadLocal<Boolean> camerao$isSelf = new ThreadLocal<>();

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"))
    private static void camrao$whoAmIRememberSelf(LivingEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        camerao$isSelf.set(camera != null && (camera == entity || camera.equals(entity)));
    }

    @Redirect(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            at = @At(value = At.INVOKE, target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private static Entity camerao$showOwnNameTag(Minecraft instance) {
        if (!Boolean.TRUE.equals(camerao$isSelf.get())) {
            return instance.getCameraEntity(); // another entity: vanilla behaviour
        }
        if (!Camerao.config.isWhoAmI()) {
            return instance.getCameraEntity(); // feature off: vanilla hides your own name tag
        }
        boolean inGui = instance.screen != null;
        // In the world: always show it. In an inventory/container: only if toggled on.
        return (!inGui || Camerao.config.isShowInInventory()) ? null : instance.getCameraEntity();
    }
}
