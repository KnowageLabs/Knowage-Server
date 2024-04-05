<template>
    <DataTable
        :value="rows"
        class="p-datatable-sm kn-table"
        v-model:selection="selectedRow"
        :selectionMode="multivalue ? false : 'single'"
        v-model:filters="filters"
        :globalFilterFields="globalFilterFields"
        :paginator="rows.length > 20"
        :rows="20"
        responsiveLayout="stack"
        breakpoint="600px"
        :scrollable="true"
        :scrollHeight="knParameterPopupDialogDescriptor.dialog.scrollHeight"
        @row-select="setSelectedRow"
        @row-unselect="setSelectedRow"
        @row-select-all="setSelectedRow"
        @row-unselect-all="setSelectedRow"
    >
        <template #empty>
            <Message class="p-m-2" severity="info" :closable="false" :style="knParameterPopupDialogDescriptor.styles.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
        </template>

        <template #header>
            <div class="table-header p-d-flex p-ai-center">
                <span id="search-container" class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                </span>
            </div>
        </template>

        <Column v-if="multivalue" selectionMode="multiple" :style="knParameterPopupDialogDescriptor.styles.checkboxColumn"></Column>
        <Column class="kn-truncated" v-for="col of columns" :field="col.field" :header="col.header" :key="col.field" :sortable="true"> </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import knParameterPopupDialogDescriptor from './KnParameterPopupDialogDescriptor.json'
import Message from 'primevue/message'

export default defineComponent({
    name: 'kn-parameter-popup-dialog',
    components: { Column, DataTable, Message },
    props: {
        parameterPopUpData: { type: Object },
        multivalue: { type: Boolean },
        multipleSelectedRows: { type: Array }
    },
    emits: ['selected'],
    data() {
        return {
            knParameterPopupDialogDescriptor,
            rows: [] as any[],
            columns: [] as { header: string; field: string }[],
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: [] as string[],
            selectedRow: null as any
        }
    },
    watch: {
        parameterPopUpData() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            if (!this.parameterPopUpData) return
            this.rows = this.parameterPopUpData.result.data

            this.columns = []
            
            let keyMap: any[] = []
            let pref: string = ''
 
            Object.keys(this.parameterPopUpData.result.metadata.colsMap).forEach((col) => {
                const colMatch = col.match(/(?<pref>[a-zA-Z_\-.]+)(?<key>\d+)/)
                if (colMatch && colMatch.groups) {
                    pref = colMatch.groups.pref // col_
                    keyMap.push(parseInt(colMatch.groups.key)) // 1-2
                }
            })

            keyMap = keyMap.sort().map((k) => pref + k)
            
            keyMap.forEach((key: string) => {
                if (this.parameterPopUpData?.result.metadata.visibleColumns?.includes(this.parameterPopUpData.result.metadata.colsMap[key])) {
                    this.columns.push({
                        header: this.parameterPopUpData?.result.metadata.colsMap[key],
                        field: key
                    })
                }
            })

            this.columns.forEach((el: any) => this.globalFilterFields.push(el.field))

            this.selectedRow = this.multipleSelectedRows
        },
        setSelectedRow() {
            setTimeout(() => this.$emit('selected', this.selectedRow), 10)
        }
    }
})
</script>
