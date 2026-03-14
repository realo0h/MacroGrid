package com.commandguibuttons.gui;

import com.commandguibuttons.config.ButtonData;
import com.commandguibuttons.config.ButtonManager;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ButtonGuiScreen extends BaseOwoScreen<FlowLayout> {


    private enum Mode { NONE, EDIT, MOVE, DELETE }
    private Mode mode = Mode.NONE;
    private ButtonData moveSource = null;

    private static final int COLS = 3;

    private FlowLayout buttonGridLayout;
    private TextBoxComponent searchBox;
    private ButtonComponent editBtn;

    private boolean firstTick = true;

    @Override
    public void tick() {
        super.tick();
        if (firstTick) {
            firstTick = false;
            if (searchBox != null) searchBox.setText("");
            if (uiAdapter != null)
                uiAdapter.rootComponent.focusHandler().focus(null, UIComponent.FocusSource.MOUSE_CLICK);
        }
    }

    @Override
    protected void init() {
        super.init();
        if (uiAdapter != null)
            uiAdapter.rootComponent.focusHandler().focus(null, UIComponent.FocusSource.MOUSE_CLICK);
        if (searchBox != null) searchBox.setText("");
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

        FlowLayout panel = UIContainers.verticalFlow(Sizing.fixed(340), Sizing.content());
        panel.surface(Surface.flat(0xC0000000)).padding(Insets.of(6));


        panel.child(UIComponents.label(
                        Text.literal("MacroGrid").formatted(Formatting.YELLOW, Formatting.BOLD))
                .horizontalSizing(Sizing.fill(100))
                .margins(Insets.bottom(6)));

        FlowLayout searchRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        searchBox = UIComponents.textBox(Sizing.fill(90));
        searchBox.setMaxLength(100);
        searchBox.setSuggestion("Search...");
        searchBox.onChanged().subscribe(s -> {
            searchBox.setSuggestion(s.isEmpty() ? "Search..." : "");
            rebuildGrid();
        });
        ButtonComponent clearBtn = UIComponents.button(Text.literal("X"), b -> searchBox.setText(""));
        clearBtn.horizontalSizing(Sizing.fixed(18));
        searchRow.child(searchBox).child(clearBtn).gap(2).margins(Insets.bottom(6));
        panel.child(searchRow);


        buttonGridLayout = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        var scroll = UIContainers.verticalScroll(Sizing.fill(100), Sizing.fixed(160), buttonGridLayout);
        scroll.margins(Insets.bottom(6));
        panel.child(scroll);


        FlowLayout bar = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        bar.gap(4);

        ButtonComponent addBtn = UIComponents.button(Text.literal("§a+ Add"), b -> openAddScreen());
        addBtn.horizontalSizing(Sizing.fixed(140));

        editBtn = UIComponents.button(Text.literal(makeModeLabel()), b -> cycleMode());
        editBtn.horizontalSizing(Sizing.fixed(110));

        ButtonComponent modeBtn = UIComponents.button(Text.literal("§7⚙"), b -> CommandGUIButtons.openModeSelect());
        modeBtn.horizontalSizing(Sizing.fixed(64));
        modeBtn.tooltip(Text.literal("Switch GUI mode (Classic / Radial)"));

        bar.child(addBtn).child(editBtn).child(modeBtn);
        panel.child(bar);

        root.child(panel);
        rebuildGrid();
    }

    private void cycleMode() {
        mode = switch (mode) {
            case NONE   -> Mode.EDIT;
            case EDIT   -> Mode.MOVE;
            case MOVE   -> Mode.DELETE;
            case DELETE -> Mode.NONE;
        };
        if (mode != Mode.MOVE) moveSource = null;
        if (editBtn != null) editBtn.setMessage(Text.literal(makeModeLabel()));
        rebuildGrid();
    }

    private String makeModeLabel() {
        return switch (mode) {
            case NONE   -> "Mode: §7Normal";
            case EDIT   -> "Mode: §e✎ Edit";
            case MOVE   -> "Mode: §9⇄ Move";
            case DELETE -> "Mode: §c✖ Delete";
        };
    }

    private void rebuildGrid() {
        if (buttonGridLayout == null) return;
        buttonGridLayout.clearChildren();

        String search = searchBox != null ? searchBox.getText().toLowerCase() : "";
        List<ButtonData> all = ButtonManager.getButtons();
        List<ButtonData> list = search.isEmpty()
                ? all
                : all.stream().filter(b -> b.getName().toLowerCase().contains(search)).toList();

        FlowLayout row = null;
        int col = 0;

        for (ButtonData button : list) {
            if (col == 0) {
                row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
                row.gap(2).margins(Insets.bottom(2));
            }

            String label = makeLabel(button);
            ButtonComponent btn = UIComponents.button(Text.literal(label), b -> handleClick(button));
            btn.horizontalSizing(Sizing.fill(33));
            btn.tooltip(makeTooltip(button));

            row.child(btn);
            col++;
            if (col >= COLS) {
                buttonGridLayout.child(row);
                col = 0;
                row = null;
            }
        }
        if (row != null) buttonGridLayout.child(row);
    }

    private String makeLabel(ButtonData btn) {
        StringBuilder sb = new StringBuilder();
        switch (mode) {
            case DELETE -> sb.append("§c✖ ");
            case EDIT   -> sb.append("§e✎ ");
            case MOVE   -> {
                if (moveSource == btn) sb.append("§a>> ");
                else if (moveSource != null) sb.append("§6→ ");
                else sb.append("§9:: ");
            }
            case NONE -> {
                if (btn.getIcon() != null && btn.getIcon() != ButtonData.ButtonIcon.NONE)
                    sb.append(btn.getIcon().getSymbol()).append(" ");
                if (btn.getColor() != null && btn.getColor() != ButtonData.ButtonColor.DEFAULT)
                    sb.append(btn.getColor().getCode());
            }
        }
        sb.append(btn.getName());
        return sb.toString();
    }

    private List<Text> makeTooltip(ButtonData btn) {
        var lines = new java.util.ArrayList<Text>();
        lines.add(Text.literal(btn.getName()).formatted(Formatting.YELLOW));
        switch (mode) {
            case DELETE -> lines.add(Text.literal("Click to DELETE").formatted(Formatting.RED, Formatting.BOLD));
            case EDIT   -> lines.add(Text.literal("Click to EDIT").formatted(Formatting.YELLOW));
            case MOVE   -> lines.add(Text.literal(moveSource == null ? "Click to pick up" : "Click to swap here").formatted(Formatting.GOLD));
            case NONE   -> {
                int n = 0;
                for (ButtonData.CommandEntry cmd : btn.getCommands()) {
                    if (n++ >= 4) { lines.add(Text.literal("...").formatted(Formatting.DARK_GRAY)); break; }
                    lines.add(Text.literal(cmd.getCommand()).formatted(Formatting.GRAY));
                }
            }
        }
        return lines;
    }

    private void handleClick(ButtonData button) {
        switch (mode) {
            case NONE   -> execute(button);
            case EDIT   -> { mode = Mode.NONE; if (editBtn != null) editBtn.setMessage(Text.literal(makeModeLabel())); openEditScreen(button); }
            case DELETE -> { deleteButton(button); }
            case MOVE   -> {
                if (moveSource == null) {
                    moveSource = button;
                    rebuildGrid();
                } else if (moveSource == button) {
                    moveSource = null;
                    rebuildGrid();
                } else {
                    swapButtons(moveSource, button);
                    moveSource = null;
                    mode = Mode.NONE;
                    if (editBtn != null) editBtn.setMessage(Text.literal(makeModeLabel()));
                    rebuildGrid();
                }
            }
        }
    }

    private void swapButtons(ButtonData a, ButtonData b) {
        List<ButtonData> list = ButtonManager.getButtons();
        int i = list.indexOf(a), j = list.indexOf(b);
        if (i != -1 && j != -1) {
            ButtonData tmp = list.get(i);
            ButtonManager.updateButton(i, list.get(j));
            ButtonManager.updateButton(j, tmp);
        }
    }

    private void execute(ButtonData button) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        for (ButtonData.CommandEntry e : button.getCommands()) {
            String cmd = e.getCommand();
            if (e.getType() == ButtonData.CommandType.COMMAND) {
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                mc.player.networkHandler.sendChatCommand(cmd);
            } else mc.player.networkHandler.sendChatMessage(cmd);
        }
        close();
    }

    private void openAddScreen() {
        MinecraftClient.getInstance().setScreen(new EditButtonScreen(null, btn -> {
            ButtonManager.addButton(btn);
            MinecraftClient.getInstance().setScreen(new ButtonGuiScreen());
        }));
    }

    private void openEditScreen(ButtonData button) {
        int index = ButtonManager.getButtons().indexOf(button);
        MinecraftClient.getInstance().setScreen(new EditButtonScreen(button, updated -> {
            ButtonManager.updateButton(index, updated);
            MinecraftClient.getInstance().setScreen(new ButtonGuiScreen());
        }));
    }

    private void deleteButton(ButtonData button) {
        List<ButtonData> list = ButtonManager.getButtons();
        int i = list.indexOf(button);
        if (i != -1) ButtonManager.removeButton(i);
        rebuildGrid();
    }
}