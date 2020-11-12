package funwayguy.bdsandm.events;
//TODO: i think this is totally gone in new forge
public class GuiHandler //implements IGuiHandler
{
  //    @Nullable
  //    @Override
  //    public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
  //    {
  //        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
  //        
  //        if(ID == 0 && tile instanceof TileEntityShipping)
  //        {
  //            return new ContainerShipping(player.inventory, ((TileEntityShipping)tile).getContainerInvo());
  //        }
  //        
  //        return null;
  //    }
  //    
  //    @Nullable
  //    @Override
  //    public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
  //    {
  //        BlockPos pos = new BlockPos(x, y, z);
  //        TileEntity tile = world.getTileEntity(pos);
  //        BlockState state = world.getBlockState(pos);
  //        
  //        if(ID == 0 && tile instanceof TileEntityShipping)
  //        {
  //            return new GuiShipping(player.inventory, ((TileEntityShipping)tile).getContainerInvo());
  //        } else if(ID == 1 && state.getBlock() instanceof IBdsmColorBlock)
  //        {
  //            return new GuiColour((IBdsmColorBlock)state.getBlock(), world, pos);
  //        }
  //        
  //        return null;
  //    }
}
