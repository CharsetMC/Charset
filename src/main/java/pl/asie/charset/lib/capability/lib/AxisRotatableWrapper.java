package pl.asie.charset.lib.capability.lib;

import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.lib.IAxisRotatable;

import java.util.List;
import java.util.function.Function;

public class AxisRotatableWrapper implements Function<List<IAxisRotatable>, IAxisRotatable> {
    @Override
    public IAxisRotatable apply(List<IAxisRotatable> iAxisRotatableList) {
        return new IAxisRotatable() {
            @Override
            public boolean rotateAround(EnumFacing axis, boolean simulate) {
                for (IAxisRotatable rotatable : iAxisRotatableList) {
                    if (!rotatable.rotateAround(axis, true)) {
                        return false;
                    }
                }

                if (!simulate) {
                    for (IAxisRotatable rotatable : iAxisRotatableList) {
                        rotatable.rotateAround(axis, false);
                    }
                }

                return true;
            }
        };
    }
}
