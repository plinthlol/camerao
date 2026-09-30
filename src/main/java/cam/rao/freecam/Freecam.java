package cam.rao.freecam;

import cam.rao.Camerao;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;

/**
 * Free cam lifecycle, ported from the Freecam mod (net.xolt.freecam):
 * enable spawns the fake camera entity and points the vanilla camera at it;
 * disable removes it and restores everything, even if the world changed.
 */
public final class Freecam {
    /** Top of the standing player's head above the player's feet. */
    private static final double PLAYER_HEAD_TOP = 1.8;
    /** Eye height of the free cam entity itself, fixed by the swimming pose it locks. */
    private static final double FREECAM_EYE_Y = 0.4;

    private static boolean enabled;
    private static FreeCamEntity freeCam;
    private static CameraType rememberedCameraType;
    /** The player's input instance captured at activation, restored on disable. */
    private static ClientInput playerInputAtEnable;
    /** Whether the player was sneaking when free cam was activated. */
    private static boolean sneakAtEnable;
    /** smartCull value captured at activation, restored on disable. */
    private static boolean smartCullAtEnable;

    private Freecam() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static FreeCamEntity getFreeCam() {
        return freeCam;
    }

    /** Runs at the start of every client tick: replaces the real player's input with a
     *  blank {@link ClientInput} so it ignores all keys while the camera entity moves freely
     *  (the original input instance is restored in {@link #disable}). With "Keep sneak"
     *  on, the sneak state captured at activation is preserved in the blank input. */
    public static void preTick(Minecraft mc) {
        if (enabled && mc.player != null && mc.player.input instanceof KeyboardInput) {
            ClientInput blank = new ClientInput();
            if (sneakAtEnable && Camerao.config.isFreeCamKeepSneak()) {
                // Input(fwd, back, left, right, jump, shift, sprint) - keep sneak held.
                blank.keyPresses = new Input(false, false, false, false, false, true, false);
            }
            mc.player.input = blank;
        }
    }

    public static void enable(Minecraft mc) {
        if (enabled) {
            return; // already active - never spawn a second entity or overwrite the captures
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        smartCullAtEnable = mc.smartCull;
        mc.smartCull = false;
        rememberedCameraType = mc.options.getCameraType();
        playerInputAtEnable = mc.player.input;
        sneakAtEnable = mc.player.input.keyPresses.shift();
        // Detached view so you can see your own body right away.
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        // Spawn so the rendered camera sits one block above the top of the player's
        // head. The free cam entity has a fixed 0.4 eye height, so its Y must sit
        // that far below the target; starting clear of the body also stops the camera
        // from appearing to drop or shift as you enter free cam.
        double spawnY = mc.player.getY() + PLAYER_HEAD_TOP + 1.0 - FREECAM_EYE_Y;
        freeCam = new FreeCamEntity((ClientLevel) mc.level,
                mc.player.getX(),
                spawnY,
                mc.player.getZ(),
                mc.player.getYRot(),
                mc.player.getXRot());
        freeCam.spawn();
        mc.setCameraEntity(freeCam);
        enabled = true;
    }

    public static void disable(Minecraft mc) {
        if (!enabled) {
            return; // nothing active - never restore a stale camera type
        }
        enabled = false;
        mc.smartCull = smartCullAtEnable;
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
        }
        if (freeCam != null) {
            freeCam.despawn();
        }
        freeCam = null;
        sneakAtEnable = false;
        if (mc.player != null) {
            // Restore whatever input instance the player had before freecam, so custom
            // ClientInput implementations from other mods are not clobbered.
            mc.player.input = playerInputAtEnable != null ? playerInputAtEnable
                    : new KeyboardInput(mc.options);
            // Refresh the restored input right away. It was never ticked while free cam was
            // active, and LocalPlayer.aiStep() reads keyPresses *before* it refreshes them, so
            // a stale read would leave sneak/sprint one tick behind after exiting.
            if (mc.player.input instanceof KeyboardInput) {
                mc.player.input.tick();
            }
        }
        playerInputAtEnable = null;
        if (rememberedCameraType != null) {
            mc.options.setCameraType(rememberedCameraType);
            rememberedCameraType = null;
        }
    }
}
