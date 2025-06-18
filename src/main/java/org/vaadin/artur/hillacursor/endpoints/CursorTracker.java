package org.vaadin.artur.hillacursor.endpoints;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.EndpointSubscription;

@AnonymousAllowed
@Endpoint
public class CursorTracker {

    private Map<UUID, Cursor> cursors = new ConcurrentHashMap<>();
    private Many<Cursor> updates;
    private ConnectableFlux<Cursor> updateFlux;

    private static final String[] colors = new String[] { "red", "green", "blue", "brown", "magenta", "mediumvioletred",
            "orange" };

    public CursorTracker() {
        updates = Sinks.many().multicast().directBestEffort();
        updateFlux = updates.asFlux().replay(Duration.ofSeconds(5));
        updateFlux.connect();
    }

    @NonNull
    public UUID join(@NonNull String name) {
        UUID id = UUID.randomUUID();

        Cursor cursor = new Cursor();
        cursor.setName(name);
        cursor.setColor(colors[id.toString().charAt(0) % colors.length]);
        cursor.setId(id);
        cursors.put(id, cursor);
        getLogger().info("Registered new cursor " + cursor);
        logCursors();
        return id;
    }

    public void updateName(@NonNull UUID ownerId, @NonNull String name) {
        long timestamp = System.currentTimeMillis();

        Cursor cursor = cursors.get(ownerId);
        cursor.setTimestamp(timestamp);
        cursor.setName(name);

        updates.emitNext(cursor, (signalType, emitResult) -> emitResult == EmitResult.FAIL_NON_SERIALIZED);

    }

    private void logCursors() {
        getLogger().info("Cursors are now:"
                + cursors.values().stream().map(cursor -> "\n" + cursor).collect(Collectors.joining()));

    }

    @NonNull
    public List<@NonNull Cursor> getCursors(@NonNull UUID ownerId) {
        return cursors.entrySet().stream().filter(entry -> !entry.getKey().equals(ownerId))
                .map(entry -> entry.getValue()).toList();
    }

    @NonNull
    public EndpointSubscription<@NonNull Cursor> subscribe(@NonNull UUID owner, long lastSeen) {
        Flux<Cursor> flux = updateFlux
                .filter(cursor -> !cursor.getId().equals(owner))
                .filter(position -> position.getTimestamp() > lastSeen);

        return EndpointSubscription.of(flux, () -> {
            cursors.remove(owner);

            Cursor cursor = new Cursor();
            cursor.setId(owner);
            cursor.setColor("DELETE");
            cursor.setTimestamp(System.currentTimeMillis());
            updates.emitNext(cursor, (signalType, emitResult) -> emitResult == EmitResult.FAIL_NON_SERIALIZED);
            getLogger().info("Removed cursor " + owner);
            logCursors();
        });
    }

    public void trackCursor(UUID id, int x, int y) {
        // getLogger().info("trackCursor({}, {}, {})", id.toString(), x, y);
        long timestamp = System.currentTimeMillis();

        Cursor cursor = cursors.get(id);
        cursor.setId(id);
        cursor.setX(x);
        cursor.setY(y);
        cursor.setTimestamp(timestamp);

        updates.emitNext(cursor, (signalType, emitResult) -> emitResult == EmitResult.FAIL_NON_SERIALIZED);
        // getLogger().info("Updated cursor with id " + id);
        // logCursors();

    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
}
