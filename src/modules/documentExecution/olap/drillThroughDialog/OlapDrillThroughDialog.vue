<template>
    <div class="p-d-flex p-flex-column kn-width-full">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-col-12 kn-flex-0">
            <template #start>{{ $t('documentExecution.olap.drillTru.title') }} </template>
        </Toolbar>

        <Card class="p-m-2">
            <template #content>
                <b>{{ $t('documentExecution.olap.drillTru.selectLevels') }}:</b>
                <div class="p-d-flex p-flex-row p-mt-2">
                    <div v-for="(parent, index) in dtTree" :key="index" class="p-d-flex p-flex-column">
                        <Button class="p-button-text p-button-plain" :label="parent.caption" @click="showMenu($event, parent)" />
                    </div>
                    <Button class="kn-button kn-button--primary p-ml-auto" :label="$t('documentExecution.olap.drillTru.clearAll')" @click="clearLevels" />
                </div>
            </template>
        </Card>

        <DataTable id="olap-custom-views-table" v-model:filters="filters" :value="dtData" class="p-datatable-sm kn-table kn-flex p-m-2 olap-drill-through-table" :paginator="drillData.length > 20" :rows="18" responsive-layout="stack" breakpoint="600px" striped-rows="true" row-hover="true">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText v-model="filters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                    </span>
                    <span class="p-float-label p-as-end p-ml-auto">
                        <Dropdown id="rows" v-model="maxRows" class="kn-material-input" :options="maxRowsOptions" :style="dtDescriptor.style.dropdown" @change="$emit('rowsChanged', maxRows)" />
                        <label for="rows" class="kn-material-input-label"> {{ $t('documentExecution.olap.drillTru.maxRows') }} </label>
                    </span>
                </div>
            </template>

            <Column v-for="(column, index) in tableColumns" :key="index" class="kn-truncated" :field="column.name" :header="$t(column.label)" :sortable="true"></Column>
        </DataTable>

        <div class="p-mt-auto p-ml-auto p-mb-3 p-mr-3">
            <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="closeDialog" />
            <Button class="kn-button kn-button--primary p-mx-2" :label="$t('common.export')" @click="exportDrill(dtData, 'DrillTrough Table', true)" />
            <Button class="kn-button kn-button--primary" :label="$t('common.apply')" @click="$emit('drill')" />
        </div>

        <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons">
            <template #item="{ item }">
                <span class="p-m-2">
                    <Checkbox v-model="item.checked" :binary="true" @click="$emit('checkCheckboxes', item, dtAssociatedLevels)" />
                    <text class="p-ml-2">{{ item.caption }}</text>
                </span>
            </template>
        </Menu>
    </div>
</template>

<script lang="ts">
import { filterDefault } from '@/helpers/commons/filterHelper'
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import dtDescriptor from './OlapDrillThroughDialogDescriptor.json'
import Menu from 'primevue/contextmenu'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import mainStore from '../../../../App.store'

export default defineComponent({
    components: { Column, DataTable, Menu, Checkbox, Dropdown },
    props: { drillData: { type: Array, required: true }, tableColumns: { type: Array, required: true }, dtLevels: { type: Array, required: true }, menuTree: { type: Array, required: true }, dtMaxRows: { type: Number, required: true } },
    emits: ['close', 'applyCustomView', 'checkCheckboxes', 'clearLevels', 'drill', 'rowsChanged'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            dtDescriptor,
            dtData: [] as any,
            formattedColumns: [] as any,
            dtAssociatedLevels: [] as any,
            dtTree: [] as any,
            menuButtons: [] as any,
            filters: { global: [filterDefault] } as Object,
            maxRowsOptions: [0, 10, 25, 50, 100, 250, 500, 1000],
            maxRows: 0
        }
    },
    watch: {
        drillData() {
            this.dtData = this.drillData
            this.maxRows = this.dtMaxRows
        },
        tableColumns() {
            this.formattedColumns = this.tableColumns
        },
        dtLevels() {
            this.dtAssociatedLevels = this.dtLevels
        },
        menuTree() {
            this.dtTree = this.menuTree
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.dtData = this.drillData
            this.formattedColumns = this.tableColumns
            this.dtAssociatedLevels = this.dtLevels
            this.dtTree = this.menuTree
        },
        closeDialog() {
            this.$emit('close')
        },
        showMenu(event, parent) {
            this.createMenuItems(parent.children)
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        createMenuItems(items) {
            this.menuButtons = []
            items.forEach((item) => {
                this.menuButtons.push(item)
            })
        },
        clearLevels() {
            this.$emit('clearLevels')
            this.dtTree.forEach((parent) => {
                parent.children.forEach((child) => {
                    child.checked = false
                })
            })
        },
        exportDrill(JSONData, ReportTitle, ShowLabel) {
            const arrData = typeof JSONData != 'object' ? JSON.parse(JSONData) : JSONData
            let CSV = ''
            CSV += ReportTitle + '\r\n\n'
            if (ShowLabel) {
                let row = ''
                for (const index in arrData[0]) {
                    row += index + ','
                }
                row = row.slice(0, -1)
                CSV += row + '\r\n'
            }
            for (let i = 0; i < arrData.length; i++) {
                let row = ''
                for (const index in arrData[i]) {
                    row += '"' + arrData[i][index] + '",'
                }
                row.slice(0, row.length - 1)
                CSV += row + '\r\n'
            }
            if (CSV == '') {
                this.store.setError({ title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.drillTru.invalidData') })
                return
            }
            let fileName = 'MyReport_'
            fileName += ReportTitle.replace(/ /g, '_')
            const uri = 'data:text/csv;charset=utf-8,' + escape(CSV)
            const link = document.createElement('a') as any
            link.href = uri
            link.style = 'visibility:hidden'
            link.download = fileName + '.csv'
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
        }
    }
})
</script>
<style lang="scss">
.olap-drill-through-table {
    overflow: auto;
    .p-datatable-wrapper,
    .p-datatable-table {
        overflow: auto;
    }
}
</style>
