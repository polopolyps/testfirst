package com.polopoly.ps.test;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;

public class NoEmptyReferencesEventHandler implements ReferenceInsertionEventHandler {

    public Object referenceInsert(String reference, Object value) {
        if (value == null && !reference.startsWith("$!")) {
            throw new AssertionError("The reference " + reference + " evaluated to null.");
        }

        return value;
    }

}
