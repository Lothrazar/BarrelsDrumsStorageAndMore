package funwayguy.bdsandm.client.color;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockContainerColor implements IBlockColor, IItemColor {

  public static final BlockContainerColor INSTANCE = new BlockContainerColor();

  @Override
  public int getColor(BlockState state, @Nullable IBlockDisplayReader worldIn, @Nullable BlockPos pos, int tintIndex) {
    if (state.getBlock() instanceof IBdsmColorBlock) {
      int[] colors = ((IBdsmColorBlock) state.getBlock()).getColors(worldIn, state, pos);
      if (tintIndex >= 0 && tintIndex < colors.length) {
        return colors[tintIndex];
      }
    }
    return 0xFFFFFFFF;
  }

  @Override
  public int getColor(ItemStack stack, int tintIndex) {
    if (stack.getItem() instanceof IBdsmColorBlock) {
      int[] colors = ((IBdsmColorItem) stack.getItem()).getColors(stack);
      if (tintIndex >= 0 && tintIndex < colors.length) {
        return colors[tintIndex];
      }
    }
    return 0xFFFFFFFF;
  }
}