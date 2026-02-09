package uk.co.finleyofthewoods.timber.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class TimberEnchantment {
    public static final RegistryKey<Enchantment> TIMBER_ENCHANTMENT = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of("timber", "timber") // Namespace, Path
    );

    public static void register() {}
}
