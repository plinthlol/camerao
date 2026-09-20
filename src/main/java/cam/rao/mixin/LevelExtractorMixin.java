package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.freecam.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla skips the local player in entity extraction unless it is the camera
 * entity. In free cam the camera entity is the fake free cam, so the player's
 * model is never extracted - we add it manually here, ported from the Freecam
 * mod's approach.
 */
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Invoker("extractEntity")
    public abstract EntityRenderState camerao$callExtractEntity(Entity entity, float partialTickTime);

    @Inject(method = "extractVisibleEntities", at = @At("TAIL"))
    public void camerao$onExtractVisibleEntities(Camera camera, Frustum frustum,
                                                 net.minecraft.client.DeltaTracker deltaTracker,
                                                 LevelRenderState levelRenderState, CallbackInfo ci) {
        if (!Camerao.isFreeCam) {
            return;
        }
        Entity player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        EntityRenderState state = camerao$callExtractEntity(player, partialTick);
        if (state != null) {
            levelRenderState.entityRenderStates.add(state);
        }
    }
}
