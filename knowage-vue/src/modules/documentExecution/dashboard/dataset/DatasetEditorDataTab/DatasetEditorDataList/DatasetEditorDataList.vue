<template>
    <div id="dataset-editor-list-card-container">
        <Card class="dataset-editor-list-card">
            <template #title>
                <Button label="Add Dataset" icon="pi pi-plus-circle" class="p-button-outlined p-mt-2 p-mr-2" @click="toggleDataDialog"></Button>
            </template>
            <template #content>
                <Listbox class="kn-list kn-list-no-border-right" :options="selectedDatasets" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="label" filterMatchMode="contains" :filterFields="['label']" :emptyFilterMessage="$t('common.info.noDataFound')" @change="selectDataset">
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" :style="dataListDescriptor.style.list.listItem">
                            <div class="kn-list-item-icon p-mx-2">
                                <i :style="dataListDescriptor.style.list.listIcon" :class="dataListDescriptor.listboxSettings.avatar.values[slotProps.option.type].icon"></i>
                            </div>
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.label }}</span>
                            </div>
                            <div class="kn-list-item-buttons">
                                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteDatasetFromModel" />
                            </div>
                        </div>
                    </template>
                </Listbox>
            </template>
        </Card>

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
    emits: ['datasetSelected', 'addSelectedDatasets'],
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
        console.log('availableDatasets', this.availableDatasetsProp)
        console.log('dashboardDatasets', this.dashboardDatasetsProp)
        console.log('selectedDatasets', this.selectedDatasets)
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
            console.log(datasetToDelete)
        },
        selectDataset(event) {
            this.$emit('datasetSelected', event.value.id.dsId)
        }
    }
})
</script>

<style lang="scss">
.dataset-editor-list-card .p-card-title {
    display: flex;
    justify-content: end;
}
.dataset-editor-list-card .p-card-body,
.dataset-editor-list-card .p-card-content {
    padding: 0;
}
</style>
