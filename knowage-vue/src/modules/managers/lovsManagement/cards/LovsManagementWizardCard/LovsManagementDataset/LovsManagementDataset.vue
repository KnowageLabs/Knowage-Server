<template>
    <div class="p-fluid p-formgrid p-grid" v-if="selectedDataset">
        <span class="p-field p-col-6 p-float-label">
            <InputText id="datasetLabel" class="kn-material-input" type="text" v-model.trim="selectedDataset.label" disabled />
            <label for="datasetLabel" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
        </span>
        <span class="p-field p-col-5 p-float-label">
            <InputText id="datasetName" class="kn-material-input" type="text" v-model.trim="selectedDataset.name" disabled />
            <label for="datasetName" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
        </span>
        <span class="p-field">
            <Button icon="pi pi-search-plus" class="p-button-text p-button-rounded p-button-plain" @click="showDatasetsDialog = true" />
        </span>
    </div>

    <Dialog contentStyle="height:100vh;width:100vw" :visible="showDatasetsDialog" :modal="true" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0 p-col">
                <template #left>Select Dataset</template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="onSave" data-test="submit-button" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="showDatasetsDialog = false" data-test="close-button" />
                </template>
            </Toolbar>
        </template>

        <DataTable
            :value="datasets"
            :paginator="true"
            class="p-datatable-sm kn-table p-ml-2 p-mr-2"
            dataKey="id"
            v-model:filters="filters"
            filterDisplay="menu"
            :globalFilterFields="lovsManagementDatasetDescriptor.globalFilterFields"
            :rows="20"
            responsiveLayout="stack"
            breakpoint="960px"
            :currentPageReportTemplate="
                $t('common.table.footer.paginated', {
                    first: '{first}',
                    last: '{last}',
                    totalRecords: '{totalRecords}'
                })
            "
            v-model:selection="selectedDataset"
            selectionMode="single"
        >
            <template #header>
                <div class="table-header">
                    <span class="p-input-icon-left">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                    </span>
                </div>
            </template>
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" :style="col.style" class="kn-truncated">
                <template #filter="{ filterModel }">
                    <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
                </template>
                <template #body="slotProps">
                    <span :title="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
                </template>
            </Column>
        </DataTable>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import lovsManagementDatasetDescriptor from './LovsManagementDatasetDescriptor.json'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'lovs-management-dataset',
    components: { Dialog, DataTable, Column },
    props: { dataset: Object },
    data() {
        return {
            lovsManagementDatasetDescriptor,
            dirty: false,
            showDatasetsDialog: false,
            datasets: [] as any,
            selectedDataset: {} as any,
            columns: lovsManagementDatasetDescriptor.columns,
            filters: {
                global: [filterDefault],
                label: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                description: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                owner: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                scope: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
        }
    },
    async created() {
        this.loadDataset()
        await this.loadDatasets()
    },
    watch: {
        dataset() {
            this.loadDataset()
        }
    },
    methods: {
        async loadDatasets() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/datasets/datasetsforlov/').then((response: AxiosResponse<any>) => (this.datasets = response.data))
        },
        loadDataset() {
            this.selectedDataset = { ...this.dataset }
        },
        onSave() {
            this.showDatasetsDialog = false
            this.$emit('selected', this.selectedDataset)
        }
    }
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
}
.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
}
</style>
