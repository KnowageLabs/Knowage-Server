<template>
    <div v-if="widgetModel" class="dashboard-editor-list-card-container p-m-3">
        <span class="p-float-label p-mx-2 p-mt-4 p-mb-1">
            <Dropdown id="dataset" class="kn-material-input kn-width-full" v-model="selectedDataset" :options="datasetOptions" optionLabel="label" @change="onDatasetSelected"></Dropdown>
            <label for="dataset" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.selectDataset') }} </label>
        </span>
        <div v-if="widgetModel.type !== 'selector'" class="p-col-12 p-d-flex">
            <label class="kn-material-input-label p-as-center p-ml-1"> {{ $t('common.columns') }} </label>
            <Button :label="$t('common.addColumn')" icon="pi pi-plus-circle" class="p-button-outlined p-ml-auto p-mr-1" @click="createNewCalcField"></Button>
        </div>

        <Listbox v-if="selectedDataset" class="kn-list kn-list-no-border-right dashboard-editor-list" :options="selectedDatasetColumns" :filter="true" :filterPlaceholder="$t('common.search')" :filterFields="descriptor.filterFields" :emptyFilterMessage="$t('common.info.noDataFound')">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="kn-list-item kn-draggable" draggable="true" :style="dataListDescriptor.style.list.listItem" @dragstart="onDragStart($event, slotProps.option)">
                    <i class="pi pi-bars" :style="dataListDescriptor.style.list.listIcon"></i>
                    <i :style="dataListDescriptor.style.list.listIcon" :class="slotProps.option.fieldType === 'ATTRIBUTE' ? 'fas fa-font' : 'fas fa-hashtag'" class="p-ml-2"></i>
                    <div class="kn-list-item-text">
                        <span>{{ slotProps.option.alias }}</span>
                    </div>
                </div>
            </template>
        </Listbox>
    </div>

    <KnCalculatedField
        v-if="calcFieldDialogVisible"
        v-model:template="selectedCalcField"
        v-model:visibility="calcFieldDialogVisible"
        :fields="calcFieldColumns"
        :descriptor="calcFieldDescriptor"
        :propCalcFieldFunctions="calcFieldDescriptor.availableFunctions"
        :readOnly="false"
        :valid="true"
        source="dashboard"
        @save="onCalcFieldSave"
        @cancel="calcFieldDialogVisible = false"
    >
    </KnCalculatedField>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDataset, IDatasetColumn, IDataset, IWidget } from '../../../../Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from './WidgetEditorDataListDescriptor.json'
import Dropdown from 'primevue/dropdown'
import mainStore from '../../../../../../../App.store'
import Listbox from 'primevue/listbox'
import Card from 'primevue/card'
import dataListDescriptor from '../../../../dataset/DatasetEditorDataTab/DatasetEditorDataList/DatasetEditorDataListDescriptor.json'
import KnCalculatedField from '@/components/functionalities/KnCalculatedField/KnCalculatedField.vue'
import calcFieldDescriptor from './WidgetEditorCalcFieldDescriptor.json'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'widget-editor-data-list',
    components: { Card, Dropdown, Listbox, KnCalculatedField },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array }, selectedDatasets: { type: Array as PropType<IDataset[]> } },
    emits: ['datasetSelected'],
    data() {
        return {
            descriptor,
            dataListDescriptor,
            model: null as IWidget | null,
            datasetOptions: [] as IDashboardDataset[],
            selectedDataset: null as IDashboardDataset | null,
            selectedDatasetColumns: [] as IDatasetColumn[],
            calcFieldDescriptor,
            calcFieldDialogVisible: false,
            calcFieldColumns: [] as any,
            selectedCalcField: null as any,
            calcFieldFunctionsToShow: [] as any
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.setEventListeners()
        this.loadDatasets()
        this.loadModel()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('editCalculatedField', this.editCalcField)
        },
        removeEventListeners() {
            emitter.off('editCalculatedField', this.editCalcField)
        },
        loadDatasets() {
            this.datasetOptions = []
            this.selectedDatasets?.forEach((dataset: IDataset) => {
                if ((this.widgetModel.type !== 'discovery' && dataset.type !== 'SbiSolrDataSet') || (this.widgetModel.type === 'discovery' && dataset.type === 'SbiSolrDataSet')) {
                    this.datasetOptions.push({
                        id: dataset.id.dsId,
                        label: dataset.label,
                        cache: dataset.cache ?? false,
                        indexes: dataset.indexes,
                        parameters: dataset.parameters
                    })
                }
            })

            if (this.datasetOptions.length === 1) {
                this.selectedDataset = this.datasetOptions[0]
                this.onDatasetSelected()
            }
        },
        loadModel() {
            this.model = this.widgetModel
            this.loadSelectedDataset()
            this.loadDatasetColumns()
        },
        loadSelectedDataset() {
            const index = this.datasetOptions?.findIndex((dataset: IDashboardDataset) => dataset.id === this.model?.dataset)
            if (index !== -1) {
                this.selectedDataset = this.datasetOptions[index]
                this.$emit('datasetSelected', this.selectedDataset)
            }
        },
        onDatasetSelected() {
            this.loadDatasetColumns()
            this.removeSelectedColumnsFromModel()
            this.widgetModel.dataset = this.selectedDataset ? this.selectedDataset.id : null
            this.$emit('datasetSelected', this.selectedDataset)
            emitter.emit('clearWidgetData', this.widgetModel.id)
        },
        removeSelectedColumnsFromModel() {
            if (!this.model?.columns) return
            for (let i = 0; i < this.model.columns.length; i++) {
                emitter.emit('columnRemoved', this.model.columns[i])
                // if (this.widgetModel.type === 'discovery') removeColumnFromDiscoveryWidgetModel(this.widgetModel, this.model.columns[i])
            }
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
            this.model.columns = []
        },
        loadDatasetColumns() {
            this.selectedDatasetColumns = []
            if (!this.selectedDatasets || this.selectedDatasets.length === 0) return

            const index = this.selectedDatasets.findIndex((dataset: any) => dataset.id?.dsId === this.selectedDataset?.id)
            if (index !== -1) this.addSelectedDatasetColumnsFromMetadata(this.selectedDatasets[index].metadata.fieldsMeta)
        },
        addSelectedDatasetColumnsFromMetadata(fieldsMeta: any[]) {
            for (let i = 0; i < fieldsMeta.length; i++) {
                if (this.widgetModel.type !== 'selector' || fieldsMeta[i].fieldType === 'ATTRIBUTE') this.selectedDatasetColumns.push({ ...fieldsMeta[i], dataset: this.selectedDataset?.id })
            }
        },
        onDragStart(event: any, datasetColumn: IDatasetColumn) {
            event.dataTransfer.setData('text/plain', JSON.stringify(datasetColumn))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        createNewCalcField() {
            this.createCalcFieldColumns()
            this.selectedCalcField = { alias: '', expression: '', format: undefined, nature: 'ATTRIBUTE', type: 'STRING' } as any
            this.calcFieldDialogVisible = true
        },
        editCalcField(calcField) {
            this.createCalcFieldColumns()
            this.selectedCalcField = calcField
            this.calcFieldDialogVisible = true
        },
        createCalcFieldColumns() {
            this.calcFieldColumns = []
            this.model?.columns.forEach((field) => {
                if (field.fieldType === 'MEASURE') this.calcFieldColumns.push({ fieldAlias: `$F{${field.alias}}`, fieldLabel: field.alias })
            })
        },
        onCalcFieldSave(calcFieldOutput) {
            if (this.selectedCalcField.id) {
                this.selectedCalcField.alias = calcFieldOutput.colName
                this.selectedCalcField.formula = calcFieldOutput.formula
            } else {
                emitter.emit('addNewCalculatedField', {
                    id: cryptoRandomString({ length: 16, type: 'base64' }),
                    columnName: calcFieldOutput.colName,
                    alias: calcFieldOutput.colName,
                    type: 'java.lang.Double',
                    fieldType: 'MEASURE',
                    filter: {},
                    formula: calcFieldOutput.formula,
                    formulaEditor: calcFieldOutput.formula,
                    aggregation: 'NONE'
                })
            }

            this.calcFieldDialogVisible = false
        }
    }
})
</script>
