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
                <RegistryFiltersCard v-if="filters.length > 0" :id="id" :propFilters="filters" :entity="entity" @filter="filterRegistry"></RegistryFiltersCard>
            </div>
            <div class="p-col-12" v-if="!loading">
                <RegistryDatatable :propColumns="columns" :propRows="rows" :propConfiguration="configuration" :columnMap="columnMap" :pagination="pagination" @rowChanged="onRowChanged" @rowDeleted="onRowDeleted" @pageChanged="updatePagination"></RegistryDatatable>
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
    props: { id: { type: String } },
    data() {
        return {
            registry: {} as any,
            configuration: [] as any[],
            columns: [] as any[],
            rows: [] as any[],
            columnMap: {} as any,
            pagination: { start: 0, limit: 15 } as any,
            updatedRows: [] as any,
            filters: [] as any[],
            selectedFilters: [] as any[],
            entity: null as string | null,
            isPivot: false,
            loading: false
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    async created() {
        await this.loadPage()
        // console.log('LOADED TEMPLATE: ', this.template)
        // console.log('LOADED REGISTRY: ', this.registry)
        // console.log('LOADED FILTERED VALUES: ', this.filteredValues)
        console.log('FILTERS ', this.filters)
        console.log('ID ', this.id)
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadRegistry()
            this.loadConfiguration()
            this.loadEntity()
            this.loadColumns()
            this.loadColumnMap()
            this.loadColumnsInfo()
            this.loadRows()
            this.checkIfFilterColumnExists()
            this.loading = false
        },
        async loadRegistry() {
            const postData = new URLSearchParams()

            if (this.pagination.size > 1000) {
                postData.append('limit', '15')
            }

            this.selectedFilters.forEach((el: any) => {
                if (el.filterValue) {
                    postData.append(el.field, el.filterValue)
                }
            })

            postData.append('start', '' + this.pagination.start)
            await axios.post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=LOAD_REGISTRY_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then((response) => {
                this.pagination.size = response.data.results
                this.registry = response.data
            })
        },
        loadColumns() {
            this.columns = this.registry.registryConfig.columns
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
            const limit = this.pagination.size <= 1000 ? this.registry.rows.length : 15
            for (let i = 0; i < limit; i++) {
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
            // console.log('LOADED CONFIGURATION: ', this.configuration)
        },
        loadEntity() {
            this.entity = this.registry.registryConfig.entity
            console.log('LOADED ENTITY: ', this.entity)
        },
        onRowChanged(row: any) {
            // console.log('CHANGED ROW: ', row)
            const tempRow = { ...row }
            const index = this.updatedRows.findIndex((el: any) => el.id === tempRow.id)
            delete tempRow.id
            delete tempRow.selected
            index === -1 ? this.updatedRows.push(tempRow) : (this.updatedRows[index] = tempRow)
        },
        async saveRegistry() {
            // console.log('UPDATED ROWS FOR SAVE: ', this.updatedRows)
            const postData = new URLSearchParams()
            postData.append('records', '' + JSON.stringify(this.updatedRows))
            await axios.post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=UPDATE_RECORDS_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.updateTitle'),
                    msg: this.$t('common.toast.updateSuccess')
                })
                this.pagination.start = 0
                this.loadPage()
            })
        },
        async onRowDeleted(row: any) {
            // console.log('ROW FOR DELETE: ', row)
            const postData = new URLSearchParams()
            postData.append('records', '' + JSON.stringify([row]))
            await axios.post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=DELETE_RECORDS_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then((response) => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })

                if (response.data.ids[0]) {
                    const index = this.rows.findIndex((el: any) => el.id === row.id)
                    this.rows.splice(index, 1)
                    this.pagination.size--
                }
            })
        },
        checkIfFilterColumnExists() {
            this.filters = []
            const tempFilters = this.registry.registryConfig.filters
            // console.log('tempFilters: ', tempFilters)
            for (let i = 0; i < tempFilters.length; i++) {
                const filter = tempFilters[i]
                // console.log('COLUMNS: ', this.columns)
                for (let j = 0; j < this.columns.length; j++) {
                    const column = this.columns[j]
                    if (filter.presentation !== 'DRIVER' && filter.field === column.field) {
                        console.log('TEMP COLMN: ', column)
                        this.filters.push({ title: filter.title, field: filter.field, presentation: filter.presentationType, static: filter.isStatic, visible: filter.isVisible, column: column })
                    }
                }
            }
        },
        async filterRegistry(filters: any[]) {
            this.selectedFilters = [...filters]
            this.pagination.start = 0
            this.pagination.size = 0
            await this.loadRegistry()
            this.loadRows()
        },
        async updatePagination(lazyParams: any) {
            console.log('UPDATE PAGINATION: ', lazyParams)
            this.pagination = { start: lazyParams.paginationStart, limit: lazyParams.paginationLimit, size: lazyParams.size }
            console.log('UPDATED PAGINATION: ', this.pagination)
            if (this.pagination.size > 1000) {
                await this.loadRegistry()
                this.loadRows()
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
