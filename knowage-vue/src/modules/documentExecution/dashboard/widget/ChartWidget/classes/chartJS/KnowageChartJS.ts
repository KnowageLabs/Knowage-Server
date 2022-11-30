
export class KnowageChartJS {
    model: any

    constructor(model) {
        this.model = model || {
            options: {},
            settings: {}
        }
    }
    dispatchEvent(e: any) {
        // TODO - add mitt ???
        const myCustomEvent = new CustomEvent(e.type, { detail: e });
        document.dispatchEvent(myCustomEvent);
    }
    getModel() {
        return this.model;
    }
}
