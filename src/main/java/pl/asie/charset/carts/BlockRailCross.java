package pl.asie.charset.carts;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.ModCharsetLib;

public class BlockRailCross extends BlockRailBase {
	public static final IProperty<EnumRailDirection> DIRECTION = PropertyEnum.create(
			"direction", EnumRailDirection.class,
			EnumRailDirection.NORTH_SOUTH);

	protected BlockRailCross() {
		super(false);
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		setUnlocalizedName("charset.rail_cross");
	}

	@Override
	public boolean isFlexibleRail(IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean canMakeSlopes(IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public EnumRailDirection getRailDirection(IBlockAccess world, BlockPos pos, IBlockState state, @javax.annotation.Nullable net.minecraft.entity.item.EntityMinecart cart) {
		if (cart != null) {
			float cartYaw = cart.rotationYaw % 180;
			while (cartYaw < 0) cartYaw += 180;
			System.out.println(cartYaw);
			if (cartYaw < 45 || cartYaw > 135)
				return EnumRailDirection.EAST_WEST;
			else
				return EnumRailDirection.NORTH_SOUTH;
		} else {
			return EnumRailDirection.EAST_WEST;
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, DIRECTION);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public IProperty<EnumRailDirection> getShapeProperty() {
		return DIRECTION;
	}
}
