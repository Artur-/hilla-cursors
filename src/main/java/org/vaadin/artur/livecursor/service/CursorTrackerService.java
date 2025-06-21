package org.vaadin.artur.livecursor.service;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.vaadin.signals.MapSignal;
import com.vaadin.signals.ValueSignal;

@Service
public class CursorTrackerService {

    private static MapSignal<@NonNull Cursor> cursors = new MapSignal<>(Cursor.class);
    private static String[] colors = new String[] { "red", "green", "blue", "brown", "magenta", "mediumvioletred",
            "orange" };

    public String registerCursor() {
        String id = UUID.randomUUID().toString();
        String color = colors[id.charAt(0) % colors.length];

        Cursor cursor = new Cursor();
        cursor.setId(id);
        cursor.setColor(color);
        cursor.setName(id);
        cursor.setX(0);
        cursor.setY(0);
        cursor.setTimestamp(Instant.now().toEpochMilli());
        cursors.put(cursor.getId(), cursor);

        System.out.println("Registered cursor: " + id);
        printCursors();
        return id;
    }

    private void printCursors() {
        System.out.println("Current cursors:");
        cursors.value().forEach((id, cursor) -> {
            System.out.println(" - " + cursor.value().getId() + " at (" + cursor.value().getX() + ", "
                    + cursor.value().getY() + ") with color " + cursor.value().getColor());
        });
        System.out.println();
    }

    public MapSignal<@NonNull Cursor> getCursors() {
        return cursors.asReadonly();
    }

    public void unregisterCursor(String id) {
        System.out.println("Unregistering cursor: " + id);
        cursors.remove(id);
        System.out.println("Unregistered cursor: " + id);
    }

    public void updateCursor(String id, int x, int y) {
        ValueSignal<Cursor> cursorSignal = cursors.value().get(id);
        if (cursorSignal != null) {
            cursorSignal.update(cursor -> {
                cursor.setX(x);
                cursor.setY(y);
                cursor.setTimestamp(Instant.now().toEpochMilli());
                System.out.println("Updated cursor " + id + " to " + x + ", " + y);
                return cursor;
            });
        }
    }

    public void updateName(String id, String name) {
        ValueSignal<Cursor> cursorSignal = cursors.value().get(id);
        if (cursorSignal != null) {
            cursorSignal.update(cursor -> {
                cursor.setName(name);
                cursor.setTimestamp(Instant.now().toEpochMilli());
                System.out.println("Updated cursor " + id + " name to " + name);
                return cursor;
            });
        } else {
            System.out.println("Cursor not found for name update: " + id);
        }
    }

}
