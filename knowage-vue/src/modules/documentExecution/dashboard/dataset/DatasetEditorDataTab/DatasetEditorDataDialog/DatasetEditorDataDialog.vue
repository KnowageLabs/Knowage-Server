<template>
    <Dialog class="kn-dialog--toolbar--primary datasetListDialogClass" :visible="visible" :header="$t('components.advancedData.title')" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <DataTable
            id="datasets-datatable"
            class="p-datatable-sm kn-table kn-page-content"
            :value="filteredDatasets"
            v-model:selection="selectedDatasets"
            :paginator="true"
            :rows="dataDialogDescriptor.rows"
            :loading="loading"
            dataKey="id.dsId"
            :responsiveLayout="dataDialogDescriptor.responsiveLayout"
            :breakpoint="dataDialogDescriptor.breakpoint"
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
                        <InputText class="kn-material-input" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchDatasets" />
                    </span>
                </div>
            </template>
            <Column selectionMode="multiple" />
            <Column class="kn-truncated" :style="col.style" v-for="col of dataDialogDescriptor.columns" :header="$t(col.header)" :key="col.field" :sortField="col.field" :sortable="true">
                <template #body="slotProps">
                    <span v-if="col.field == 'type'" v-tooltip.top="slotProps.data[col.field]"> {{ dataDialogDescriptor.datasetTypes[slotProps.data[col.field]] }} </span>
                    <span v-else v-tooltip.top="slotProps.data[col.field]"> {{ slotProps.data[col.field] }}</span>
                </template>
            </Column>
            <Column field="tags" :header="$t('importExport.gallery.column.tags')" :sortable="true">
                <template #body="slotProps">
                    <span v-if="slotProps.data.tags.length > 0">
                        <Chip v-for="(tag, index) of slotProps.data.tags" :key="index"> {{ tag.name }} </Chip>
                    </span>
                </template>
            </Column>
            <Column :header="$t('workspace.myData.parametrical')">
                <template #body="slotProps">
                    <i v-if="slotProps.data.parameters?.length > 0 || slotProps.data.drivers?.length > 0" class="fas fa-check p-button-link" />
                </template>
            </Column>
        </DataTable>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="$emit('close')" />
            <Button class="kn-button kn-button--primary" v-t="'common.add'" @click="formatSelectedDatasets" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import Chip from 'primevue/chip'
import dataDialogDescriptor from './DatasetEditorDataDialogDescriptor.json'
import dashStore from '../../../Dashboard.store'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Column, DataTable, Dialog, Chip },
    props: { dashboardDatasetsProp: { required: true, type: Array as any } },
    emits: ['close', 'addSelectedDatasets'],
    data() {
        return {
            dataDialogDescriptor,
            datasets: [] as any[],
            modelDatasets: [] as any[],
            filteredDatasets: [] as any[],
            selectedDatasets: [] as any,
            searchWord: '',
            loading: false
        }
    },
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    async created() {
        await this.getWidgetTypes()
    },
    updated() {
        this.filteredDatasets = [...this.datasets]
    },
    methods: {
        async getWidgetTypes() {
            //TODO: Which service to use? Because we need only basic info, we can use the fastest service. Maybe make new service like one for dataprep 3.0?
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/?asPagedList=true&seeTechnical=true`)
                .then((response: AxiosResponse<any>) => {
                    this.datasets = this.filterOutSelectedDatasets(this.dashboardDatasetsProp, response.data.item)
                    this.filteredDatasets = [...this.datasets]
                })
                .finally(() => (this.loading = false))
        },
        filterOutSelectedDatasets(selectedDatasets, allDatasets) {
            return allDatasets.filter((responseDataset) => {
                return !selectedDatasets.find((dashboardDataset) => {
                    return responseDataset.id.dsId === dashboardDataset.id
                })
            })
        },
        searchDatasets() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredDatasets = [...this.datasets] as any[]
                } else {
                    this.filteredDatasets = this.datasets.filter((tempDataset: any) => {
                        return tempDataset.label.toLowerCase().includes(this.searchWord.toLowerCase()) || tempDataset.name.toLowerCase().includes(this.searchWord.toLowerCase()) || tempDataset.type.toLowerCase().includes(this.searchWord.toLowerCase()) || this.datasetTagFound(tempDataset)
                    })
                }
            }, 250)
        },
        datasetTagFound(dataset: any) {
            let tagFound = false
            for (let i = 0; i < dataset.tags.length; i++) {
                const tempTag = dataset.tags[i]
                if (tempTag.name.toLowerCase().includes(this.searchWord.toLowerCase())) {
                    tagFound = true
                    break
                }
            }
            return tagFound
        },
        formatSelectedDatasets() {
            let formattedDatasets = this.selectedDatasets.map((dataset) => {
                return { id: dataset.id.dsId, label: dataset.label, parameters: dataset.parameters ?? [], type: dataset.type, cache: false }
            })
            this.$emit('addSelectedDatasets', formattedDatasets)
        }
    }
})
</script>

<style lang="scss">
.datasetListDialogClass {
    min-width: 600px;
    width: 60%;
    max-width: 1200px;
}

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
