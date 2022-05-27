import { Subscription, State } from '@hilla/frontend';
import { html, LitElement } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { throttle } from 'throttle-debounce';
import './cursor-indicator';
import client from './generated/connect-client.default';
import { CursorTracker } from './generated/endpoints';
import Cursor from './generated/org/vaadin/artur/hillacursor/endpoints/Cursor';

@customElement('cursor-tracker')
export class TheView extends LitElement {
  cursorX: number = -1;
  cursorY: number = -1;

  id: string = Math.random().toString(36).substring(2, 9);

  @state()
  cursors: Cursor[] = [];
  cursorId: string | undefined;
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
      this.cursorId = await CursorTracker.join(this.id);
      this.cursors = await CursorTracker.getCursors(this.cursorId);
      const maxTimestamp = Math.max(...this.cursors.map((c) => c.timestamp));
      this.sub = CursorTracker.subscribe(this.cursorId, maxTimestamp).onNext((value: Cursor) => {
        console.debug('Got ', value);
        if (value.id !== this.cursorId) {
          this.cursors = [...this.cursors.filter((cursor) => cursor.id !== value.id)];
          if (value.color !== 'DELETE') {
            this.cursors = [...this.cursors, value];
          }
        }
      });
    });

    const sendCursor = throttle(200, (cursorX: number, cursorY: number) => {
      console.log('Sending', this.cursorId, cursorX, cursorY);
      CursorTracker.trackCursor(this.cursorId, cursorX, cursorY);
    });

    document.addEventListener('mousemove', (e: MouseEvent) => {
      if (this.cursorId) {
        sendCursor(e.clientX, e.clientY);
      }
    });
  }

  render() {
    return html`<button @click=${() => this.close()}>close</button>

      ${this.updatesPaused
        ? html`<cursor-indicator
            .cursor=${{
              name: 'Server connection lost',
              y: 200,
              x: 200,
            }}
          ></cursor-indicator>`
        : this.cursors.map((cursor) => html`<cursor-indicator .cursor=${cursor}></cursor-indicator>`)} `;
  }

  close() {
    this.sub?.cancel();
  }
}
function keepAlive(onReconnect: () => void) {
  if (client.fluxConnection.state === State.ACTIVE) {
    onReconnect();
  }
  client.fluxConnection.addEventListener('state-changed', (e: CustomEvent) => {
    if (e.detail.active) {
      onReconnect();
    }
  });
}
