package cam.rao.config;

import cam.rao.Camerao;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CameraoConfigScreen {
    private CameraoConfigScreen() {
    }

    private static Component label(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.WHITE);
    }

    private static Component[] tooltip(String key) {
        return new Component[]{Component.translatable(key).withStyle(ChatFormatting.WHITE)};
    }

    private static Component prettyEnum(Enum<?> value) {
        String name = value.name().toLowerCase().replace('_', ' ');
        String pretty = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return Component.literal(pretty).withStyle(ChatFormatting.WHITE);
    }

    public static net.minecraft.client.gui.screens.Screen build(Screen parent) {
        CameraoConfig config = Camerao.config;
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("camerao.config.title").withStyle(ChatFormatting.WHITE))
                .setSavingRunnable(config::save);

        var entryBuilder = builder.entryBuilder();
        var perspective = builder.getOrCreateCategory(
                Component.translatable("camerao.config.category.perspective").withStyle(ChatFormatting.WHITE));

        perspective.addEntry(entryBuilder.startEnumSelector(
                        label("camerao.config.mode"),
                        CameraoConfig.Mode.class, config.getMode())
                .setTooltip(tooltip("camerao.config.mode.tooltip"))
                .setEnumNameProvider(value -> prettyEnum(value))
                .setSaveConsumer(config::setMode)
                .build());

        perspective.addEntry(entryBuilder.startEnumSelector(
                        label("camerao.config.perspective"),
                        CameraType.class, config.getPerspective())
                .setTooltip(tooltip("camerao.config.perspective.tooltip"))
                .setEnumNameProvider(value -> prettyEnum(value))
                .setSaveConsumer(config::setPerspective)
                .build());

        perspective.addEntry(entryBuilder.startIntSlider(
                        label("camerao.config.max_yaw"),
                        config.getMaxYaw(), 5, 360)
                .setTooltip(tooltip("camerao.config.max_yaw.tooltip"))
                .setSaveConsumer(config::setMaxYaw)
                .build());

        perspective.addEntry(entryBuilder.startIntSlider(
                        label("camerao.config.max_pitch"),
                        config.getMaxPitch(), 5, 90)
                .setTooltip(tooltip("camerao.config.max_pitch.tooltip"))
                .setSaveConsumer(config::setMaxPitch)
                .build());

        perspective.addEntry(entryBuilder.startBooleanToggle(
                        label("camerao.config.invert_y"),
                        config.isInvertY())
                .setTooltip(tooltip("camerao.config.invert_y.tooltip"))
                .setSaveConsumer(config::setInvertY)
                .build());

        perspective.addEntry(entryBuilder.startBooleanToggle(
                        label("camerao.config.smooth_camera"),
                        config.isSmoothCamera())
                .setTooltip(tooltip("camerao.config.smooth_camera.tooltip"))
                .setSaveConsumer(config::setSmoothCamera)
                .build());

        perspective.addEntry(entryBuilder.startBooleanToggle(
                        label("camerao.config.zoom_out"),
                        config.isZoomOut())
                .setTooltip(tooltip("camerao.config.zoom_out.tooltip"))
                .setSaveConsumer(config::setZoomOut)
                .build());

        perspective.addEntry(entryBuilder.startIntSlider(
                        label("camerao.config.zoom_in_limit"),
                        config.getMinZoom(), 2, 16)
                .setTooltip(tooltip("camerao.config.zoom_in_limit.tooltip"))
                .setSaveConsumer(config::setMinZoom)
                .build());

        perspective.addEntry(entryBuilder.startIntSlider(
                        label("camerao.config.zoom_out_limit"),
                        config.getMaxZoom(), 2, 64)
                .setTooltip(tooltip("camerao.config.zoom_out_limit.tooltip"))
                .setSaveConsumer(config::setMaxZoom)
                .build());

        var zoom = builder.getOrCreateCategory(
                Component.translatable("camerao.config.category.zoom").withStyle(ChatFormatting.WHITE));

        zoom.addEntry(entryBuilder.startIntSlider(
                        label("camerao.config.zoom_factor"),
                        config.getZoomMagnification(), 100, 10000)
                .setTooltip(tooltip("camerao.config.zoom_factor.tooltip"))
                .setSaveConsumer(config::setZoomMagnification)
                .build());

        zoom.addEntry(entryBuilder.startBooleanToggle(
                        label("camerao.config.show_zoom_indicator"),
                        config.isShowZoomIndicator())
                .setTooltip(tooltip("camerao.config.show_zoom_indicator.tooltip"))
                .setSaveConsumer(config::setShowZoomIndicator)
                .build());

        zoom.addEntry(entryBuilder.startBooleanToggle(
                        label("camerao.config.invert_scroll"),
                        config.isInvertScroll())
                .setTooltip(tooltip("camerao.config.invert_scroll.tooltip"))
                .setSaveConsumer(config::setInvertScroll)
                .build());

        var freeCam = builder.getOrCreateCategory(
                Component.translatable("camerao.config.category.freecam").withStyle(ChatFormatting.WHITE));

        freeCam.addEntry(entryBuilder.startEnumSelector(
                        label("camerao.config.freecam_mode"),
                        CameraoConfig.Mode.class, config.getFreeCamMode())
                .setTooltip(tooltip("camerao.config.freecam_mode.tooltip"))
                .setEnumNameProvider(value -> prettyEnum(value))
                .setSaveConsumer(config::setFreeCamMode)
                .build());

        freeCam.addEntry(entryBuilder.startBooleanToggle(
                        label("camerao.config.freecam_collision"),
                        config.isFreeCamCollision())
                .setTooltip(tooltip("camerao.config.freecam_collision.tooltip"))
                .setSaveConsumer(config::setFreeCamCollision)
                .build());

        freeCam.addEntry(entryBuilder.startBooleanToggle(
                        label("camerao.config.freecam_keep_sneak"),
                        config.isFreeCamKeepSneak())
                .setTooltip(tooltip("camerao.config.freecam_keep_sneak.tooltip"))
                .setSaveConsumer(config::setFreeCamKeepSneak)
                .build());

        freeCam.addEntry(entryBuilder.startBooleanToggle(
                        label("camerao.config.freecam_exit_on_damage"),
                        config.isFreeCamExitOnDamage())
                .setTooltip(tooltip("camerao.config.freecam_exit_on_damage.tooltip"))
                .setSaveConsumer(config::setFreeCamExitOnDamage)
                .build());

        return builder.build();
    }
}
