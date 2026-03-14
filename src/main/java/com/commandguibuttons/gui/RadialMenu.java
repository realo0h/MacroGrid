package com.commandguibuttons.gui;

import com.commandguibuttons.config.ButtonData;
import com.commandguibuttons.config.ButtonManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

public class RadialMenu {

    private static boolean visible = false;
    private static int hoveredIndex = -2;

    private static float cx, cy;

    private static final float RADIUS     = 110f;
    private static final float DEAD_ZONE  = 34f;
    private static final int   BTN_W      = 86;
    private static final int   BTN_H      = 18;

    public static void register() {
        HudRenderCallback.EVENT.register(RadialMenu::render);
    }

    public static void show() {
        MinecraftClient mc = MinecraftClient.getInstance();
        cx = mc.getWindow().getScaledWidth()  / 2f;
        cy = mc.getWindow().getScaledHeight() / 2f;
        hoveredIndex = -2;
        visible = true;
    }

    public static void hide(boolean execute) {
        if (!visible) return;
        visible = false;

        int idx = hoveredIndex;
        hoveredIndex = -2;

        if (!execute) return;

        if (idx == -1) {
            MinecraftClient mc2 = MinecraftClient.getInstance();
            mc2.execute(() -> mc2.setScreen(new ButtonGuiScreen()));
            return;
        }
        if (idx < 0) return;

        List<ButtonData> buttons = ButtonManager.getButtons();
        if (idx < buttons.size()) {
            executeButton(buttons.get(idx));
        }
    }

    public static boolean isVisible() { return visible; }


    private static void render(DrawContext ctx, RenderTickCounter tick) {
        if (!visible) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        double scale = mc.getWindow().getScaleFactor();
        float mx = (float)(mc.mouse.getX() / scale);
        float my = (float)(mc.mouse.getY() / scale);

        float dx = mx - cx;
        float dy = my - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        List<ButtonData> buttons = ButtonManager.getButtons();
        int count = buttons.size();


        ctx.fill(0, 0, sw, sh, 0x66000000);

        drawCircle(ctx, (int)cx, (int)cy, (int)RADIUS, 0x33FFFFFF);

        if (count > 0) {
            if (dist <= DEAD_ZONE) {
                hoveredIndex = -1;
            } else {
                float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
                if (angle < 0) angle += 360f;
                float sector = 360f / count;
                float adj = (angle + 90f + sector / 2f) % 360f;
                hoveredIndex = (int)(adj / sector) % count;
            }
        } else {
            hoveredIndex = -1;
        }


        if (count > 0) {
            float sector = 360f / count;
            for (int i = 0; i < count; i++) {
                float aRad = (float) Math.toRadians(i * sector - 90f);
                float bx = cx + (float)(Math.cos(aRad) * RADIUS);
                float by = cy + (float)(Math.sin(aRad) * RADIUS);
                int lc = (i == hoveredIndex) ? 0x885599FF : 0x33888888;
                drawLine(ctx, (int)cx, (int)cy, (int)bx, (int)by, lc);
            }
        }

        if (count > 0) {
            float sector = 360f / count;
            for (int i = 0; i < count; i++) {
                float aRad = (float) Math.toRadians(i * sector - 90f);
                float bx = cx + (float)(Math.cos(aRad) * RADIUS);
                float by = cy + (float)(Math.sin(aRad) * RADIUS);
                drawBtn(ctx, mc, bx, by, BTN_W, BTN_H, makeLabel(buttons.get(i)), i == hoveredIndex);
            }
        } else {
            drawCenteredText(ctx, mc, "§cNo buttons! Open editor to add some.", (int)cx, (int)(cy - RADIUS - 12), 0xFFAAAAAA);
        }

        drawBtn(ctx, mc, cx, cy, 70, 18, "§e⚙ Edit/Add", hoveredIndex == -1);

        String hint = switch (hoveredIndex) {
            case -2 -> "Move mouse to select";
            case -1 -> "§eRelease G to open editor";
            default -> "§aRelease G to run";
        };
        drawCenteredText(ctx, mc, hint, (int)cx, (int)(cy + RADIUS + 14), 0xFFAAAAAA);
    }


    private static void drawBtn(DrawContext ctx, MinecraftClient mc,
                                float bx, float by, int w, int h,
                                String label, boolean hovered) {
        int x = (int)(bx - w / 2f);
        int y = (int)(by - h / 2f);


        if (hovered) ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x883366CC);


        int border = hovered ? 0xFF6699FF : 0xFF555555;
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);


        int bg = hovered ? 0xEE1A3A6A : 0xBB181818;
        ctx.fill(x, y, x + w, y + h, bg);


        int tw = mc.textRenderer.getWidth(label);
        int tx = (int)(bx - tw / 2f);
        int ty = (int)(by - mc.textRenderer.fontHeight / 2f);
        ctx.drawText(mc.textRenderer, label, tx, ty, hovered ? 0xFFFFFFFF : 0xFFBBBBBB, true);
    }

    private static void drawCircle(DrawContext ctx, int cx, int cy, int r, int color) {
        int steps = 64;
        for (int i = 0; i < steps; i++) {
            if (i % 3 == 2) continue;
            double a1 = 2 * Math.PI * i / steps;
            double a2 = 2 * Math.PI * (i + 1) / steps;
            int x1 = (int)(cx + Math.cos(a1) * r);
            int y1 = (int)(cy + Math.sin(a1) * r);
            int x2 = (int)(cx + Math.cos(a2) * r);
            int y2 = (int)(cy + Math.sin(a2) * r);
            drawLine(ctx, x1, y1, x2, y2, color);
        }
    }

    private static void drawLine(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        float ddx = x2 - x1, ddy = y2 - y1;
        int steps = (int) Math.sqrt(ddx * ddx + ddy * ddy);
        if (steps < 1) return;
        for (int i = 0; i <= steps; i++) {
            int px = x1 + (int)(ddx * i / steps);
            int py = y1 + (int)(ddy * i / steps);
            ctx.fill(px, py, px + 1, py + 1, color);
        }
    }

    private static void drawCenteredText(DrawContext ctx, MinecraftClient mc, String t, int x, int y, int color) {
        ctx.drawCenteredTextWithShadow(mc.textRenderer, t, x, y, color);
    }

    private static String makeLabel(ButtonData btn) {
        StringBuilder sb = new StringBuilder();
        if (btn.getIcon() != null && btn.getIcon() != ButtonData.ButtonIcon.NONE)
            sb.append(btn.getIcon().getSymbol()).append(" ");
        if (btn.getColor() != null && btn.getColor() != ButtonData.ButtonColor.DEFAULT)
            sb.append(btn.getColor().getCode());
        String name = btn.getName();
        if (name.length() > 9) name = name.substring(0, 8) + "…";
        sb.append(name);
        return sb.toString();
    }

    private static void executeButton(ButtonData btn) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.execute(() -> {
            for (ButtonData.CommandEntry e : btn.getCommands()) {
                String cmd = e.getCommand();
                if (e.getType() == ButtonData.CommandType.COMMAND) {
                    if (cmd.startsWith("/")) cmd = cmd.substring(1);
                    mc.player.networkHandler.sendChatCommand(cmd);
                } else mc.player.networkHandler.sendChatMessage(cmd);
            }
        });
    }
}