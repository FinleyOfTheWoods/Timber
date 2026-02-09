package uk.co.finleyofthewoods.timber.utils;

import com.github.quiltservertools.ledger.actions.ActionType;
import com.github.quiltservertools.ledger.actionutils.ActionFactory;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.github.quiltservertools.ledger.Ledger;
import uk.co.finleyofthewoods.timber.config.TimberConfig;

@Slf4j(topic = "LedgerLog")
public class LedgerLog {
    private static final TimberConfig config = TimberConfig.getInstance();
    public static void insert(World world, PlayerEntity player, BlockPos pos, BlockState state, ItemPlacementContext context, BlockEntity blockEntity) {
        if (config.isEnableLedger()) {
            try {
                log.debug("Logging block break action for {} at {}", player.getName().toString(), pos.toString());
                ActionType action = ActionFactory.INSTANCE.blockBreakAction(world, pos, state, player.getName().toString(), blockEntity);
                Ledger.getApi().logAction(action);
            } catch (Exception e) {
                log.error("Failed to log block break action", e);
            }
        } else {
            log.debug("Ledger logging disabled");
        }
    }
}
