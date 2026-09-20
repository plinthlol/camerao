package cam.rao;

/**
 * Duck interface applied to {@link net.minecraft.world.entity.Entity} via {@code EntityMixin}
 * to store detached perspective camera rotation without touching vanilla state.
 */
public interface CameraDuck {
    float camerao$getCameraPitch();

    float camerao$getCameraYaw();

    void camerao$setCameraPitch(float pitch);

    void camerao$setCameraYaw(float yaw);
}
