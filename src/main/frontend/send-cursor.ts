import { throttle } from 'throttle-debounce';

const sendCursor = throttle(200, (cursorX: number, cursorY: number) => {
  console.log('Sending', cursorX, cursorY);
  const tracker = document.querySelector('cursor-tracker');
  if (tracker) {
    (tracker as any).$server.updateCursor(cursorX, cursorY);
  }
});

const moveListener = (e: MouseEvent) => {
  sendCursor(e.clientX, e.clientY);
};

document.addEventListener('mousemove', moveListener);
