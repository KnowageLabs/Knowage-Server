<template>
    <Dialog :header="$t('managers.lovsManagement.preview')" :visible="visible" :modal="true" class="lovPreviewDialog kn-dialog--toolbar--primary" :closable="false">
        <Message class="p-m-4" severity="info" :closable="false">
            {{ $t('managers.lovsManagement.filterNullValues') }}
        </Message>

        <DataTable :value="rows" class="p-datatable-sm kn-table" data-key="field" scroll-height="flex" :scrollable="true" scroll-direction="both" :lazy="true" :paginator="true" :rows="15" :total-records="lazyParams.size" responsive-layout="stack" breakpoint="960px" @page="onPage($event)" @sort="onSort">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <Column v-for="col of columns" :key="col.field" class="kn-truncated" :field="col.field" :header="col.header" :sortable="true" style="flex-grow:1; flex-basis:200px"></Column>
        </DataTable>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'

export default defineComponent({
    name: 'lovs-management-preview-dialog',
    components: { Column, DataTable, Dialog, Message },
    props: {
        visible: { type: Boolean },
        dataForPreview: { type: Object, required: true },
        pagination: { type: Object }
    },
    emits: ['close', 'pageChanged'],
    data() {
        return {
            columns: [] as any[],
            rows: [] as any[],
            lazyParams: {} as any,
            sorted: 'ASC'
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
        },
        formatColumns() {
            this.columns = []
            for (let i = 0; i < this.dataForPreview.metaData.fields.length; i++) {
                this.columns.push({ field: this.dataForPreview.metaData.fields[i].name, header: this.dataForPreview.metaData.fields[i].name })
            }
        },
        loadPagination() {
            this.lazyParams = this.pagination as any
        },
        onPage(event: any) {
            this.lazyParams = { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows, size: this.lazyParams.size }
            this.$emit('pageChanged', this.lazyParams)
        },
        onSort() {
            if (this.sorted === 'DESC') {
                this.rows = this.rows.sort((a: any, b: any) => (a.sortField > b.sortField ? 1 : -1))
                this.sorted = 'ASC'
            } else {
                this.rows = this.rows.sort((a: any, b: any) => (a.sortField < b.sortField ? 1 : -1))
                this.sorted = 'DESC'
            }
        }
    }
})
</script>

<style lang="scss">
.lovPreviewDialog {
    .p-dialog-content {
        max-width: 960px;
    }
}
</style>
