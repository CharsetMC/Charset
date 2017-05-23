package pl.asie.charset.module.transport.rails;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.ModCharset;

public class BlockRailCharset extends BlockRailBase {
	// hacks! <3
	public static final IProperty<EnumRailDirection> DIRECTION = PropertyEnum.create(
			"direction", EnumRailDirection.class,
			EnumRailDirection.NORTH_SOUTH, EnumRailDirection.EAST_WEST,
			EnumRailDirection.NORTH_EAST, EnumRailDirection.NORTH_WEST,
			EnumRailDirection.SOUTH_EAST, EnumRailDirection.SOUTH_WEST);

	protected BlockRailCharset() {
		super(false);
		setCreativeTab(ModCharset.CREATIVE_TAB);
		setHardness(0.7F);
		setSoundType(SoundType.METAL);
		setUnlocalizedName("charset.rail_charset");
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.charset.rail_cross";
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
			EnumRailDirection value;

			if (cartYaw < 45 || cartYaw > 135)
				value = EnumRailDirection.EAST_WEST;
			else
				value = EnumRailDirection.NORTH_SOUTH;

			if (value != state.getValue(DIRECTION) && world instanceof World)
				((World) world).setBlockState(pos, state.withProperty(DIRECTION, value), 6);
			return value;
		} else {
			return state.getValue(DIRECTION);
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
