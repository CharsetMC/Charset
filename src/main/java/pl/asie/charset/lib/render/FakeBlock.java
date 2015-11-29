package pl.asie.charset.lib.render;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FakeBlock extends Block {
	public static final FakeBlock INSTANCE = new FakeBlock();

	private int renderMask;
	private IIcon[] iconArray;
	private IIcon icon;

	protected FakeBlock() {
		super(Material.cactus);
		resetRenderMask();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
		return (renderMask & (1 << side)) != 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if (iconArray != null && iconArray.length > 0) {
			switch (iconArray.length) {
				case 1:
					return iconArray[0];
				case 2:
					return iconArray[side < 2 ? 0 : 1];
				case 3:
					return iconArray[side < 2 ? side : 2];
				case 4:
					return iconArray[side < 2 ? side : (side < 4 ? 2 : 3)];
				case 5:
					return iconArray[side < 2 ? 0 : (side - 1)];
				case 6:
					return iconArray[side];
				default:
					return iconArray[side % iconArray.length];
			}
		} else {
			return icon;
		}
	}

	public int getRenderMask() {
		return renderMask;
	}

	public void resetRenderMask() {
		this.renderMask = 0x3f;
	}

	public void setRenderMask(int renderMask) {
		this.renderMask = renderMask;
	}

	public IIcon[] getIconArray() {
		return iconArray;
	}

	public void setIconArray(IIcon[] iconArray) {
		this.icon = null;
		this.iconArray = iconArray;
	}

	public IIcon getIcon() {
		return icon;
	}

	public void setIcon(IIcon icon) {
		this.icon = icon;
		this.iconArray = null;
	}
}
