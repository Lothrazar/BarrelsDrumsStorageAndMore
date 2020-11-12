package funwayguy.bdsandm.client.color;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;

public interface IBdsmColorBlock {

  int getColorCount(World blockAccess, BlockState state, BlockPos pos);

  int[] getColors(IBlockDisplayReader blockAccess, BlockState state, BlockPos pos);

  void setColors(World blockAccess, BlockState state, BlockPos pos, int[] colors);
}