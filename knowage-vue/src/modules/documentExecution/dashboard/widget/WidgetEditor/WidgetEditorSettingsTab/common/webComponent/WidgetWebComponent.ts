class WidgetWebComponent extends HTMLElement {

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

        console.log('>>>>>>>>>> connectedCallback: ', this.attributes)
    }

    static get observedAttributes() {
        return [];
    }

    attributeChangedCallback(name, oldValue, newValue) {
        console.log(`Value changed for ${name} from ${oldValue} to ${newValue}`);
    }

    get htmlContent() {
        return this.htmlContent
    }

    set htmlContent(value: string) {
        //this.htmlContent = value;
        console.log("htmlContent:", value)
        if (this.shadowRoot) {
            const temp = this.shadowRoot.querySelector('.component-wrapper')
            console.log("TEMP: ", temp)
            if (temp) temp.innerHTML = value
        }
    }


    get webComponentCss() {
        return this.htmlContent
    }

    set webComponentCss(value: string) {
        console.log("webComponentCss:", value)
        if (this.shadowRoot) {
            const temp = this.shadowRoot.querySelector('.style-wrapper')
            console.log("TEMP: ", temp)
            if (temp) temp.innerHTML = value
        }
    }

}

customElements.define("widget-web-component", WidgetWebComponent)

export { }