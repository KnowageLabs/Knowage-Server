<template>
    <DataTable
        class="p-datatable-sm kn-table p-m-2"
        :value="inputColumns"
        edit-mode="cell"
        :data-key="functionsCatalogDatasetFormColumnsTableDescriptor.dataKey"
        :responsive-layout="functionsCatalogDatasetFormColumnsTableDescriptor.responsiveLayout"
        :breakpoint="functionsCatalogDatasetFormColumnsTableDescriptor.breakpoint"
        @cell-edit-complete="onCellEditComplete"
    >
        <Column class="kn-truncated" field="name" :header="$t('managers.functionsCatalog.inputColumnName')"> </Column>
        <Column class="kn-truncated" field="type" :header="$t('common.type')">
            <template #body="slotProps">
                <i :class="getIconClass(slotProps.data.type)"></i>
                {{ slotProps.data.type }}
            </template>
        </Column>
        <Column :header="$t('managers.functionsCatalog.datasetColumn')">
            <template #editor="slotProps">
                <div class="p-d-flex p-flex-row p-ai-center">
                    <Dropdown v-model="slotProps.data['dsColumn']" :style="functionsCatalogDatasetFormColumnsTableDescriptor.dropdownStyle" class="p-mr-2 kn-flex" :options="datasetColumns" />
                    <i class="pi pi-pencil edit-icon kn-flex" />
                </div>
            </template>
            <template #body="slotProps">
                <span class="p-mr-2">{{ slotProps.data['dsColumn'] }}</span>
                <i class="pi pi-pencil edit-icon" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iInputColumn } from '../../../../FunctionsCatalog'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import functionsCatalogDatasetFormColumnsTableDescriptor from './FunctionsCatalogDatasetFormColumnsTableDescriptor.json'

export default defineComponent({
    name: 'function-catalog-dateset-form-columns-table',
    components: { Column, DataTable, Dropdown },
    props: { columns: { type: Array }, datasetColumns: { type: Array } },
    data() {
        return {
            functionsCatalogDatasetFormColumnsTableDescriptor,
            inputColumns: [] as iInputColumn[]
        }
    },
    watch: {
        columns() {
            this.loadInputColumns()
        }
    },
    created() {
        this.loadInputColumns()
    },
    methods: {
        loadInputColumns() {
            this.inputColumns = this.columns as iInputColumn[]
        },
        getIconClass(type: string) {
            switch (type) {
                case 'NUMBER':
                    return 'fa fa-hashtag'
                case 'STRING':
                    return 'fa fa-quote-right'
                case 'DATE':
                    return 'fa fa-calendar'
                default:
                    return ''
            }
        },
        onCellEditComplete(event: any) {
            this.inputColumns[event.index] = event.newData
        }
    }
})
</script>
