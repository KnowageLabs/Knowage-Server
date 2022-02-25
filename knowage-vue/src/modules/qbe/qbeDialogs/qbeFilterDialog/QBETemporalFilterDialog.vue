<template>
    <Dialog id="qbe-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEFilterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false" :base-z-index="4203">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('qbe.filters.temporalFilter') }}
                </template>
            </Toolbar>
        </template>

        <DataTable class="p-datatable-sm kn-table p-m-4" :value="filters" v-model:selection="selectedFilter" selectionMode="single" responsiveLayout="stack" breakpoint="960px">
            <template #empty>
                <div id="noFunctionsFound">
                    {{ $t('common.info.noDataFound') }}
                </div>
            </template>
            <Column :field="'name'" :header="$t('common.name')" :sortable="true"> </Column>
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import QBEFilterDialogDescriptor from './QBEFilterDialogDescriptor.json'

export default defineComponent({
    name: 'qbe-temporal-filter-dialog',
    components: { Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, temporalFilters: { type: Array } },
    emits: ['save', 'close'],
    data() {
        return {
            QBEFilterDialogDescriptor,
            filters: [] as any,
            selectedFilter: null
        }
    },
    watch: {
        visible() {
            this.selectedFilter = null
        },
        temporalFilters() {
            this.loadTemporalFilters()
        }
    },
    created() {
        this.loadTemporalFilters()
    },
    methods: {
        loadTemporalFilters() {
            this.filters = this.temporalFilters
        },
        closeDialog() {
            this.$emit('close')
        },
        save() {
            this.$emit('save', this.selectedFilter)
        }
    }
})
</script>

<style lang="scss">
#qbe-temporal-filter-dialog.p-dialog-header,
#qbe-temporal-filter-dialog.p-dialog-content {
    padding: 0;
}
#qbe-temporal-filter-dialog.p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
