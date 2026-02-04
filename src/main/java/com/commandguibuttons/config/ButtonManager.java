package com.commandguibuttons.config;

import com.commandguibuttons.gui.CommandGUIButtons;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ButtonManager {
    private static final String CONFIG_FILE = "command_gui_buttons.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<ButtonData> buttons = new ArrayList<>();
    private static Path configPath;

    public static void init() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        load();
    }

    public static List<ButtonData> getButtons() {
        return new ArrayList<>(buttons);
    }

    public static void addButton(ButtonData button) {
        buttons.add(button);
        save();
    }

    public static void removeButton(int index) {
        if (index >= 0 && index < buttons.size()) {
            buttons.remove(index);
            save();
        }
    }

    public static void updateButton(int index, ButtonData button) {
        if (index >= 0 && index < buttons.size()) {
            buttons.set(index, button);
            save();
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            JsonArray buttonsArray = new JsonArray();

            for (ButtonData button : buttons) {
                buttonsArray.add(button.toJson());
            }

            root.add("buttons", buttonsArray);

            Files.createDirectories(configPath.getParent());

            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                GSON.toJson(root, writer);
            }

            CommandGUIButtons.LOGGER.info("Saved {} buttons to config", buttons.size());
        } catch (IOException e) {
            CommandGUIButtons.LOGGER.error("Failed to save buttons config", e);
        }
    }

    public static void load() {
        buttons.clear();

        if (!Files.exists(configPath)) {
            CommandGUIButtons.LOGGER.info("Config file not found, creating new one");
            save();
            return;
        }

        try (FileReader reader = new FileReader(configPath.toFile())) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            if (root.has("buttons")) {
                JsonArray buttonsArray = root.getAsJsonArray("buttons");

                for (int i = 0; i < buttonsArray.size(); i++) {
                    JsonObject buttonJson = buttonsArray.get(i).getAsJsonObject();
                    buttons.add(ButtonData.fromJson(buttonJson));
                }
            }

            CommandGUIButtons.LOGGER.info("Loaded {} buttons from config", buttons.size());
        } catch (IOException | JsonParseException e) {
            CommandGUIButtons.LOGGER.error("Failed to load buttons config", e);
        }
    }
}
