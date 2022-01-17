<template>
    <DataTable class="p-datatable-sm kn-table p-m-2" :value="rows" editMode="cell" responsiveLayout="stack" breakpoint="960px">
        <Column v-for="column in QBESimpleTableDescriptor.columns" :key="column.header" :header="$t(column.header)" :field="column.field">
            <template #editor="slotProps">
                <div class="p-d-flex p-flex-row">
                    <InputText v-if="column.field === 'alias'" class="kn-material-input p-inputtext-sm" v-model="slotProps.data[slotProps.column.props.field]"></InputText>
                    <Checkbox v-else-if="['visible', 'inUse'].includes(column.field)" v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
                    <Checkbox v-else-if="column.field === 'group'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="slotProps.data['funct'] = 'NONE'"></Checkbox>
                    <Dropdown v-else-if="column.field === 'order'" v-model="slotProps.data[slotProps.column.props.field]" :options="QBESimpleTableDescriptor.orderingOptions" />
                    <Dropdown v-else-if="column.field === 'funct'" v-model="slotProps.data[slotProps.column.props.field]" :options="getAttributeOptions(slotProps.data)" :disabled="slotProps.data['group']" />
                    <span v-else>{{ slotProps.data[slotProps.column.props.field] }}</span>
                    <i v-if="['alias', 'order', 'funct'].includes(column.field)" class="pi pi-pencil p-ml-2" />
                </div>
            </template>
            <template #body="slotProps">
                <div class="p-d-flex p-flex-row">
                    <Checkbox v-if="['visible', 'inUse'].includes(column.field)" v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
                    <Checkbox v-else-if="column.field === 'group'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="slotProps.data['funct'] = 'NONE'"></Checkbox>
                    <Dropdown v-else-if="column.field === 'funct'" v-model="slotProps.data[slotProps.column.props.field]" :options="getAttributeOptions(slotProps.data)" :disabled="slotProps.data['group']" />
                    <span v-else>{{ slotProps.data[slotProps.column.props.field] }}</span>
                    <i v-if="['alias', 'order', 'funct'].includes(column.field)" class="pi pi-pencil p-ml-2" />
                </div>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iQuery, iField } from '../../QBE'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import QBESimpleTableDescriptor from './QBESimpleTableDescriptor.json'

export default defineComponent({
    name: 'qbe-simple-table',
    props: { query: { type: Object as PropType<iQuery> } },
    components: { Checkbox, Column, DataTable, Dropdown },
    data() {
        return {
            QBESimpleTableDescriptor,
            rows: [] as iField[]
        }
    },
    watch: {
        queryResult() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            console.log('QBE  QUERY INSIDE TABLE: ', this.query)
            if (!this.query) return

            this.rows = this.query.fields as iField[]
            console.log('LOADED ROWS: ', this.rows)
        },
        getAttributeOptions(row: iField) {
            return row.fieldType === 'attribute' ? this.QBESimpleTableDescriptor.attributeAggregationOptions : this.QBESimpleTableDescriptor.aggregationOptions
        }
    }
})
</script>
