package com.commandguibuttons.gui;

import com.commandguibuttons.config.ButtonData;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.text.Text;
import java.util.function.Consumer;

public class IconSelectionScreen extends CottonClientScreen {

    public IconSelectionScreen(Consumer<ButtonData.ButtonIcon> onIconSelected, Runnable onCancel) {
        super(new IconSelectionGui(onIconSelected, onCancel));
    }

    private static class IconSelectionGui extends LightweightGuiDescription {
        public IconSelectionGui(Consumer<ButtonData.ButtonIcon> onIconSelected, Runnable onCancel) {
            WGridPanel root = new WGridPanel();
            setRootPanel(root);
            root.setSize(300, 240);
            root.setInsets(Insets.ROOT_PANEL);

            WLabel title = new WLabel(Text.literal("Choose an Icon"));
            root.add(title, 0, 0, 8, 1);


            WGridPanel iconGrid = new WGridPanel();
            int col = 0;
            int row = 0;

            for (ButtonData.ButtonIcon icon : ButtonData.ButtonIcon.values()) {
                String label = icon == ButtonData.ButtonIcon.NONE ? "None" : icon.getSymbol() + " " + icon.getName();
                WButton btn = new WButton(Text.literal(label));

                btn.setOnClick(() -> {
                    onIconSelected.accept(icon);
                });


                iconGrid.add(btn, col * 5, row, 5, 1);

                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
            }

            WScrollPanel scrollPanel = new WScrollPanel(iconGrid);
            root.add(scrollPanel, 0, 1, 16, 10);

            WButton cancelButton = new WButton(Text.literal("Cancel"));
            cancelButton.setOnClick(onCancel);
            root.add(cancelButton, 0, 12, 16, 1);

            root.validate(this);
        }
    }
}