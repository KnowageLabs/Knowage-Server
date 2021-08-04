<template>
    <Dialog :style="lovsManagementPreviewDialogDescriptor.dialog.style" :header="$t('managers.lovsManagement.preview')" :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div id="filter-info" class="p-d-flex p-ai-center p-jc-center">
            <p>{{ $t('managers.lovsManagement.filterNullValues') }}</p>
        </div>
        <DataTable :value="rows" class="p-datatable-sm kn-table" dataKey="field" v-model:filters="filters" :lazy="true" :paginator="true" :rows="20" :totalRecords="lazyParams.size" :globalFilterFields="globalFilterFields" responsiveLayout="stack" breakpoint="960px" @page="onPage($event)">
            <template #header>
                <div class="table-header">
                    <span class="p-input-icon-left">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                    </span>
                </div>
            </template>
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <Column class="kn-truncated" v-for="col of columns" :field="col.field" :header="col.header" :key="col.field" :sortable="true"> </Column>
        </DataTable>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import lovsManagementPreviewDialogDescriptor from './LovsManagementPreviewDialogDescriptor.json'

export default defineComponent({
    name: 'lovs-management-preview-dialog',
    components: { Column, DataTable, Dialog },
    emits: ['close', 'pageChanged'],
    props: {
        dataForPreview: { type: Object, required: true },
        pagination: { type: Object }
    },
    data() {
        return {
            lovsManagementPreviewDialogDescriptor,
            columns: [] as any[],
            rows: [] as any[],
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: [] as string[],
            lazyParams: {} as any
        }
    },
    watch: {
        dataForPreview() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            if (this.dataForPreview.metaData?.fields) {
                this.formatColumns()
            }

            this.loadPagination()
            this.rows = this.dataForPreview.root
            // console.log('aaa - COLUMNS', this.columns)
            // console.log('aaa - ROWS', this.rows)
            // console.log('DATA FOR PREVIEW', this.dataForPreview)
        },
        formatColumns() {
            this.columns = []
            // console.log('DATA FOR PREVIEW asas', this.dataForPreview.metaData.fields)
            for (let i = 0; i < this.dataForPreview.metaData.fields.length; i++) {
                // console.log('aaa - meta', this.dataForPreview.metaData.fields[i])
                this.columns.push({ field: this.dataForPreview.metaData.fields[i].name, header: this.dataForPreview.metaData.fields[i].name })
                this.globalFilterFields.push(this.dataForPreview.metaData.fields[i].name)
            }
        },
        loadPagination() {
            this.lazyParams = this.pagination as any
        },
        onPage(event: any) {
            // console.log('EVENT PAGIANATION', event)
            this.lazyParams = { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows, size: this.lazyParams.size }
            this.$emit('pageChanged', this.lazyParams)
            // console.log('LAZY PARAMS PAGINATION', this.lazyParams)
        }
    }
})
</script>

<style lang="scss" scoped>
#filter-info {
    margin-top: 2rem;
    font-size: 0.8rem;
    text-transform: uppercase;
    display: flex;
    justify-content: center;
    border: 1px solid rgba(59, 103, 140, 0.1);
    background-color: #eaf0f6;
    p {
        margin: 0.3rem;
    }
}
</style>
