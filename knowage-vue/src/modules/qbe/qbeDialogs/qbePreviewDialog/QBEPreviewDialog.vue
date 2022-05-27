<template>
    <div id="qbe-preview-dialog" class="p-fluid kn-dialog--toolbar--primary">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
            <template #start>
                {{ $t('common.preview') }}
            </template>
            <template #end>
                <i class="fa fa-times kn-cursor-pointer" @click="close"></i>
            </template>
        </Toolbar>

        <DataTable
            id="qbe-preview-datatable"
            class="p-datatable-sm kn-table"
            v-model:first="first"
            :value="rows"
            :paginator="true"
            :lazy="true"
            :rows="20"
            :totalRecords="lazyParams.size"
            paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
            :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
            responsiveLayout="stack"
            breakpoint="960px"
            :scrollable="true"
            :scrollHeight="QBEPreviewDialogDescriptor.scrollHeight"
            @page="onPage($event)"
        >
            <template #empty>
                <div>
                    {{ $t('common.info.noDataFound') }}
                </div>
            </template>
            <Column class="kn-truncated" v-for="col of columns" :field="col.dataIndex" :header="col.header" :key="col.field" :sortable="true">
                <template #body="slotProps">
                    {{ col.metawebDateFormat ? getFormattedDate(slotProps.data[col.dataIndex], col) : slotProps.data[col.dataIndex] }}
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { formatDate } from '@/helpers/commons/localeHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import QBEPreviewDialogDescriptor from './QBEPreviewDialogDescriptor.json'
import QBEDescriptor from '../../QBEDescriptor.json'

export default defineComponent({
    name: 'qbe-preview-dialog',
    components: { Column, DataTable },
    props: { id: { type: String }, queryPreviewData: { type: Object }, pagination: { type: Object }, entities: { type: Array }, selectedQuery: { type: Object, required: true } },
    emits: ['close', 'pageChanged'],
    data() {
        return {
            QBEPreviewDialogDescriptor,
            QBEDescriptor,
            columns: [] as any[],
            rows: [] as any[],
            lazyParams: {} as any,
            first: 0,
            loading: false
        }
    },
    watch: {
        queryPreviewData() {
            this.loadData()
        },
        pagination() {
            this.loadPagination()
        }
    },
    created() {
        this.loadData()
        this.loadPagination()
    },
    methods: {
        loadData() {
            if (this.queryPreviewData) {
                this.setPreviewColumns(this.queryPreviewData)
                this.rows = this.queryPreviewData.rows
            }
        },
        setPreviewColumns(data: any) {
            this.columns = []

            for (let i = 1; i < data.metaData?.fields?.length; i++) {
                const tempColumn = data.metaData?.fields[i]
                if (['timestamp', 'date'].includes(tempColumn.type)) {
                    const field = this.findField(tempColumn) as any
                    if (field) tempColumn.metawebDateFormat = field.format
                    if (!tempColumn.metawebDateFormat) {
                        this.setCalculatedFieldDateFormat(tempColumn, i - 1)
                    }
                }
                this.columns.push(data.metaData?.fields[i])
            }
        },
        setCalculatedFieldDateFormat(tempColumn: any, index: number) {
            if (!this.selectedQuery || !this.selectedQuery.fields) return
            if (this.selectedQuery.fields[index]) tempColumn.metawebDateFormat = this.selectedQuery.fields[index].id.format ?? 'L'
        },
        findField(column: any) {
            if (!this.entities) return

            let field = null

            for (let i = 0; i < this.entities.length; i++) {
                const tempEntity = this.entities[i] as any
                for (let j = 0; j < tempEntity.children.length; j++) {
                    if (tempEntity.children[j].attributes.field === column.header) {
                        field = tempEntity.children[j]
                        break
                    }
                }
            }

            return field
        },
        loadPagination() {
            this.lazyParams = this.pagination as any
            this.first = this.pagination?.start
        },
        onPage(event: any) {
            this.lazyParams = { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows, size: this.lazyParams.size }
            this.$emit('pageChanged', this.lazyParams)
        },
        close() {
            this.$emit('close')
            this.first = 0
            this.lazyParams = {}
        },
        getFormattedDate(date: any, column: any) {
            const inputFormat = column.type === 'timestamp' ? 'DD/MM/YYYY HH:mm:ss.SSS' : 'DD/MM/YYYY'
            let format = undefined as string | undefined
            if (QBEDescriptor.admissibleDateFormats.includes(column.metawebDateFormat)) {
                format = column.metawebDateFormat
            }
            return formatDate(date, format, inputFormat)
        }
    }
})
</script>

<style lang="scss">
#qbe-preview-dialog .p-dialog-header,
#qbe-preview-dialog .p-dialog-content {
    padding: 0;
    padding-bottom: 35px;
}

#qbe-preview-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
