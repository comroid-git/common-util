package org.comroid.listnr.model;

import org.comroid.spellbind.model.TypeFragment;
import org.jetbrains.annotations.Nullable;

public interface EventPayload<D, ET extends EventType<?, D, ? extends EventPayload<D, ? super ET>>>
        extends TypeFragment {
    ET getMasterEventType();

    class Basic<D, ET extends EventType<?, D, ? extends EventPayload<D, ? super ET>>>
            implements EventPayload<D, ET> {
        private final ET masterEventType;

        @Override
        public ET getMasterEventType() {
            return masterEventType;
        }

        public Basic(ET masterEventType) {
            this.masterEventType = masterEventType;
        }
    }
}
