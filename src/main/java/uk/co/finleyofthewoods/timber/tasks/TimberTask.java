package uk.co.finleyofthewoods.timber.tasks;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

@Slf4j
public class TimberTask {
    private final World world;
    private final PlayerEntity player;
    private final ItemStack tool;
    private final Iterator<BlockPos> blockIterator;

    public TimberTask(World world, PlayerEntity player, ItemStack tool, Iterator<BlockPos> blockIterator) {
        this.world = world;
        this.player = player;
        this.tool = tool;
        this.blockIterator = blockIterator;
    }

    public boolean isComplete() {
        return !blockIterator.hasNext();
    }

    public void process() {
        int processedCount = 0;
        int blocksPerTick = 10;
        while(blockIterator.hasNext() && processedCount < blocksPerTick) {
            BlockPos pos = blockIterator.next();
            processedCount++;
            BlockState state = world.getBlockState(pos);

            if (state.isAir() || state.getHardness(world, pos) == -1.0f) {
                log.debug("Block at {} is not valid for chopping", pos);
                continue;
            }

            if (!state.isIn(BlockTags.LOGS) || !state.isIn(BlockTags.AXE_MINEABLE) || !tool.isSuitableFor(state)) {
                log.debug("Block at {} is not suitable for chopping", pos);
                continue;
            }

            if (tool.canMine(state, world, pos, player)) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                Block.dropStacks(state, world, pos, blockEntity, player, tool);
                if (world.breakBlock(pos, false)) {
                    log.debug("Block at {} successfully chopped", pos);
                }
                if (!player.isCreative()) {
                    int damage = (int) Math.max(1, 1 * 0.2);
                    tool.damage(damage, player, EquipmentSlot.MAINHAND);
                    log.debug("Tool {} damaged by {} damage", tool.getItemName(), damage);
                } else {
                    log.debug("Player is in creative mode, not damaging tool");
                }
            }
        }
    }

}
