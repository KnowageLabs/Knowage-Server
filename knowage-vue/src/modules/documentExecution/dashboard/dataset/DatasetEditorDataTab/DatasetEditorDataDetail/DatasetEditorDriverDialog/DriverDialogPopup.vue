<template>
    <div v-if="driver && driver.parameterValue" class="p-fluid p-formgrid p-grid p-jc-center p-ai-center p-p-5 p-m-0">
        <DataTable
            v-model:selection="selectedRows"
            v-model:filters="filters"
            :value="rows"
            class="p-datatable-sm kn-table p-col-12"
            :loading="loading"
            :selection-mode="driver.multivalue ? 'multiple' : 'single'"
            :global-filter-fields="globalFilterFields"
            :paginator="rows.length > 20"
            :rows="20"
            responsive-layout="stack"
            breakpoint="600px"
            @row-select="onRowSelect"
            @row-unselect="onRowSelect"
            @row-select-all="onRowSelect"
            @row-unselect-all="onRowSelect"
        >
            <template #empty>
                <Message class="p-m-2" severity="info" :closable="false" :style="descriptor.style.message">
                    {{ $t('common.info.noDataFound') }}
                </Message>
            </template>

            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <Column v-if="driver.multivalue" selection-mode="multiple" :style="descriptor.style.checkboxColumn"></Column>
            <Column v-for="col of columns" :key="col.name" class="kn-truncated" :field="col.name" :header="col.header" :sortable="true"> </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import descriptor from '../DatasetEditorDataDetailDescriptor.json'
import { getFormattedDrivers, getUserRole } from './DatasetEditorDriverHelper'
import { mapState } from 'pinia'
import mainStore from '@/App.store'

export default defineComponent({
    name: 'driver-dialog-popup',
    components: { Column, DataTable, Message },
    props: { propDriver: { type: Object as PropType<IDashboardDatasetDriver | null>, required: true }, dashboardId: { type: String, required: true }, selectedDatasetProp: { required: true, type: Object }, drivers: { type: Array as PropType<IDashboardDatasetDriver[]>, required: true } },
    data() {
        return {
            descriptor,
            driver: null as IDashboardDatasetDriver | null,
            rows: [] as any[],
            columns: [] as { header: string; name: string }[],
            filters: { global: [filterDefault] } as any,
            globalFilterFields: [] as string[],
            selectedRows: null as any,
            loading: false
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        })
    },
    watch: {
        propDriver() {
            this.loadDriver()
        }
    },
    created() {
        this.loadDriver()
    },
    methods: {
        loadDriver() {
            this.driver = this.propDriver
            this.getDriverPopupInfo()
        },
        async getDriverPopupInfo() {
            if (!this.driver || !this.selectedDatasetProp) return
            this.loading = true
            const role = getUserRole(this.user)
            const postData = {
                OBJECT_NAME: this.selectedDatasetProp.configuration?.qbeDatamarts,
                ROLE: role,
                PARAMETER_ID: this.driver.urlName,
                MODE: 'extra',
                PARAMETERS: getFormattedDrivers(this.drivers)
            }

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/businessModelOpening/getParameters', postData)
                .then((response: AxiosResponse<any>) => {
                    this.columns = response.data.result.metaData.fields.splice(1, response.data.result.metaData.fields.length - 2)
                    this.columns.forEach((column: { header: string; name: string }) => this.globalFilterFields.push(column.name))
                    this.rows = response.data.result.root
                })
                .catch((error: any) => console.log('ERROR: ', error))

            this.setSelectedRows()
            this.loading = false
        },
        setSelectedRows() {
            this.selectedRows = []
            this.driver?.parameterValue.forEach((parameterValue: { value: string | number | Date; description: string }) => {
                const index = this.rows.findIndex((row: any) => row.value === parameterValue.value && row.description == parameterValue.description)
                if (index !== -1) this.selectedRows.push(this.rows[index])
            })
        },
        onRowSelect() {
            if (!this.driver) return
            if (this.driver.multivalue) {
                this.driver.parameterValue = []
                this.selectedRows.forEach((row: any) => this.driver?.parameterValue.push({ value: row.value, description: row.description }))
            } else {
                this.driver.parameterValue[0] = { value: this.selectedRows.value, description: this.selectedRows.description }
            }
        }
    }
})
</script>
