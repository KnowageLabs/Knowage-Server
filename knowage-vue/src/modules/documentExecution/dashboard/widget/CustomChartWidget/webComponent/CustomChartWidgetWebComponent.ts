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
    }

    get htmlContent() {
        return this.htmlContent
    }

    set htmlContent(value: string) {
        if (this.shadowRoot) {
            const temp = this.shadowRoot.querySelector('.component-wrapper')
            if (temp) temp.innerHTML = '<script src="https://code.highcharts.com/highcharts.js"></script>' + value
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

    get webComponentJs() {
        return this.htmlContent
    }

    set webComponentJs(value: string) {
        if (this.shadowRoot) {
            console.log("-------- js value: ", value)
            console.log('>>>>>>>>>>>>>>>>>>> TEST:', document)
            console.log('>>>>>>>>>>>>>>>>>>> TEST 2:', this)
            const scr = document.createElement('script')
            scr.type = 'text/javascript'
            scr.text = 'alert("ALERT 1")'
            scr.addEventListener('onload', () => {
                console.log("IT'S LOADED")
            })
            var webCompEl = document.getElementById('webComponent')
            console.log('>>>>>>>>>>>>>>>>>>> webCompEl:', webCompEl)
            var JS = document.createElement('script')
            // window.bojanTest = 'bojan test web component'
            // window.bojanFunction = function () {
            //     console.log('THIS IS ALSO WORKING')
            // }

            // console.log('>>>>>>>> TYPE OF: ', typeof window.bojanFunction)
            // JS.text = `alert('test')`
            // JS.text = `alert(bojanTest)`
            //   JS.text = `console.log(bojanFunction())`

            //JS.text = "function test() {console.log('stil working')} test()"
            JS.text = value
            document.body.appendChild(JS)
            // webCompEl?.body.appendChild(JS)
            //  eval(`alert(dataToShow)`)
        }
    }
}

customElements.define('custom-chart-widget-web-component', CustomChartWidgetWebComponent)

export { }
