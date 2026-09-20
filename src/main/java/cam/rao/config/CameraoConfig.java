package cam.rao.config;

import cam.rao.Camerao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class CameraoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum Mode {
        HOLD, TOGGLE
    }

    private Mode mode = Mode.HOLD;
    private CameraType perspective = CameraType.THIRD_PERSON_BACK;
    private int maxYaw = 360;
    private int maxPitch = 90;
    private boolean invertY = false;
    private boolean smoothCamera = false;
    private boolean zoomOut = false;
    private int minZoom = 2;
    private int maxZoom = 32;
    private Mode freeCamMode = Mode.TOGGLE;
    private boolean freeCamCollision = false;
    private boolean freeCamKeepSneak = true;
    private int zoomMagnification = 300;
    private boolean showZoomIndicator = true;
    private boolean invertScroll = false;

    public synchronized Mode getMode() {
        return mode;
    }

    public synchronized void setMode(Mode mode) {
        this.mode = mode != null ? mode : Mode.HOLD;
    }

    public synchronized boolean isToggle() {
        return mode == Mode.TOGGLE;
    }

    public synchronized CameraType getPerspective() {
        return perspective != null ? perspective : CameraType.THIRD_PERSON_BACK;
    }

    public synchronized void setPerspective(CameraType perspective) {
        this.perspective = perspective != null ? CameraType.THIRD_PERSON_BACK : perspective;
    }

    public synchronized int getMaxYaw() {
        return maxYaw;
    }

    public synchronized void setMaxYaw(int maxYaw) {
        this.maxYaw = Mth.clamp(maxYaw, 5, 360);
    }

    public synchronized int getMaxPitch() {
        return maxPitch;
    }

    public synchronized void setMaxPitch(int maxPitch) {
        this.maxPitch = Mth.clamp(maxPitch, 5, 90);
    }

    public synchronized boolean isInvertY() {
        return invertY;
    }

    public synchronized void setInvertY(boolean invertY) {
        this.invertY = invertY;
    }

    public synchronized boolean isSmoothCamera() {
        return smoothCamera;
    }

    public synchronized void setSmoothCamera(boolean smoothCamera) {
        this.smoothCamera = smoothCamera;
    }

    public synchronized boolean isZoomOut() {
        return zoomOut;
    }

    public synchronized void setZoomOut(boolean zoomOut) {
        this.zoomOut = zoomOut;
    }

    public synchronized int getMinZoom() {
        return minZoom;
    }

    public synchronized void setMinZoom(int minZoom) {
        this.minZoom = Mth.clamp(minZoom, 2, 16);
    }

    public synchronized int getMaxZoom() {
        return maxZoom;
    }

    public synchronized void setMaxZoom(int maxZoom) {
        this.maxZoom = Mth.clamp(maxZoom, 2, 64);
    }

    public synchronized int getZoomMagnification() {
        return zoomMagnification;
    }

    public synchronized void setZoomMagnification(int zoomMagnification) {
        this.zoomMagnification = Mth.clamp(zoomMagnification, 100, 10000);
    }

    public synchronized boolean isShowZoomIndicator() {
        return showZoomIndicator;
    }

    public synchronized void setShowZoomIndicator(boolean showZoomIndicator) {
        this.showZoomIndicator = showZoomIndicator;
    }

    public synchronized boolean isInvertScroll() {
        return invertScroll;
    }

    public synchronized void setInvertScroll(boolean invertScroll) {
        this.invertScroll = invertScroll;
    }

    public synchronized boolean isFreeCamToggle() {
        return freeCamMode == Mode.TOGGLE;
    }

    public synchronized Mode getFreeCamMode() {
        return freeCamMode;
    }

    public synchronized void setFreeCamMode(Mode mode) {
        this.freeCamMode = mode != null ? mode : Mode.TOGGLE;
    }

    public synchronized boolean isFreeCamCollision() {
        return freeCamCollision;
    }

    public synchronized void setFreeCamCollision(boolean freeCamCollision) {
        this.freeCamCollision = freeCamCollision;
    }

    public synchronized boolean isFreeCamKeepSneak() {
        return freeCamKeepSneak;
    }

    public synchronized void setFreeCamKeepSneak(boolean freeCamKeepSneak) {
        this.freeCamKeepSneak = freeCamKeepSneak;
    }

    public synchronized void save() {
        File folder = new File(Minecraft.getInstance().gameDirectory, "config");
        if (!folder.isDirectory() && !folder.mkdirs()) {
            Camerao.LOGGER.error("Failed to create missing config folder");
            return;
        }
        File file = new File(folder, "camerao.json");
        try (FileWriter fw = new FileWriter(file)) {
            GSON.toJson(this, CameraoConfig.class, fw);
        } catch (Exception e) {
            Camerao.LOGGER.error("Failed to write config file {}", file.getName(), e);
        }
    }

    public synchronized void load() {
        File file = new File(new File(Minecraft.getInstance().gameDirectory, "config"), "camerao.json");
        if (!file.exists()) {
            return;
        }
        try (FileReader fr = new FileReader(file)) {
            CameraoConfig loaded = GSON.fromJson(fr, CameraoConfig.class);
            if (loaded == null) {
                return;
            }
            setMode(loaded.mode);
            setPerspective(loaded.perspective);
            setMaxYaw(loaded.maxYaw);
            setMaxPitch(loaded.maxPitch);
            setInvertY(loaded.invertY);
            setSmoothCamera(loaded.smoothCamera);
            setZoomOut(loaded.zoomOut);
            setMinZoom(loaded.minZoom);
            setMaxZoom(loaded.maxZoom);
            setZoomMagnification(loaded.zoomMagnification);
            setShowZoomIndicator(loaded.showZoomIndicator);
            setInvertScroll(loaded.invertScroll);
            setFreeCamMode(loaded.freeCamMode);
            setFreeCamCollision(loaded.freeCamCollision);
            setFreeCamKeepSneak(loaded.freeCamKeepSneak);
        } catch (Exception e) {
            Camerao.LOGGER.error("Failed to read config file {}", file.getName(), e);
        }
    }
}
