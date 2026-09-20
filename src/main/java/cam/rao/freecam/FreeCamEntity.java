package cam.rao.freecam;

import cam.rao.Camerao;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Invisible client-only player entity the camera attaches to in free cam mode.
 * Ported from the approach used by the Freecam mod (net.xolt.freecam):
 * it is added to the client world so vanilla ticks, interpolates and streams
 * chunks around it, but a dispatcher mixin keeps it from being rendered.
 */
public class FreeCamEntity extends AbstractClientPlayer {
    private static final double DIAGONAL_MULTIPLIER = Mth.sin((float) Math.toRadians(45));

    private final ClientInput input = new KeyboardInput(Minecraft.getInstance().options);

    /** Bob state for the first person hand rendering, mirrors LocalPlayer. */
    public float xBob;
    public float xBobO;
    public float yBob;
    public float yBobO;

    public FreeCamEntity(ClientLevel level, double x, double y, double z, float yaw, float pitch) {
        super(level, new GameProfile(UUID.randomUUID(), "FreeCam"));
        setId(-420);
        snapTo(x, y, z, yaw, pitch);
        // Lock the swimming pose and sync the eye height field immediately, so the
        // camera never lerps from the standing eye height (1.62) down to 0.4 on spawn.
        setPose(Pose.SWIMMING);
        refreshDimensions();
        xBob = getXRot();
        yBob = getYRot();
        xBobO = xBob;
        yBobO = yBob;
        getAbilities().flying = true;
    }

    public void spawn() {
        ((ClientLevel) level()).addEntity(this);
    }

    public void despawn() {
        ((ClientLevel) level()).removeEntity(getId(), Entity.RemovalReason.DISCARDED);
    }

    @Override
    public void tick() {
        input.tick();
        doMotion();
        getAbilities().flying = true;
        setOnGround(false);
        super.tick();
    }

    /** Sets velocity directly from the key states so speed is exact and unaffected by physics. */
    private void doMotion() {
        getAbilities().setFlyingSpeed(0.0F);

        Input keys = input.keyPresses;
        float yaw = getYRot();
        Vec3 forward = Vec3.directionFromRotation(0, yaw);
        Vec3 side = Vec3.directionFromRotation(0, yaw + 90);

        // Speed in reference units (blocks/tick-ish); the scroll wheel tunes it live.
        double hSpeed = Camerao.freeCamSpeed / 10.0;
        double vSpeed = Camerao.freeCamSpeed / 10.0;

        double velocityX = 0.0;
        double velocityY = 0.0;
        double velocityZ = 0.0;

        boolean straight = false;
        if (keys.forward()) {
            velocityX += forward.x * hSpeed;
            velocityZ += forward.z * hSpeed;
            straight = true;
        }
        if (keys.backward()) {
            velocityX -= forward.x * hSpeed;
            velocityZ -= forward.z * hSpeed;
            straight = true;
        }

        boolean strafing = false;
        if (keys.right()) {
            velocityX += side.x * hSpeed;
            velocityZ += side.z * hSpeed;
            strafing = true;
        }
        if (keys.left()) {
            velocityX -= side.x * hSpeed;
            velocityZ -= side.z * hSpeed;
            strafing = true;
        }

        if (straight && strafing) {
            velocityX *= DIAGONAL_MULTIPLIER;
            velocityZ *= DIAGONAL_MULTIPLIER;
        }

        if (keys.jump()) {
            velocityY += vSpeed;
        }
        if (keys.shift()) {
            velocityY -= vSpeed;
        }

        setDeltaMovement(velocityX, velocityY, velocityZ);
    }

    @Override
    protected void applyInput() {
        Vec2 moveVector = this.input.getMoveVector();
        if (moveVector.lengthSquared() != 0.0F) {
            moveVector = moveVector.scale(0.98F);
        }
        this.xxa = moveVector.x;
        this.zza = moveVector.y;
        this.jumping = this.input.keyPresses.jump();
        this.setSprinting(Minecraft.getInstance().options.keySprint.isDown() && this.input.keyPresses.forward());
        this.xBobO = this.xBob;
        this.yBobO = this.yBob;
        this.xBob = this.xBob + (this.getXRot() - this.xBob) * 0.5F;
        this.yBob = this.yBob + (this.getYRot() - this.yBob) * 0.5F;
    }

    // Enables vanilla movement ticking for the fake entity.

    @Override
    public boolean isEffectiveAi() {
        return true;
    }

    // Noclip-friendly behavior: never collide, never splash, never take fall damage.

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState landedState,
                                   net.minecraft.core.BlockPos landedPosition) {
        // no-op: no fall damage sounds
    }

    @Override
    protected void doWaterSplashEffect() {
        // no-op
    }

    @Override
    public void setPose(Pose pose) {
        super.setPose(Pose.SWIMMING);
    }

    // The camera should always look where the free cam looks, never interpolate vanilla player rotation.

    @Override
    public float getViewXRot(float partialTick) {
        return this.getXRot();
    }

    @Override
    public float getViewYRot(float partialTick) {
        return this.getYRot();
    }

    // Note: vanilla passes the real player (Minecraft.player) to hand rendering in
    // 1.21.11, so swing and item state follow the real player automatically. The
    // ItemInHandRendererMixin routes only the view rotation/bob to this entity.
}
