<template>
    <div class="dataset-editor-list-card-container p-m-2">
        <div class="dataset-editor-list-card">
            <Button label="Add Dataset" icon="pi pi-plus-circle" class="p-button-outlined p-mt-2 p-mx-2" @click="toggleDataDialog"></Button>
            <Listbox
                class="kn-list kn-list-no-border-right dataset-editor-list"
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
                    <div class="kn-list-item" :style="dataListDescriptor.style.list.listItem" v-tooltip.left="slotProps.option.label">
                        <i class="p-mx-2" :style="dataListDescriptor.style.list.listIcon" :class="dataListDescriptor.listboxSettings.avatar.values[slotProps.option.type].icon"></i>
                        <span class="kn-list-item-text">{{ slotProps.option.label }}</span>
                        <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain p-ml-auto" @click.stop="deleteDatasetFromModel(slotProps.option)" />
                    </div>
                </template>
            </Listbox>
        </div>

        <DataDialog v-if="dataDialogVisible" :visible="dataDialogVisible" :selectedDatasetsProp="selectedDatasets" :availableDatasetsProp="availableDatasetsProp" @addSelectedDatasets="addSelectedDatasets" @close="toggleDataDialog" />
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

<style lang="scss">
.dataset-editor-list-card-container {
    display: flex;
    width: 300px;
    background: #ffffff;
    color: rgba(0, 0, 0, 0.87);
    box-shadow: 0 2px 1px -1px rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%), 0 1px 3px 0 rgb(0 0 0 / 12%);
    border-radius: 4px;
    .dataset-editor-list-card,
    .dataset-editor-list {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-height: 0;
        border-radius: 4px !important;
        .kn-list-item-text {
            text-overflow: ellipsis;
            max-width: 190px;
            overflow: hidden;
            white-space: nowrap;
        }
    }
}
</style>
