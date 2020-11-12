package funwayguy.bdsandm.events;

import funwayguy.bdsandm.blocks.BlockBarrelBase;
import funwayguy.bdsandm.blocks.BlockCrateBase;
import net.minecraft.block.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class EventHandler {

  @SubscribeEvent
  public static void onBlockHit(LeftClickBlock event) {
    BlockState state = event.getWorld().getBlockState(event.getPos());
    if (!event.getPlayer().isSneaking() && event.getPlayer().isCreative() && (state.getBlock() instanceof BlockCrateBase || state.getBlock() instanceof BlockBarrelBase)) {
      event.setCanceled(true);
      state.getBlock().onBlockClicked(state, event.getWorld(), event.getPos(), event.getPlayer());
    }
  }
}
