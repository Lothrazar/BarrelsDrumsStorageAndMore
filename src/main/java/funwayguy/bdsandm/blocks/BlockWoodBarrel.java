package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

import static net.minecraft.block.BlockFire.AGE;

public class BlockWoodBarrel extends BlockBarrelBase
{
    public BlockWoodBarrel()
    {
        super(Material.WOOD, 64, 1024);
        this.setHardness(2.0F).setResistance(5.0F);
        Blocks.FIRE.setFireInfo(this, 5, 20);
        this.setTickRandomly(true);
        this.setTranslationKey(BDSM.MOD_ID + ".wood_barrel");
    }
    
    @Override
    public void updateTick(World worldIn, BlockPos pos, BlockState state, Random rand)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        
        if(tile instanceof TileEntityBarrel)
        {
            IBarrel barrelCap = tile.getCapability(BdsmCapabilies.BARREL_CAP, null);
            if(barrelCap == null || barrelCap.getCount() <= 0 || barrelCap.getRefFluid() == null) return;
            if(barrelCap.getRefFluid().getFluid().getTemperature() > 500) // Roughly based on the wood ignition temperature in Kelvin
            {
                int j = -50;
                this.tryCatchFire(worldIn, pos.east(), 300 + j, rand, Direction.WEST);
                this.tryCatchFire(worldIn, pos.west(), 300 + j, rand, Direction.EAST);
                this.tryCatchFire(worldIn, pos.down(), 250 + j, rand, Direction.UP);
                this.tryCatchFire(worldIn, pos.up(), 250 + j, rand, Direction.DOWN);
                this.tryCatchFire(worldIn, pos.north(), 300 + j, rand, Direction.SOUTH);
                this.tryCatchFire(worldIn, pos.south(), 300 + j, rand, Direction.NORTH);
                
                worldIn.scheduleUpdate(pos, this, 20 + rand.nextInt(10));
            }
        }
    }

    private void tryCatchFire(World worldIn, BlockPos pos, int chance, Random random, Direction face)
    {
        int i = worldIn.getBlockState(pos).getBlock().getFlammability(worldIn, pos, face);

        if (random.nextInt(chance) < i)
        {
            BlockState BlockState = worldIn.getBlockState(pos);

            if (random.nextInt(10) < 5 && !worldIn.isRainingAt(pos))
            {
                int j = random.nextInt(5) / 4;

                if (j > 15)
                {
                    j = 15;
                }

                worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState().withProperty(AGE, j), 3);
            }
            else
            {
                worldIn.setBlockToAir(pos);
            }

            if (BlockState.getBlock() == Blocks.TNT)
            {
                Blocks.TNT.onPlayerDestroy(worldIn, pos, BlockState.withProperty(BlockTNT.EXPLODE, true));
            }
        }
    }
}
