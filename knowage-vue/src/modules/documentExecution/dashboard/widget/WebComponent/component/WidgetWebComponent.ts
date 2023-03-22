class WidgetWebComponent extends HTMLElement {
    selectEvent = null as any
    widgetType = '' as string

    constructor() {
        super()
    }

    connectedCallback() {
        const shadow = this.attachShadow({ mode: 'open' })
        const style = document.createElement('style')
        style.classList.add('style-wrapper')

        const wrapper = document.createElement('div')
        wrapper.classList.add('component-wrapper')

        wrapper.style.position = 'relative'
        wrapper.style.overflow = 'auto'
        wrapper.style.height = '100%'

        wrapper.textContent = ''
        shadow.appendChild(style)
        shadow.appendChild(wrapper)
    }

    get htmlContent() {
        return this.htmlContent
    }

    set htmlContent(value: string) {
        if (this.shadowRoot) {
            const temp = this.shadowRoot.querySelector('.component-wrapper')
            if (temp) temp.innerHTML = value

            this.setSelectonElementsListeners()
            this.setPreviewElementsListeners()
            this.setCrossNavElementsListeners()
        }
    }

    get webComponentCss() {
        return this.webComponentCss
    }

    set webComponentCss(value: string) {
        if (this.shadowRoot) {
            const temp = this.shadowRoot.querySelector('.style-wrapper')
            if (temp) temp.innerHTML = value
        }
    }

    setSelectonElementsListeners = () => {
        const selectionElements = this.shadowRoot?.querySelectorAll('.select-class-temp')
        if (selectionElements)
            selectionElements.forEach((el: any) => {
                el.addEventListener(
                    'click',
                    (event: any) => {
                        const eventTarget = event.target as any
                        if (eventTarget.attributes) {
                            const selectionColumn = eventTarget.attributes['kn-selection-column'].value
                            const selectionValue = eventTarget.attributes['kn-selection-value'].value
                            this.dispatchEvent(
                                new CustomEvent('selectEvent', {
                                    bubbles: true,
                                    cancelable: false,
                                    composed: true,
                                    detail: { selectionColumn: selectionColumn, selectionValue: selectionValue }
                                })
                            )
                        }
                    },
                    false
                )
            })
    }

    setPreviewElementsListeners = () => {
        const previewElements = this.shadowRoot?.querySelectorAll('.preview-class-temp')
        if (previewElements)
            previewElements.forEach((el: any) => {
                el.addEventListener(
                    'click',
                    (event: any) => {
                        const eventTarget = event.target as any
                        if (eventTarget.attributes) {
                            const datasetLabel = eventTarget.attributes['kn-preview'].value
                            this.dispatchEvent(
                                new CustomEvent('previewEvent', {
                                    bubbles: true,
                                    cancelable: false,
                                    composed: true,
                                    detail: { datasetLabel: datasetLabel }
                                })
                            )
                        }
                    },
                    false
                )
            })
    }

    setCrossNavElementsListeners = () => {
        const crossNavElements = this.shadowRoot?.querySelectorAll('.cross-nav-class-temp')
        if (crossNavElements)
            crossNavElements.forEach((el: any) => {
                el.addEventListener(
                    'click',
                    (event: any) => {
                        const eventTarget = event.target as any
                        if (eventTarget.attributes) {
                            const crossValue = this.widgetType === 'html' ? eventTarget.attributes['kn-cross'].value : eventTarget.innerHTML
                            this.dispatchEvent(
                                new CustomEvent('crossNavEvent', {
                                    bubbles: true,
                                    cancelable: false,
                                    composed: true,
                                    detail: { crossValue: crossValue }
                                })
                            )
                        }
                    },
                    false
                )
            })
    }
}

customElements.define('widget-web-component', WidgetWebComponent)

export { }
