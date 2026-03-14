package com.commandguibuttons.gui;

import com.commandguibuttons.config.ButtonManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandGUIButtons implements ClientModInitializer {
    public static final String MOD_ID = "commandguibuttons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Command GUI Buttons");

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.commandguibuttons.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.create(Identifier.of("commandguibuttons", "general"))
        ));

        UiConfig.init();
        ButtonManager.init();
        RadialMenu.register();

        boolean[] wasDown = {false};

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;


            if (client.currentScreen != null && !(client.currentScreen instanceof RadialOverlayScreen)) {
                if (RadialMenu.isVisible()) RadialMenu.hide(false);
                wasDown[0] = false;
                return;
            }


            if (!(client.currentScreen instanceof RadialOverlayScreen)) {
                while (openGuiKey.wasPressed()) {
                    if (UiConfig.isFirstLaunch()) {
                        client.setScreen(new ModeSelectScreen(false));
                        return;
                    }
                    if (UiConfig.getGuiMode() == UiConfig.GuiMode.CLASSIC) {
                        client.setScreen(new ButtonGuiScreen());
                    }
                }
            }

            if (UiConfig.getGuiMode() == UiConfig.GuiMode.RADIAL) {
                boolean isDown = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_G);

                if (isDown && !wasDown[0]) {
                    RadialMenu.show();
                    client.setScreen(new RadialOverlayScreen());
                }

                if (!isDown && wasDown[0] && RadialMenu.isVisible()) {
                    RadialMenu.hide(true);
                    if (client.currentScreen instanceof RadialOverlayScreen) {
                        client.currentScreen.close();
                        client.setScreen(null);
                    }
                }

                wasDown[0] = isDown;
            }
        });
    }

    public static void openModeSelect() {
        MinecraftClient.getInstance().setScreen(new ModeSelectScreen(true));
    }
}