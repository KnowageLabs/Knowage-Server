<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #start>{{ title }} </template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-p-2 p-fluid p-d-flex p-flex-column kn-height-full kn-overflow-y" data-test="drivers-form">
        <DriversDetailCard class="p-mt-2" :selectedDriver="driver" :types="filteredTypes" @touched="setDirty"></DriversDetailCard>
        <UseMode class="kn-flex-grow p-mt-2" :propModes="modes" :roles="roles" :constraints="constraints" :layers="layers" :lovs="lovs" :selectionTypes="filteredSelectionTypes" :isDate="isDateType" :showMapDriver="showMapDriver"></UseMode>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iDriver } from './DriversManagement'
import DriversDetailCard from './DriversDetailCard.vue'
import UseMode from './useModes/DriversManagementUseMode.vue'
import { AxiosResponse } from 'axios'
import driversManagemenDetailtDescriptor from './DriversManagementDetailDescriptor.json'

export default defineComponent({
    name: 'metadata-management-detail',
    components: { DriversDetailCard, UseMode },
    props: {
        selectedDriver: {
            type: Object,
            required: false
        }
    },
    computed: {
        buttonDisabled(): any {
            return !this.driver.label || !this.driver.name || !this.driver.type || this.invalidUseModes > 0 || this.noRoleSelected > 0
        },
        invalidUseModes(): any {
            return this.modes.filter((mode: any) => mode.numberOfErrors > 0).length
        },
        noRoleSelected(): any {
            return this.modes.filter((mode: any) => mode.associatedRoles.length === 0).length
        },
        title(): any {
            return this.driver.id ? this.driver.name : this.$t('common.new')
        },
        filteredTypes(): any {
            return this.types.filter((type) => type.VALUE_CD != 'DATE_RANGE')
        },
        filteredSelectionTypes(): any {
            return this.selectionTypes.filter((type) => type.VALUE_CD != 'SLIDER')
        },
        isDateType(): any {
            return this.driver.type === 'Date'
        }
    },
    data() {
        return {
            driver: {} as iDriver,
            types: [] as any[],
            modes: [] as any[],
            modesToSave: [] as any[],
            roles: [] as any[],
            constraints: [] as any[],
            selectionTypes: [] as any[],
            layers: [] as any[],
            lovs: [] as any[],
            operation: 'insert',
            useModeOperation: 'insert',
            showMapDriver: false,
            driversManagemenDetailtDescriptor
        }
    },
    watch: {
        selectedDriver() {
            this.driver = { ...this.selectedDriver } as any
            this.getModes()
        }
    },
    mounted() {
        if (this.driver) {
            this.driver = { ...this.selectedDriver } as any
            this.getModes()
        }
        this.showMapDriver = (this.store.$state as any).user.functionalities.indexOf('MapDriverManagement') > -1
        this.loadAll()
    },

    methods: {
        async getTypes() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'domains/listValueDescriptionByType?DOMAIN_TYPE=PAR_TYPE').then((response: AxiosResponse<any>) => (this.types = response.data))
        },
        async getModes() {
            if (this.driver.id) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/' + this.driver.id + '/modes/').then((response: AxiosResponse<any>) => (this.modes = response.data))
            } else this.modes = []
        },
        async getRoles() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/roles').then((response: AxiosResponse<any>) => (this.roles = response.data))
        },
        async getConstraints() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/checks').then((response: AxiosResponse<any>) => (this.constraints = response.data))
        },
        async getselectionTypes() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'domains/listValueDescriptionByType?DOMAIN_TYPE=SELECTION_TYPE').then((response: AxiosResponse<any>) => (this.selectionTypes = response.data))
        },
        async getLayers() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/analyticalDriversee/layers').then((response: AxiosResponse<any>) => (this.layers = response.data))
        },
        async getLovs() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all').then((response: AxiosResponse<any>) => (this.lovs = response.data))
        },
        loadAll() {
            this.getTypes()
            this.getRoles()
            this.getConstraints()
            this.getselectionTypes()
            if (this.showMapDriver) this.getLayers()
            this.getLovs()
        },
        formatDriver() {
            this.driver.length = 0
            let selectedType = this.types.filter((val) => {
                return val.VALUE_CD === this.driver.type
            })
            this.driver.type = selectedType[0].VALUE_CD
            this.driver.typeId = selectedType[0].VALUE_ID
        },
        formatUseMode() {
            let tmp = this.modes.filter((mode) => mode.edited)
            this.modesToSave = []
            tmp.forEach((mode) => {
                mode.maximizerEnabled = false
                mode.manualInput = mode.valueSelection == 'man_in' ? 1 : 0
                if (mode.idLov === null) {
                    mode.idLov = -1
                }
                if (mode.idLovForDefault === null) {
                    mode.idLovForDefault = -1
                }
                if (mode.idLovForMax === null) {
                    mode.idLovForMax = -1
                }

                const obj = JSON.parse(JSON.stringify(mode))
                delete obj.numberOfErrors
                delete obj.defLov
                delete obj.typeLov
                delete obj.maxLov
                delete obj.edited
                this.modesToSave.push(obj)
            })
        },
        async handleSubmit() {
            this.formatDriver()
            this.formatUseMode()

            let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/'
            if (this.driver.id) {
                this.operation = 'update'
                url += this.driver.id
            } else {
                this.operation = 'insert'
            }

            let driverSavedMessage = ''
            const driverSavingErrors = [] as string[]
            await this.sendRequest(url)
                .then((response: AxiosResponse<any>) => {
                    if (this.operation === 'insert') {
                        this.driver = response.data
                    }
                    this.$emit('created', this.driver)
                    driverSavedMessage = 'OK'
                })
                .catch((error: any) => {
                    driverSavedMessage = error.message
                })

            for (let i = 0; i < this.modesToSave.length; i++) {
                const mode = this.modesToSave[i]
                let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/modes/'
                mode.id = this.driver.id
                if (mode.useID != -1) {
                    this.useModeOperation = 'update'
                    url += mode.id
                } else {
                    delete mode.useID
                    this.useModeOperation = 'insert'
                }
                await this.sendUseModeRequest(url, mode).catch((error: any) => driverSavingErrors.push(error?.message))
                this.getModes()
            }

            if (driverSavedMessage === 'OK' && driverSavingErrors.length === 0) {
                this.store.commit('setInfo', {
                    title: this.$t(this.driversManagemenDetailtDescriptor.operation[this.operation].toastTitle),
                    msg: this.$t(this.driversManagemenDetailtDescriptor.operation.success)
                })
            } else if (driverSavingErrors.length > 0) {
                const message = driverSavedMessage === 'OK' ? this.$t('managers.driversManagement.partialSuccessMessage') + '\n\n' : ''
                this.store.commit('setError', {
                    title: this.$t('common.toast.errorTitle'),
                    msg: message.concat(driverSavingErrors.join('\n\n'))
                })
            } else {
                this.store.commit('setError', {
                    title: this.$t('common.toast.errorTitle'),
                    msg: driverSavedMessage
                })
            }
        },
        sendRequest(url: string) {
            if (this.operation === 'insert') {
                return this.$http.post(url, this.driver, { headers: { 'X-Disable-Errors': 'true' } })
            } else {
                return this.$http.put(url, this.driver, { headers: { 'X-Disable-Errors': 'true' } })
            }
        },
        sendUseModeRequest(url: string, useMode: any) {
            if (this.useModeOperation === 'insert') {
                return this.$http.post(url, useMode, { headers: { 'X-Disable-Errors': 'true' } })
            } else {
                return this.$http.put(url, useMode, { headers: { 'X-Disable-Errors': 'true' } })
            }
        },
        setDirty(): void {
            this.$emit('touched')
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
<style lang="scss" scoped>
.kn-flex-grow {
    flex-grow: 1;
}
</style>
