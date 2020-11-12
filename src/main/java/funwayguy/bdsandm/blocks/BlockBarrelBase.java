package funwayguy.bdsandm.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;
import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.core.BdsmConfig;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import funwayguy.bdsandm.network.PacketBdsm;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockBarrelBase extends DirectionalBlock implements ITileEntityProvider, IStorageBlock {

  private final int initCap;
  private final int maxCap;

  @SuppressWarnings("WeakerAccess")
  protected BlockBarrelBase(Material materialIn, int initCap, int maxCap) {
    super(materialIn);
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
    if (!(tile instanceof TileEntityBarrel)) return;
    TileEntityBarrel tileBarrel = (TileEntityBarrel) tile;
    CapabilityBarrel barrel = (CapabilityBarrel) tile.getCapability(BdsmCapabilies.BARREL_CAP, null);
    if (barrel == null || (!isHit && barrel.installUpgrade(player, player.getHeldItem(hand)))) return;
    if (!isHit) {
      if (!player.isSneaking()) {
        IFluidHandlerItem container = player.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (barrel.getRefFluid() != null && container != null && container.drain(Integer.MAX_VALUE, false) == null) {
          withdrawItem(barrel, player, 0);
        }
        else {
          depositItem(barrel, player, hand);
        }
      }
    }
    else {
      if (altMode) {
        withdrawItem(barrel, player, player.isSneaking() ? 0 : 2);
      }
      else if (!player.isSneaking()) {
        int curClick = tileBarrel.getClickCount(world.getGameTime(), clickDelay);
        if (curClick >= 0) withdrawItem(barrel, player, curClick);
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

  private void depositItem(CapabilityBarrel barrel, PlayerEntity player, Hand hand) {
    ItemStack refItem = barrel.getRefItem();
    FluidStack refFluid = barrel.getRefFluid();
    ItemStack held = player.getHeldItem(hand);
    IFluidHandlerItem container = held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    int maxDrain = barrel.getStackCap() < 0 ? Integer.MAX_VALUE : (barrel.getStackCap() * 1000 - (refFluid == null ? 0 : barrel.getCount()));
    if (container != null && refItem.isEmpty() && !held.isEmpty()) // Fill fluid
    {
      FluidStack drainStack;
      if (refFluid == null) {
        drainStack = container.drain(maxDrain / held.getCount(), false);
      }
      else {
        drainStack = refFluid.copy();
        drainStack.amount = maxDrain / held.getCount();
        drainStack = container.drain(drainStack, false);
      }
      if (drainStack != null && drainStack.amount > 0) {
        drainStack.amount *= held.getCount();
        drainStack.amount = barrel.fill(drainStack, true);
        if (!player.capabilities.isCreativeMode && drainStack.amount > 0) {
          drainStack.amount /= held.getCount();
          container.drain(drainStack, true);
          player.setHeldItem(hand, container.getContainer());
        }
        return;
      }
    }
    if (BdsmConfig.multiPurposeBarrel) {
      if (!held.isEmpty() && (refItem.isEmpty() || barrel.canMergeWith(held) || held.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))) {
        if (held.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
          IItemHandler heldCrate = held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
          assert heldCrate != null;
          for (int s = 0; s < heldCrate.getSlots(); s++) {
            ItemStack transfer = heldCrate.extractItem(s, Integer.MAX_VALUE, true);
            ItemStack transStack = transfer.copy();
            transStack.setCount(transStack.getCount() * held.getCount());
            int prev = transStack.getCount();
            if (prev > 0 && (refItem.isEmpty() || barrel.canMergeWith(transStack))) {
              transStack = barrel.insertItem(barrel.getSlots() - 1, transStack, false);
              if (transStack.getCount() != prev) heldCrate.extractItem(s, (prev - transStack.getCount()) / held.getCount(), false);
            }
          }
        }
        else {
          player.setHeldItem(hand, barrel.insertItem(barrel.getSlots() - 1, held, false));
        }
      }
      else if (!refItem.isEmpty()) // Insert all
      {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
          ItemStack invoStack = player.inventory.getStackInSlot(i);
          if (barrel.canMergeWith(invoStack)) {
            invoStack = barrel.insertItem(barrel.getSlots() - 1, invoStack, false);
            boolean done = !invoStack.isEmpty();
            player.inventory.setInventorySlotContents(i, invoStack);
            if (done) {
              break;
            }
          }
        }
      }
    }
  }

  private void withdrawItem(CapabilityBarrel barrel, PlayerEntity player, int clickCount) {
    ItemStack ref = barrel.getRefItem();
    FluidStack refFluid = barrel.getRefFluid();
    ItemStack held = player.getHeldItem(Hand.MAIN_HAND);
    IFluidHandlerItem container = held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    int maxFill = barrel.getStackCap() < 0 ? Integer.MAX_VALUE : barrel.getCount();
    if (clickCount <= 0) maxFill = Math.min(1000, maxFill);
    if (container != null && refFluid != null && !held.isEmpty() && barrel.getCount() >= held.getCount()) {
      FluidStack fillStack = refFluid.copy();
      fillStack.amount = maxFill / held.getCount();
      int testFill = container.fill(fillStack, false); // Doesn't really matter if we overfill here. Just checking capacity and fluid match
      if (testFill > 0) {
        fillStack.amount = testFill * held.getCount();
        FluidStack drained = barrel.drain(fillStack, true);
        if (drained != null) {
          drained.amount /= held.getCount();
          container.fill(drained, true);
          player.setHeldItem(Hand.MAIN_HAND, container.getContainer());
        }
      }
    }
    else if (!ref.isEmpty()) {
      int count = clickCount <= 0 ? 1 : ref.getMaxStackSize();
      if (clickCount == 1) count--;
      ItemStack out = barrel.extractItem(0, count, false);
      if (player.getHeldItem(Hand.MAIN_HAND).isEmpty()) {
        player.setHeldItem(Hand.MAIN_HAND, out);
      }
      else if (!player.addItemStackToInventory(out)) player.dropItem(out, false, false);
    }
  }

  @Nonnull
  @Override
  @SuppressWarnings("deprecation")
  public EnumBlockRenderType getRenderType(BlockState state) {
    return EnumBlockRenderType.MODEL;
  }

  @Nonnull
  @Override
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isFullCube(BlockState state) {
    return false;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isOpaqueCube(BlockState state) {
    return false;
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
    return new TileEntityBarrel(initCap, maxCap);
  }

  @Nonnull
  @Override
  public BlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Direction facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, Hand hand) {
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

  @Override
  public void dropBlockAsItemWithChance(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, float chance, int fortune) {}

  @Override
  public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
    TileEntity tile = worldIn.getTileEntity(pos);
    if (tile instanceof TileEntityBarrel) {
      ((TileEntityBarrel) tile).setCreativeBroken(player.capabilities.isCreativeMode);
    }
  }

  public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityBarrel && !((TileEntityBarrel) tileentity).isCreativeBroken()) {
      TileEntityBarrel tileBarrel = (TileEntityBarrel) tileentity;
      ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
      IBarrel tileCap = tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null);
      IBarrel itemCap = stack.getCapability(BdsmCapabilies.BARREL_CAP, null);
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
    if (tileBarrel instanceof TileEntityBarrel) {
      IBarrel tileCap = tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null);
      IBarrel itemCap = stack.getCapability(BdsmCapabilies.BARREL_CAP, null);
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
    if (tileBarrel instanceof TileEntityBarrel) {
      CapabilityBarrel tileCap = (CapabilityBarrel) tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null);
      assert tileCap != null;
      long max = tileCap.getStackCap() < 0 ? (1 << 15) : tileCap.getStackCap();
      max *= tileCap.getRefFluid() != null ? 1000L : tileCap.getRefItem().getMaxStackSize();
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
