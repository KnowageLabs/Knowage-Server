<template>
    <Card id="dataset-card">
        <template #content>
            <DataTable
                id="dataset-datatable"
                :value="datasets"
                :paginator="true"
                :rows="15"
                :loading="loading"
                class="p-datatable-sm kn-table kn-small-paginator"
                dataKey="id"
                v-model:filters="filters"
                filterDisplay="menu"
                :globalFilterFields="functionsCatalogDatasetTableDescriptor.globalFilterFields"
                :responsiveLayout="functionsCatalogDatasetTableDescriptor.responsiveLayout"
                :breakpoint="functionsCatalogDatasetTableDescriptor.breakpoint"
                pageLinkSize="2"
                scrollable="true"
                :scrollHeight="functionsCatalogDatasetTableDescriptor.scrollHeight"
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
                            <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                        </span>
                    </div>
                </template>
                <Column class="kn-truncated" :style="col.style" v-for="col of functionsCatalogDatasetTableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true">
                    <template #filter="{filterModel}">
                        <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
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
