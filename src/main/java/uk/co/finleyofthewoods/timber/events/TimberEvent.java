package uk.co.finleyofthewoods.timber.events;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.Before;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.co.finleyofthewoods.timber.config.TimberConfig;
import uk.co.finleyofthewoods.timber.enchantment.TimberEnchantment;
import uk.co.finleyofthewoods.timber.tasks.TimberTask;

import java.util.*;

@Slf4j(topic = "TimberEvent")
public class TimberEvent implements Before{
    private static final Set<TimberTask> TASKS = new HashSet<>();
    private static final TimberConfig config = TimberConfig.getInstance();
    @Override
    public boolean beforeBlockBreak(
            @NonNull World world, @NonNull PlayerEntity player, @NonNull BlockPos pos,
            @NonNull BlockState state, @Nullable BlockEntity blockEntity) {
        if (world.isClient()) {
            log.debug("Client world detected, cancelling event");
            return true;
        }
        if (!state.isIn(BlockTags.AXE_MINEABLE)) {
            log.debug("Block not mineable by an axe, cancelling event");
            return true;
        }
        Map<Integer, Integer> maxBlocks = Map.ofEntries(
                Map.entry(1, 10),
                Map.entry(2, 50),
                Map.entry(3, 100)
        );
        Set<BlockPos> breakingPositions = new HashSet<>();
        ItemStack heldItemStack = player.getMainHandStack();
        int enchantmentLevel = getEnchantmentLevel(world, heldItemStack);
        int maxBreaks = maxBlocks.get(enchantmentLevel);
        if (!heldItemStack.isIn(ItemTags.AXES) || heldItemStack.isEmpty() || enchantmentLevel == 0) {
            log.debug("Player not holding an enchanted axe, cancelling event");
            return true;
        }
        log.debug("TimberEvent fired");
        log.debug("Enchantment level: {}", enchantmentLevel);
        double damage = Math.max(1, 1 * config.getDurabilityFactor()) * maxBreaks;
        if ((heldItemStack.getMaxDamage() - heldItemStack.getDamage()) < damage) {
            log.debug("Player's axe is too damaged, cancelling event");
            player.sendMessage(Text.literal("§cNot enough durability to chop."), true);
            return true;
        }
        findConnectedLogs(world, pos, player, heldItemStack, breakingPositions, maxBreaks);
        return true;
    }

    private int getEnchantmentLevel(World world, ItemStack stack) {
        return world.getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT)
                .getOptional(TimberEnchantment.TIMBER_ENCHANTMENT)
                .map(entry -> EnchantmentHelper.getLevel(entry, stack))
                .orElse(0);
    }

    private void findConnectedLogs(World world, BlockPos startPos, PlayerEntity player, ItemStack tool, Set<BlockPos> breakingPositions, int maxBlocks) {
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> currentLayer = new HashSet<>();
        currentLayer.add(startPos);
        visited.add(startPos);
        breakingPositions.add(startPos);

        while (!currentLayer.isEmpty() && breakingPositions.size() < maxBlocks) {
            Set<BlockPos> nextLayer = new HashSet<>();
            for (BlockPos pos : currentLayer) {
                for (BlockPos neighbour : getNeighbours(pos)) {
                    if (visited.contains(neighbour) || breakingPositions.size() >= maxBlocks) continue;
                    visited.add(neighbour);
                    BlockState neighbourState = world.getBlockState(neighbour);
                    if (neighbourState.isIn(BlockTags.LOGS)) {
                        breakingPositions.add(neighbour);
                        currentLayer.add(neighbour);
                    }
                }
                BlockPos above = pos.up();
                if (!visited.contains(above) && breakingPositions.size() < maxBlocks) {
                    visited.add(above);
                    BlockState aboveState = world.getBlockState(above);
                    if (aboveState.isIn(BlockTags.LOGS) || aboveState.isIn(BlockTags.LEAVES)) {
                        breakingPositions.add(above);
                        nextLayer.add(above);
                    }
                }
            }
            currentLayer = nextLayer;
        }
        log.debug("Found {} connected logs", breakingPositions.size());
        TASKS.add(new TimberTask(world, player, tool, breakingPositions.iterator()));
    }

    private Iterable<BlockPos> getNeighbours(BlockPos pos) {
        Set<BlockPos> neighbours = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                neighbours.add(pos.add(x, 0, z));
            }
        }
        return neighbours;
    }

    public static void onTick(MinecraftServer server) {
        if (TASKS.isEmpty()) return;
        Iterator<TimberTask> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            TimberTask task = iterator.next();
            if (task.isComplete()) {
                iterator.remove();
            } else {
                task.process();
            }
        }
    }
}
