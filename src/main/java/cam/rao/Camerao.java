package cam.rao;

import cam.rao.config.CameraoConfig;
import cam.rao.config.CameraoConfigScreen;
import cam.rao.freecam.Freecam;
import cam.rao.freecam.FreeCamEntity;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import cam.rao.hud.ZoomHud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class Camerao implements ClientModInitializer {
    public static final String MOD_ID = "camerao";
    public static final Logger LOGGER = LoggerFactory.getLogger("Camerao");
    public static final CameraoConfig config = new CameraoConfig();

    public static boolean isPerspectiveActive = false;
    private static CameraType lastPerspective;
    private static CameraType activePerspectiveType;
    public static float zoomDistance = 4.0F;
    /** Current animated FOV multiplier for the zoom key; 1.0 = no zoom. */
    public static float zoomFovFactor = 1.0F;
    public static boolean isZooming = false;
    /** Zoom strength for the current zoom session; reset to the config default when zoom starts. */
    public static int currentZoomMagnification = 300;

    public static boolean isFreeCam = false;
    public static boolean isCamDetached = false;
    /** Parked camera position and rotation while detached. */
    public static float detachedX;
    public static float detachedY;
    public static float detachedZ;
    public static float detachedXRot;
    public static float detachedYRot;
    private static CameraType detachRememberedCameraType;
    /** Fly speed for the current free cam session, in reference units. */
    public static float freeCamSpeed = 10.0F;

    public static final float FREECAM_DEFAULT_SPEED = 10.0F;
    public static final float FREECAM_MIN_SPEED = 1.0F;
    public static final float FREECAM_MAX_SPEED = 100.0F;

    /** The player's cinematic camera setting before perspective turned it on. */
    private static boolean smoothCameraBefore = false;

    public static final float DEFAULT_ZOOM_DISTANCE = 4.0F;
    /** Exponential FOV easing rate (per second) while zooming; equivalent to easing 0.4
     *  of the remaining distance per frame at 60 FPS, but framerate independent. */
    public static final float ZOOM_FOV_RATE = (float) (-60.0 * Math.log(1.0 - 0.4));

    private KeyMapping perspectiveKeyBind;
    private KeyMapping configScreenKeyBind;
    private KeyMapping zoomKeyBind;
    private KeyMapping freeCamKeyBind;
    private KeyMapping detachKeyBind;

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, MOD_ID));

    @Override
    public void onInitializeClient() {
        config.load();

        perspectiveKeyBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "camerao.key.activate", InputConstants.Type.KEYBOARD, InputConstants.KEY_LALT, CATEGORY));
        configScreenKeyBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "camerao.key.menu", InputConstants.UNKNOWN.getValue(), CATEGORY));
        zoomKeyBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "camerao.key.zoom", InputConstants.Type.KEYBOARD, InputConstants.KEY_C, CATEGORY));
        freeCamKeyBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "camerao.key.freecam", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, CATEGORY));
        detachKeyBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "camerao.key.detach", InputConstants.Type.KEYBOARD, InputConstants.KEY_G, CATEGORY));

        ClientTickEvents.START_CLIENT_TICK.register(Freecam::preTick);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTickEnd);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isFreeCam = false;
            Freecam.disable(client);
            freeCamSpeed = FREECAM_DEFAULT_SPEED;
            isCamDetached = false;
        });
        ZoomHud.register();
    }

    private void onTickEnd(Minecraft client) {
        if (configScreenKeyBind.consumeClick()) {
            client.setScreenAndShow(CameraoConfigScreen.build(client.gui.screen()));
        }

        // Detach camera keybind: park the camera, keep full player control.
        if (detachKeyBind.consumeClick()) {
            if (!isCamDetached) {
                startDetach(client);
            } else {
                stopDetach(client);
            }
        }

        // Free cam keybind and ticking. Hold mode ignores the key while perspective
        // to prevent both features flip-flopping when both keys are held.
        if (!config.isFreeCamToggle()) {
            if (!isFreeCam && !isPerspectiveActive && freeCamKeyBind.isDown()) {
                startFreeCam(client);
            } else if (isFreeCam && !freeCamKeyBind.isDown()) {
                stopFreeCam(client);
            }
        } else {
            if (freeCamKeyBind.consumeClick()) {
                if (!isFreeCam) {
                    startFreeCam(client);
                } else {
                    stopFreeCam(client);
                }
            }
        }

        if (isFreeCam) {
            if (client.player == null || client.level == null || client.player.isDeadOrDying()) {
                // Also exits on death: the death screen sends no further damage events,
                // so the camera would otherwise keep flying around the corpse.
                forceStopFreeCam(client);
            }
            // The FreeCamEntity is ticked by vanilla; player input is blanked in preTick.
        }

        boolean zoomHeld = zoomKeyBind.isDown();
        if (zoomHeld && !isZooming) {
            // Zoom just started: begin from the configured default zoom strength.
            currentZoomMagnification = config.getZoomMagnification();
        }
        isZooming = zoomHeld;

        if (!config.isToggle()) {
            // Hold mode: active while the key is pressed down. Ignores the key while
            // in free cam to prevent both features flip-flopping when both keys are held.
            if (!isPerspectiveActive && !isFreeCam && perspectiveKeyBind.isDown()) {
                startPerspective(client, config.getPerspective());
            } else if (isPerspectiveActive && !perspectiveKeyBind.isDown()) {
                stopPerspective(client);
            }
        } else {
            // Toggle mode: press to start, press again to stop.
            if (perspectiveKeyBind.consumeClick()) {
                if (!isPerspectiveActive) {
                    startPerspective(client, config.getPerspective());
                } else {
                    stopPerspective(client);
                }
            }
        }
    }

    private void startFreeCam(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        if (isCamDetached) {
            stopDetach(client); // freecam and detach are mutually exclusive
        }
        if (isPerspectiveActive) {
            stopPerspective(client); // freecam and perspective are mutually exclusive
        }
        Freecam.enable(client);
        if (!Freecam.isEnabled()) {
            return;
        }
        isFreeCam = true;
        freeCamSpeed = FREECAM_DEFAULT_SPEED;
    }

    private void stopFreeCam(Minecraft client) {
        forceStopFreeCam(client);
    }

    /** Tears down free cam and restores the real player; safe to call from mixins on the main thread. */
    public static void forceStopFreeCam(Minecraft client) {
        // Clear the flag first so OptionsMixin does not eat the camera restore inside disable().
        isFreeCam = false;
        Freecam.disable(client);
        freeCamSpeed = FREECAM_DEFAULT_SPEED;
    }

    public static FreeCamEntity getActiveDrone() {
        return Freecam.getFreeCam();
    }

    public static boolean isFreeCamEnabled() {
        return Freecam.isEnabled();
    }

    private void startDetach(Minecraft client) {
        if (client.player == null) {
            return;
        }
        if (isFreeCam) {
            stopFreeCam(client);
        }
        if (isPerspectiveActive) {
            stopPerspective(client);
        }
        detachedXRot = client.player.getViewXRot(0F);
        detachedYRot = client.player.getViewYRot(0F);
        detachedX = (float) client.player.getX();
        detachedY = (float) client.player.getEyeY();
        detachedZ = (float) client.player.getZ();
        detachRememberedCameraType = client.options.getCameraType();
        client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        client.gameRenderer.checkEntityPostEffect(null);
        isCamDetached = true;
    }

    private void stopDetach(Minecraft client) {
        isCamDetached = false;
        if (detachRememberedCameraType != null) {
            client.options.setCameraType(detachRememberedCameraType);
            detachRememberedCameraType = null;
        }
        client.gameRenderer.checkEntityPostEffect(
                client.options.getCameraType().isFirstPerson() ? client.getCameraEntity() : null);
    }

    /** Exits detach when the player cycles perspective with F5. */
    public static void exitDetach() {
        isCamDetached = false;
    }

    private void startPerspective(Minecraft client, CameraType requestedPerspective) {
        if (isFreeCam) {
            stopFreeCam(client); // perspective and free cam are mutually exclusive
        }
        if (isCamDetached) {
            stopDetach(client); // perspective and detach are mutually exclusive
        }
        lastPerspective = client.options.getCameraType();
        activePerspectiveType = requestedPerspective;
        // Only switch perspective if currently in first person, looks weird otherwise.
        if (lastPerspective == CameraType.FIRST_PERSON) {
            client.options.setCameraType(requestedPerspective);
        }
        isPerspectiveActive = true;
        zoomDistance = DEFAULT_ZOOM_DISTANCE;
        smoothCameraBefore = client.options.smoothCamera;
        if (config.isSmoothCamera()) {
            client.options.smoothCamera = true;
        }
    }

    private void stopPerspective(Minecraft client) {
        isPerspectiveActive = false;
        activePerspectiveType = null;
        zoomDistance = DEFAULT_ZOOM_DISTANCE;
        client.options.setCameraType(lastPerspective);
        client.options.smoothCamera = smoothCameraBefore;
    }

    public static CameraType getActivePerspective() {
        return activePerspectiveType;
    }
}
