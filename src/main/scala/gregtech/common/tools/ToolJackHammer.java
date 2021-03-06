package gregtech.common.tools;

import gregtech.api.recipes.RecipeMaps;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ToolJackHammer extends ToolDrillLV {

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 4;
    }

    @Override
    public int getToolDamagePerDropConversion(ItemStack stack) {
        return 4;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 32;
    }

    @Override
    public int getToolDamagePerEntityAttack(ItemStack stack) {
        return 8;
    }

    @Override
    public int getBaseQuality(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 3.0F;
    }

    @Override
    public float getDigSpeedMultiplier(ItemStack stack) {
        return 1.0F;
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 2.0F;
    }

    @Override
    public boolean isMinableBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        ItemStack itemStack = new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block));
        return (tool != null && (tool.equals("hammer") || tool.equals("pickaxe"))) ||
            block.getMaterial() == Material.ROCK ||
            block.getMaterial() == Material.GLASS ||
            block.getMaterial() == Material.ICE ||
            block.getMaterial() == Material.PACKED_ICE ||
            RecipeMaps.FORGE_HAMMER_RECIPES.findRecipe(Long.MAX_VALUE,
                Collections.singletonList(itemStack), Collections.emptyList()) != null;
    }

    @Override
    public boolean allowRecursiveConversion() {
        return true;
    }

    @Override
    public int convertBlockDrops(World world, BlockPos centerPos, IBlockState blockState, EntityPlayer harvester, List<ItemStack> drops, boolean recursive) {
        int conversionsApplied = ToolUtility.applyHammerDrops(world.rand, blockState, drops);
        if (recursive)
            //on recursive calls, do not try to break multiple blocks
            return conversionsApplied;

        EnumFacing sideHit = ToolUtility.getSideHit(world, centerPos, harvester);
        ItemStack selfStack = harvester.getHeldItem(EnumHand.MAIN_HAND);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                //do not check center block - it's handled now
                if (x == 0 && y == 0) continue;
                BlockPos block = rotate(centerPos, x, y, sideHit);
                if (!isMinableBlock(world.getBlockState(block), selfStack) ||
                    !world.canMineBlockBody(harvester, block) ||
                    !((EntityPlayerMP) harvester).interactionManager.tryHarvestBlock(block))
                    continue;
                conversionsApplied++;
            }
        }
        return conversionsApplied;
    }

    private static BlockPos rotate(BlockPos origin, int x, int y, EnumFacing sideHit) {
        switch (sideHit.getAxis()) {
            case X: return origin.add(0, y, x);
            case Z: return origin.add(x, y, 0);
            case Y: return origin.add(x, 0, y);
            default: throw new IllegalArgumentException("Unknown axis");
        }
    }
}
