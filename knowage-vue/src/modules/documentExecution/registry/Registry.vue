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
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <div class="p-col-12">
                <RegistryFiltersCard v-if="filters.length > 0" :id="id" :propFilters="filters" :entity="entity" @filter="filterRegistry"></RegistryFiltersCard>
            </div>
            <div class="p-col-12" v-if="!loading">
                <RegistryPivotDatatable
                    v-if="isPivot"
                    :columns="columns"
                    :id="id"
                    :rows="rows"
                    :entity="entity"
                    :propConfiguration="configuration"
                    :propPagination="pagination"
                    @rowChanged="onRowChanged"
                    @rowDeleted="onRowDeleted"
                    @pageChanged="updatePagination"
                    @resetRows="updatedRows = []"
                    @warningChanged="setWarningState"
                ></RegistryPivotDatatable>
                <RegistryDatatable
                    v-else
                    :propColumns="columns"
                    :id="id"
                    :propRows="rows"
                    :propConfiguration="configuration"
                    :columnMap="columnMap"
                    :pagination="pagination"
                    :entity="entity"
                    :stopWarningsState="stopWarningsState"
                    @rowChanged="onRowChanged"
                    @rowDeleted="onRowDeleted"
                    @pageChanged="updatePagination"
                    @warningChanged="setWarningState"
                ></RegistryDatatable>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import registryDescriptor from './RegistryDescriptor.json'
import RegistryDatatable from './tables/RegistryDatatable.vue'
import RegistryPivotDatatable from './tables/RegistryPivotDatatable.vue'
import RegistryFiltersCard from './RegistryFiltersCard.vue'

export default defineComponent({
    name: 'registry',
    components: {
        RegistryDatatable,
        RegistryPivotDatatable,
        RegistryFiltersCard
    },
    props: { id: { type: String } },
    data() {
        return {
            registryDescriptor,
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
            stopWarningsState: [] as any[],
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
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadRegistry()
            this.loadRegistryData()
            this.loading = false
        },
        async loadRegistry() {
            const postData = new URLSearchParams()

            if (this.pagination.size > registryDescriptor.paginationLimit) {
                postData.append('limit', '' + registryDescriptor.paginationNumberOfItems)
            }

            this.selectedFilters.forEach((el: any) => {
                if (el.filterValue) {
                    postData.append(el.field, el.filterValue)
                }
            })

            postData.append('start', '' + this.pagination.start)
            await axios
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=LOAD_REGISTRY_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/x-www-form-urlencoded'
                    }
                })
                .then((response) => {
                    this.pagination.size = response.data.results
                    this.registry = response.data
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
        },
        loadRegistryData() {
            this.loadConfiguration()
            this.loadEntity()
            this.loadColumns()
            this.loadColumnMap()
            this.loadColumnsInfo()
            this.loadRows()
            this.getFilters()
        },
        loadColumns() {
            this.columns = []
            this.registry.registryConfig.columns.map((el: any) => {
                if (el.type === 'merge') {
                    this.isPivot = true
                }
                this.columns.push(el)
            })
        },
        loadColumnMap() {
            this.columnMap = { id: 'id' }
            for (let i = 1; i < this.registry.metaData.fields.length; i++) {
                this.columnMap[this.registry.metaData.fields[i].name] = this.registry.metaData.fields[i].header
            }
        },
        loadColumnsInfo() {
            for (let i = 1; i < this.registry.metaData.fields.length; i++) {
                this.columns[i - 1].columnInfo = this.registry.metaData.fields[i]
            }
        },
        loadRows() {
            this.rows = []
            const limit = this.pagination.size <= registryDescriptor.paginationLimit ? this.registry.rows.length : registryDescriptor.paginationNumberOfItems
            for (let i = 0; i < limit; i++) {
                const tempRow = {}
                Object.keys(this.registry.rows[i]).forEach((key) => {
                    tempRow[this.columnMap[key]] = this.registry.rows[i][key]
                })
                this.rows.push(tempRow)
            }
        },
        loadConfiguration() {
            this.configuration = this.registry.registryConfig.configurations
        },
        loadEntity() {
            this.entity = this.registry.registryConfig.entity
        },
        onRowChanged(row: any) {
            const tempRow = { ...row }
            const index = this.updatedRows.findIndex((el: any) => el.id === tempRow.id)
            index === -1 ? this.updatedRows.push(tempRow) : (this.updatedRows[index] = tempRow)
        },
        async saveRegistry() {
            this.updatedRows.forEach((el: any) => {
                if (this.isPivot) {
                    this.formatPivotRows(el)
                }
                delete el.id
                delete el.isNew
                delete el.edited
            })
            const postData = new URLSearchParams()
            postData.append('records', '' + JSON.stringify(this.updatedRows))
            await axios
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=UPDATE_RECORDS_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.updateSuccess')
                    })
                    this.pagination.start = 0
                    this.loadPage()
                })
                .finally(() => (this.updatedRows = []))
        },
        async onRowDeleted(row: any) {
            if (this.isPivot) {
                this.formatPivotRows(row)
            }
            const postData = new URLSearchParams()
            postData.append('records', '' + JSON.stringify([row]))
            await axios
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=DELETE_RECORDS_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then((response) => {
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
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
        },
        getFilters() {
            this.filters = []
            const tempFilters = this.registry.registryConfig.filters

            for (let i = 0; i < tempFilters.length; i++) {
                const filter = tempFilters[i]

                for (let j = 0; j < this.columns.length; j++) {
                    const column = this.columns[j]
                    if (filter.presentation !== 'DRIVER' && filter.field === column.field) {
                        this.filters.push({
                            title: filter.title,
                            field: filter.field,
                            presentation: filter.presentationType,
                            static: filter.isStatic,
                            visible: filter.isVisible,
                            column: column
                        })
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
            this.updatedRows = []
            this.pagination = {
                start: lazyParams.paginationStart,
                limit: lazyParams.paginationLimit,
                size: lazyParams.size
            }

            if (this.pagination.size > registryDescriptor.paginationLimit) {
                await this.loadRegistry()
                this.loadRows()
            }
        },
        formatPivotRows(row: any) {
            Object.keys(row).forEach((key: any) => {
                if (key !== 'id') {
                    row[key] = row[key].data
                }
            })
        },
        setWarningState(warnings: any[]) {
            this.stopWarningsState = warnings
        }
    }
})
</script>
