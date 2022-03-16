<template>
    <Dialog class="kn-dialog--toolbar--primary datasetListDialogClass" v-bind:visible="visibility" :header="$t('components.advancedData.title')" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <DataTable
            id="datasets-datatable"
            :value="filteredDatasets"
            :selection="selectedDataset"
            selectionMode="single"
            :paginator="true"
            :rows="KnDatasetListDescriptor.rows"
            :loading="loading"
            class="p-datatable-sm kn-table kn-page-content"
            dataKey="id"
            :responsiveLayout="KnDatasetListDescriptor.responsiveLayout"
            :breakpoint="KnDatasetListDescriptor.breakpoint"
            @rowClick="$emit('selected', $event.data)"
        >
            <template #loading>
                {{ $t('common.info.dataLoading') }}
            </template>
            <template #empty>
                <div id="noDatasetsFound">
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
            <Column class="kn-truncated" :style="col.style" v-for="col of KnDatasetListDescriptor.columns" :header="$t(col.header)" :key="col.field" :sortField="col.field" :sortable="true">
                <template #body="slotProps">
                    <span v-tooltip.top="slotProps.data[col.field]"> {{ slotProps.data[col.field] }}</span>
                </template>
            </Column>
        </DataTable>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="cancel" />
            <Button class="kn-button kn-button--primary" v-t="'common.apply'" @click="apply" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDataset } from './KnDatasetList'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import KnDatasetListDescriptor from './KnDatasetListDescriptor.json'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Column, DataTable, Dialog },
    props: {
        items: [] as PropType<Array<iDataset>>,
        visibility: Boolean
    },
    emits: ['selected', 'save', 'cancel'],
    data() {
        return {
            KnDatasetListDescriptor,
            datasets: [] as iDataset[],
            filteredDatasets: [] as iDataset[],
            selectedDataset: {},
            searchWord: '',
            loading: false
        }
    },
    updated() {
        if (this.items) this.datasets = this.items
        this.filteredDatasets = [...this.datasets]
    },
    methods: {
        apply(): void {
            this.$emit('save', this.selectedDataset)
            this.clearForm()
        },
        cancel(): void {
            this.$emit('cancel', this.selectedDataset)
            this.clearForm()
        },
        clearForm(): void {
            this.selectedDataset = {}
        },
        searchDatasets() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredDatasets = [...this.datasets] as any[]
                } else {
                    this.filteredDatasets = this.datasets.filter((tempDataset: any) => {
                        return tempDataset.label.toLowerCase().includes(this.searchWord.toLowerCase()) || tempDataset.name.toLowerCase().includes(this.searchWord.toLowerCase()) || tempDataset.dsTypeCd.toLowerCase().includes(this.searchWord.toLowerCase())
                    })
                }
            }, 250)
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
