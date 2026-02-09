package uk.co.finleyofthewoods.timber.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j(topic = "TimberConfig")
public class TimberConfig {
    private static TimberConfig INSTANCE;
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("timber.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    public boolean enableLedger = false;
    @Getter
    public int blocksPerTick = 10;
    @Getter
    public double durabilityFactor = 0.2;

    public static TimberConfig getInstance() {
        if (INSTANCE == null) {
            log.debug("Timber config not loaded, loading now");
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            log.debug("Timber config found, loading");
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, TimberConfig.class);
            } catch (Exception e) {
                log.error("Failed to load timber config", e);
                INSTANCE = new TimberConfig();
            }
        } else {
            log.debug("Timber config not found, creating new one");
            INSTANCE = new TimberConfig();
            save();
        }
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            log.debug("Saving timber config");
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            log.error("Failed to save timber config", e);
        }
    }
}
