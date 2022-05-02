package org.vaadin.artur.hillacursor.endpoints;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;
import reactor.core.scheduler.Scheduler;

@AnonymousAllowed
@Endpoint
public class CursorTracker {

    private Map<UUID, Cursor> cursors = new ConcurrentHashMap<>();
    private Many<Cursor> updates;
    private ConnectableFlux<Cursor> updateFlux;

    private static final String[] colors = new String[] { "red", "green", "blue", "brown", "magenta", "mediumvioletred",
            "orange" };

    @Autowired
    public CursorTracker() {
        updates = Sinks.many().multicast().directBestEffort();
        updateFlux = updates.asFlux().replay(Duration.ofSeconds(5));
        updateFlux.connect();
    }

    @Nonnull
    public UUID join(@Nonnull String name) {
        UUID id = UUID.randomUUID();

        Cursor c = new Cursor();
        c.setName(name);
        c.setColor(colors[id.toString().charAt(0) % colors.length]);
        c.setId(id);
        cursors.put(id, c);

        return id;
    }

    @Nonnull
    public List<@Nonnull Cursor> getCursors(@Nonnull UUID ownerId) {
        System.out.println("Sending " + cursors.values());
        return cursors.entrySet().stream().filter(entry -> !entry.getKey().equals(ownerId))
                .map(entry -> entry.getValue()).toList();
    }

    @Nonnull
    public Flux<@Nonnull Cursor> subscribe(@Nonnull UUID owner, long lastSeen) {
        getLogger().info("subscribe({}, {})", owner.toString(), lastSeen);
        Flux<Cursor> flux = updateFlux
                .filter(cursor -> !cursor.getId().equals(owner))
                .filter(position -> position.getTimestamp() > lastSeen);

        return flux;
    }

    public void trackCursor(UUID id, int x, int y) {
        getLogger().info("trackCursor({}, {}, {})", id.toString(), x, y);
        long timestamp = System.currentTimeMillis();

        Cursor cursor = cursors.get(id);
        cursor.setId(id);
        cursor.setX(x);
        cursor.setY(y);
        cursor.setTimestamp(timestamp);

        updates.emitNext(cursor, (signalType, emitResult) -> emitResult == EmitResult.FAIL_NON_SERIALIZED);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
}
