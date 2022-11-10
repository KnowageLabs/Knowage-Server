class WidgetWebComponent extends HTMLElement {
    selectEvent = null as any

    constructor() {
        super();

        this.selectEvent = new CustomEvent("selectEvent", {
            bubbles: true,
            cancelable: false,
            composed: true
        });
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
            console.log(">>>>> TEMP 2: ", temp2)
            if (temp2) temp2.addEventListener('click', event => {
                console.log("CAAAAAAAALED select: ", event.target)
                console.log("CAAAAAAAALED select: ", event.target?.attributes)
                this.dispatchEvent(this.selectEvent);
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