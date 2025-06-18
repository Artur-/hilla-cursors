import { Subscription } from '@vaadin/hilla-frontend';
import { Signal, useSignal } from '@vaadin/hilla-react-signals';
import { useEffect } from 'react';
import { throttle } from 'throttle-debounce';
import { CursorIndicator } from './CursorIndicator';
import client from './generated/connect-client.default';
import Cursor from './generated/org/vaadin/artur/hillacursor/endpoints/Cursor';
import { keepAlive } from './util';
import { CursorTrackerService } from './generated/endpoints';

let name: string = Math.random().toString(36).substring(2, 9);
let cursorId: string | undefined;

export function CursorTracker() {
  const updatesPaused = useSignal(false);
  const cursors: Signal<Cursor[]> = useSignal([]);
  const sub: Signal<Subscription<Cursor> | undefined> = useSignal(undefined);

  useEffect(() => {
    client.fluxConnection.addEventListener('state-changed', (e: CustomEvent) => {
      updatesPaused.value = !e.detail.active;
    });

    keepAlive(async () => {
      cursorId = await CursorTrackerService.join(name);
      cursors.value = await CursorTrackerService.getCursors(cursorId);
      const maxTimestamp = Math.max(...cursors.value.map((c) => c.timestamp));
      sub.value = CursorTrackerService.subscribe(cursorId, maxTimestamp).onNext((value: Cursor) => {
        console.debug('Got ', value);
        if (value.id !== cursorId) {
          cursors.value = [...cursors.value.filter((cursor) => cursor.id !== value.id)];
          if (value.color !== 'DELETE') {
            cursors.value = [...cursors.value, value];
          }
        }
      });
    });

    const sendCursor = throttle(200, (cursorX: number, cursorY: number) => {
      console.log('Sending', cursorId, cursorX, cursorY);
      CursorTrackerService.trackCursor(cursorId, cursorX, cursorY);
    });

    document.addEventListener('mousemove', (e: MouseEvent) => {
      if (cursorId) {
        sendCursor(e.clientX, e.clientY);
      }
    });
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
    return cursors.value.map((cursor) => <CursorIndicator key={cursor.id} cursor={cursor}></CursorIndicator>);
  }
}

export const updateName = (newName: string): void => {
  if (cursorId) {
    CursorTrackerService.updateName(cursorId, newName);
  }
};
