package funwayguy.bdsandm.items;

import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.core.BDSM;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemColorTool extends Item
{
    @Override
    public EnumActionResult onItemUse(PlayerEntity player, World worldIn, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    {
        BlockState state = worldIn.getBlockState(pos);
        
        if(state.getBlock() instanceof IBdsmColorBlock)
        {
            player.openGui(BDSM.INSTANCE, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        
        return EnumActionResult.SUCCESS;
    }
}
