<template>
    <div>
        <Message v-if="listIsInvalid" class="p-text-center p-m-4" severity="warn" :closable="false">{{ $t('managers.lovsManagement.fixedListInvalidError') }}</Message>
        <DataTable
            v-model:filters="filters"
            :value="values"
            class="p-datatable-sm kn-table p-m-5"
            edit-mode="cell"
            :global-filter-fields="lovsManagementFixedLovsTableDescriptor.globalFilterFields"
            responsive-layout="stack"
            breakpoint="960px"
            data-test="values-list"
            @rowReorder="setPositionOnReorder"
            @cell-edit-complete="onCellEditComplete"
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
                            <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" data-test="filter-input" />
                        </span>
                        <Button id="add-fixed-value-button" class="kn-button kn-button--primary" :label="$t('managers.lovsManagement.add')" data-test="new-button" @click="addFixedValue"></Button>
                    </div>
                </div>
            </template>
            <Column :row-reorder="true" :header-style="lovsManagementFixedLovsTableDescriptor.table.reorderColumn.headerStyle" :reorderable-column="false" />

            <Column class="kn-truncated p-mr-2" :style="lovsManagementFixedLovsTableDescriptor.table.inputColumnStyle" field="VALUE" :header="$t('managers.lovsManagement.value')">
                <template #editor="slotProps">
                    <InputText v-model.trim="slotProps.data[slotProps.column.props.field]" class="p-mr-2" type="text" max-length="20" data-test="value-input" @input="$emit('touched')" />
                    <i class="pi pi-pencil edit-icon" />
                </template>
                <template #body="slotProps">
                    <span class="p-mr-2" data-test="value-body">{{ slotProps.data.VALUE }}</span>
                    <i class="pi pi-pencil edit-icon" />
                </template>
            </Column>
            <Column class="kn-truncated" :style="lovsManagementFixedLovsTableDescriptor.table.textAreaColumnStyle" field="DESCRIPTION" :header="$t('managers.lovsManagement.description')">
                <template #editor="slotProps">
                    <Textarea v-model.trim="slotProps.data[slotProps.column.props.field]" class="p-mr-2" type="text" max-length="160" rows="2" cols="80" data-test="description-input" @input="$emit('touched')" />
                    <i class="pi pi-pencil edit-icon" />
                </template>
                <template #body="slotProps">
                    <span class="p-mr-2" data-test="description-body">{{ slotProps.data.DESCRIPTION }}</span>
                    <i class="pi pi-pencil edit-icon" />
                </template>
            </Column>
            <Column :style="lovsManagementFixedLovsTableDescriptor.table.iconColumn.style">
                <template #body="slotProps">
                    <Button icon="pi pi-trash" class="p-button-link" :data-test="'delete-button-' + slotProps.index" @click="deleteValueConfirm(slotProps.index)" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFixedValue } from '../../../LovsManagement'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import lovsManagementFixedLovsTableDescriptor from './LovsManagementFixedLovsTableDescriptor.json'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'lovs-management-fixed-lovs-table',
    components: { Column, DataTable, Message, Textarea },
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
    computed: {
        listIsInvalid() {
            for (let i = 0; i < this.values.length; i++) {
                const fixedLovListItem = this.values[i] as { VALUE: string; DESCRIPTION: string }
                if (fixedLovListItem.VALUE?.trim() && fixedLovListItem.DESCRIPTION?.trim()) {
                    return false
                }
            }
            return true
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
