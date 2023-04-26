<template>
    <DataTable v-model:filters="filters" :value="data" class="p-datatable-sm kn-table" data-key="field" :global-filter-fields="lovsManagementSimpleDatatableDescriptor.globalFilterFields" responsive-layout="stack" breakpoint="960px" @page="onPage($event)">
        <template #header>
            <div class="table-header">
                <span class="p-input-icon-left">
                    <i class="pi pi-search" />
                    <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                </span>
            </div>
        </template>
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <Column class="kn-truncated" field="name" :header="$t('common.name')"> </Column>
        <Column :header="$t('managers.lovsManagement.value')">
            <template #body="slotProps">
                <RadioButton v-model="selectedValue" name="value" :value="slotProps.data.name" @change="setSelectedValue" />
            </template>
        </Column>
        <Column :header="$t('common.description')">
            <template #body="slotProps">
                <RadioButton v-model="selectedDescription" name="description" :value="slotProps.data.name" @change="setSelectedDescription" />
            </template>
        </Column>
        <Column :header="$t('managers.lovsManagement.visible')">
            <template #body="slotProps">
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
    emits: ['modelChanged'],
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
    watch: {
        tableData() {
            this.loadData()
        },
        treeListTypeModel() {
            this.loadModel()
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
        },
        loadModel() {
            this.model = this.treeListTypeModel as any
        },
        loadSelectedModel() {
            this.selectedValue = this.treeListTypeModel['VALUE-COLUMN'] as any
            this.selectedDescription = this.treeListTypeModel['DESCRIPTION-COLUMN'] as any

            this.selectedVisible = []
            this.treeListTypeModel['VISIBLE-COLUMNS'].split(',').forEach((el: any) => {
                if (el) {
                    this.selectedVisible.push(el)
                }
            })
        },
        setSelectedValue() {
            this.model['VALUE-COLUMN'] = this.selectedValue
            this.$emit('modelChanged', this.model)
        },
        setSelectedDescription() {
            this.model['DESCRIPTION-COLUMN'] = this.selectedDescription
            this.$emit('modelChanged', this.model)
        },
        setSelectedVisible() {
            this.model['VISIBLE-COLUMNS'] = this.selectedVisible.toString()
            this.$emit('modelChanged', this.model)
        }
    }
})
</script>
