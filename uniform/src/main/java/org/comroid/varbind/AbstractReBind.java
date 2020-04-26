package org.comroid.varbind;

import org.comroid.common.Polyfill;

public abstract class AbstractReBind<EXTR, DPND, REMAP> implements ReBind<EXTR, DPND, REMAP> {
    private final VarBind<?, DPND, ?, EXTR> underlying;
    private final GroupBind                 group;

    public AbstractReBind(VarBind<?, DPND, ?, EXTR> underlying, GroupBind group) {
        this.underlying = underlying;
        this.group      = group;

        group.children.add(Polyfill.uncheckedCast(this));
    }

    @Override
    public VarBind<?, DPND, ?, EXTR> getUnderlying() {
        return underlying;
    }

    @Override
    public GroupBind getGroup() {
        return group;
    }
}
