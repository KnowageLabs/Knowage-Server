class CustomChartWidgetWebComponent extends HTMLElement {
    selectEvent = null as any

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

        console.log('>>>>>>>>>>>>>>>>>>> TEST:', document)
        console.log('>>>>>>>>>>>>>>>>>>> TEST 2:', shadow)
        console.log('>>>>>>>>>>>>>>>>>>> TEST 3:', this)
        const scr = document.createElement('script')
        scr.type = 'text/javascript'
        scr.text = 'alert("ALERT 1")'
        scr.addEventListener('onload', () => {
            console.log("IT'S LOADED")
        })
        var webCompEl = document.getElementById('webComponent')
        console.log('>>>>>>>>>>>>>>>>>>> webCompEl:', webCompEl)
        var JS = document.createElement('script')
        window.bojanTest = 'bojan test web component'
        window.bojanFunction = function () {
            alert('THIS IS ALSO WORKING')
        }

        console.log('>>>>>>>> TYPE OF: ', typeof window.bojanFunction)
        // JS.text = `alert('test')`
        // JS.text = `alert(bojanTest)`
        JS.text = `alert(bojanFunction())`

        JS.text = "function test() {alert('stil working')} test()"
        document.body.appendChild(JS)
        // webCompEl?.body.appendChild(JS)
        //  eval(`alert(dataToShow)`)
    }

    get htmlContent() {
        return this.htmlContent
    }

    set htmlContent(value: string) {
        if (this.shadowRoot) {
            const temp = this.shadowRoot.querySelector('.component-wrapper')
            if (temp) temp.innerHTML = value
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

customElements.define('custom-chart-widget-web-component', CustomChartWidgetWebComponent)

export { }
