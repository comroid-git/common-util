package org.comroid.varbind.model;

import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.ReBind;
import org.comroid.varbind.bind.VarBind;

public abstract class AbstractReBind<EXTR, DPND, REMAP> implements ReBind<EXTR, DPND, REMAP> {
    private final VarBind<?, DPND, ?, EXTR> underlying;
    private final GroupBind<?, DPND> group;

    @Override
    public VarBind<?, DPND, ?, EXTR> getUnderlying() {
        return underlying;
    }

    @Override
    public GroupBind<?, DPND> getGroup() {
        return group;
    }

    public AbstractReBind(VarBind<?, DPND, ?, EXTR> underlying, GroupBind<?, DPND> group) {
        this.underlying = underlying;
        this.group = group;

        group.addChild(this);
    }
}
