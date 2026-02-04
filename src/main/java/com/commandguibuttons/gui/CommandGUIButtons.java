package com.commandguibuttons.gui;

import com.commandguibuttons.config.ButtonManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
        ButtonManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new ButtonGuiScreen());
                }
            }
        });
    }
}
