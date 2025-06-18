import { Subscription } from '@vaadin/hilla-frontend';
import { signal, Signal, useSignal, ValueSignal } from '@vaadin/hilla-react-signals';
import { useEffect } from 'react';
import { throttle } from 'throttle-debounce';
import { CursorIndicator } from './CursorIndicator';
import client from './generated/connect-client.default';
import Cursor from './generated/org/vaadin/artur/hillacursor/endpoints/Cursor';
import { keepAlive } from './util';
import { CursorTrackerService } from './generated/endpoints';
import { v4 as uuid } from 'uuid';
let name: string = Math.random().toString(36).substring(2, 9);

const id: string = uuid();
const colors = ['red', 'green', 'blue', 'brown', 'magenta', 'mediumvioletred', 'orange'];
const updatesPaused: Signal<boolean> = signal(false);
const color = colors[id.charCodeAt(0) % colors.length];
const cursors = CursorTrackerService.subscribe();
console.log('Cursors', cursors);

function findCursor(): ValueSignal<Cursor> | undefined {
  return cursors.value.find((c) => c.value.id === id);
}
export function CursorTracker() {
  useEffect(() => {
    const cursor: Cursor = {
      name,
      id,
      color,
      timestamp: Date.now(),
      x: 0,
      y: 0,
    };
    cursors
      .insertLast(cursor)
      .result.then(() => {
        console.log('Cursor added', id, cursor);
      })
      .catch((e) => {
        console.error('Error adding cursor', id, e);
      });
    const sendCursor = throttle(200, (cursorX: number, cursorY: number) => {
      const thisCursor = findCursor();
      if (thisCursor) {
      console.log('Sending', id, cursorX, cursorY);
        thisCursor.value.x = cursorX;
        thisCursor.value.y = cursorY;
        thisCursor.value.timestamp = Date.now();
      }
    });

    const moveListener = (e: MouseEvent) => {
      sendCursor(e.clientX, e.clientY);
    };

    document.addEventListener('mousemove', moveListener);
    return () => {
      console.log('Unsubscribing from cursor updates');
      const thisCursor = findCursor();
      if (thisCursor) {
        cursors.remove(thisCursor);
      } else {
        console.error('Did not find cursor to remove', id);
      }
      document.removeEventListener('mousemove', moveListener);
    };
  }, []);

  if (updatesPaused.value) {
    return (
      <CursorIndicator
        cursor={{
          name: 'Server connection lost',
          y: 200,
          x: 200,
          id: 'lost',
          color: 'red',
          timestamp: Date.now(),
        }}></CursorIndicator>
    );
  } else {
    console.log('Rendering cursors', cursors.value);

    return cursors.value.map((cursor) => (
      <CursorIndicator key={cursor.value.id} cursor={cursor.value}></CursorIndicator>
    ));
  }
}

export const updateName = (newName: string): void => {
  const thisCursor = cursors.value.find((c) => c.id === id);
  if (thisCursor) {
    thisCursor.value.name = newName;
  }
};
