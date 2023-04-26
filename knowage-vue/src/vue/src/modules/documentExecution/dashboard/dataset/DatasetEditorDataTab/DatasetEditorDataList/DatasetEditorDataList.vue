<template>
    <div class="dashboard-editor-list-card-container p-m-3">
        <div class="dashboard-editor-list-card">
            <Button label="Add Dataset" icon="pi pi-plus-circle" class="p-button-outlined p-mt-2 p-mx-2" data-test="add-dataset-button" @click="toggleDataDialog"></Button>
            <Listbox
                class="kn-list kn-list-no-border-right dashboard-editor-list"
                :options="selectedDatasets"
                :filter="true"
                :filter-placeholder="$t('common.search')"
                option-label="label"
                filter-match-mode="contains"
                :filter-fields="['label']"
                :empty-filter-message="$t('common.info.noDataFound')"
                @change="selectDataset"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div v-tooltip.left="slotProps.option.label" class="kn-list-item" :style="dataListDescriptor.style.list.listItem" data-test="dataset-list-item">
                        <i class="p-mx-2" :style="dataListDescriptor.style.list.listIcon" :class="dataListDescriptor.listboxSettings.avatar.values[slotProps.option.type].icon"></i>
                        <span class="kn-list-item-text">{{ slotProps.option.label }}</span>
                        <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain p-ml-auto" data-test="delete-dataset-list-item" @click.stop="deleteDatasetFromModel(slotProps.option)" />
                    </div>
                </template>
            </Listbox>
        </div>

        <DataDialog v-if="dataDialogVisible" :visible="dataDialogVisible" :selected-datasets-prop="selectedDatasets" :available-datasets-prop="availableDatasetsProp" data-test="dataset-data-dialog" @addSelectedDatasets="addSelectedDatasets" @close="toggleDataDialog" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset } from '../../../Dashboard'
import Listbox from 'primevue/listbox'
import DataDialog from '../DatasetEditorDataDialog/DatasetEditorDataDialog.vue'
import dashStore from '../../../Dashboard.store'
import dataListDescriptor from './DatasetEditorDataListDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-data-list',
    components: { Listbox, DataDialog },
    props: { dashboardDatasetsProp: { required: true, type: Array as any }, availableDatasetsProp: { required: true, type: Array as PropType<IDataset[]> }, selectedDatasetsProp: { required: true, type: Array as any } },
    emits: ['datasetSelected', 'addSelectedDatasets', 'deleteDataset'],
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    data() {
        return {
            dataListDescriptor,
            selectedDatasets: [] as any,
            dataDialogVisible: false
        }
    },
    watch: {
        selectedDatasetsProp() {
            this.loadSelectedDatasets()
        }
    },
    created() {
        this.loadSelectedDatasets()
    },
    methods: {
        loadSelectedDatasets() {
            this.selectedDatasets = this.selectedDatasetsProp
        },
        toggleDataDialog() {
            this.dataDialogVisible = !this.dataDialogVisible
        },
        addSelectedDatasets(datasetsToAdd) {
            this.$emit('addSelectedDatasets', datasetsToAdd)
            this.dataDialogVisible = false
        },
        deleteDatasetFromModel(datasetToDelete) {
            this.$emit('deleteDataset', datasetToDelete)
        },
        selectDataset(event) {
            this.$emit('datasetSelected', event.value.id.dsId)
        }
    }
})
</script>
