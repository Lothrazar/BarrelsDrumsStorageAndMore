//package funwayguy.bdsandm.client.renderer;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import javax.annotation.Nullable;
//import org.apache.commons.lang3.tuple.Pair;
//import net.minecraft.block.BlockState;
//import net.minecraft.client.renderer.model.BakedQuad;
//import net.minecraft.client.renderer.model.IBakedModel;
//import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
//import net.minecraft.client.renderer.model.ItemOverrideList;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.util.Direction;
//import net.minecraft.util.math.vector.Matrix4f;
//import net.minecraftforge.client.model.PerspectiveMapWrapper;
//import net.minecraftforge.client.model.obj.OBJModel;
//
//public class OBJBakedModelTinted implements IBakedModel {
//
//  private final OBJModel original;// was OBJBakedModel
//  private List<BakedQuad> replacedQuads;
//
//  public OBJBakedModelTinted(OBJModel obj) {
//    this.original = obj;
//  }
//
//  @Override
//  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
//    if (replacedQuads != null) {
//      return replacedQuads;
//    }
//    List<BakedQuad> quads = original.getQuads(state, side, rand);
//    replacedQuads = new ArrayList<>(quads.size());
//    for (BakedQuad bq : quads) {
//      replacedQuads.add(new BakedQuad(bq.getVertexData(), 0, bq.getFace(), bq.getSprite(), bq.shouldApplyDiffuseLighting(), bq.getFormat()));
//    }
//    return replacedQuads;
//  }
//
//  @Override
//  public boolean isAmbientOcclusion() {
//    return original.isAmbientOcclusion();
//  }
//
//  @Override
//  public boolean isGui3d() {
//    return original.isGui3d();
//  }
//
//  @Override
//  public boolean isBuiltInRenderer() {
//    return original.isBuiltInRenderer();
//  }
//
//  @Override
//  public TextureAtlasSprite getParticleTexture() {
//    return original.getParticleTexture();
//  }
//
//  @Override
//  public ItemOverrideList getOverrides() {
//    return original.getOverrides();
//  }
//
//  @Override
//  public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
//    return PerspectiveMapWrapper.handlePerspective(this, original.getState(), cameraTransformType);
//  }
//
//  @Override
//  public String toString() {
//    return original.toString();
//  }
//}
