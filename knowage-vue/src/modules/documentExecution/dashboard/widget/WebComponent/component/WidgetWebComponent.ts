class WidgetWebComponent extends HTMLElement {
    selectEvent = null as any

    constructor() {
        super();

    }

    connectedCallback() {
        const shadow = this.attachShadow({ mode: 'open' });
        const style = document.createElement('style');
        style.classList.add('style-wrapper');

        const wrapper = document.createElement('div');
        wrapper.classList.add('component-wrapper');
        wrapper.textContent = '';
        shadow.appendChild(style);
        shadow.appendChild(wrapper);
    }

    get htmlContent() {
        return this.htmlContent
    }

    set htmlContent(value: string) {
        if (this.shadowRoot) {
            const temp = this.shadowRoot.querySelector('.component-wrapper')
            if (temp) temp.innerHTML = value

            const temp2 = this.shadowRoot.querySelector('.select-class-temp')
            if (temp2) temp2.addEventListener('click', event => {
                const eventTarget = event.target as any

                if (eventTarget.attributes) {
                    const selectionColumn = eventTarget.attributes['kn-selection-column'].value
                    const selectionValue = eventTarget.attributes['kn-selection-value'].value
                    this.dispatchEvent(new CustomEvent("selectEvent", {
                        bubbles: true,
                        cancelable: false,
                        composed: true,
                        detail: { selectionColumn: selectionColumn, selectionValue: selectionValue }
                    }));
                }
            }, false);
        }
    }

    get webComponentCss() {
        return this.htmlContent
    }

    set webComponentCss(value: string) {
        if (this.shadowRoot) {
            const temp = this.shadowRoot.querySelector('.style-wrapper')
            if (temp) temp.innerHTML = value
        }
    }
}

customElements.define("widget-web-component", WidgetWebComponent)

export { }