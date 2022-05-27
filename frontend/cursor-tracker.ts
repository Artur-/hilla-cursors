import { Subscription, State } from '@hilla/frontend';
import { html, LitElement } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { throttle } from 'throttle-debounce';
import './cursor-indicator';
import client from './generated/connect-client.default';
import { CursorTracker } from './generated/endpoints';
import Cursor from './generated/org/vaadin/artur/hillacursor/endpoints/Cursor';
import { keepAlive } from './util';

let name: string = Math.random().toString(36).substring(2, 9);
let cursorId: string | undefined;

@customElement('cursor-tracker')
export class CursorTrackerElement extends LitElement {
  cursorX: number = -1;
  cursorY: number = -1;

  @state()
  cursors: Cursor[] = [];
  sendTimer: any;
  sub?: Subscription<Cursor>;
  @state()
  updatesPaused = false;

  async connectedCallback() {
    super.connectedCallback();

    client.fluxConnection.addEventListener('state-changed', (e: CustomEvent) => {
      this.updatesPaused = !e.detail.active;
    });

    keepAlive(async () => {
      cursorId = await CursorTracker.join(name);
      this.cursors = await CursorTracker.getCursors(cursorId);
      const maxTimestamp = Math.max(...this.cursors.map((c) => c.timestamp));
      this.sub = CursorTracker.subscribe(cursorId, maxTimestamp).onNext((value: Cursor) => {
        console.debug('Got ', value);
        if (value.id !== cursorId) {
          this.cursors = [...this.cursors.filter((cursor) => cursor.id !== value.id)];
          if (value.color !== 'DELETE') {
            this.cursors = [...this.cursors, value];
          }
        }
      });
    });

    const sendCursor = throttle(200, (cursorX: number, cursorY: number) => {
      console.log('Sending', cursorId, cursorX, cursorY);
      CursorTracker.trackCursor(cursorId, cursorX, cursorY);
    });

    document.addEventListener('mousemove', (e: MouseEvent) => {
      if (cursorId) {
        sendCursor(e.clientX, e.clientY);
      }
    });
  }

  render() {
    return html`
      ${this.updatesPaused
        ? html`<cursor-indicator
            .cursor=${{
              name: 'Server connection lost',
              y: 200,
              x: 200,
            }}
          ></cursor-indicator>`
        : this.cursors.map((cursor) => html`<cursor-indicator .cursor=${cursor}></cursor-indicator>`)}
    `;
  }
}

export const updateName = (newName: string): void => {
  if (cursorId) {
    CursorTracker.updateName(cursorId, newName);
  }
};
