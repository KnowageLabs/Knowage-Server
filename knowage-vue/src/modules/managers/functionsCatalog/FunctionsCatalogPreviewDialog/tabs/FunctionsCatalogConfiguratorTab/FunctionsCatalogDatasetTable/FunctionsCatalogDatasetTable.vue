<template>
    <Card id="dataset-card">
        <template #content>
            <DataTable
                id="dataset-datatable"
                v-model:selection="selectedDataset"
                v-model:filters="filters"
                :value="datasets"
                :paginator="true"
                :rows="15"
                class="p-datatable-sm kn-table kn-small-paginator"
                data-key="id"
                selection-mode="single"
                filter-display="menu"
                :global-filter-fields="functionsCatalogDatasetTableDescriptor.globalFilterFields"
                :responsive-layout="functionsCatalogDatasetTableDescriptor.responsiveLayout"
                :breakpoint="functionsCatalogDatasetTableDescriptor.breakpoint"
                page-link-size="2"
                scrollable="true"
                :scroll-height="functionsCatalogDatasetTableDescriptor.scrollHeight"
                @rowClick="$emit('selected', $event.data)"
            >
                <template #empty>
                    <div id="noFunctionsFound">
                        {{ $t('common.info.noDataFound') }}
                    </div>
                </template>
                <template #header>
                    <div class="table-header p-d-flex">
                        <span class="p-input-icon-left p-mr-3 p-col-12">
                            <i class="pi pi-search" />
                            <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                        </span>
                    </div>
                </template>
                <Column v-for="col of functionsCatalogDatasetTableDescriptor.columns" :key="col.field" class="kn-truncated" :style="col.style" :field="col.field" :header="$t(col.header)" :sortable="true">
                    <template #filter="{ filterModel }">
                        <InputText v-model="filterModel.value" type="text" class="p-column-filter"></InputText>
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import functionsCatalogDatasetTableDescriptor from './FunctionsCatalogDatasetTableDescriptor.json'

export default defineComponent({
    name: 'function-catalog-dateset-table',
    components: { Card, Column, DataTable },
    props: { datasets: { type: Array } },
    emits: ['selected'],
    data() {
        return {
            functionsCatalogDatasetTableDescriptor,
            selectedDataset: null,
            filters: {
                global: [filterDefault],
                label: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                dsType: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
        }
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss">
#dataset-datatable .p-datatable-wrapper {
    height: auto;
}

#dataset-datatable .p-paginator {
    justify-content: center;
}

#dataset-card .p-card-body {
    padding: 0;
}

#dataset-card .p-card-content {
    height: 60vh;
}
</style>
