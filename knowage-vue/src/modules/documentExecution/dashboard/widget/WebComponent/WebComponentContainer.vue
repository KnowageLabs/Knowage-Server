<template>
    <widget-web-component id="webComponent" class="kn-flex" ref="webComponent"></widget-web-component>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDriver, IDataset, ISelection, IVariable } from '../../Dashboard'
import './component/WidgetWebComponent'
import { mapActions } from 'pinia'
import store from '../../Dashboard.store'
import appStore from '../../../../../App.store'
import { IWidget } from '../../Dashboard'
import { parseHtml, parseText } from '../WidgetEditor/helpers/htmlParser/ParserHelper'
import { executeCrossNavigation, executePreview, updateStoreSelections } from '../interactionsHelpers/InteractionHelper'

export default defineComponent({
    name: 'widget-component-container',
    emits: ['interaction', 'pageChanged', 'launchSelection', 'sortingChanged'],
    components: {},
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        widgetData: { type: Object as any, required: true },
        dashboardId: { type: String, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        editorMode: { type: Boolean }
    },
    data() {
        return {
            dataToShow: {} as any,
            activeSelections: [] as ISelection[],
            htmlContent: '' as string,
            webComponentCss: '' as string,
            webComponentRef: {} as any,
            drivers: [] as IDashboardDriver[],
            bojanTest: 'bojanTest value'
        }
    },
    watch: {
        widgetData() {
            this.loadDataToShow()
        },
        propActiveSelections() {
            this.loadActiveSelections()
        }
    },
    mounted() {
        this.webComponentRef = this.$refs.webComponent as any
        this.loadDrivers()
        this.loadActiveSelections()
        this.loadDataToShow()
        //  this.test()
    },
    methods: {
        test() {
            console.log('>>>>>>>>>>>>>>>>>>> TEST:')
            const scr = document.createElement('script')
            scr.type = 'text/javascript'
            scr.text = 'alert("ALERT 1")'
            scr.addEventListener('onload', () => {
                console.log("IT'S LOADED")
            })
            var webCompEl = document.getElementById('webComponent')
            console.log('>>>>>>>>>>>>>>>>>>> webCompEl:', webCompEl)
            var JS = document.createElement('script')
            window.bojanTest = this.bojanTest
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
        },
        ...mapActions(store, ['getInternationalization', 'setSelections', 'getAllDatasets', 'getDashboardDrivers']),
        ...mapActions(appStore, ['setError']),
        loadDrivers() {
            this.drivers = this.getDashboardDrivers(this.dashboardId)
        },
        async loadDataToShow() {
            this.dataToShow = this.widgetData
            await this.loadHTML()
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        async loadHTML() {
            if (this.propWidget.type !== 'html' && this.propWidget.type !== 'text') return
            let temp = {} as any
            if (this.propWidget.type === 'html') {
                temp = parseHtml(this.propWidget, this.drivers, this.variables, this.activeSelections, this.getInternationalization(), this.dataToShow, this.$toast)
                this.htmlContent = temp.html
                this.webComponentCss = temp.css
            } else {
                this.htmlContent = parseText(this.propWidget, this.drivers, this.variables, this.activeSelections, this.getInternationalization(), this.dataToShow, this.$toast)
            }

            if (!this.webComponentRef) return
            this.webComponentRef.htmlContent = this.propWidget.type === 'text' ? '<div style="position: absolute;height: 100%;width: 100%;">' + this.htmlContent + '</div>' : this.htmlContent
            this.webComponentRef.webComponentCss = this.webComponentCss
            this.webComponentRef.addEventListener('selectEvent', this.onSelect)
            this.webComponentRef.addEventListener('previewEvent', this.onPreview)
            this.webComponentRef.addEventListener('crossNavEvent', this.onCrossNavigation)
        },
        onSelect(event: any) {
            if (this.editorMode || !event.detail) return
            const value = event.detail.selectionValue
            const selectionColumnName = event.detail.selectionColumn
            updateStoreSelections(this.createNewSelection([value], selectionColumnName), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
        },
        getDatasetLabel(datasetId: number) {
            const datasets = this.getAllDatasets()
            const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
            return index !== -1 ? datasets[index].label : ''
        },
        createNewSelection(value: (string | number)[], columnName: string) {
            return { datasetId: this.propWidget.dataset as number, datasetLabel: this.getDatasetLabel(this.propWidget.dataset as number), columnName: columnName, value: value, aggregated: false, timestamp: new Date().getTime() }
        },
        onPreview(event: any) {
            if (this.editorMode || !event.detail) return
            const datasetLabel = event.detail.datasetLabel
            executePreview(datasetLabel)
        },
        onCrossNavigation(event: any) {
            if (this.editorMode || !event.detail || !this.propWidget) return
            const crossValue = event.detail.crossValue
            const crossNavigationConfiguration = this.propWidget.settings.interactions.crossNavigation
            executeCrossNavigation(crossValue, crossNavigationConfiguration)
        }
    }
})
</script>
<style lang="scss" scoped>
.widget-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background-color: #fff;
    flex: 1;
    .widget-container-renderer {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }
}
</style>
