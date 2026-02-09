package uk.co.finleyofthewoods.timber;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import uk.co.finleyofthewoods.timber.enchantment.TimberEnchantment;

@Slf4j
public class Timber implements ModInitializer {
    public static final String MOD_ID = "timber";
    private static final ModContainer INSTANCE = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(() ->
            new RuntimeException("Could not find mod container for " + MOD_ID));
    public static final String MOD_NAME = INSTANCE.getMetadata().getName();
    public static final String VERSION = INSTANCE.getMetadata().getVersion().getFriendlyString();
    @Override
    public void onInitialize() {
        log.info("{} {} initialised!", MOD_NAME, VERSION);
        TimberEnchantment.register();
    }
}
