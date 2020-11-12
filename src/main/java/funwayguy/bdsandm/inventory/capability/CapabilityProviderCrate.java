package funwayguy.bdsandm.inventory.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class CapabilityProviderCrate implements ICapabilityProvider, ICapabilitySerializable<CompoundNBT> {

  private final ICrate crate;
  private ItemStack stack;

  public CapabilityProviderCrate(int initCap, int maxCap) {
    crate = new CapabilityCrate(initCap, maxCap);
  }

  public CapabilityProviderCrate setParentStack(ItemStack stack) {
    this.stack = stack;
    final ItemStack finStack = stack;
    crate.setCallback(() -> {
      CompoundNBT sTag = stack.getTag();
      if (sTag == null) {
        sTag = new CompoundNBT();
        finStack.setTag(sTag);
      }
      sTag.put("crateCap", crate.serializeNBT()); // Purely for display purposes client side. Not to be trusted as accurate
    });
    return this;
  }

  @Nullable
  @Override
  @Nonnull
  public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> capability, final @Nullable Direction side) {
    if (capability == BdsmCapabilies.CRATE_CAP) {
      LazyOptional<Capability<ICrate>> test = LazyOptional.of(() -> BdsmCapabilies.CRATE_CAP);
      return test.cast();
      //      return BdsmCapabilies.CRATE_CAP.cast(crate);
    }
    else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      LazyOptional<ICrate> testt = LazyOptional.of(() -> crate);
      return testt.cast();
      //      return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(crate);
    }
    return null;
  }

  @Override
  public CompoundNBT serializeNBT() {
    CompoundNBT tag = crate.serializeNBT();
    if (stack != null) stack.setTagInfo("crateCap", tag.copy()); // Purely for display purposes client side. Not to be trusted as accurate
    return tag;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    crate.deserializeNBT(nbt);
    if (stack != null) stack.setTagInfo("crateCap", nbt.copy()); // Purely for display purposes client side. Not to be trusted as accurate
  }
}
