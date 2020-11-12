package funwayguy.bdsandm.core;

import java.util.ArrayList;
import java.util.List;
import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.blocks.tiles.TileEntityCrate;
import funwayguy.bdsandm.blocks.tiles.TileEntityShipping;
import funwayguy.bdsandm.client.color.BlockContainerColor;
import funwayguy.bdsandm.client.obj.OBJLoaderColored;
import funwayguy.bdsandm.client.renderer.TileEntityRenderBarrel;
import funwayguy.bdsandm.client.renderer.TileEntityRenderCrate;
import funwayguy.bdsandm.items.ItemBarrel;
import funwayguy.bdsandm.items.ItemCrate;
import funwayguy.bdsandm.items.ItemShipping;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class RegEventHandler {

  private static final List<Item> All_ITEMS = new ArrayList<>();
  private static final List<Block> ALL_BLOCKS = new ArrayList<>();

  @SubscribeEvent
  public static void registerBlockEvent(RegistryEvent.Register<Block> event) {
    event.getRegistry().registerAll(ALL_BLOCKS.toArray(new Block[0]));
  }

  @SubscribeEvent
  public static void registerItemEvent(RegistryEvent.Register<Item> event) {
    event.getRegistry().registerAll(All_ITEMS.toArray(new Item[0]));
  }

  public static void initContent() {
    regItem(BDSM.itemUpgrade, "upgrade");
    regItem(BDSM.itemKey, "crate_key");
    regItem(BDSM.itemColor, "color_tool");
    regBlock(BDSM.blockWoodCrate, new ItemCrate(BDSM.blockWoodCrate, 64, 1024), "wood_crate");
    regBlock(BDSM.blockWoodBarrel, new ItemBarrel(BDSM.blockWoodBarrel, 64, 1024), "wood_barrel");
    regBlock(BDSM.blockMetalCrate, new ItemCrate(BDSM.blockMetalCrate, 64, 1 << 15), "metal_crate");
    regBlock(BDSM.blockMetalBarrel, new ItemBarrel(BDSM.blockMetalBarrel, 64, 1 << 15), "metal_barrel");
    regBlock(BDSM.blockShippingContainer, new ItemShipping(BDSM.blockShippingContainer), "shipping_container");
    GameRegistry.registerTileEntity(TileEntityCrate.class, new ResourceLocation(BDSM.MOD_ID, "crate"));
    GameRegistry.registerTileEntity(TileEntityBarrel.class, new ResourceLocation(BDSM.MOD_ID, "barrel"));
    GameRegistry.registerTileEntity(TileEntityShipping.class, new ResourceLocation(BDSM.MOD_ID, "shipping"));
    Blocks.FIRE.setFireInfo(BDSM.blockWoodCrate, 5, 20);
    Blocks.FIRE.setFireInfo(BDSM.blockWoodBarrel, 5, 20);
  }
  //
  //  private static void regBlock(Block block, String name) {
  //    regBlock(block, new BlockItem(block), name);
  //  }
  //
  //  private static void regBlock(Block block, ItemBlock item, String name) {
  //    ResourceLocation res = new ResourceLocation(BDSM.MOD_ID, name);
  //    ALL_BLOCKS.add(block.setRegistryName(res));
  //    All_ITEMS.add(item.setRegistryName(res));
  //  }
  //
  //  private static void regItem(Item item, String name) {
  //    All_ITEMS.add(item.setRegistryName(new ResourceLocation(BDSM.MOD_ID, name)));
  //  }

  @SubscribeEvent
  @OnlyIn(Dist.CLIENT)
  public static void initBlockColors(ColorHandlerEvent.Block event) {
    event.getBlockColors().register(BlockContainerColor.INSTANCE, BDSM.blockMetalBarrel, BDSM.blockMetalCrate, BDSM.blockShippingContainer);
  }

  @SubscribeEvent
  @OnlyIn(Dist.CLIENT)
  public static void initItemColors(ColorHandlerEvent.Item event) {
    event.getItemColors().register(BlockContainerColor.INSTANCE, BDSM.blockMetalBarrel, BDSM.blockMetalCrate, BDSM.blockShippingContainer);
  }

  @SubscribeEvent
  @OnlyIn(Dist.CLIENT)
  public static void registerModelEvent(ModelRegistryEvent event) {
    OBJLoaderColored.INSTANCE.addDomain(BDSM.MOD_ID);
    //OBJLoader.INSTANCE.addDomain(BDSM.MOD_ID);
    registerItemModel(BDSM.itemUpgrade, 0, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_64", "inventory"));
    registerItemModel(BDSM.itemUpgrade, 1, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_256", "inventory"));
    registerItemModel(BDSM.itemUpgrade, 2, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_1024", "inventory"));
    registerItemModel(BDSM.itemUpgrade, 3, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_4096", "inventory"));
    registerItemModel(BDSM.itemUpgrade, 4, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_creative", "inventory"));
    registerItemModel(BDSM.itemUpgrade, 5, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_ore", "inventory"));
    registerItemModel(BDSM.itemUpgrade, 6, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_void", "inventory"));
    registerItemModel(BDSM.itemUpgrade, 7, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_uninstall", "inventory"));
    registerItemModel(BDSM.itemKey);
    registerItemModel(BDSM.itemColor);
    registerBlockModel(BDSM.blockWoodCrate);
    registerBlockModel(BDSM.blockWoodBarrel);
    registerBlockModel(BDSM.blockMetalCrate);
    registerBlockModel(BDSM.blockMetalBarrel);
    registerBlockModel(BDSM.blockShippingContainer);
    ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrate.class, new TileEntityRenderCrate());
    ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new TileEntityRenderBarrel());
  }
  //  @SideOnly(Side.CLIENT)
  //  private static void registerBlockModel(Block block) {
  //    registerItemModel(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName().toString(), "normal"));
  //    registerItemModel(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName().toString(), "inventory"));
  //  }

  @OnlyIn(Dist.CLIENT)
  private static void registerBlockModel(Block block, int meta, ModelResourceLocation model) {
    registerItemModel(Item.getItemFromBlock(block), meta, model);
  }

  @OnlyIn(Dist.CLIENT)
  private static void registerItemModel(Item item) {
    registerItemModel(item, 0, new ModelResourceLocation(item.getRegistryName().toString(), "inventory"));
  }
  //  @OnlyIn(Dist.CLIENT)
  //  private static void registerItemModel(Item item, int meta, ModelResourceLocation model) {
  //    if (!model.getPath().equalsIgnoreCase(item.getRegistryName().getPath())) {
  //      ModelBakery.registerItemVariants(item, model);
  //    }
  //    ModelLoader.setCustomModelResourceLocation(item, meta, model);
  //  }

  @OnlyIn(Dist.CLIENT)
  public static void registerBlockColors(IBlockColor blockColor, IItemColor itemColor, Block... blocks) {
    Minecraft.getInstance().getBlockColors().register(blockColor, blocks);
    Minecraft.getInstance().getItemColors().register(itemColor, blocks);
  }

  @OnlyIn(Dist.CLIENT)
  public static void registerItemColors(IItemColor itemColor, Item... items) {
    Minecraft.getInstance().getItemColors().register(itemColor, items);
  }
}
