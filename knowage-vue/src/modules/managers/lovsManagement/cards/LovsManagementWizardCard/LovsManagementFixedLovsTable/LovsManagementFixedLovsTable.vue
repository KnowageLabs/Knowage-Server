<template>
    <DataTable
        :value="values"
        class="p-datatable-sm kn-table p-m-5"
        editMode="cell"
        v-model:filters="filters"
        :globalFilterFields="lovsManagementFixedLovsTableDescriptor.globalFilterFields"
        responsiveLayout="stack"
        breakpoint="960px"
        @rowReorder="setPositionOnReorder"
        @cell-edit-complete="onCellEditComplete"
        data-test="values-list"
    >
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <template #loading>
            {{ $t('common.info.dataLoading') }}
        </template>
        <template #header>
            <div class="table-header">
                <div class="table-header p-d-flex p-ai-center p-jc-between">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filter-input" />
                    </span>
                    <Button id="add-fixed-value-button" class="kn-button kn-button--primary" :label="$t('managers.lovsManagement.add')" @click="addFixedValue" data-test="new-button"></Button>
                </div>
            </div>
        </template>
        <Column :rowReorder="true" :headerStyle="lovsManagementFixedLovsTableDescriptor.table.reorderColumn.headerStyle" :reorderableColumn="false" />

        <Column class="kn-truncated p-mr-2" :style="lovsManagementFixedLovsTableDescriptor.table.inputColumnStyle" field="VALUE" :header="$t('managers.lovsManagement.value')">
            <template #editor="slotProps">
                <InputText class="p-mr-2" type="text" v-model.trim="slotProps.data[slotProps.column.props.field]" maxLength="20" @input="$emit('touched')" data-test="value-input" />
                <i class="pi pi-pencil edit-icon" />
            </template>
            <template #body="slotProps">
                <span class="p-mr-2" data-test="value-body">{{ slotProps.data.VALUE }}</span>
                <i class="pi pi-pencil edit-icon" />
            </template>
        </Column>
        <Column class="kn-truncated" :style="lovsManagementFixedLovsTableDescriptor.table.textAreaColumnStyle" field="DESCRIPTION" :header="$t('managers.lovsManagement.description')">
            <template #editor="slotProps">
                <Textarea class="p-mr-2" type="text" v-model.trim="slotProps.data[slotProps.column.props.field]" maxLength="160" rows="2" cols="80" @input="$emit('touched')" data-test="description-input" />
                <i class="pi pi-pencil edit-icon" />
            </template>
            <template #body="slotProps">
                <span class="p-mr-2" data-test="description-body">{{ slotProps.data.DESCRIPTION }}</span>
                <i class="pi pi-pencil edit-icon" />
            </template>
        </Column>
        <Column :style="lovsManagementFixedLovsTableDescriptor.table.iconColumn.style">
            <template #body="slotProps">
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteValueConfirm(slotProps.index)" :data-test="'delete-button-' + slotProps.index" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFixedValue } from '../../../LovsManagement'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import lovsManagementFixedLovsTableDescriptor from './LovsManagementFixedLovsTableDescriptor.json'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'lovs-management-fixed-lovs-table',
    components: { Column, DataTable, Textarea },
    props: {
        listForFixLov: {
            type: Array
        }
    },
    emits: ['touched', 'sorted'],
    data() {
        return {
            lovsManagementFixedLovsTableDescriptor,
            selectedValue: {} as iFixedValue,
            values: [] as iFixedValue[],
            filters: { global: [filterDefault] } as Object
        }
    },
    created() {
        this.load()
    },
    methods: {
        load() {
            this.values = this.listForFixLov as any[]
        },
        addFixedValue() {
            this.values.push({ VALUE: '', DESCRIPTION: '' })
        },
        setPositionOnReorder(event) {
            this.values = event.value
            this.$emit('sorted', this.values)
        },
        deleteValueConfirm(index: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.deleteValue(index)
                }
            })
        },
        deleteValue(index: number) {
            this.values.splice(index, 1)
        },
        onCellEditComplete(event: any) {
            this.values[event.index] = event.newData
        }
    }
})
</script>

<style lang="scss" scoped>
#add-fixed-value-button {
    flex: 0.2;
    height: 2.3rem;
}
</style>
