<template>
    <div v-if="driver && driver.parameterValue" class="p-fluid p-formgrid p-grid p-jc-center p-ai-center p-p-5 p-m-0">
        {{ driver.parameterValue }}
        <DataTable
            :value="rows"
            class="p-datatable-sm kn-table p-col-12"
            v-model:selection="selectedRows"
            :loading="loading"
            :selectionMode="driver.multivalue ? 'multiple' : 'single'"
            v-model:filters="filters"
            :globalFilterFields="globalFilterFields"
            :paginator="rows.length > 20"
            :rows="20"
            responsiveLayout="stack"
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
                        <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <Column v-if="driver.multivalue" selectionMode="multiple" :style="descriptor.style.checkboxColumn"></Column>
            <Column class="kn-truncated" v-for="col of columns" :field="col.name" :header="col.header" :key="col.name" :sortable="true"> </Column>
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
import moment from 'moment'

export default defineComponent({
    name: 'driver-dialog-popup',
    components: { Column, DataTable, Message },
    props: { propDriver: { type: Object as PropType<IDashboardDatasetDriver | null>, required: true }, dashboardId: { type: String, required: true }, selectedDatasetProp: { required: true, type: Object }, drivers: { type: Array as PropType<IDashboardDatasetDriver[]>, required: true } },
    computed: {},
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
            const role = '/demo/admin' // TODO - see about user role
            const postData = {
                OBJECT_NAME: this.selectedDatasetProp.configuration?.qbeDatamarts,
                ROLE: role,
                PARAMETER_ID: this.driver.urlName,
                MODE: 'extra',
                PARAMETERS: this.getFormattedDrivers()
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
            this.driver?.parameterValue.forEach((parameterValue: { value: string; description: string }) => {
                const index = this.rows.findIndex((row: any) => row.value === parameterValue.value && row.description == parameterValue.description)
                if (index !== -1) this.selectedRows.push(this.rows[index])
            })
        },
        getFormattedDrivers() {
            console.log('>>>>>>>>>>>>> ALLL DRIVERS: ', this.drivers)
            const formattedDrivers = {} as any
            this.drivers.forEach((driver: IDashboardDatasetDriver) => {
                if (driver.typeCode === 'MAN_IN' && (driver.type === 'NUM' || driver.type === 'STRING')) {
                    driver.type === 'NUM' ? this.getFormattedManualNumberDriver(driver, formattedDrivers) : this.getFormattedManualStringDriver(driver, formattedDrivers)
                } else if (driver.type === 'DATE') {
                    this.getFormattedDateDriver(driver, formattedDrivers)
                } else if (driver.selectionType === 'LIST') {
                    this.getFormattedListDriver(driver, formattedDrivers)
                } else if (driver.selectionType === 'COMBOBOX') {
                    this.getFormattedDropdownDriver(driver, formattedDrivers)
                } else if (driver.selectionType === 'LOOKUP') {
                    this.getFormattedPopupDriver(driver, formattedDrivers)
                }
                // TODO - Tree
            })
            return formattedDrivers
        },
        getFormattedManualStringDriver(driver: any, formattedDrivers: any) {
            formattedDrivers[driver.urlName] = driver.parameterValue[0].value
            formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0].description
        },
        getFormattedManualNumberDriver(driver: any, formattedDrivers: any) {
            console.log('>>>>>>>>>>>>> ----------------- TEEEEEEST: ', driver)
            formattedDrivers[driver.urlName] = driver.parameterValue[0].value ? +driver.parameterValue[0].value : driver.parameterValue[0].value
            formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0].description
        },
        getFormattedDateDriver(driver: any, formattedDrivers: any) {
            const formattedDate = moment(driver.parameterValue[0].value).format('MMM DD, YYYY')
            formattedDrivers[driver.urlName] = formattedDate
            formattedDrivers[driver.urlName + '_field_visible_description'] = formattedDate
        },
        getFormattedListDriver(driver: any, formattedDrivers: any) {
            if (driver.multivalue) {
                const driverValues = [] as string[]
                const driverDescriptions = [] as string[]
                driver.parameterValue.forEach((parameterValue: { value: string; description: string }) => {
                    driverValues.push(parameterValue.value)
                    driverDescriptions.push(parameterValue.description)
                })
                formattedDrivers[driver.urlName] = driverValues
                formattedDrivers[driver.urlName + '_field_visible_description'] = driverDescriptions.join(';')
            } else {
                formattedDrivers[driver.urlName] = driver.parameterValue[0].value
                formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0].description
            }
        },
        getFormattedDropdownDriver(driver: any, formattedDrivers: any) {
            if (driver.multivalue) {
                const driverValues = [] as string[]
                const driverDescriptions = [] as string[]
                driver.parameterValue.forEach((parameterValue: { value: string; description: string }) => {
                    driverValues.push(parameterValue.value)
                    driverDescriptions.push(parameterValue.description)
                })
                formattedDrivers[driver.urlName] = driverValues
                formattedDrivers[driver.urlName + '_field_visible_description'] = driverDescriptions.join(';')
            } else {
                formattedDrivers[driver.urlName] = driver.parameterValue[0].value
                formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0].description
            }
        },
        getFormattedPopupDriver(driver: any, formattedDrivers: any) {
            console.log('>>>>>>>>>>> DRIVER: ', driver)
            if (driver.multivalue) {
                const driverValues = [] as string[]
                const driverDescriptions = [] as string[]
                driver.parameterValue.forEach((parameterValue: { value: string; description: string }) => {
                    driverValues.push(parameterValue.value)
                    driverDescriptions.push(parameterValue.description)
                })
                formattedDrivers[driver.urlName] = driverValues
                formattedDrivers[driver.urlName + '_field_visible_description'] = driverDescriptions.join(';')
            } else {
                formattedDrivers[driver.urlName] = driver.parameterValue[0].value
                formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0].description
            }
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
