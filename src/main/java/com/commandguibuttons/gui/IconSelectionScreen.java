package com.commandguibuttons.gui;

import com.commandguibuttons.config.ButtonData;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class IconSelectionScreen extends BaseOwoScreen<FlowLayout> {

    private final Consumer<ButtonData.ButtonIcon> onIconSelected;
    private final Runnable onCancel;

    private static final int ICONS_PER_ROW = 4;

    public IconSelectionScreen(Consumer<ButtonData.ButtonIcon> onIconSelected, Runnable onCancel) {
        this.onIconSelected = onIconSelected;
        this.onCancel = onCancel;
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

        FlowLayout panel = UIContainers.verticalFlow(Sizing.fixed(300), Sizing.content());
        panel.surface(Surface.flat(0xB0000000)).padding(Insets.of(6));


        panel.child(UIComponents.label(Text.literal("Choose an Icon").formatted(Formatting.YELLOW, Formatting.BOLD))
                .margins(Insets.bottom(4)));

        FlowLayout iconGrid = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());

        FlowLayout row = null;
        int col = 0;

        for (ButtonData.ButtonIcon icon : ButtonData.ButtonIcon.values()) {
            if (col == 0) {
                row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
                row.gap(2).margins(Insets.bottom(2));
            }

            String label = (icon == ButtonData.ButtonIcon.NONE) ? "None" : icon.getSymbol() + " " + icon.getName();
            var btn = UIComponents.button(Text.literal(label), b -> onIconSelected.accept(icon));
            btn.horizontalSizing(Sizing.fill(25));
            btn.tooltip(Text.literal(icon.getName()));

            row.child(btn);
            col++;

            if (col >= ICONS_PER_ROW) {
                iconGrid.child(row);
                col = 0;
                row = null;
            }
        }
        if (row != null) iconGrid.child(row);

        var scroll = UIContainers.verticalScroll(Sizing.fill(100), Sizing.fixed(180), iconGrid);
        scroll.margins(Insets.bottom(4));
        panel.child(scroll);

        var cancelBtn = UIComponents.button(Text.literal("Cancel"), b -> onCancel.run());
        cancelBtn.horizontalSizing(Sizing.fill(100));
        panel.child(cancelBtn);

        root.child(panel);
    }
}