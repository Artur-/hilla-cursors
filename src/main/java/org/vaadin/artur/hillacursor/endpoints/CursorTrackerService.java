package org.vaadin.artur.hillacursor.endpoints;

import org.jspecify.annotations.NonNull;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.signals.ListSignal;

@AnonymousAllowed
@Endpoint
public class CursorTrackerService {

    private static ListSignal<@NonNull Cursor> cursors = new ListSignal<>(Cursor.class);

    @NonNull
    public ListSignal<@NonNull Cursor> subscribe() {
        return cursors;
    }

}
