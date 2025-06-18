import '@vaadin/button';
import '@vaadin/notification';
import { Notification } from '@vaadin/notification';
import '@vaadin/text-field';
import { updateName } from 'Frontend/cursor-tracker';
import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { MobxLitElement } from '@adobe/lit-mobx';

@customElement('hello-world-view')
export class HelloWorldView extends MobxLitElement {
  name = '';

  connectedCallback() {
    super.connectedCallback();
    this.classList.add('flex', 'p-m', 'gap-m', 'items-end');
  }

  protected createRenderRoot(): HTMLElement | DocumentFragment {
      return this;
  }
  
  render() {
    return html`
      <vaadin-text-field label="Your name" @value-changed=${this.nameChanged}></vaadin-text-field>
      <vaadin-button @click=${this.sayHello}>Say hello</vaadin-button>
    `;
  }

  nameChanged(e: CustomEvent) {
    this.name = e.detail.value;
  }

  async sayHello() {
    updateName(this.name);
    Notification.show(`Hello ${this.name}`);
  }
}
