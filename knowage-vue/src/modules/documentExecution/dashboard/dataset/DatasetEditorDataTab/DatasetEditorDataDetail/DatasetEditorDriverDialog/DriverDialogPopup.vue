<template>
    <div v-if="driver && driver.parameterValue" class="p-fluid p-formgrid p-grid p-jc-center p-ai-center p-p-5 p-m-0">
        {{ 'POPUP' }}
        {{ driver }}
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import { mapActions } from 'pinia'
import mainStore from '@/App.store'
import moment from 'moment'

export default defineComponent({
    name: 'driver-dialog-popup',
    components: {},
    props: { propDriver: { type: Object as PropType<IDashboardDatasetDriver | null>, required: true }, dashboardId: { type: String, required: true }, selectedDatasetProp: { required: true, type: Object }, drivers: { type: Array as PropType<IDashboardDatasetDriver[]>, required: true } },
    computed: {},
    data() {
        return {
            driver: null as IDashboardDatasetDriver | null,
            driverPopupData: {} as any
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
        ...mapActions(mainStore, ['setLoading']),
        loadDriver() {
            this.driver = this.propDriver
            this.getDriverPopupInfo()
        },
        async getDriverPopupInfo() {
            if (!this.driver || !this.selectedDatasetProp) return
            this.setLoading(true)
            const role = '/demo/admin' // TODO - see about user role
            console.log('>>>>>>>>>>>>> selectedDatasetProp: ', this.selectedDatasetProp)
            const postData = {
                OBJECT_NAME: this.selectedDatasetProp.name,
                ROLE: role,
                PARAMETER_ID: this.driver.urlName,
                MODE: 'extra',
                PARAMETERS: this.getFormattedDrivers()
            }

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/businessModelOpening/getParameters', postData)
                .then((response: AxiosResponse<any>) => (this.driverPopupData = response.data))
                .catch((error: any) => console.log('ERROR: ', error))

            console.log('>>>>>>> LOADED POPUP DATA: ', this.driverPopupData)
            this.setLoading(false)
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
            formattedDrivers[driver.urlName] = +driver.parameterValue[0].value
            formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0].description
        },
        getFormattedDateDriver(driver: any, formattedDrivers: any) {
            // TODO - Format Date
            formattedDrivers[driver.urlName] = driver.parameterValue[0].value
            formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0].description
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
        }
    }
})
</script>
