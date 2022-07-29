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
import { IWidgetEditorDataset, IDatasetOptions, IWidget, IWidgetColumn, IIcon } from '../../Dashboard'
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
                columns: [],
                conditionalStyles: [],
                datasets: [],
                interactions: [],
                theme: '',
                styles: {
                    th: {
                        enabled: true,
                        'background-color': 'rgb(255, 255, 255)',
                        color: 'rgb(137, 158, 175)',
                        'justify-content': 'flex-start',
                        'font-size': '14px',
                        multiline: false,
                        height: 25,
                        'font-style': '',
                        'font-weight': '',
                        'font-family': ''
                    }
                },
                settings: {
                    rowThresholds: {
                        enabled: false,
                        list: []
                    }
                },
                temp: {}
            } as any
            if (widget.type === 'tableWidget') {
                widget.settings.pagination = { enabled: false, itemsNumber: 0 }
                widget.functions = {
                    itemsPerPageIsDisabled: () => {
                        return !widget.settings.pagination.enabled
                    },
                    getColumnIcons: (column: any) => {
                        return column.fieldType === 'ATTRIBUTE' ? 'fas fa-font' : 'fas fa-hashtag'
                    },
                    onColumnDrop: (event: any, model: IWidget) => {
                        if (event.dataTransfer.getData('text/plain') === 'b') return
                        const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
                        const tempColumn = {
                            dataset: eventData.dataset,
                            name: '(' + eventData.name + ')',
                            alias: eventData.alias,
                            type: eventData.type,
                            fieldType: eventData.fieldType,
                            aggregation: eventData.aggregation,
                            style: {
                                hiddenColumn: false,
                                'white-space': 'nowrap',
                                tooltip: { prefix: '', suffix: '', precision: 0 },
                                enableCustomHeaderTooltip: false,
                                customHeaderTooltip: ''
                            },
                            enableTooltip: false,
                            visType: ''
                        }
                        tempColumn.aggregation = 'NONE'

                        model.columns.push(tempColumn)
                        emitter.emit('collumnAdded', eventData)
                    },
                    updateColumnVisibility: (column: IWidgetColumn, model: IWidget) => {
                        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === column.name)
                        if (index !== -1) {
                            if (!model.columns[index].style) {
                                model.columns[index].style = {}
                            }
                            ;(model.columns[index].style.hiddenColumn = false), (model.columns[index].style['white-space'] = 'nowrap')
                            model.columns[index].style.hiddenColumn = !model.columns[index].style.hiddenColumn
                        }
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
                    },
                    getColumnAggregationOptions: () => {
                        return this.descriptor.columnAggregationOptions
                    },
                    showAggregationDropdown: (column: IWidgetColumn) => {
                        return column.fieldType === 'MEASURE'
                    },
                    setSelectedColumn: (column: IWidgetColumn, model: IWidget) => {
                        if (!model || !model.temp) return
                        model.temp.selectedColumn = { ...column }
                    },
                    columnIsSelected: (model: IWidget) => {
                        return model && model.temp.selectedColumn
                    },
                    updateSelectedColumn: (model: IWidget) => {
                        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === model.temp.selectedColumn.name)
                        if (index !== -1) model.columns[index] = { ...model.temp.selectedColumn }
                    },
                    selectedColumnDropdownIsVisible: (model: IWidget) => {
                        return model?.temp.selectedColumn?.fieldType === 'MEASURE'
                    },
                    getVisualizationTypeOptions: () => {
                        return this.descriptor.visualizationTypeOptions
                    },
                    visualizationTypeDropdownIsVisible: (model: IWidget) => {
                        return model?.temp.selectedColumn?.fieldType === 'MEASURE'
                    },
                    tooltipPrecisionIsVisible: (model: IWidget) => {
                        return model?.temp.selectedColumn?.fieldType === 'MEASURE'
                    },
                    tooltipCustomHeaderTextIsDisabled: (model: IWidget) => {
                        return !model?.temp.selectedColumn?.style.enableCustomHeaderTooltip
                    },
                    headerIsDisabled: (model: IWidget) => {
                        return !model?.styles.th.enabled
                    },
                    updateFontWeight: (model: IWidget) => {
                        if (!model) return
                        model.styles.th['font-weight'] = model.styles.th['font-weight'] === 'bold' ? '' : 'bold'
                    },
                    boldIconIsActive: (model: IWidget) => {
                        return model?.styles.th['font-weight'] === 'bold'
                    },
                    updateFontStyle: (model: IWidget) => {
                        if (!model) return
                        model.styles.th['font-style'] = model.styles.th['font-style'] === 'italic' ? '' : 'italic'
                    },
                    fontStyleIconIsActive: (model: IWidget) => {
                        return model?.styles.th['font-style'] === 'italic'
                    },
                    getFontSizeOptions: () => {
                        return this.descriptor.fontSizeOptions
                    },
                    updateFontSize: (newValue: string, model: IWidget) => {
                        if (!model) return
                        model.styles.th['font-size'] = newValue
                    },
                    getCellAlignmentOptions: () => {
                        return this.descriptor.cellAlignmentOptions
                    },
                    updateCellAlignment: (newValue: string, model: IWidget) => {
                        if (!model) return
                        model.styles.th['justify-content'] = newValue
                    },
                    getFontFamilyOptions: () => {
                        return this.descriptor.fontFamilyOptions
                    },
                    updateFontFamily: (newValue: string, model: IWidget) => {
                        if (!model) return
                        model.styles.th['font-family'] = newValue
                    },
                    getFontSize: (model: IWidget) => {
                        return model.styles.th['font-size']
                    },
                    getFontColor: (model: IWidget) => {
                        return model.styles.th.color
                    },
                    setFontColor: (newValue: string, model: IWidget) => {
                        if (!model) return
                        model.styles.th.color = newValue
                    },
                    getBackgroundColor: (model: IWidget) => {
                        return model.styles.th['background-color']
                    },
                    setBackgroundColor: (newValue: string, model: IWidget) => {
                        if (!model) return
                        model.styles.th['background-color'] = newValue
                    },
                    getRowThresholdsList: (model: IWidget) => {
                        if (!model) return
                        return model.settings.rowThresholds?.list
                    },
                    createRowThresholdListItem: (model: IWidget) => {
                        if (!model || !model.settings.rowThresholds?.list || !model.settings.rowThresholds.enabled) return
                        // TODO - CHANGE DEFAULT?
                        model.settings.rowThresholds.list.push({
                            column: '',
                            condition: '',
                            compareValueType: '',
                            compareValue: '',
                            'background-color': '',
                            'justify-content': '',
                            'font-size': '',
                            'font-style': '',
                            'font-weight': '',
                            'font-family': '',
                            color: ''
                        })
                    },
                    deleteRowThresholdListItem: (model: IWidget, itemIndex: number) => {
                        if (model.settings.rowThresholds.enabled) model.settings.rowThresholds.list.splice(itemIndex, 1)
                    },
                    onRowThresholdsEnabled: (model: IWidget) => {
                        if (!model || !model.settings.rowThresholds?.list) return
                        if (model.settings.rowThresholds.list.length === 0) {
                            model.functions.createRowThresholdListItem(model)
                        }
                    },
                    getDatasetColumns: () => {
                        // TODO - REMOVE MOCK
                        return [
                            { value: 'Dataset 1', label: 'Dataset 1' },
                            { value: 'Dataset 2', label: 'Dataset 2' },
                            { value: 'Dataset 3', label: 'Dataset 3' }
                        ]
                    },
                    updateThresholdListItem: (model: IWidget, item: any, index: number) => {
                        if (!model || !model.settings.rowThresholds?.list) return
                        if (index !== -1) {
                            if (model.settings.rowThresholds.list[index].column !== item.column && item.compareValueType !== 'static') {
                                item.compareValue = ''
                            }
                            if (model.settings.rowThresholds.list[index].compareValueType !== item.compareValueType) item.compareValue = ''
                            model.settings.rowThresholds.list[index] = { ...item }
                        }
                    },
                    getColumnConditionOptions: () => {
                        return this.descriptor.columnConditionOptions
                    },
                    getRowStyleCompareValueTypes: () => {
                        return this.descriptor.rowStyleCompareValueTypes
                    },
                    compareValueInputTextIsVisible: (model: IWidget, item: any) => {
                        if (!item) return
                        return item.compareValueType === 'static'
                    },
                    getColumnVariables: (model: IWidget) => {
                        // TODO - remove mock
                        return [
                            { value: 'Variable 1', label: 'Varibale 1' },
                            { value: 'Variable 2', label: 'Varibale 2' }
                        ]
                    },
                    compareValueVariablesDropdownIsVisible: (model: IWidget, item: any) => {
                        if (!item) return
                        return item.compareValueType === 'variable'
                    },
                    getColumnVariableOptions: (item: any) => {
                        // TODO - remove mock
                        return [
                            { value: 'Variable OPTION 1', label: 'Varibale OPTION 1' },
                            { value: 'Variable OPTION 2', label: 'Varibale OPTION 2' }
                        ]
                    },
                    getColumnParameters: (model: IWidget) => {
                        // TODO - remove mock
                        return [
                            { value: 'Parameter 1', label: 'Parameter 1' },
                            { value: 'Parameter 2', label: 'Parameter 2' }
                        ]
                    },
                    compareValueParameterDropdownIsVisible: (model: IWidget, item: any) => {
                        if (!item) return
                        return item.compareValueType === 'parameter'
                    },
                    updateThresholdListItemFontItemWeight: (model: IWidget, item: any, itemIndex: number) => {
                        model.settings.rowThresholds.list[itemIndex]['font-weight'] = model.settings.rowThresholds.list[itemIndex]['font-weight'] === 'bold' ? '' : 'bold'
                    },
                    thresholdItemBoldIconIsActive: (model: IWidget, item: any, itemIndex: number) => {
                        if (!model.settings.rowThresholds.list[itemIndex]) return false
                        return model.settings.rowThresholds.list[itemIndex]['font-weight'] === 'bold'
                    },
                    updateThresholdListItemFontStyle: (model: IWidget, item: any, itemIndex: number) => {
                        model.settings.rowThresholds.list[itemIndex]['font-style'] = model.settings.rowThresholds.list[itemIndex]['font-style'] === 'italic' ? '' : 'italic'
                    },
                    thresholdListItemFontStyleIconIsActive: (model: IWidget, item: any, itemIndex: number) => {
                        if (!model.settings.rowThresholds.list[itemIndex]) return false
                        return model.settings.rowThresholds.list[itemIndex]['font-style'] === 'italic'
                    },
                    updateThresholdListItemFontSize: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
                        model.settings.rowThresholds.list[itemIndex]['font-size'] = newValue
                    },
                    getThresholdListItemFontSize: (model: IWidget, item: any, itemIndex: number) => {
                        return model.settings.rowThresholds.list[itemIndex] ? model.settings.rowThresholds.list[itemIndex]['font-size'] : ''
                    },

                    updateThresholdListItemCellAlignment: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
                        model.settings.rowThresholds.list[itemIndex]['justify-content'] = newValue
                    },
                    updateThresholdListItemFontFamily: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
                        model.settings.rowThresholds.list[itemIndex]['font-family'] = newValue
                    },
                    getThresholdListItemFontColor: (model: IWidget, item: any, itemIndex: number) => {
                        return model.settings.rowThresholds.list[itemIndex] ? model.settings.rowThresholds.list[itemIndex].color : ''
                    },
                    setThresholdListItemFontColor: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
                        model.settings.rowThresholds.list[itemIndex].color = newValue
                    },
                    getThresholdListItemBackgroundColor: (model: IWidget, item: any, itemIndex: number) => {
                        return model.settings.rowThresholds.list[itemIndex] ? model.settings.rowThresholds.list[itemIndex]['background-color'] : ''
                    },
                    setThresholdListItemBackgroundColor: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
                        model.settings.rowThresholds.list[itemIndex]['background-color'] = newValue
                    },
                    rowThresholdsIsDisabled: (model: IWidget) => {
                        return !model?.settings.rowThresholds?.enabled
                    },
                    setThresholdListItemIcon: (icon: IIcon, model: IWidget, item: any, itemIndex: number) => {
                        model.settings.rowThresholds.list[itemIndex].icon = icon?.value
                    },
                    getThresholdListItemIcon: (model: IWidget, item: any, itemIndex: number) => {
                        return model.settings.rowThresholds.list[itemIndex] ? model.settings.rowThresholds.list[itemIndex].icon : ''
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
