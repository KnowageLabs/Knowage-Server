<template>
    <div>
        <div class="p-fluid p-field">
            <div class="p-d-flex">
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.selectDataset') }} </label>
            </div>
            <Dropdown class="kn-material-input" v-model="selectedDataset" :options="datasetOptions" optionLabel="label" @change="onDatasetSelected"></Dropdown>
        </div>

        <div class="p-d-flex p-jc-around">
            <label class="kn-material-input-label"> {{ $t('common.columns') }} </label>
            <Button class="kn-button kn-button--primary" @click="showCalculatedFieldDialog"> {{ $t('common.addColumn') }}</Button>
        </div>

        <div>
            <Listbox v-if="selectedDataset" class="kn-list--column" :options="selectedDatasetColumns" :filter="true" :filterPlaceholder="$t('common.search')" filterMatchMode="contains" :filterFields="[]" :emptyFilterMessage="$t('common.info.noDataFound')">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item kn-draggable" draggable="true" @dragstart="onDragStart($event, slotProps.option)">
                        <i class="pi pi-bars"></i>
                        <i :class="slotProps.option.fieldType === 'ATTRIBUTE' ? 'fas fa-font' : 'fas fa-hashtag'" class="p-ml-2"></i>
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.alias }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IWidgetEditorDataset, IDatasetColumn } from '../../../../Dashboard'
import descriptor from './WidgetEditorDataListDescriptor.json'
import Dropdown from 'primevue/dropdown'
import mainStore from '../../../../../../../App.store'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'widget-editor-data-list',
    components: { Dropdown, Listbox },
    props: { datasets: { type: Array }, modelDatasets: { type: Array } },
    emits: ['datasetSelected'],
    data() {
        return {
            descriptor,
            datasetOptions: [
                {
                    id: 1,
                    label: 'dew',
                    cache: true,
                    parameters: []
                }
            ] as IWidgetEditorDataset[],
            selectedDataset: null as IWidgetEditorDataset | null,
            selectedDatasetColumns: [] as IDatasetColumn[]
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    async created() {
        this.loadDatasets()
    },
    methods: {
        loadDatasets() {
            // TODO - remove mocked
            const mockedDatasets = [
                { dsId: 165, name: 'AirBnB-NY 1', dsLabel: 'AirBnB-NY 1 - GIS Working', useCache: true, frequency: 0, parameters: {} },
                { dsId: 166, name: 'AirBnB-NY 1', dsLabel: 'test', useCache: true, frequency: 0, parameters: {} }
            ]

            this.datasetOptions = mockedDatasets.map((dataset: any) => {
                return {
                    id: dataset.dsId,
                    label: dataset.dsLabel,
                    cache: dataset.useCache,
                    parameters: dataset.parameters
                }
            })
        },
        onDatasetSelected() {
            this.loadDatasetColumns()
            this.$emit('datasetSelected', this.selectedDataset)
            console.log('onDatasetSelected() - allDatasets: ', this.datasets)
        },
        showCalculatedFieldDialog() {
            console.log('showCalculatedFieldDialog() - TODO!')
        },
        loadDatasetColumns() {
            this.selectedDatasetColumns = []
            if (!this.datasets || this.datasets.length === 0) return

            const index = this.datasets.findIndex((dataset: any) => dataset.id?.dsId === this.selectedDataset?.id)
            if (index !== -1) this.selectedDatasetColumns = (this.datasets[index] as any).metadata.fieldsMeta
            console.log('loadDatasetColumns() - selectedDatasetColumns: ', this.selectedDatasetColumns)
        },
        onDragStart(event: any, datasetColumn: IDatasetColumn) {
            event.dataTransfer.setData('text/plain', JSON.stringify(datasetColumn))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        }
    }
})
</script>
