package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.freecam.FreeCam;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    /**
     * While in free cam with collision disabled (default), blocks have no collision
     * shape for the free cam entity, so it can fly through anything.
     */
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"), cancellable = true)
    public void camerao$onGetCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context,
                                            CallbackInfoReturnable<VoxelShape> cir) {
        if (!Camerao.isFreeCam || Camerao.config.isFreeCamCollision()) {
            return;
        }
        if (context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof FreeCam) {
            cir.setReturnValue(Shapes.empty());
        }
    }
}
