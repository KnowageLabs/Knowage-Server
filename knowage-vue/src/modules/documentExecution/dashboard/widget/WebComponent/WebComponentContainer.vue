<template>
    <widget-web-component class="kn-flex" ref="webComponent"></widget-web-component>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the common widget elements to be rendered.
 */
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
        drivers: { type: Array as PropType<IDashboardDriver[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        editorMode: { type: Boolean }
    },
    data() {
        return {
            dataToShow: {} as any,
            activeSelections: [] as ISelection[],
            htmlContent: '' as string,
            webComponentCss: '' as string
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
    created() {
        this.loadActiveSelections()
        this.loadDataToShow()
    },
    methods: {
        ...mapActions(store, ['getInternationalization', 'setSelections', 'getAllDatasets']),
        ...mapActions(appStore, ['setError']),
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

            const webComponentRef = this.$refs.webComponent as any
            if (!webComponentRef) return
            webComponentRef.htmlContent = this.propWidget.type === 'text' ? '<div style="position: absolute;height: 100%;width: 100%;">' + this.htmlContent + '</div>' : this.htmlContent
            webComponentRef.webComponentCss = this.webComponentCss
            webComponentRef.addEventListener('selectEvent', this.onSelect)
            webComponentRef.addEventListener('previewEvent', this.onPreview)
            webComponentRef.addEventListener('crossNavEvent', this.onCrossNavigation)
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
            const crossNavigationConfiguration = this.propWidget.settings.interactions.crosssNavigation
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
