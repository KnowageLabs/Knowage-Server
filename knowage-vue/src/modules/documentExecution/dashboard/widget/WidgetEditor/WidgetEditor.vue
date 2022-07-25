<template>
    <Teleport to=".dashboard-container">
        <div class="widgetEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ widget.type }} Widget Editor </template>
                <template #end> <Button @click="close">Close</Button> </template>
            </Toolbar>
            <div class="widgetEditor-container">
                <WidgetEditorTabs :propWidget="widget" :datasets="datasets" @datasetSelected="onDatasetSelected" />
                <WidgetEditorPreview :propWidget="widget" />
            </div>
        </div>
    </Teleport>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing.
 */
import { defineComponent, PropType } from 'vue'
import { IWidgetEditorDataset, IDatasetOptions, IWidget, IWidgetColumn } from '../../Dashboard'
import { AxiosResponse } from 'axios'
import { emitter } from '../../DashboardHelpers'
import WidgetEditorPreview from './WidgetEditorPreview.vue'
import WidgetEditorTabs from './WidgetEditorTabs.vue'
import mainStore from '../../../../../App.store'
import descriptor from './WidgetEditorDescriptor.json'

export default defineComponent({
    name: 'widget-editor',
    components: { WidgetEditorPreview, WidgetEditorTabs },
    emits: ['close'],
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array } },
    data() {
        return {
            descriptor,
            widget: {} as any,
            previewData: null as any,
            datasetFunctions: {} as { availableFunctions: string[]; nullifFunction: string[] }
        }
    },
    watch: {
        propWidget() {
            this.loadWidget()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.loadWidget()
    },
    methods: {
        loadWidget() {
            // TODO - uncomment this, remove mock
            // this.widget = this.propWidget ? deepcopy(this.propWidget) : this.createNewWidget()
            this.widget = this.createNewWidget()
        },
        createNewWidget() {
            // TODO - remove hardcoded
            const widget = {
                type: 'tableWidget',
                columns: [
                    {
                        dataset: 1,
                        name: 'column1',
                        alias: 'column1 alias',
                        type: 'java.math.BigDecimal',
                        fieldType: 'MEASURE',
                        aggregation: 'SUM'
                    },
                    {
                        dataset: 2,
                        name: 'column2',
                        alias: 'column2 alias',
                        type: 'java.math.BigDecimal',
                        fieldType: 'ATTRIBUTE',
                        aggregation: 'SUM'
                    }
                ],
                conditionalStyles: [],
                datasets: [],
                interactions: [],
                theme: '',
                styles: {},
                settings: {},
                temp: {}
            } as any
            if (widget.type === 'tableWidget') {
                widget.settings.pagination = { enabled: false, itemsNumber: 0 }
                widget.functions = {
                    disabledTest: () => {
                        return !widget.settings.pagination.enabled
                    },
                    getColumnIcons: (column: any) => {
                        return column.fieldType === 'ATTRIBUTE' ? 'fas fa-font' : 'fas fa-hashtag'
                    },
                    onColumnDrop: (event: any, model: IWidget) => {
                        if (event.dataTransfer.getData('text/plain') === 'b') return
                        const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))

                        model.columns.push({ dataset: eventData.dataset, name: '(' + eventData.name + ')', alias: eventData.alias, type: eventData.type, fieldType: eventData.fieldType, aggregation: eventData.aggregation, style: { hiddenColumn: false, 'white-space': 'nowrap' } })
                        emitter.emit('collumnAdded', eventData)
                    },
                    updateColumnVisibility: (column: IWidgetColumn, model: IWidget) => {
                        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === column.name)
                        if (index !== -1) {
                            if (!model.columns[index].style) {
                                model.columns[index].style = {
                                    hiddenColumn: false,
                                    'white-space': 'nowrap'
                                }
                            }
                            model.columns[index].style.hiddenColumn = !model.columns[index].style.hiddenColumn
                        }
                        console.log('updateColumnVisibility() - ', model.columns[index])
                    },
                    removeColumn: (column: IWidgetColumn, model: IWidget) => {
                        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === column.name)
                        if (index !== -1) {
                            model.columns.splice(index, 1)
                            emitter.emit('collumnRemoved', column)
                        }
                    },
                    updateColumnValue: (column: IWidgetColumn, model: IWidget, field: string) => {
                        if (!model || !model.columns) return
                        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === column.name)
                        if (index !== -1) {
                            model.columns[index][field] = column[field]
                            if (model.columns[index][field].fieldType === 'ATTRIBUTE') model.columns[index][field].aggregation = 'NONE'
                            if (model.temp.selectedColumn.name === model.columns[index].name) model.temp.selectedColumn = { ...model.columns[index] }
                        }
                        console.log('updateColumnValues() - ', model.columns[index])
                    },
                    getColumnAggregationOptions: () => {
                        return this.descriptor.columnAggregationOptions
                    },
                    showAggregationDropdown: (column: IWidgetColumn) => {
                        return column.fieldType === 'MEASURE'
                    },
                    setSelectedColumn(column: IWidgetColumn, model: IWidget) {
                        console.log('setSelectedColumn', column)
                        if (!model || !model.temp) return
                        model.temp.selectedColumn = { ...column }
                        console.log('SELECTED COLUMN: ', model.temp)
                    },
                    columnIsSelected(model: IWidget) {
                        console.log('columnIsSelected', model)
                        return model && model.temp.selectedColumn
                    },
                    updateSelectedColumn(model: IWidget) {
                        console.log('!!!!!!!!!!!!!!!!! updateSelectedColumn !!!!!!!!!!!!!!!!!!!!!!!!!!!!', model)
                        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === model.temp.selectedColumn.name)
                        console.log('!!!!!!!!!!!!!!!!! index !!!!!!!!!!!!!!!!!!!!!!!!!!!!', index)
                        if (index !== -1) {
                            model.columns[index] = { ...model.temp.selectedColumn }
                            console.log('!!!!!!!!!!!!!!!!! updateSelectedColumn !!!!!!!!!!!!!!!!!!!!!!!!!!!! updated', model.columns[index])
                        }
                    },
                    selectedColumnDropdownIsVisible(model: IWidget) {
                        return model?.temp.selectedColumn?.fieldType === 'MEASURE'
                    }
                }
            }
            return widget
        },
        onDatasetSelected(dataset: IWidgetEditorDataset) {
            this.loadPreviewData(dataset)
            this.loadAvailableFunctions(dataset)
        },
        async loadPreviewData(dataset: IWidgetEditorDataset) {
            this.store.setLoading(true)
            // TODO - remove hardcoded
            const postData = {
                aggregations: {
                    measures: [],
                    categories: [],
                    dataset: dataset.label
                },
                parameters: {},
                selections: {},
                indexes: []
            } as IDatasetOptions
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/${dataset.label}/data?offset=0&size=10&nearRealtime=true&widgetName=widget_table_1658220241151`, postData)
                .then((response: AxiosResponse<any>) => (this.previewData = response.data))
                .catch(() => {})
            this.store.setLoading(false)
            console.log('loadPreviewData() - previewData: ', this.previewData)
        },
        async loadAvailableFunctions(dataset: IWidgetEditorDataset) {
            this.store.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/availableFunctions/${dataset.id}?useCache=false`)
                .then((response: AxiosResponse<any>) => (this.datasetFunctions = response.data))
                .catch(() => {})
            this.store.setLoading(false)
        },
        close() {
            this.$emit('close')
        }
    }
})
</script>
<style lang="scss">
.widgetEditor {
    height: 100vh;
    width: 100%;
    top: 0;
    left: 0;
    background-color: white;
    position: absolute;
    z-index: 999;
    display: flex;
    flex-direction: column;
    .widgetEditor-container {
        flex: 1;
        display: flex;
    }
}
</style>
