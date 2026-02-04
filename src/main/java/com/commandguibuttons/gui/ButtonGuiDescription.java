package com.commandguibuttons.gui;

import com.commandguibuttons.config.ButtonData;
import com.commandguibuttons.config.ButtonManager;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;


public class ButtonGuiDescription extends LightweightGuiDescription {
    private WGridPanel root;
    private WGridPanel buttonGrid;
    private WScrollPanel scrollPanel;
    private WTextField searchField;

    private boolean editMode = false;
    private boolean deleteMode = false;
    private boolean moveMode = false;

    private ButtonData selectedForMove = null;

    private WToggleButton editModeToggle;
    private WToggleButton deleteModeToggle;
    private WToggleButton moveModeToggle;

    private static final int BUTTONS_PER_ROW = 3;

    public ButtonGuiDescription() {
        root = new WGridPanel();
        setRootPanel(root);
        root.setSize(360, 240);
        root.setInsets(Insets.ROOT_PANEL);

        WLabel title = new WLabel(Text.literal("Command GUI Buttons"));
        root.add(title, 0, 0, 20, 1);

        searchField = new WTextField();
        searchField.setMaxLength(100);
        searchField.setSuggestion(Text.literal("Search..."));
        searchField.setChangedListener(this::rebuildButtonGrid);
        root.add(searchField, 0, 1, 17, 1);

        WButton clearButton = new WButton(Text.literal("X"));
        clearButton.setOnClick(() -> {
            searchField.setText("");
            rebuildButtonGrid("");
        });
        root.add(clearButton, 17, 1, 2, 1);

        buttonGrid = new WGridPanel();
        scrollPanel = new WScrollPanel(buttonGrid);
        root.add(scrollPanel, 0, 3, 20, 8);

        rebuildButtonGrid();


        WButton addButton = new WButton(Text.literal("+ Add"));
        addButton.setOnClick(this::openAddButtonScreen);
        root.add(addButton, 0, 12, 5, 1);

        editModeToggle = new WToggleButton(Text.literal("✎ Edit"));
        editModeToggle.setOnToggle(on -> {
            if (on) setMode(1); else editMode = false;
            rebuildButtonGrid();
        });
        root.add(editModeToggle, 5, 12, 5, 1);

        moveModeToggle = new WToggleButton(Text.literal("⇄ Move"));
        moveModeToggle.setOnToggle(on -> {
            if (on) setMode(2); else moveMode = false;
            selectedForMove = null;
            rebuildButtonGrid();
        });
        root.add(moveModeToggle, 10, 12, 5, 1);

        deleteModeToggle = new WToggleButton(Text.literal("✖ Del"));
        deleteModeToggle.setOnToggle(on -> {
            if (on) setMode(3); else deleteMode = false;
            rebuildButtonGrid();
        });
        root.add(deleteModeToggle, 15, 12, 5, 1);

        root.validate(this);
    }

    private void setMode(int mode) {
        editMode = (mode == 1);
        moveMode = (mode == 2);
        deleteMode = (mode == 3);

        editModeToggle.setToggle(editMode);
        moveModeToggle.setToggle(moveMode);
        deleteModeToggle.setToggle(deleteMode);

        if (!moveMode) selectedForMove = null;
    }

    private void rebuildButtonGrid(String search) { rebuildButtonGrid(); }

    private void rebuildButtonGrid() {
        if (scrollPanel != null) root.remove(scrollPanel);

        String search = searchField != null ? searchField.getText().toLowerCase() : "";
        List<ButtonData> allButtons = ButtonManager.getButtons();
        List<ButtonData> filtered = search.isEmpty() ? allButtons : allButtons.stream().filter(b -> b.getName().toLowerCase().contains(search)).toList();

        buttonGrid = new WGridPanel();
        int row = 0;
        int col = 0;

        for (ButtonData button : filtered) {
            StringBuilder buttonText = new StringBuilder();

            boolean isSelectedForMove = (moveMode && selectedForMove == button);

            if (deleteMode) {
                buttonText.append("✖ ");
            } else if (moveMode) {
                if (isSelectedForMove) {
                    buttonText.append(">> ");
                } else if (selectedForMove != null) {
                    buttonText.append("⇄ ");
                } else {
                    buttonText.append(":: ");
                }
            } else if (button.getIcon() != null && button.getIcon() != ButtonData.ButtonIcon.NONE) {
                buttonText.append(button.getIcon().getSymbol()).append(" ");
            }

            if (isSelectedForMove) {
                buttonText.append("§a");
            } else if (button.getColor() != null && button.getColor() != ButtonData.ButtonColor.DEFAULT) {
                buttonText.append(button.getColor().getCode());
            }

            buttonText.append(button.getName());

            if (editMode) buttonText = new StringBuilder("✎ " + button.getName());

            String finalText = buttonText.toString();
            if (finalText.length() > 16) finalText = finalText.substring(0, 15) + "...";

            WButton btn = new WButton(Text.literal(finalText)) {
                @Override
                public void addTooltip(TooltipBuilder builder) {
                    if (deleteMode) {
                        builder.add(Text.literal("CLICK TO DELETE").formatted(Formatting.RED, Formatting.BOLD));
                    } else if (moveMode) {
                        if (selectedForMove == null) {
                            builder.add(Text.literal("Click to PICK UP").formatted(Formatting.GREEN));
                        } else if (isSelectedForMove) {
                            builder.add(Text.literal("SELECTED").formatted(Formatting.GREEN, Formatting.BOLD));
                            builder.add(Text.literal("Click elsewhere to DROP").formatted(Formatting.GRAY));
                        } else {
                            builder.add(Text.literal("Click to SWAP here").formatted(Formatting.GOLD));
                        }
                    } else {
                        builder.add(Text.literal(button.getName()).formatted(Formatting.YELLOW));
                        int limit = 0;
                        for (ButtonData.CommandEntry cmd : button.getCommands()) {
                            if (limit >= 3) { builder.add(Text.literal("...").formatted(Formatting.DARK_GRAY)); break; }
                            builder.add(Text.literal(cmd.getCommand()).formatted(Formatting.GRAY));
                            limit++;
                        }
                    }
                }
            };
            btn.setOnClick(() -> handleButtonClick(button));

            buttonGrid.add(btn, col * 6, row, 6, 1);

            col++;
            if (col >= BUTTONS_PER_ROW) {
                col = 0;
                row++;
            }
        }

        scrollPanel = new WScrollPanel(buttonGrid);
        root.add(scrollPanel, 0, 3, 20, 8);
        root.validate(this);
    }

    private void handleButtonClick(ButtonData button) {
        if (editMode) {
            openEditButtonScreen(button);
        } else if (deleteMode) {
            deleteButton(button);
        } else if (moveMode) {
            handleMoveClick(button);
        } else {
            executeButton(button);
        }
    }

    private void handleMoveClick(ButtonData clickedButton) {
        if (selectedForMove == null) {
            selectedForMove = clickedButton;
            rebuildButtonGrid();
            return;
        }

        if (selectedForMove == clickedButton) {
            selectedForMove = null;
            rebuildButtonGrid();
            return;
        }

        List<ButtonData> list = ButtonManager.getButtons();
        int indexSource = list.indexOf(selectedForMove);
        int indexTarget = list.indexOf(clickedButton);

        if (indexSource != -1 && indexTarget != -1) {
            ButtonData data1 = list.get(indexSource);
            ButtonData data2 = list.get(indexTarget);
            ButtonManager.updateButton(indexSource, data2);
            ButtonManager.updateButton(indexTarget, data1);

            selectedForMove = null;
            rebuildButtonGrid();
        }
    }

    private void executeButton(ButtonData button) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        for (ButtonData.CommandEntry entry : button.getCommands()) {
            String cmd = entry.getCommand();
            if (entry.getType() == ButtonData.CommandType.COMMAND) {
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                client.player.networkHandler.sendChatCommand(cmd);
            } else client.player.networkHandler.sendChatMessage(cmd);
        }
        if (client.currentScreen != null) client.currentScreen.close();
    }

    private void openAddButtonScreen() {
        MinecraftClient.getInstance().setScreen(new EditButtonScreen(null, button -> {
            ButtonManager.addButton(button);
            MinecraftClient.getInstance().setScreen(new ButtonGuiScreen());
        }));
    }

    private void openEditButtonScreen(ButtonData button) {
        int index = ButtonManager.getButtons().indexOf(button);
        MinecraftClient.getInstance().setScreen(new EditButtonScreen(button, updated -> {
            ButtonManager.updateButton(index, updated);
            MinecraftClient.getInstance().setScreen(new ButtonGuiScreen());
        }));
    }

    private void deleteButton(ButtonData button) {
        List<ButtonData> list = ButtonManager.getButtons();
        int index = list.indexOf(button);
        if (index != -1) {
            ButtonManager.removeButton(index);
            rebuildButtonGrid();
        }
    }
}