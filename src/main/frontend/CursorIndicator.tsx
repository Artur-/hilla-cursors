import { ReactElement } from 'react';
import { ReactAdapterElement, RenderHooks } from './generated/flow/ReactAdapter';

type Cursor = {
  x: number;
  y: number;
  name: string;
  color: string;
};

class CursorIndicatorClass   extends ReactAdapterElement {
  protected override render(hooks: RenderHooks): ReactElement | null {
    const [cursor, _setCursor] = hooks.useState<Cursor>('cursor');
    return <CursorIndicator cursor={cursor} />;
  }
}
export function CursorIndicator({ cursor }: { cursor: Cursor }) {
  return (
    <svg
      style={{
        pointerEvents: 'none',
        fill: cursor.color,
        position: 'absolute',
        left: cursor.x + 'px',
        top: cursor.y + 'px',
        height: '40px',
        zIndex: 1000,
      }}
      viewBox="8 8 200 28">
      <rect
        x="12.5"
        y="13.6"
        transform="matrix(0.9221 -0.3871 0.3871 0.9221 -5.7605 6.5909)"
        width="2"
        height="8"></rect>
      <polygon points="9.2,7.3 9.2,18.5 12.2,15.6 12.6,15.5 17.4,15.5 "></polygon>
      <text x="20" y="20" style={{ fontSize: '15px' }}>
        {cursor.name}
      </text>
    </svg>
  );
}

customElements.define('cursor-indicator', CursorIndicatorClass);
