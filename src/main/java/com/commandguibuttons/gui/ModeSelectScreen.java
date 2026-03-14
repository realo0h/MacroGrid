package com.commandguibuttons.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class ModeSelectScreen extends BaseOwoScreen<FlowLayout> {


    private final boolean fromSettings;

    public ModeSelectScreen(boolean fromSettings) {
        this.fromSettings = fromSettings;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout panel = UIContainers.verticalFlow(Sizing.fixed(320), Sizing.content());
        panel.surface(Surface.flat(0xC0000000)).padding(Insets.of(12));

        panel.child(UIComponents.label(
                        Text.literal("Choose GUI Mode").formatted(Formatting.YELLOW, Formatting.BOLD))
                .horizontalSizing(Sizing.fill(100))
                .margins(Insets.bottom(4)));

        panel.child(UIComponents.label(
                        Text.literal("How do you want to open your buttons?").formatted(Formatting.GRAY))
                .horizontalSizing(Sizing.fill(100))
                .margins(Insets.bottom(16)));


        FlowLayout classicCard = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        classicCard.surface(Surface.flat(0x80333333)).padding(Insets.of(8)).margins(Insets.bottom(8));

        classicCard.child(UIComponents.label(
                        Text.literal("Classic Menu").formatted(Formatting.WHITE, Formatting.BOLD))
                .margins(Insets.bottom(3)));
        classicCard.child(UIComponents.label(
                        Text.literal("Press G → opens a screen with all your buttons in a grid. Click to run.").formatted(Formatting.GRAY))
                .horizontalSizing(Sizing.fill(100))
                .margins(Insets.bottom(6)));

        var classicBtn = UIComponents.button(Text.literal("§aUse Classic Menu"), b -> select(UiConfig.GuiMode.CLASSIC));
        classicBtn.horizontalSizing(Sizing.fill(100));
        classicCard.child(classicBtn);
        panel.child(classicCard);


        FlowLayout radialCard = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        radialCard.surface(Surface.flat(0x80333333)).padding(Insets.of(8)).margins(Insets.bottom(16));

        radialCard.child(UIComponents.label(
                        Text.literal("Radial Menu").formatted(Formatting.WHITE, Formatting.BOLD))
                .margins(Insets.bottom(3)));
        radialCard.child(UIComponents.label(
                        Text.literal("Hold G → wheel appears around cursor. Move mouse to select, release G to run.").formatted(Formatting.GRAY))
                .horizontalSizing(Sizing.fill(100))
                .margins(Insets.bottom(6)));

        var radialBtn = UIComponents.button(Text.literal("§bUse Radial Menu"), b -> select(UiConfig.GuiMode.RADIAL));
        radialBtn.horizontalSizing(Sizing.fill(100));
        radialCard.child(radialBtn);
        panel.child(radialCard);


        if (fromSettings) {
            var cancelBtn = UIComponents.button(Text.literal("Cancel"), b -> close());
            cancelBtn.horizontalSizing(Sizing.fill(100));
            panel.child(cancelBtn);
        } else {
            panel.child(UIComponents.label(
                            Text.literal("You can change this later in the mod settings.").formatted(Formatting.DARK_GRAY))
                    .horizontalSizing(Sizing.fill(100)));
        }

        root.child(panel);
    }

    private void select(UiConfig.GuiMode mode) {
        UiConfig.setGuiMode(mode);
        close();
        CommandGUIButtons.LOGGER.info("GUI mode set to: {}", mode);
    }
}