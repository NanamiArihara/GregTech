package gregtech.api.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

public class TankRenderer implements IIconRegister {

    private final String basePath;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sideSprite;

    public TankRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sideSprite = textureMap.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/" + basePath));
    }

    public void render(CCRenderState renderState, Matrix4 translation, int baseColor, IVertexOperation[] pipeline, int capacity, FluidStack fluidStack) {

        IVertexOperation[] basePipeline = ArrayUtils.add(pipeline, new ColourMultiplier(baseColor));
        Cuboid6 fluidCuboid = null;
        IVertexOperation[] fluidPipeline = null;
        TextureAtlasSprite fluidSprite = null;

        if(fluidStack != null) {
            double fluidLevel = fluidStack == null ? 0.0 : fluidStack.amount / (capacity * 1.0) * 0.99;
            fluidCuboid = new Cuboid6(0.01, 0.01, 0.01, 0.99, fluidLevel, 0.99);
            ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA(fluidStack.getFluid().getColor(fluidStack)));
            fluidPipeline = fluidStack.getFluid().getLuminosity(fluidStack) > 0 ?
                new IVertexOperation[] {multiplier} :
                ArrayUtils.add(pipeline, multiplier);
            fluidSprite = TextureUtils.getTexture(fluidStack.getFluid().getStill(fluidStack));
        }

        for(EnumFacing renderSide : EnumFacing.VALUES) {
            Textures.renderFace(renderState, translation, basePipeline, renderSide, Cuboid6.full, sideSprite);
            Textures.renderFace(renderState, translation.copy()
                .translate(renderSide.getFrontOffsetX(), renderSide.getFrontOffsetY(), renderSide.getFrontOffsetZ()),
                basePipeline, renderSide.getOpposite(), Cuboid6.full, sideSprite); //for rendering sides from inside too
            if(fluidStack != null) {
                if(fluidStack.getFluid().getLuminosity(fluidStack) > 0) {
                    renderState.brightness = 0xF000F0;
                }
                Textures.renderFace(renderState, translation, fluidPipeline, renderSide, fluidCuboid, fluidSprite);
            }
        }
    }

}
