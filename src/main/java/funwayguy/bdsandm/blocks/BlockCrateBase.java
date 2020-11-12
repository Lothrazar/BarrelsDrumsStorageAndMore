package funwayguy.bdsandm.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import funwayguy.bdsandm.blocks.tiles.TileEntityCrate;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.ICrate;
import funwayguy.bdsandm.network.PacketBdsm;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockCrateBase extends DirectionalBlock implements ITileEntityProvider, IStorageBlock {

  private final int initCap;
  private final int maxCap;

  @SuppressWarnings("WeakerAccess")
  public BlockCrateBase(Material material, int initCap, int maxCap) {
    super(material);
    this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.NORTH));
    this.setCreativeTab(BDSM.tabBdsm);
    this.initCap = initCap;
    this.maxCap = maxCap;
  }

  @Override
  public void onPlayerInteract(World world, BlockPos pos, BlockState state, Direction side, ServerPlayerEntity player, Hand hand, boolean isHit, boolean altMode, int clickDelay) {
    Direction curFace = state.getValue(FACING);
    if (curFace != side.getOpposite()) return;
    TileEntity tile = world.getTileEntity(pos);
    if (!(tile instanceof TileEntityCrate)) return;
    TileEntityCrate tileCrate = (TileEntityCrate) tile;
    ICrate crate = tile.getCapability(BdsmCapabilies.CRATE_CAP, null);
    if (crate == null || (!isHit && crate.installUpgrade(player, player.getHeldItem(hand)))) return;
    if (!isHit) {
      if (!player.isSneaking()) depositItem(crate, player, hand);
    }
    else {
      if (altMode) {
        withdrawItem(crate, player, player.isSneaking() ? 0 : 2);
      }
      else if (!player.isSneaking()) {
        int curClick = tileCrate.getClickCount(world.getTotalWorldTime(), clickDelay);
        if (curClick >= 0) withdrawItem(crate, player, curClick);
      }
    }
  }

  @Override
  public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
    if (worldIn.isRemote || !(playerIn instanceof ServerPlayerEntity)) return true;
    CompoundNBT tag = new CompoundNBT();
    tag.setInteger("msgType", 2);
    tag.setLong("pos", pos.toLong());
    tag.setBoolean("isHit", false);
    tag.setBoolean("offHand", hand == Hand.OFF_HAND);
    BDSM.INSTANCE.network.sendTo(new PacketBdsm(tag), (ServerPlayerEntity) playerIn);
    return true;
  }

  @Override
  public void onBlockClicked(World worldIn, BlockPos pos, PlayerEntity playerIn) {
    if (worldIn.isRemote || !(playerIn instanceof ServerPlayerEntity)) return;
    CompoundNBT tag = new CompoundNBT();
    tag.setInteger("msgType", 2);
    tag.setLong("pos", pos.toLong());
    tag.setBoolean("isHit", true);
    tag.setBoolean("offHand", false);
    BDSM.INSTANCE.network.sendTo(new PacketBdsm(tag), (ServerPlayerEntity) playerIn);
  }

  private void depositItem(ICrate crate, PlayerEntity player, Hand hand) {
    ItemStack ref = crate.getRefItem();
    ItemStack held = player.getHeldItem(hand);
    if (!held.isEmpty() && (ref.isEmpty() || crate.canMergeWith(held) || held.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))) {
      if (held.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
        IItemHandler heldCrate = held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        assert heldCrate != null;
        for (int s = 0; s < heldCrate.getSlots(); s++) {
          ItemStack transfer = heldCrate.extractItem(s, Integer.MAX_VALUE, true);
          ItemStack transStack = transfer.copy();
          transStack.setCount(transStack.getCount() * held.getCount());
          int prev = transStack.getCount();
          if (prev > 0 && (ref.isEmpty() || crate.canMergeWith(transStack))) {
            transStack = crate.insertItem(crate.getSlots() - 1, transStack, false);
            if (transStack.getCount() != prev) heldCrate.extractItem(s, (prev - transStack.getCount()) / held.getCount(), false);
          }
        }
      }
      else {
        player.setHeldItem(hand, crate.insertItem(crate.getSlots() - 1, held, false));
      }
    }
    else if (!ref.isEmpty()) // Insert all
    {
      for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
        ItemStack invoStack = player.inventory.getStackInSlot(i);
        if (crate.canMergeWith(invoStack)) {
          invoStack = crate.insertItem(crate.getSlots() - 1, invoStack, false);
          boolean done = !invoStack.isEmpty();
          player.inventory.setInventorySlotContents(i, invoStack);
          if (done) break;
        }
      }
    }
  }

  private void withdrawItem(ICrate crate, PlayerEntity player, int clickCount) {
    if (!crate.getRefItem().isEmpty()) {
      int count = clickCount <= 0 ? 1 : crate.getRefItem().getMaxStackSize();
      if (clickCount == 1) count--;
      ItemStack out = crate.extractItem(0, count, false);
      if (player.getHeldItem(Hand.MAIN_HAND).isEmpty()) {
        player.setHeldItem(Hand.MAIN_HAND, out);
      }
      else if (!player.addItemStackToInventory(out)) player.dropItem(out, false, false);
    }
  }
  //
  //  @Nonnull
  //  @Override
  //  @SuppressWarnings("deprecation")
  //  public EnumBlockRenderType getRenderType(BlockState state) {
  //    return EnumBlockRenderType.MODEL;
  //  }
  //
  //  @Nonnull
  //  @Override
  //  @SideOnly(Side.CLIENT)
  //  public BlockRenderLayer getRenderLayer() {
  //    return BlockRenderLayer.CUTOUT;
  //  }
  //  @Override
  //  @SuppressWarnings("deprecation")
  //  public boolean isFullCube(BlockState state) {
  //    return false;
  //  }
  //
  //  @Override
  //  @SuppressWarnings("deprecation")
  //  public boolean isOpaqueCube(BlockState state) {
  //    this.opa
  //    return false;
  //  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
    return new TileEntityCrate(initCap, maxCap);
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, EntityLivingBase placer, ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    if (worldIn.isRemote) return;
    TileEntity tile = worldIn.getTileEntity(pos);
    if (tile instanceof TileEntityCrate) ((TileEntityCrate) tile).onCrateChanged();
  }

  @Nonnull
  @Override
  public BlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Direction facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, @Nonnull Hand hand) {
    return this.getDefaultState().withProperty(FACING, Direction.getDirectionFromEntityLiving(pos, placer).getOpposite());
  }

  @Override
  public int getMetaFromState(BlockState state) {
    return (state.getValue(FACING)).getIndex();
  }

  @Nonnull
  @Override
  @SuppressWarnings("deprecation")
  public BlockState getStateFromMeta(int meta) {
    return this.getDefaultState().withProperty(FACING, Direction.byIndex(meta & 7));
  }

  @Nonnull
  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, FACING);
  }
  // =v= DROP MODIFICATIONS =v=

  public void dropBlockAsItemWithChance(World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, float chance, int fortune) {}

  public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
    TileEntity tile = worldIn.getTileEntity(pos);
    if (tile instanceof TileEntityCrate) {
      ((TileEntityCrate) tile).setCreativeBroken(player.capabilities.isCreativeMode);
    }
  }

  public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityCrate && !((TileEntityCrate) tileentity).isCreativeBroken()) {
      TileEntityCrate tileBarrel = (TileEntityCrate) tileentity;
      ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
      ICrate tileCap = tileBarrel.getCapability(BdsmCapabilies.CRATE_CAP, null);
      ICrate itemCap = stack.getCapability(BdsmCapabilies.CRATE_CAP, null);
      assert itemCap != null;
      itemCap.copyContainer(tileCap);
      spawnAsEntity(worldIn, pos, stack);
    }
    super.breakBlock(worldIn, pos, state);
  }

  @Nonnull
  @Override
  @SuppressWarnings("deprecation")
  public ItemStack getItem(World worldIn, BlockPos pos, @Nonnull BlockState state) {
    ItemStack stack = super.getItem(worldIn, pos, state);
    TileEntity tileBarrel = worldIn.getTileEntity(pos);
    if (tileBarrel instanceof TileEntityCrate) {
      ICrate tileCap = tileBarrel.getCapability(BdsmCapabilies.CRATE_CAP, null);
      ICrate itemCap = stack.getCapability(BdsmCapabilies.CRATE_CAP, null);
      assert itemCap != null;
      itemCap.copyContainer(tileCap);
    }
    return stack;
  }

  @Nonnull
  @Override
  @SuppressWarnings("deprecation")
  public BlockState withRotation(@Nonnull BlockState state, Rotation rot) {
    return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
  }

  @Nonnull
  @Override
  @SuppressWarnings("deprecation")
  public BlockState withMirror(@Nonnull BlockState state, Mirror mirrorIn) {
    return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
  }

  @Override
  public boolean rotateBlock(World world, @Nonnull BlockPos pos, @Nonnull Direction axis) {
    if (world.isRemote) return super.rotateBlock(world, pos, axis);
    TileEntity tile = world.getTileEntity(pos);
    boolean changed = super.rotateBlock(world, pos, axis);
    TileEntity nTile = world.getTileEntity(pos);
    if (changed && tile instanceof TileEntityCrate && nTile instanceof TileEntityCrate) {
      //noinspection ConstantConditions
      nTile.getCapability(BdsmCapabilies.CRATE_CAP, null).copyContainer(tile.getCapability(BdsmCapabilies.CRATE_CAP, null));
      ((TileEntityCrate) nTile).onCrateChanged();
    }
    return changed;
  }

  @Override
  @Deprecated
  @SuppressWarnings("deprecation")
  public boolean hasComparatorInputOverride(BlockState state) {
    return true;
  }

  @Override
  @Deprecated
  @SuppressWarnings("deprecation")
  public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
    TileEntity tileBarrel = worldIn.getTileEntity(pos);
    if (tileBarrel instanceof TileEntityCrate) {
      ICrate tileCap = tileBarrel.getCapability(BdsmCapabilies.CRATE_CAP, null);
      assert tileCap != null;
      long max = tileCap.getStackCap() < 0 ? (1 << 15) : tileCap.getStackCap();
      max *= tileCap.getRefItem().getMaxStackSize();
      double fill = tileCap.getCount() / (double) max;
      return MathHelper.floor(fill * 14D) + (tileCap.getCount() > 0 ? 1 : 0);
    }
    return 0;
  }

  @Override
  public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
    return true;
  }
}
