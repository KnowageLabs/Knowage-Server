<template>
    <Dialog class="kn-dialog--toolbar--primary" :visible="visible" :header="$t('dashboard.datasetEditor.selectDatasets')" :style="dataDialogDescriptor.style.datasetListDialog" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <DataTable
            id="datasets-datatable"
            v-model:selection="selectedDatasets"
            class="p-datatable-sm kn-table kn-page-content"
            data-key="id.dsId"
            :value="filteredDatasets"
            :paginator="true"
            :rows="dataDialogDescriptor.rows"
            :breakpoint="dataDialogDescriptor.breakpoint"
            :loading="loading"
            responsive-layout="stack"
        >
            <template #loading>
                {{ $t('common.info.dataLoading') }}
            </template>
            <template #empty>
                <div v-if="!loading" id="noDatasetsFound">
                    {{ $t('managers.advancedData.noDatasetsFound') }}
                </div>
            </template>
            <template #header>
                <div class="table-header p-d-flex">
                    <span class="p-input-icon-left p-mr-3 p-col-12">
                        <i class="pi pi-search" />
                        <InputText v-model="searchWord" class="kn-material-input" type="text" :placeholder="$t('common.search')" @input="searchDatasets" />
                    </span>
                </div>
            </template>
            <Column selection-mode="multiple" />
            <Column v-for="col of dataDialogDescriptor.columns" :key="col.field" class="kn-truncated" :style="col.style" :header="$t(col.header)" :sort-field="col.field" :sortable="true">
                <template #body="slotProps">
                    <span v-if="col.field == 'type'" v-tooltip.top="slotProps.data[col.field]"> {{ dataDialogDescriptor.datasetTypes[slotProps.data[col.field]] }} </span>
                    <span v-else v-tooltip.top="slotProps.data[col.field]"> {{ slotProps.data[col.field] }}</span>
                </template>
            </Column>
        </DataTable>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="$emit('close')" />
            <Button v-t="'common.add'" class="kn-button kn-button--primary" @click="addSelectedDatasets" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import dataDialogDescriptor from './MapWidgetLayersTabDescriptor.json'
import dashStore from '../../../Dashboard.store'
import { IDataset } from '../../../Dashboard'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Column, DataTable, Dialog },
    props: { visible: { required: true, type: Boolean }, availableDatasetsProp: { required: true, type: Array as PropType<IDataset[]> }, selectedDatasetsProp: { required: true, type: Array as any } },
    emits: ['close', 'addSelectedDatasets'],
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    data() {
        return {
            dataDialogDescriptor,
            datasets: [] as any[],
            filteredDatasets: [] as IDataset[],
            selectedDatasets: [] as any,
            searchWord: '',
            loading: false
        }
    },
    watch: {
        availableDatasetsProp() {
            this.setDatasetList()
        }
    },
    async created() {
        await this.setDatasetList()
        console.log(this.datasets)
    },
    updated() {
        this.filteredDatasets = [...this.datasets]
    },
    methods: {
        async setDatasetList() {
            this.datasets = this.filterOutSelectedDatasets(this.selectedDatasetsProp, this.availableDatasetsProp)
            this.filteredDatasets = [...this.datasets]
        },
        filterOutSelectedDatasets(selectedDatasets, allDatasets) {
            return allDatasets.filter((responseDataset) => {
                return !selectedDatasets?.find((selectedDataset) => {
                    return responseDataset.id.dsId === selectedDataset.dsId
                })
            })
        },
        searchDatasets() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredDatasets = [...this.datasets] as any[]
                } else {
                    this.filteredDatasets = this.datasets.filter((tempDataset: any) => {
                        return tempDataset.name.toLowerCase().includes(this.searchWord.toLowerCase())
                    })
                }
            }, 250)
        },
        addSelectedDatasets() {
            // this.$emit('addSelectedDatasets', this.selectedDatasets)
        }
    }
})
</script>

<style lang="scss">
#noDatasetsFound {
    margin: 0 auto;
    border: 1px solid rgba(204, 204, 204, 0.6);
    padding: 0.5rem;
    background-color: #e6e6e6;
    text-align: center;
    text-transform: uppercase;
    font-size: 0.8rem;
    width: 80%;
}

#datasets-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
