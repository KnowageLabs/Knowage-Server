<template>
    {{ selectedValues }}
    <DataTable
        class="p-datatable-sm kn-table p-m-2"
        :paginator="true"
        :rows="10"
        :value="rows"
        :loading="loading"
        v-model:filters="filters"
        v-model:selection="selectedValues"
        :selectionMode="['CONTAINS', 'NOT CONTAINS', 'IN', 'NOT IN'].includes(filterOperator) ? false : 'single'"
        filterDisplay="menu"
        responsiveLayout="stack"
        breakpoint="960px"
        @rowSelect="onSelect"
        @rowUnselect="onSelect"
        @rowSelectAll="onSelect"
        @rowUnselectAll="onSelect"
    >
        <template #empty>
            <div>
                {{ $t('common.info.noDataFound') }}
            </div>
        </template>
        <Column :selectionMode="['CONTAINS', 'NOT CONTAINS', 'IN', 'NOT IN'].includes(filterOperator) ? 'multiple' : 'single'" :headerStyle="QBEFilterDialogDescriptor.selectionColumnHeaderStyle"></Column>
        <Column v-for="column in columns" :key="column.header" :field="column.dataIndex" :header="column.header" :sortable="true">
            <template #filter="{filterModel}">
                <InputText v-model="filterModel.value" class="p-column-filter"></InputText>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { FilterOperator } from 'primevue/api'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import QBEFilterDialogDescriptor from './QBEFilterDialogDescriptor.json'

export default defineComponent({
    name: 'qbe-filter-values-table',
    components: { Column, DataTable },
    props: { filterValuesData: { type: Object }, loading: { type: Boolean }, loadedSelectedValues: { type: Array }, filterOperator: { type: String, required: true } },
    emits: ['selected'],
    data() {
        return {
            QBEFilterDialogDescriptor,
            columns: [] as { dataIndex: string; header: string; multiValue: boolean; name: string; type: string }[],
            rows: [],
            filters: {},
            selectedValues: [] as any[]
        }
    },
    watch: {
        filterValuesData() {
            this.loadData()
        },
        filterOperator() {
            this.selectedValues = []
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            if (this.filterValuesData) {
                this.columns = this.filterValuesData.metaData.fields.slice(1)
                this.rows = this.filterValuesData.rows

                this.columns.forEach((column: any) => (this.filters[column.dataIndex] = { operator: FilterOperator.AND, constraints: [filterDefault] }))

                this.selectedValues = []
                this.loadedSelectedValues?.forEach((el: any) => this.selectedValues.push({ column_1: '' + el }))
            }
        },
        onSelect() {
            let tempSelectedValues = [] as string[]
            if (['CONTAINS', 'NOT CONTAINS', 'IN', 'NOT IN'].includes(this.filterOperator)) {
                tempSelectedValues = this.selectedValues.map((value: any) => '' + value.column_1)
            } else {
                tempSelectedValues = []
                Object.keys(this.selectedValues).forEach((key: string) => {
                    tempSelectedValues.push('' + this.selectedValues[key])
                })
            }

            this.$emit('selected', tempSelectedValues)
        }
    }
})
</script>
