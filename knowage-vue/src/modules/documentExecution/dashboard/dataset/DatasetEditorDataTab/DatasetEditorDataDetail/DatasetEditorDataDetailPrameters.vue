<template>
    <Accordion class="p-mb-3">
        <AccordionTab :header="$t('common.parameters')">
            <!-- PARAMETERS ---------------- -->
            <div v-for="(parameter, index) of selectedDatasetProp.parameters" :key="index" class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" :disabled="true" v-model="parameter.name" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.parameter') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <Dropdown id="type" class="kn-material-input" :options="parameterTypes" v-model="parameter.modelType" />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }}</label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-4 p-d-flex">
                    <span class="p-float-label kn-flex">
                        <InputText id="label" class="kn-material-input" type="text" v-model="parameter.value" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.value') }} </label>
                    </span>
                    <Button v-if="parameter.modelType === 'dynamic' && documentDriversProp && documentDriversProp.filterStatus.length > 0" icon="fa-solid fa-link" class="p-button-text p-button-rounded p-button-plain p-as-end" @click.stop="showMenu($event, parameter.name)" />
                </div>
            </div>

            <!-- DRIVERS ---------------- -->
            <div v-for="(driver, index) of drivers" :key="index" class="p-field p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" :disabled="true" v-model="driver.label" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.driver') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-8 p-d-flex">
                    <span class="p-float-label kn-flex">
                        <InputText v-if="(!driver.multivalue || (driver.typeCode === 'MAN_IN' && (driver.type === 'NUM' || driver.type === 'STRING'))) && driver.parameterValue[0]" class="kn-material-input" v-model="driver.parameterValue[0].value" />
                        <Chips v-else v-model="driver.parameterValue" :disabled="true">
                            <template #chip="slotProps">
                                <div>
                                    <span>{{ slotProps.value.value }}</span>
                                </div>
                            </template>
                        </Chips>
                        <label class="kn-material-input-label"> {{ $t('common.value') }} </label>
                    </span>
                    <Button icon="pi pi-pencil" class="p-button-text p-button-rounded p-button-plain" @click.stop="openDriverDialog(driver)" />
                    <Button icon="fa fa-eraser" class="p-button-text p-button-rounded p-button-plain" @click="resetDefaultValue(driver)" />
                </div>
            </div>
        </AccordionTab>
    </Accordion>

    <Menu id="parameterPickerMenu" ref="parameterPickerMenu" :model="menuButtons" />
    <DatasetEditorDriverDialog :visible="driversDialogVisible" :propDriver="selectedDriver" @updateDriver="onUpdateDriver" @close="onDriversDialogClose"></DatasetEditorDriverDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IDashboardDatasetDriver } from '../../../Dashboard'
import Card from 'primevue/card'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/contextmenu'
import Chips from 'primevue/chips'
import DatasetEditorDriverDialog from './DatasetEditorDriverDialog/DatasetEditorDriverDialog.vue'

import mockedDrivers from './mockedDrivers.json'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Card, Accordion, AccordionTab, Dropdown, Menu, Chips, DatasetEditorDriverDialog },
    props: { selectedDatasetProp: { required: true, type: Object }, dashboardDatasetsProp: { required: true, type: Array as any }, documentDriversProp: { type: Array as any } },
    emits: [],
    data() {
        return {
            parameterTypes: ['static', 'dynamic'],
            menuButtons: [] as any,
            drivers: deepcopy(mockedDrivers) as IDashboardDatasetDriver[],
            driversDialogVisible: false,
            selectedDriver: null as IDashboardDatasetDriver | null
        }
    },
    setup() {},
    async created() {
        console.log('>>>>>>>> selectedDatasetProp: ', this.selectedDatasetProp)
    },
    methods: {
        showMenu(event, parameter) {
            this.createMenuItems(parameter)
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.parameterPickerMenu.toggle(event)
        },
        createMenuItems(paramName) {
            this.menuButtons = this.documentDriversProp.filterStatus.map((driver) => {
                return { label: driver.label, urlName: driver.urlName, command: () => this.addDriverValueToParameter(driver.label, paramName) }
            })
        },
        addDriverValueToParameter(driverUrl, paramName) {
            this.selectedDatasetProp.parameters.find((parameter) => parameter.name === paramName).value = '$P{' + driverUrl + '}'
        },
        openDriverDialog(driver: IDashboardDatasetDriver) {
            console.log('>>>>>>>> OPEN DRIVER DIALOG WITH: ', driver)
            this.selectedDriver = driver
            this.driversDialogVisible = true
        },
        resetDefaultValue(driver: IDashboardDatasetDriver) {
            console.log('>>>>>>>> RESET DEFAULT VALUE: ', driver.defaultValue)
            if (!driver.defaultValue) return
            driver.parameterValue = driver.defaultValue
        },
        onDriversDialogClose() {
            this.driversDialogVisible = false
            this.selectedDriver = null
        },
        onUpdateDriver(driver: IDashboardDatasetDriver) {
            console.log('>>>>>>>> ON UPDATE DRIVER: ', driver)
            this.driversDialogVisible = false
            const index = this.drivers.findIndex((tempDriver: IDashboardDatasetDriver) => tempDriver.urlName === driver.urlName)
            if (index !== -1) this.drivers[index] = driver
        }
    }
})
</script>
