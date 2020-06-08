package org.comroid.listnr.model;

import org.comroid.api.SelfDeclared;
import org.comroid.spellbind.model.TypeFragment;

public interface EventPayload<D,
        ET extends EventType<?, D, ET, EP>,
        EP extends EventPayload<D, ET, EP>>
        extends TypeFragment, SelfDeclared<EP> {
    ET getMasterEventType();

    class Basic<D,
            ET extends EventType<?, D, ET, EP>,
            EP extends EventPayload<D, ET, EP>>
            implements EventPayload<D, ET, EP> {
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
