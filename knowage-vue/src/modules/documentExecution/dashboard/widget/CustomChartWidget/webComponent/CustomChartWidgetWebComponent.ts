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
            if (temp) temp.innerHTML = value
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

    get webComponentJs() {
        return this.webComponentJs
    }

    set webComponentJs(value: string) {
        if (this.shadowRoot) {
            console.log('>>>>>>>>>>>>>>>>>>> TEST:', document)
            console.log('>>>>>>>>>>>>>>>>>>> shadowRoot:', this.shadowRoot)


            var webCompEl = document.getElementById('webComponent')
            // console.log('>>>>>>>>>>>>>>>>>>> webCompEl:', webCompEl)

            const testJS = document.createElement('script')
            testJS.setAttribute('src', 'https://code.highcharts.com/highcharts.js');
            testJS.addEventListener("load", () => alert('LOADED SCRIPT!'));
            // testJS.setAttribute('src', 'https://code.highcharts.com/highcharts/modules/drilldown.js');
            document.body.appendChild(testJS)

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
            console.log(">>>>>>>>>>>>> DOCUMENT BODY: ", document.body)
            console.log("this.shadowRoot.querySelector('#container_1'): ", this.shadowRoot.querySelector('#container_1'))
            console.log("document.getElementById('container_1') ", document.getElementById('container_1'))
            // setTimeout(() => document.body.appendChild(JS), 2000)
            setTimeout(() => this.shadowRoot?.appendChild(JS), 2000)
            // document.body.appendChild(JS)
            // webCompEl?.body.appendChild(JS)
            //  eval(`alert(dataToShow)`)
        }
    }
}

customElements.define('custom-chart-widget-web-component', CustomChartWidgetWebComponent)

export { }