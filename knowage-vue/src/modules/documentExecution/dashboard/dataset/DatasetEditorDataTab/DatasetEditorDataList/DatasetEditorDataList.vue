<template>
    <div class="dashboard-editor-list-card-container p-m-3">
        <div class="dashboard-editor-list-card">
            <Button label="Add Dataset" icon="pi pi-plus-circle" class="p-button-outlined p-mt-2 p-mx-2" @click="toggleDataDialog" data-test="add-dataset-button"></Button>
            <Listbox
                class="kn-list kn-list-no-border-right dashboard-editor-list"
                :options="selectedDatasets"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                optionLabel="label"
                filterMatchMode="contains"
                :filterFields="['label']"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="selectDataset"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" :style="dataListDescriptor.style.list.listItem" v-tooltip.left="slotProps.option.label" data-test="dataset-list-item">
                        <i class="p-mx-2" :style="dataListDescriptor.style.list.listIcon" :class="dataListDescriptor.listboxSettings.avatar.values[slotProps.option.type].icon"></i>
                        <span class="kn-list-item-text">{{ slotProps.option.label }}</span>
                        <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain p-ml-auto" @click.stop="deleteDatasetFromModel(slotProps.option)" data-test="delete-dataset-list-item" />
                    </div>
                </template>
            </Listbox>
        </div>

        <DataDialog v-if="dataDialogVisible" :visible="dataDialogVisible" :selectedDatasetsProp="selectedDatasets" :availableDatasetsProp="availableDatasetsProp" @addSelectedDatasets="addSelectedDatasets" @close="toggleDataDialog" data-test="dataset-data-dialog" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import DataDialog from '../DatasetEditorDataDialog/DatasetEditorDataDialog.vue'
import dashStore from '../../../Dashboard.store'
import dataListDescriptor from './DatasetEditorDataListDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-data-list',
    components: { Card, Listbox, DataDialog },
    props: { dashboardDatasetsProp: { required: true, type: Array as any }, availableDatasetsProp: { required: true, type: Array as any }, selectedDatasetsProp: { required: true, type: Array as any } },
    emits: ['datasetSelected', 'addSelectedDatasets', 'deleteDataset'],
    data() {
        return {
            dataListDescriptor,
            selectedDatasets: [] as any,
            dataDialogVisible: false
        }
    },
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    created() {
        this.selectedDatasets = this.selectedDatasetsProp
    },
    methods: {
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
