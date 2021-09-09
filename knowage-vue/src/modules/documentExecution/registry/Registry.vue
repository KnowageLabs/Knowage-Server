<template>
    <div class="kn-page">
        <div class="kn-page-content p-m-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('documentExecution.registry.title') }}
                </template>
                <template #right>
                    <div class="p-d-flex p-flex-row">
                        <Button class="kn-button p-button-text" @click="saveRegistry">{{ $t('common.save') }}</Button>
                    </div>
                </template>
            </Toolbar>
            <div id="spinner" v-if="loading">
                <ProgressSpinner />
            </div>
            <div class="p-col-12">
                <RegistryFiltersCard :propFilters="filters"></RegistryFiltersCard>
            </div>
            <div class="p-col-12" v-if="!loading">
                <RegistryDatatable :propColumns="columns" :propRows="rows" :propConfiguration="configuration" :columnMap="columnMap" :filteredValues="filteredValues" @rowChanged="onRowChanged" @rowDeleted="onRowDeleted"></RegistryDatatable>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import ProgressSpinner from 'primevue/progressspinner'
import RegistryDatatable from './tables/RegistryDatatable.vue'
import RegistryFiltersCard from './RegistryFiltersCard.vue'

export default defineComponent({
    name: 'registry',
    components: { ProgressSpinner, RegistryDatatable, RegistryFiltersCard },
    data() {
        return {
            registry: {} as any,
            configuration: [] as any[],
            columns: [] as any[],
            rows: [] as any[],
            columnMap: {} as any,
            pagination: { start: 0, limit: 20 },
            updatedRows: [] as any,
            filters: [] as any[],
            isPivot: false,
            loading: false
        }
    },
    async created() {
        this.loading = true
        await this.loadRegistry()
        this.loadConfiguration()
        this.loadColumns()
        this.loadColumnMap()
        this.loadColumnsInfo()
        this.loadRows()
        this.checkIfFilterColumnExists()
        this.loading = false
        // console.log('LOADED TEMPLATE: ', this.template)
        // console.log('LOADED REGISTRY: ', this.registry)
        // console.log('LOADED FILTERED VALUES: ', this.filteredValues)
        console.log('FILTERS ', this.filters)
    },
    methods: {
        async loadRegistry() {
            const postData = new URLSearchParams()
            postData.append('start', '0')
            postData.append('limit', '15')
            await axios
                //.post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=LOAD_REGISTRY_ACTION&SBI_EXECUTION_ID=e7a8d9ed113e11ec8dd79b53a6e80fe7`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .get('../data/demo_registry.json')
                .then((response) => (this.registry = response.data))
        },
        loadColumns() {
            this.columns = this.registry.registryConfig.columns
            this.columns = this.columns.map((el: any) => ({ field: el.field, title: el.title, visible: el.isVisible, editable: el.isEditable, editor: el.editorType }))
            console.log('LOADED COLUMNS: ', this.columns)
        },
        loadColumnMap() {
            this.columnMap = { id: 'id' }
            for (let i = 1; i < this.registry.metaData.fields.length; i++) {
                this.columnMap[this.registry.metaData.fields[i].name] = this.registry.metaData.fields[i].header
            }
            console.log('COLUMN MAP: ', this.columnMap)
        },
        loadColumnsInfo() {
            for (let i = 1; i < this.registry.metaData.fields.length; i++) {
                this.columns[i - 1].columnInfo = this.registry.metaData.fields[i]
            }
        },
        loadRows() {
            this.rows = []
            for (let i = 0; i < this.registry.rows.length; i++) {
                const tempRow = {}
                Object.keys(this.registry.rows[i]).forEach((key) => {
                    // console.log(key, this.registry.rows[i][key])
                    tempRow[this.columnMap[key]] = this.registry.rows[i][key]
                })
                this.rows.push(tempRow)
            }

            // console.log('LOADED ROWS: ', this.rows)
        },
        loadConfiguration() {
            this.configuration = this.registry.registryConfig.configurations
            console.log('LOADED CONFIGURATION: ', this.configuration)
        },
        onRowChanged(row: any) {
            console.log('CHANGED ROW: ', row)
            const index = this.updatedRows.findIndex((el: any) => el.id === row.id)
            if (index === -1) {
                this.updatedRows.push(row)
            }
        },
        saveRegistry() {
            console.log('UPDATED ROWS FOR SAVE: ', this.updatedRows)
        },
        onRowDeleted(row: any) {
            console.log('ROW FOR DELETE: ', row)
        },
        checkIfFilterColumnExists() {
            this.filters = []
            const tempFilters = this.registry.registryConfig.filters
            console.log('tempFilters: ', tempFilters)
            for (let i = 0; i < tempFilters.length; i++) {
                const filter = tempFilters[i]
                console.log('COLUMNS: ', this.columns)
                for (let j = 0; j < this.columns.length; j++) {
                    const column = this.columns[j]
                    if (filter.presentation !== 'DRIVER' && filter.field === column.field) {
                        this.filters.push({ title: filter.title, field: filter.field, presentation: filter.presentationType, static: filter.isStatic, visible: filter.isVisible })
                    }
                }
            }
        }
    }
})
</script>

<style lang="scss" scoped>
#spinner {
    position: fixed;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    opacity: 0.7;
    background-color: rgba(0, 0, 0, $alpha: 0.5);
    z-index: 99;
}
::v-deep(.p-progress-spinner-circle) {
    animation: p-progress-spinner-color 4s ease-in-out infinite;
}
@keyframes p-progress-spinner-color {
    100%,
    0% {
        stroke: #43749e;
    }
    80%,
    90% {
        stroke: #d62d20;
    }
}
</style>
