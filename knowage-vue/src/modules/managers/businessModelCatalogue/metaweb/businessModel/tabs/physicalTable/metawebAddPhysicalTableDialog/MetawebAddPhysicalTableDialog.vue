<template>
    <Dialog id="metaweb-add-physical-table-dialog" :style="metawebAddPhysicalTableDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #left>
                    {{ $t('metaweb.businessModel.addPhysicalTables') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <DataTable
            :value="rows"
            class="p-datatable-sm kn-table"
            v-model:selection="selectedTables"
            dataKey="value"
            v-model:filters="filters"
            :globalFilterFields="metawebAddPhysicalTableDialogDescriptor.globalFilterFields"
            @row-select="onTablesSelect"
            @row-unselect="onTablesSelect"
            @row-select-all="onTablesSelect"
            @row-unselect-all="onTablesSelect"
        >
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #header>
                <div class="table-header p-d-flex">
                    <span class="p-input-icon-left p-mr-3 p-col-12">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>
            <Column selectionMode="multiple" :style="metawebAddPhysicalTableDialogDescriptor.selectColumnStyle" />
            <Column field="name" :header="$t('common.name')" />
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="addPhysicalTable"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import metawebAddPhysicalTableDialogDescriptor from './MetawebAddPhysicalTableDialogDescriptor.json'

export default defineComponent({
    name: 'metaweb-add-physical-table-dialog',
    components: { Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, physicalTables: { type: Array } },
    emits: ['close'],
    data() {
        return {
            metawebAddPhysicalTableDialogDescriptor,
            rows: [] as any[],
            selectedTables: [] as any[],
            filters: {
                global: [filterDefault]
            } as Object,
            loading: false
        }
    },
    watch: {
        physicalTables() {
            this.loadTables()
        }
    },
    created() {
        this.loadTables()
    },
    methods: {
        loadTables() {
            this.rows = this.physicalTables as any[]
            console.log('LOADED TABLES: ', this.rows)
        },
        closeDialog() {
            this.$emit('close')
        },
        addPhysicalTable() {
            console.log('addPhysicalTable CLICKED!')
        },
        onTablesSelect() {
            console.log('SELECTED TABLES: ', this.selectedTables)
        }
    }
})
</script>

<style lang="scss">
#metaweb-add-physical-table-dialog .p-dialog-header,
#metaweb-add-physical-table-dialog .p-dialog-content {
    padding: 0;
}

#metaweb-add-physical-table-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
