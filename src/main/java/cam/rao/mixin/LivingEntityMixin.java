package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /** Exits free cam when the real player takes damage (configurable, default on). */
    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    public void camerao$onDamageEvent(DamageSource source, CallbackInfo ci) {
        if (Camerao.isFreeCam
                && Camerao.config.isFreeCamExitOnDamage()
                && (Object) this == Minecraft.getInstance().player) {
            Camerao.forceStopFreeCam(Minecraft.getInstance());
        }
    }
}
