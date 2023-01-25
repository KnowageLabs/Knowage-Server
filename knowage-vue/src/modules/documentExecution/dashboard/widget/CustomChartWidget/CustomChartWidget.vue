<template>
    <!-- <custom-chart-widget-web-component class="kn-flex" ref="webComponent"></custom-chart-widget-web-component> -->
    <div id="containerElement"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDriver, IDataset, ISelection, IVariable } from '../../Dashboard'
import './webComponent/CustomChartWidgetWebComponent'
import { mapActions } from 'pinia'
import store from '../../Dashboard.store'
import appStore from '../../../../../App.store'
import { IWidget } from '../../Dashboard'
import { updateStoreSelections } from '../interactionsHelpers/InteractionHelper'
import { CustomChartDatastore } from '../WidgetEditor/WidgetEditorSettingsTab/CustomChartWidget/datastore/CustomChartWidgetDatastore'

export default defineComponent({
    name: 'custom-chart-widget',
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
            webComponentJs: '' as string,
            webComponentRef: {} as any,
            drivers: [] as IDashboardDriver[],
            datastore: new CustomChartDatastore(null)
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
        console.log('DATASTORE ---------------', this.datastore)
    },
    methods: {
        ...mapActions(store, ['getInternationalization', 'setSelections', 'getAllDatasets', 'getDashboardDrivers']),
        ...mapActions(appStore, ['setError']),
        loadDrivers() {
            this.drivers = this.getDashboardDrivers(this.dashboardId) // TODO
        },
        async loadDataToShow() {
            this.dataToShow = this.widgetData
            await this.loadHTML()
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections // TODO
        },
        async loadHTML() {
            if (!this.propWidget.settings || !this.propWidget.settings.editor) return

            this.htmlContent = this.propWidget.settings.editor.html
            this.webComponentCss = this.propWidget.settings.editor.css
            this.webComponentJs = this.propWidget.settings.editor.js

            this.test()

            // TODO
            if (!this.webComponentRef) return
            this.webComponentRef.htmlContent = this.propWidget.type === 'text' ? '<div style="position: absolute;height: 100%;width: 100%;">' + this.htmlContent + '</div>' : this.htmlContent
            this.webComponentRef.webComponentCss = this.webComponentCss
            this.webComponentRef.webComponentJs = this.webComponentJs
        },
        onSelect(event: any) {
            // TODO
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
        test() {
            const containerElement = document.getElementById('containerElement')
            console.log('----------- CONTAINER ELEMENT: ', containerElement)
            if (!containerElement) return

            const style = document.createElement('style')
            style.classList.add('style-wrapper')

            const wrapper = document.createElement('div')
            wrapper.classList.add('component-wrapper')

            wrapper.style.position = 'relative'
            wrapper.style.overflow = 'auto'
            wrapper.style.height = '100%'

            wrapper.textContent = ''
            containerElement.appendChild(style)
            containerElement.appendChild(wrapper)

            const temp = document.querySelector('.component-wrapper')
            console.log('------------- TEMP: ', temp)
            if (temp) temp.innerHTML = this.htmlContent

            const temp2 = document.querySelector('.style-wrapper')
            console.log('------------- TEMP 2: ', temp2)
            if (temp2) temp2.innerHTML = this.webComponentCss

            const testJS = document.createElement('script')
            testJS.setAttribute('src', 'https://code.highcharts.com/highcharts.js')
            testJS.setAttribute('src', 'https://code.highcharts.com/modules/drilldown.js')
            // testJS.addEventListener('load', () => alert('LOADED SCRIPT!'))
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
            JS.text = this.webComponentJs
            console.log('>>>>>>>>>>>>> DOCUMENT BODY: ', document.body)

            setTimeout(() => document.body.appendChild(JS), 2000)
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
