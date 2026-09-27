package cam.rao.mixin;

import cam.rao.whoami.WhoAmI;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The inventory doll decides its nametag inside this extract, before it is drawn. */
@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("HEAD"))
    private static void camerao$whoAmIStart(LivingEntity entity, CallbackInfoReturnable<EntityRenderState> cir) {
        WhoAmI.inventoryPreview = true;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN"))
    private static void camerao$whoAmIEnd(LivingEntity entity, CallbackInfoReturnable<EntityRenderState> cir) {
        WhoAmI.inventoryPreview = false;
    }
}
