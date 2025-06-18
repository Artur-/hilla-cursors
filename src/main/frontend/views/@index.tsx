import { Button, Notification, TextField } from '@vaadin/react-components';
import { useSignal } from '@vaadin/hilla-react-signals';

export default function HelloWorldView() {
  const name = useSignal('');
  return (
    <div className="flex p-m gap-m items-end">
      <TextField label="Your name" onValueChanged={(e) => (name.value = e.detail.value)}></TextField>
      <Button onClick={(e) => Notification.show(name.value)}>Say hello</Button>
    </div>
  );
}
