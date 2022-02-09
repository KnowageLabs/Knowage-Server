<template>
    <DataTable
        id="preview-datatable"
        v-model:first="first"
        :value="rows"
        :lazy="true"
        :paginator="lazyParams.size > 15"
        :rows="15"
        :totalRecords="lazyParams.size"
        paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
        class="p-datatable-sm kn-table kn-flex"
        v-model:filters="filters"
        filterDisplay="menu"
        :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
        stripedRows
        responsiveLayout="stack"
        breakpoint="960px"
        @page="onPage($event)"
        @sort="onSort"
    >
        <template #empty>
            <div id="noFunctionsFound">
                {{ $t('common.info.noDataFound') }}
            </div>
        </template>
        <Column class="kn-truncated" :style="datasetPreviewTableDescriptor.columnStyle" v-for="col of columns" :field="col.field" :key="col.field" :sortable="previewType === 'dataset' ? false : true">
            <template #header>
                <div class="dropdown">
                    <div clas="p-d-flex p-flex-column">
                        <p class="p-m-0">{{ col.header }}</p>
                        <small>{{ col.type }}</small>
                    </div>
                    <div v-if="previewType !== 'dataset'" class="dropdown-icon-container">
                        <i class="pi pi-filter-icon pi-filter p-ml-5" :class="{ 'filter-icon-active': searchInput[col.field] }" @click="searchVisible[col.field] = !searchVisible[col.field]" />
                        <div class="dropdown-content" v-if="searchVisible[col.field]">
                            <InputText v-model="searchInput[col.field]" class="p-inputtext-sm p-column-filter" @input="onFilter(col)"></InputText>
                        </div>
                    </div>
                </div>
            </template>
            <template #body="slotProps">
                <span v-tooltip.top="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import datasetPreviewTableDescriptor from './DatasetPreviewTableDescriptor.json'

export default defineComponent({
    name: 'function-catalog-preview-table',
    components: { Column, DataTable },
    props: { previewColumns: { type: Array }, previewRows: { type: Array }, pagination: { type: Object }, previewType: String },
    emits: ['pageChanged', 'sort', 'filter'],
    data() {
        return {
            datasetPreviewTableDescriptor,
            columns: [] as any[],
            rows: [] as any[],
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: [] as string[],
            lazyParams: {} as any,
            sorted: 'ASC',
            timer: null as any,
            searchInput: {} as any,
            searchVisible: {} as any,
            customFilters: [] as any,
            first: 0
        }
    },
    watch: {
        previewColumns() {
            this.loadColumns()
        },
        previewRows() {
            this.loadRows()
        },
        pagination() {
            this.loadPagination()
        }
    },
    created() {
        this.loadColumns()
        this.loadRows()
        this.loadPagination()
    },
    methods: {
        loadColumns() {
            this.columns = []
            this.previewColumns?.forEach((el: any) => {
                this.columns.push(el)
                this.globalFilterFields.push(el.field)
                this.filters[el.field] = { operator: FilterOperator.AND, constraints: [filterDefault] }
            })
        },
        loadRows() {
            this.rows = this.previewRows as any[]
        },
        loadPagination() {
            this.lazyParams = this.pagination as any
        },
        onPage(event: any) {
            this.lazyParams = { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows, size: this.lazyParams.size }
            this.$emit('pageChanged', this.lazyParams)
        },
        onSort(event: any) {
            let column = ''
            const index = this.columns.findIndex((el: any) => el.field === event.sortField)
            if (index !== -1) {
                column = this.columns[index].header
            }
            const order = event.sortOrder === 1 ? 'asc' : 'desc'
            this.$emit('sort', { column: column, order: order })
        },
        onFilter(column: any) {
            if (this.timer) {
                clearTimeout(this.timer)
                this.timer = null
            }

            this.timer = setTimeout(() => {
                const filter = { column: column.header, value: this.searchInput[column.field] }
                const index = this.customFilters.findIndex((el: any) => el.column === column.header)

                if (index !== -1) {
                    if (!filter.value) {
                        this.customFilters.splice(index, 1)
                    } else {
                        this.customFilters[index] = filter
                    }
                } else {
                    this.customFilters.push(filter)
                }
                this.$emit('filter', this.customFilters)
            }, 1000)
        }
    }
})
</script>

<style lang="scss">
#preview-datatable .p-datatable-wrapper {
    height: auto;
}

.dropdown {
    position: relative;
    display: flex;
    flex-direction: row;
    align-items: center;
}

.dropdown-icon-container {
    position: relative;
}

.dropdown-content {
    display: block;
    position: absolute;
    top: 0;
    left: 50px;
    min-width: 160px;
    box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.2);
    z-index: 5000;
}

.filter-icon-active {
    color: $color-primary;
}
</style>
