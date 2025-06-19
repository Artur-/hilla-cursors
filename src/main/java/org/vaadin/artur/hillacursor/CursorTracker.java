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
    private MyCursor myCursor;

    public CursorTracker(CursorTrackerService cursorTrackerService, MyCursor myCursor) {
        this.cursorTrackerService = cursorTrackerService;
        this.myCursor = myCursor;
        ComponentEffect.effect(this, () -> {
            removeAll();
            cursorTrackerService.getCursors().value().forEach(cursor -> {
                if (cursor.value().getId().equals(myCursor.getId())) {
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
        myCursor.setId(cursorTrackerService.registerCursor());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        cursorTrackerService.unregisterCursor(myCursor.getId());
        myCursor.setId(null);
    }

    @ClientCallable
    public void updateCursor(int x, int y) {
        cursorTrackerService.updateCursor(myCursor.getId(), x, y);
    }
}
