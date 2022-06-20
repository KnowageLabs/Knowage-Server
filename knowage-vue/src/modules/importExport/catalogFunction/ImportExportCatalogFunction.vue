<template>
    <DataTable
        ref="dt"
        :value="functions"
        v-model:selection="selectedCatalogFunctionItems"
        v-model:filters="filters"
        class="p-datatable-sm kn-table"
        dataKey="id"
        :paginator="true"
        :rows="10"
        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
        :rowsPerPageOptions="[10, 15, 20]"
        responsiveLayout="stack"
        breakpoint="960px"
        :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
        :globalFilterFields="['name', 'type', 'tags']"
    >
        <template #header>
            <div class="table-header">
                <span class="p-input-icon-left">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" />
                </span>
            </div>
        </template>
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <template #loading>
            {{ $t('common.info.dataLoading') }}
        </template>

        <Column selectionMode="multiple" :exportable="false" :style="importExportDescriptor.export.catalogFunction.column.selectionMode.style"></Column>
        <Column field="name" :header="$t(importExportDescriptor.export.catalogFunction.column.name.header)" :sortable="true" :style="importExportDescriptor.export.catalogFunction.column.name.style"></Column>
        <Column field="type" :header="$t(importExportDescriptor.export.catalogFunction.column.type.header)" :sortable="true" :style="importExportDescriptor.export.catalogFunction.column.type.style"> </Column>

        <Column field="tags" :header="$t(importExportDescriptor.export.catalogFunction.column.tags.header)" :sortable="true" :style="importExportDescriptor.export.catalogFunction.column.tags.style">
            <template #body="{data}">
                <span class="p-float-label kn-material-input">
                    <Tag class="importExportTags p-mr-1" v-for="(tag, index) in data.tags" v-bind:key="index" rounded :value="tag"> </Tag>
                </span>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { FilterMatchMode, FilterOperator } from 'primevue/api'
import InputText from 'primevue/inputtext'

import Tag from 'primevue/tag'
import importExportDescriptor from '../ImportExportDescriptor.json'
import { ICatalogFunctionTemplate } from '@/modules/importExport/catalogFunction/ICatalogFunctionTemplate'

export default defineComponent({
    name: 'import-export-catalog-function',
    components: { Column, DataTable, InputText, Tag },
    props: { selectedItems: Object },
    data() {
        return {
            filters: {},
            importExportDescriptor: importExportDescriptor,
            product: {},
            selectedCatalogFunctionItems: [],
            functions: [] as Array<ICatalogFunctionTemplate>,
            FUNCTIONALITY: 'catalogFunction'
        }
    },
    created() {
        this.filters = {
            global: { value: null, matchMode: FilterMatchMode.CONTAINS },
            name: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
            type: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
            tags: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }] }
        }
        this.loadAllFunctions()
    },
    emits: ['onItemSelected', 'update:loading'],
    methods: {
        loadAllFunctions(): void {
            this.$emit('update:loading', true)
            this.$http
                .get(import.meta.env.VUE_APP_API_PATH + '1.0/functioncatalog')
                .then((response: AxiosResponse<any>) => {
                    this.functions = response.data

                    if (this.selectedItems) {
                        this.selectedCatalogFunctionItems = this.selectedItems[this.FUNCTIONALITY].filter((element) => {
                            return this.functions.filter((el) => el.id === element.id).length == 1
                        })
                    }
                })
                .catch((error) => console.error(error))
                .finally(() => {
                    this.$emit('update:loading', false)
                })
        }
    },
    watch: {
        selectedCatalogFunctionItems(newSelectedCatalogFunctionItems, oldSelectedCatalogFunctionItems) {
            if (oldSelectedCatalogFunctionItems != newSelectedCatalogFunctionItems) {
                this.$emit('onItemSelected', { items: this.selectedCatalogFunctionItems, functionality: this.FUNCTIONALITY })
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.imageOverlayPanel {
    position: absolute !important;
    top: 0px !important;
    left: 0px !important;
}

.importExportTags {
    background-color: var(--kn-color-default);
}

.p-paginator p-component p-paginator-bottom {
    height: 50px;
}
</style>
