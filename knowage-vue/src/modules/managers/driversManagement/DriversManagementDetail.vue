<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #left>{{ title }} </template>
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center" style="overflow:auto">
        <DriversDetailCard :selectedDriver="driver" :types="filteredTypes" @touched="setDirty"></DriversDetailCard>
        <UseMode :propModes="modes" :roles="roles" :constraints="constraints" :layers="layers" :lovs="lovs" :selectionTypes="filteredSelectionTypes" :isDate="isDateType"></UseMode>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import DriversDetailCard from './DriversDetailCard.vue'
import UseMode from './useModes/UseMode.vue'
import axios from 'axios'
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
            return !this.driver.label || !this.driver.name || !this.driver.typeId
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
            return this.driver.type === 'DATE'
        }
    },
    data() {
        return {
            driver: {} as any,
            types: [] as any[],
            modes: [] as any[],
            roles: [] as any[],
            constraints: [] as any[],
            selectionTypes: [] as any[],
            layers: [] as any[],
            lovs: [] as any[],
            operation: 'insert',
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
        this.loadAll()
    },

    methods: {
        async getTypes() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '/domains/listValueDescriptionByType?DOMAIN_TYPE=PAR_TYPE').then((response) => (this.types = response.data))
        },
        async getModes() {
            if (this.driver.id) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/' + this.driver.id + '/modes/').then((response) => (this.modes = response.data))
            } else this.modes = []
        },
        async getRoles() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles').then((response) => (this.roles = response.data))
        },
        async getConstraints() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/checks').then((response) => (this.constraints = response.data))
        },
        async getselectionTypes() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '/domains/listValueDescriptionByType?DOMAIN_TYPE=SELECTION_TYPE').then((response) => (this.selectionTypes = response.data))
        },
        async getLayers() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '/2.0/analyticalDriversee/layers').then((response) => (this.layers = response.data))
        },
        async getLovs() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all').then((response) => (this.lovs = response.data))
        },
        loadAll() {
            this.getTypes()
            this.getRoles()
            this.getConstraints()
            this.getselectionTypes()
            this.getLayers()
            this.getLovs()
        },
        formatDriver() {
            this.driver.length = 0
            let selectedType = this.types.filter((val) => {
                return val.VALUE_ID === this.driver.typeId
            })
            this.driver.type = selectedType[0].VALUE_CD
        },
        async handleSubmit() {
            this.formatDriver()

            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/'
            if (this.driver.id) {
                this.operation = 'update'
                url += this.driver.id
            } else {
                this.operation = 'insert'
            }

            await this.sendRequest(url)
                .then((response) => {
                    if (this.operation === 'insert') {
                        this.driver = response.data
                    }
                    this.$emit('created', this.driver)
                    this.$store.commit('setInfo', {
                        title: this.$t(this.driversManagemenDetailtDescriptor.operation[this.operation].toastTitle),
                        msg: this.$t(this.driversManagemenDetailtDescriptor.operation.success)
                    })
                })
                .catch((error) => {
                    this.$store.commit('setError', {
                        title: this.$t('managers.constraintManagment.saveError'),
                        msg: error.message
                    })
                })
        },
        sendRequest(url: string) {
            if (this.operation === 'insert') {
                return axios.post(url, this.driver)
            } else {
                return axios.put(url, this.driver)
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
