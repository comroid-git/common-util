package org.comroid.common.spellbind.factory;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.spellbind.model.Invocable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public final class OrderedParamInstanceFactory<T> extends ParamFactory.Abstract<Object[], T> {
    private final Invocable invocable;

    public OrderedParamInstanceFactory(Invocable invocable) {
        this.invocable = invocable;
    }

    public Class[] getTypeOrder() {
        return invocable.typeOrder();
    }

    @Override
    public T create(@Nullable Object[] parameter) {
        try {
            return (T) invocable.invokeAutoOrder(parameter);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
