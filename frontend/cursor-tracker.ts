import { Subscription } from '@hilla/frontend';
import { html, LitElement } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { throttle } from 'throttle-debounce';
import './cursor-indicator';
import { CursorTracker } from './generated/endpoints';
import Cursor from './generated/org/vaadin/artur/hillacursor/endpoints/Cursor';

@customElement('cursor-tracker')
export class TheView extends LitElement {
  cursorX: number = -1;
  cursorY: number = -1;

  @state()
  cursors: Cursor[] = [];
  cursorId: string | undefined;
  sendTimer: any;
  sub?: Subscription<Cursor>;

  async connectedCallback() {
    super.connectedCallback();

    this.cursorId = await CursorTracker.join(window.navigator.userAgent);
    this.cursors = await CursorTracker.getCursors(this.cursorId);
    this.cursors = this.cursors.filter(cursor => cursor.id !== this.cursorId);
    console.debug('Got cursors', this.cursors);
    const maxTimestamp = Math.max(...this.cursors.map((c) => c.timestamp));
    this.sub = CursorTracker.subscribe(this.cursorId, maxTimestamp).onNext((value) => {
      console.debug('Got ', value);
      if (value.id !== this.cursorId) {
        this.cursors = [...this.cursors.filter((cursor) => cursor.id !== value.id), value];
      }
    });

    const sendCursor = throttle(200, (cursorX: number, cursorY: number) => {
      console.log('Sending', this.cursorId, cursorX, cursorY);
      CursorTracker.trackCursor(this.cursorId, cursorX, cursorY);
    });

    document.addEventListener('mousemove', (e: MouseEvent) => {
      sendCursor(e.clientX, e.clientY);
    });
  }

  render() {
    return html`<button @click=${() => this.close()}>close</button> ${this.cursors.map((cursor) => html`<cursor-indicator .cursor=${cursor}></cursor-indicator>`)} `;
  }

  close() {
    this.sub?.cancel();
  }
}
