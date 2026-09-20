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
    private static boolean enabled;
    private static FreeCam freeCam;
    private static CameraType rememberedCameraType;
    /** Whether the player was sneaking when free cam was activated. */
    private static boolean sneakAtEnable;

    private Freecam() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static FreeCam getFreeCam() {
        return freeCam;
    }

    /** Runs at the start of every client tick: keep the real player's input blanked.
     *  With "Keep sneak" on, the sneak state captured at activation is preserved. */
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

    public static void toggle(Minecraft mc) {
        if (enabled) {
            disable(mc);
        } else {
            enable(mc);
        }
    }

    public static void enable(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            return;
        }
        mc.smartCull = false;
        rememberedCameraType = mc.options.getCameraType();
        sneakAtEnable = mc.player.input.keyPresses.shift();
        // Detached view so you can see your own body right away.
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        // Spawn one block above the head so the body is in view.
        // Spawn at the player's eye level so the detached camera sits right at your head.
        freeCam = new FreeCam((ClientLevel) mc.level,
                mc.player.getX(),
                mc.player.getEyeY() - 0.4,
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
        mc.smartCull = true;
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
        }
        if (freeCam != null) {
            freeCam.despawn();
        }
        freeCam = null;
        sneakAtEnable = false;
        if (mc.player != null) {
            mc.player.input = new KeyboardInput(mc.options);
        }
        if (rememberedCameraType != null) {
            mc.options.setCameraType(rememberedCameraType);
            rememberedCameraType = null;
        }
    }
}
