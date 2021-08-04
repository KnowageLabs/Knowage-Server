<template>
    <DataTable :value="data" class="p-datatable-sm kn-table" dataKey="field" v-model:filters="filters" :globalFilterFields="lovsManagementSimpleDatatableDescriptor.globalFilterFields" responsiveLayout="stack" breakpoint="960px" @page="onPage($event)">
        <template #header>
            <div class="table-header">
                <span class="p-input-icon-left">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                </span>
            </div>
        </template>
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <Column class="kn-truncated" field="name" :header="$t('common.name')"> </Column>
        <Column :header="$t('managers.lovsManagement.value')">
            <template #body="slotProps">
                {{ slotProps.data }}
                <RadioButton v-model="selectedValue" name="value" :value="slotProps.data.name" @change="setSelectedValue" />
            </template>
        </Column>
        <Column :header="$t('common.description')">
            <template #body="slotProps">
                {{ slotProps.data }}
                <RadioButton v-model="selectedDescription" name="description" :value="slotProps.data.name" @change="setSelectedDescription" />
            </template>
        </Column>
        <Column :header="$t('managers.lovsManagement.visible')">
            <template #body="slotProps">
                {{ slotProps.data }}
                <Checkbox v-model="selectedVisible" name="visible" :value="slotProps.data.name" @change="setSelectedVisible" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import lovsManagementSimpleDatatableDescriptor from './LovsManagementSimpleDatatableDescriptor.json'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'lovs-management-simple-datatable',
    components: { Checkbox, Column, DataTable, RadioButton },
    props: {
        tableData: {
            type: Object
        },
        treeListTypeModel: {
            type: Object,
            required: true
        }
    },
    data() {
        return {
            lovsManagementSimpleDatatableDescriptor,
            filters: { global: [filterDefault] } as Object,
            data: {} as any,
            model: {} as any,
            selectedValue: null,
            selectedDescription: null,
            selectedVisible: [] as any[],
            visibleOptions: [] as any[]
        }
    },
    created() {
        this.loadData()
        this.loadSelectedModel()
        this.loadModel()
    },
    methods: {
        loadData() {
            this.data = this.tableData as any
            this.visibleOptions = []
            if (this.data && this.data.length > 0) {
                this.data.forEach((el: any) => this.visibleOptions.push(el.name))
            }
            console.log('DATA FOR TABLE', this.data)
        },
        loadModel() {
            this.model = this.treeListTypeModel as any
            console.log('MODEL FOR TABLE', this.model)
        },
        loadSelectedModel() {
            console.log('SELECTED MODELS', this.treeListTypeModel)
            console.log('VALUE COLUMN', this.treeListTypeModel['VALUE-COLUMN'])
            this.selectedValue = this.treeListTypeModel['VALUE-COLUMN'] as any
            this.selectedDescription = this.treeListTypeModel['DESCRIPTION-COLUMN'] as any

            this.selectedVisible = []
            this.treeListTypeModel['VISIBLE-COLUMNS'].split(',').forEach((el: any) => {
                if (el) {
                    this.selectedVisible.push(el)
                }
            })

            console.log('SELECTED VALUE: ', this.selectedValue)
            console.log('SELECTED DESCRIPTION: ', this.selectedDescription)
            console.log('SELECTED VISIBLE: ', this.selectedVisible)
        },
        setSelectedValue() {
            console.log('NEW SELECTED VALUE', this.selectedValue)
            this.model['VALUE-COLUMN'] = this.selectedValue
            console.log('NEW MODEL', this.model)
        },
        setSelectedDescription() {
            console.log('NEW SELECTED DESCRIPTION', this.selectedDescription)
            this.model['DESCRIPTION-COLUMN'] = this.selectedDescription
            console.log('NEW MODEL', this.model)
        },
        setSelectedVisible() {
            console.log('NEW SELECTED VISIBLE', this.selectedVisible)
            this.model['VISIBLE-COLUMNS'] = this.selectedVisible.toString()
            console.log('NEW MODEL', this.model)
        }
    }
})
</script>
