package cam.rao.hud;

import cam.rao.Camerao;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Shows the current zoom percentage above the hotbar while zooming,
 * styled like the vanilla held item name display.
 */
public class ZoomHud implements HudElement {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(Camerao.MOD_ID, "zoom_indicator");

    public static void register() {
        HudElementRegistry.addLast(ID, new ZoomHud());
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!Camerao.config.isShowZoomIndicator()
                || Camerao.zoomFovFactor >= 0.999F) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        // Show the target magnification so the number is stable and accurate.
        int percent = Camerao.currentZoomMagnification;
        Font font = mc.font;
        graphics.drawCenteredString(font,
                Component.translatable("camerao.hud.zoom", percent).withStyle(ChatFormatting.WHITE),
                graphics.guiWidth() / 2,
                graphics.guiHeight() - 59,
                0xFFFFFFFF);
    }
}
