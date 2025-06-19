package org.vaadin.artur.livecursor.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.vaadin.signals.ListSignal;
import com.vaadin.signals.ValueSignal;

@Service
public class CursorTrackerService {

    private static ListSignal<@NonNull Cursor> cursors = new ListSignal<>(Cursor.class);
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
        cursors.insertLast(cursor);

        System.out.println("Registered cursor: " + id);
        printCursors();
        return id;
    }

    private void printCursors() {
        System.out.println("Current cursors:");
        cursors.value().forEach(cursor -> {
            System.out.println(" - " + cursor.value().getId() + " at (" + cursor.value().getX() + ", "
                    + cursor.value().getY() + ") with color " + cursor.value().getColor());
        });
        System.out.println();
    }

    public ListSignal<Cursor> getCursors() {
        return cursors;
    }

    public void unregisterCursor(String id) {
        System.out.println("Unregistering cursor: " + id);
        findCursor(id).ifPresentOrElse(cursor -> {
            cursors.remove(cursor);
            System.out.println("Unregistered cursor: " + id);
        }, () -> {
            System.out.println("Cursor not found: " + id);
        });
    }

    private Optional<ValueSignal<Cursor>> findCursor(String id) {
        return cursors.value().stream()
                .filter(cursor -> id.equals(cursor.value().getId()))
                .findFirst();
    }

    public void updateCursor(String id, int x, int y) {
        findCursor(id).ifPresentOrElse(cursorSignal -> cursorSignal.update(cursor -> {
            cursor.setX(x);
            cursor.setY(y);
            cursor.setTimestamp(Instant.now().toEpochMilli());
            System.out.println("Updated cursor " + id + " to " + x + ", " + y);
            return cursor;
        }), () -> {
            System.out.println("Cursor not found for update: " + id);
        });
    }

    public void updateName(String id, String name) {
        findCursor(id).ifPresentOrElse(cursorSignal -> cursorSignal.update(cursor -> {
            cursor.setName(name);
            cursor.setTimestamp(Instant.now().toEpochMilli());
            System.out.println("Updated cursor " + id + " name to " + name);
            return cursor;
        }), () -> {
            System.out.println("Cursor not found for name update: " + id);
        });
    }

}
