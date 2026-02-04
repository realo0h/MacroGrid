package com.commandguibuttons.gui;

import com.commandguibuttons.config.ButtonData;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class EditButtonScreen extends CottonClientScreen {

    public EditButtonScreen(ButtonData button, Consumer<ButtonData> onSave) {
        this(button, onSave, button == null);
    }

    private EditButtonScreen(ButtonData button, Consumer<ButtonData> onSave, boolean isAddMode) {
        super(new EditButtonGuiDescription(button, onSave, isAddMode));
    }

    private static class EditButtonGuiDescription extends LightweightGuiDescription {
        private WGridPanel root;
        private WTextField nameField;
        private List<CommandWidget> commandWidgets;
        private WBox commandListBox;
        private WScrollPanel commandScrollPanel;

        private Consumer<ButtonData> onSave;
        private final boolean isAddMode;

        private ButtonData.ButtonColor currentColor = ButtonData.ButtonColor.DEFAULT;
        private ButtonData.ButtonIcon currentIcon = ButtonData.ButtonIcon.NONE;

        private static class CommandWidget {
            WTextField textField;
            ButtonData.CommandType type;
            CommandWidget(String text, ButtonData.CommandType type) {
                this.textField = new WTextField();
                this.textField.setText(text);
                this.textField.setMaxLength(256);
                this.type = type;
            }
            String getText() { return textField.getText(); }
            ButtonData.CommandType getType() { return type; }
        }

        public EditButtonGuiDescription(ButtonData button, Consumer<ButtonData> onSave, boolean isAddMode) {
            this.onSave = onSave;
            this.isAddMode = isAddMode;

            root = new WGridPanel();
            setRootPanel(root);
            root.setSize(360, 300);
            root.setInsets(Insets.ROOT_PANEL);

            commandWidgets = new ArrayList<>();
            if (button != null) {
                for (ButtonData.CommandEntry cmd : button.getCommands()) {
                    commandWidgets.add(new CommandWidget(cmd.getCommand(), cmd.getType()));
                }
                currentColor = button.getColor();
                currentIcon = button.getIcon();
            }

            root.add(new WLabel(Text.literal(isAddMode ? "Add New Button" : "Edit Button")), 0, 0, 20, 1);
            root.add(new WLabel(Text.literal("Button Name:")), 0, 1, 20, 1);
            nameField = new WTextField();
            nameField.setMaxLength(100);
            nameField.setSuggestion(Text.literal("Enter button name..."));
            if (button != null && button.getName() != null) nameField.setText(button.getName());
            root.add(nameField, 0, 2, 20, 1);

            root.add(new WLabel(Text.literal("Color:")), 0, 4, 2, 1);
            WButton colorButton = new WButton(Text.literal(currentColor.getDisplayName()));
            colorButton.setOnClick(() -> {
                ButtonData.ButtonColor[] colors = ButtonData.ButtonColor.values();
                int currentIndex = Arrays.asList(colors).indexOf(currentColor);
                currentColor = colors[(currentIndex + 1) % colors.length];
                colorButton.setLabel(Text.literal(currentColor.getDisplayName()));
            });
            root.add(colorButton, 2, 4, 7, 1);

            root.add(new WLabel(Text.literal("Icon:")), 10, 4, 2, 1);
            String iconText = currentIcon == ButtonData.ButtonIcon.NONE ? "None" : currentIcon.getSymbol() + " " + currentIcon.getName();
            WButton iconButton = new WButton(Text.literal(iconText));

            iconButton.setOnClick(() -> {
                ButtonData currentData = buildButtonData();
                MinecraftClient.getInstance().setScreen(new IconSelectionScreen(
                        (selectedIcon) -> {
                            currentData.setIcon(selectedIcon);
                            MinecraftClient.getInstance().setScreen(new EditButtonScreen(currentData, onSave, this.isAddMode));
                        },
                        () -> {
                            MinecraftClient.getInstance().setScreen(new EditButtonScreen(currentData, onSave, this.isAddMode));
                        }
                ));
            });
            root.add(iconButton, 12, 4, 8, 1);

            root.add(new WLabel(Text.literal("Actions:")), 0, 5, 20, 1);

            commandListBox = new WBox(io.github.cottonmc.cotton.gui.widget.data.Axis.VERTICAL);
            rebuildCommandList();

            WButton addCommandButton = new WButton(Text.literal("+ Command"));
            addCommandButton.setOnClick(() -> {
                commandWidgets.add(new CommandWidget("/", ButtonData.CommandType.COMMAND));
                rebuildCommandList();
            });
            root.add(addCommandButton, 0, 14, 10, 1);

            WButton addMessageButton = new WButton(Text.literal("+ Message"));
            addMessageButton.setOnClick(() -> {
                commandWidgets.add(new CommandWidget("", ButtonData.CommandType.MESSAGE));
                rebuildCommandList();
            });
            root.add(addMessageButton, 10, 14, 10, 1);

            WButton saveButton = new WButton(Text.literal("Save"));
            saveButton.setOnClick(() -> saveAndClose());
            root.add(saveButton, 0, 15, 10, 1);

            WButton cancelButton = new WButton(Text.literal("Cancel"));
            cancelButton.setOnClick(() -> MinecraftClient.getInstance().setScreen(null));
            root.add(cancelButton, 10, 15, 10, 1);

            root.validate(this);
        }

        private ButtonData buildButtonData() {
            String name = nameField.getText();
            List<ButtonData.CommandEntry> cmds = new ArrayList<>();
            for (CommandWidget w : commandWidgets) {
                cmds.add(new ButtonData.CommandEntry(w.getText(), w.getType()));
            }
            ButtonData data = new ButtonData(name, cmds);
            data.setColor(currentColor);
            data.setIcon(currentIcon);
            return data;
        }

        private void rebuildCommandList() {
            commandListBox = new WBox(io.github.cottonmc.cotton.gui.widget.data.Axis.VERTICAL);
            for (int i = 0; i < commandWidgets.size(); i++) {
                final int index = i;
                CommandWidget cmdWidget = commandWidgets.get(i);
                WGridPanel panel = new WGridPanel();

                String typeLabel = cmdWidget.type == ButtonData.CommandType.COMMAND ? "CMD" : "MSG";
                panel.add(new WLabel(Text.literal(typeLabel)), 0, 0, 2, 1);
                panel.add(cmdWidget.textField, 2, 0, 10, 1);

                WButton upBtn = new WButton(Text.literal("↑"));
                upBtn.setOnClick(() -> moveUp(index));
                panel.add(upBtn, 12, 0, 2, 1);

                WButton downBtn = new WButton(Text.literal("↓"));
                downBtn.setOnClick(() -> moveDown(index));
                panel.add(downBtn, 14, 0, 2, 1);

                WButton delBtn = new WButton(Text.literal("X"));
                delBtn.setOnClick(() -> deleteCommand(index));
                panel.add(delBtn, 16, 0, 2, 1);

                commandListBox.add(panel);
            }

            if (commandScrollPanel != null) {
                root.remove(commandScrollPanel);
            }

            commandScrollPanel = new WScrollPanel(commandListBox);
            root.add(commandScrollPanel, 0, 6, 20, 7);
            root.validate(this);
        }

        private void moveUp(int index) { if (index > 0) { CommandWidget temp = commandWidgets.get(index); commandWidgets.set(index, commandWidgets.get(index - 1)); commandWidgets.set(index - 1, temp); rebuildCommandList(); } }
        private void moveDown(int index) { if (index < commandWidgets.size() - 1) { CommandWidget temp = commandWidgets.get(index); commandWidgets.set(index, commandWidgets.get(index + 1)); commandWidgets.set(index + 1, temp); rebuildCommandList(); } }
        private void deleteCommand(int index) { if (index >= 0 && index < commandWidgets.size()) { commandWidgets.remove(index); rebuildCommandList(); } }

        private void saveAndClose() {
            String buttonName = nameField.getText().trim();
            if (buttonName.isEmpty()) return;
            ButtonData newButton = buildButtonData();
            onSave.accept(newButton);
            MinecraftClient.getInstance().setScreen(null);
        }
    }
}