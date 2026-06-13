package com.example.voidecho.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.voidecho.VoidEcho;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("void_echo.json");
    private static volatile ModConfig INSTANCE;

    // Config values with defaults
    public double bossHealthMultiplier = 1.0;
    public double voidDimensionDifficulty = 1.0;

    public static ModConfig getInstance() {
        if (INSTANCE == null) INSTANCE = new ModConfig();
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                VoidEcho.LOGGER.info("Loaded config from {}", CONFIG_PATH);
            } catch (Exception e) {
                VoidEcho.LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(getInstance(), writer);
        } catch (Exception e) {
            VoidEcho.LOGGER.error("Failed to save config", e);
        }
    }
}
