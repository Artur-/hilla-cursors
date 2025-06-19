package org.vaadin.artur.livecursor;

import org.vaadin.artur.livecursor.service.Cursor;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.signals.ValueSignal;

@JsModule("./CursorIndicator.tsx")
@Tag("cursor-indicator")
public class CursorIndicator extends ReactAdapterComponent {

    public CursorIndicator(ValueSignal<Cursor> cursor) {
        setState("cursor", cursor.value());
    }

}
