package org.vaadin.artur.hillacursor;

import org.vaadin.artur.hillacursor.service.CursorTrackerService;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;

@JsModule("./send-cursor.ts")
@Tag("cursor-tracker")
public class CursorTracker extends Div {

    private CursorTrackerService cursorTrackerService;
    private String id;

    public CursorTracker(CursorTrackerService cursorTrackerService) {
        this.cursorTrackerService = cursorTrackerService;
        ComponentEffect.effect(this, () -> {
            removeAll();
            cursorTrackerService.getCursors().value().forEach(cursor -> {
                if (cursor.value().getId().equals(id)) {
                    // Don't add own cursor
                    return;
                }
                add(new CursorIndicator(cursor));
            });
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        id = cursorTrackerService.registerCursor();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        cursorTrackerService.unregisterCursor(id);
        id = null;
    }

    @ClientCallable
    public void updateCursor(int x, int y) {
        cursorTrackerService.updateCursor(id, x, y);
    }

}
