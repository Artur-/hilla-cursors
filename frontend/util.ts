import { State } from "@hilla/frontend";
import client from "./generated/connect-client.default";

export function keepAlive(onReconnect: () => void) {
    if (client.fluxConnection.state === State.ACTIVE) {
      onReconnect();
    }
    client.fluxConnection.addEventListener('state-changed', (e) => {
      if (e.detail.active) {
        onReconnect();
      }
    });
  }
  