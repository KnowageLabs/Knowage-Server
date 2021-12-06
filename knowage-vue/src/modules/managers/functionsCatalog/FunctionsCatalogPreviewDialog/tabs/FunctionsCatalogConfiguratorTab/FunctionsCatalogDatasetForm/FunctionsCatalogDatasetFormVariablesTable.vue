<template>
    <DataTable
        class="p-datatable-sm kn-table p-m-2"
        :value="inputVariables"
        :editMode="functionsCatalogDatasetFormVariablesTableDescriptor.editMode"
        :dataKey="functionsCatalogDatasetFormVariablesTableDescriptor.dataKey"
        :responsiveLayout="functionsCatalogDatasetFormVariablesTableDescriptor.responsiveLayout"
        :breakpoint="functionsCatalogDatasetFormVariablesTableDescriptor.breakpoint"
    >
        <Column class="kn-truncated" field="name" :header="$t('managers.functionsCatalog.variableName')"> </Column>
        <Column class="kn-truncated" field="type" :header="$t('common.type')">
            <template #body="slotProps">
                <i :class="getIconClass(slotProps.data.type)"></i>
                {{ slotProps.data.type }}
            </template>
        </Column>
        <Column :header="$t('common.value')">
            <template #editor="slotProps">
                <div class="p-d-flex p-flex-row p-ai-center">
                    <InputText v-if="slotProps.data.type !== 'DATE'" :style="functionsCatalogDatasetFormVariablesTableDescriptor.inputStyle" class="p-mr-2  kn-flex" v-model="slotProps.data['value']" :type="slotProps.data.type === 'NUMBER' ? 'number' : 'text'" />
                    <Calendar v-else v-model="slotProps.data['value']" class="kn-flex"></Calendar>
                    <i class="pi pi-pencil edit-icon kn-flex" />
                </div>
            </template>
            <template #body="slotProps">
                <span class="p-mr-2">{{ slotProps.data.type === 'DATE' && slotProps.data.value ? getFormatedDate(slotProps.data['value']) : slotProps.data['value'] }}</span>
                <i class="pi pi-pencil edit-icon" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { formatDate } from '@/helpers/commons/localeHelper'
import { iInputVariable } from '../../../../FunctionsCatalog'
import Calendar from 'primevue/calendar'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import functionsCatalogDatasetFormVariablesTableDescriptor from './FunctionsCatalogDatasetFormVariablesTableDescriptor.json'

export default defineComponent({
    name: 'function-catalog-dateset-form-variables-table',
    components: { Calendar, Column, DataTable },
    props: { variables: { type: Array } },
    data() {
        return { functionsCatalogDatasetFormVariablesTableDescriptor, inputVariables: [] as iInputVariable[] }
    },
    watch: {
        propinputColumns() {
            this.loadinputColumns()
        }
    },
    created() {
        this.loadinputColumns()
    },
    methods: {
        loadinputColumns() {
            this.inputVariables = this.variables as iInputVariable[]
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
        getFormatedDate(date: any) {
            return formatDate(date, 'MM/DD/YYYY')
        }
    }
})
</script>
