<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ selectedBusinessModel.name }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card">
        <TabView class="tabview-custom">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.buisnessModelCatalogue.details') }}</span>
                </template>

                <BusinessModelDetailsCard :selectedBusinessModel="selectedBusinessModel" :domainCategories="categories" :datasourcesMeta="datasources" @fieldChanged="onFieldChange" @fileUploaded="uploadedFile = $event"></BusinessModelDetailsCard>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.buisnessModelCatalogue.metadata') }}</span>
                </template>

                <MetadataCard v-if="businessModelVersions.length > 0" :id="selectedBusinessModel.id"></MetadataCard>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.buisnessModelCatalogue.savedVersions') }}</span>
                </template>

                <BusinessModelVersionsCard :id="selectedBusinessModel.id" :versions="businessModelVersions" @deleted="loadVersions"></BusinessModelVersionsCard>
            </TabPanel>

            <TabPanel>
                <template #header v-if="id">
                    <span>{{ $t('managers.buisnessModelCatalogue.drivers') }}</span>
                </template>

                <BusinessModelDriversCard v-if="id" :id="selectedBusinessModel.id" :drivers="drivers" :driversOptions="analyticalDrivers" @delete="setDriversForDelete"></BusinessModelDriversCard>
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iBusinessModel, iBusinessModelVersion } from './BusinessModelCatalogue'
import axios from 'axios'
import BusinessModelDetailsCard from './cards/businessModelDetailsCard/BusinessModelDetailsCard.vue'
import BusinessModelDriversCard from './cards/businessModelDriversCard/BusinessModelDriversCard.vue'
import BusinessModelVersionsCard from './cards/businessModelVersionsCard/BusinessModelVersionsCard.vue'
import MetadataCard from './cards/metadataCard/MetadataCard.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'business-model-catalogue-detail',
    components: {
        BusinessModelDetailsCard,
        BusinessModelDriversCard,
        BusinessModelVersionsCard,
        MetadataCard,
        TabView,
        TabPanel
    },
    props: {
        id: {
            type: String,
            required: false
        }
    },
    emits: ['touched', 'closed', 'inserted'],
    data() {
        return {
            selectedBusinessModel: {} as iBusinessModel,
            businessModelVersions: [] as iBusinessModelVersion[],
            analyticalDrivers: [],
            drivers: [] as any[],
            driversForDelete: [] as any[],
            categories: [] as any[],
            datasources: [] as any[],
            uploadedFile: null as any,
            loading: false,
            touched: false,
            operation: 'insert',
            v$: useValidate() as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    async created() {
        await this.loadPage()
        console.log('SELECTED BUSINESS MODEL: ', this.selectedBusinessModel)
        console.log('SELECTED BUSINESS VERSIONS: ', this.businessModelVersions)
        console.log('CATEGORIES: ', this.categories)
        console.log('DATASOURCES: ', this.datasources)
        console.log('ANALYTICAL DRIVERS: ', this.analyticalDrivers)
        console.log('DRIVERS: ', this.drivers)
    },
    methods: {
        async loadSelectedBusinessModelData() {
            if (this.id) {
                await this.loadselectedBusinessModel()
                await this.loadVersions()
                await this.loadDrivers()

                const index = this.categories.findIndex((category) => category.VALUE_ID === this.selectedBusinessModel.category)
                this.selectedBusinessModel = { ...this.selectedBusinessModel, category: this.categories[index] }
                console.log('BM CATEGORY', this.selectedBusinessModel.category)
            } else {
                this.selectedBusinessModel = { modelLocked: false, smartView: false } as iBusinessModel
                this.businessModelVersions = []
                this.drivers = []
                this.analyticalDrivers = []
            }
        },
        async loadselectedBusinessModel() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}`).then((response) => (this.selectedBusinessModel = response.data))
        },
        async loadVersions() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/versions/`).then((response) => (this.businessModelVersions = response.data.versions))
        },
        async loadDrivers() {
            this.drivers = []
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers`).then((response) =>
                response.data.forEach((driver: any) => {
                    this.drivers.push({ ...driver, status: 'NOT_CHANGED' })
                })
            )
        },
        async loadAnalyticalDrivers() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/').then((response) => (this.analyticalDrivers = response.data))
        },
        async loadCategories() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'domains/listValueDescriptionByType?DOMAIN_TYPE=BM_CATEGORY').then((response) => (this.categories = response.data))
        },
        async loadDatasources() {
            this.datasources = []
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasources/?type=meta').then((response) => response.data.forEach((datasource) => this.datasources.push(datasource.label)))
        },
        setDriversForDelete(drivers: any) {
            this.driversForDelete = drivers
        },
        async handleSubmit() {
            console.log('DRIVERS IN SUBMIT', this.drivers)
            console.log('DRIVERS IN SUBMIT FOR DELETE', this.driversForDelete)
            this.driversForDelete.forEach((driver) => this.deleteDriver(driver.id))
            console.log('DRIVERS AFTER SUBMIT FOR DELETE', this.driversForDelete)

            this.drivers.forEach((driver) => {
                if (driver.status === 'CHANGED') {
                    if (driver.id) {
                        console.log('EDITED DRIVER!!! ', driver)
                        delete driver.status
                        this.updateDriver(driver)
                    } else {
                        console.log('NEW DRIVER!!! ', driver)
                        delete driver.status
                        this.saveDriver(driver)
                    }
                }
            })

            // BM SUBMIT
            console.log('SELECTED BM FOR SUBMIT', this.selectedBusinessModel)
            await this.saveBusinessModel()
            console.log('SLECTED BM ID AFTER POST', this.selectedBusinessModel.id)
            console.log('UPLOADED FILE', this.uploadedFile)
            if (this.selectedBusinessModel.id) {
                console.log('called upload')
                await this.uploadFile()
            }

            console.log('test', this.selectedBusinessModel)
        },
        async saveBusinessModel() {
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/businessmodels/', this.selectedBusinessModel).then((response) => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.createTitle'),
                    msg: this.$t('common.toast.success')
                })

                this.selectedBusinessModel = response.data
            })
        },
        async uploadFile() {
            const formData = new FormData()
            formData.append('file', this.uploadedFile)
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.selectedBusinessModel.id}/versions`, formData, { headers: { 'Content-Type': 'multipart/form-data' } }).then((response) => {
                // TODO CHANGE ERRORS
                if (response.data.errors) {
                    this.$store.commit('setError', { title: this.$t('managers.mondrianSchemasManagement.toast.uploadFile.error'), msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { title: this.$t('managers.mondrianSchemasManagement.toast.uploadFile.uploaded'), msg: this.$t('managers.mondrianSchemasManagement.toast.uploadFile.ok') })
                }
            })
        },
        saveDriver(driver: any) {
            axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers`, { ...driver, parID: driver.parameter.id }).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.createTitle'),
                    msg: this.$t('common.toast.success')
                })
            })
        },
        updateDriver(driver: any) {
            axios.put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers/${driver.id}`, { ...driver, parID: driver.parameter.id }).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.updateTitle'),
                    msg: this.$t('common.toast.success')
                })
            })
        },
        deleteDriver(driverId: number) {
            axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers/${driverId}`)
        },
        async loadPage() {
            this.loading = true
            await this.loadAnalyticalDrivers()
            await this.loadCategories()
            await this.loadDatasources()
            await this.loadSelectedBusinessModelData()
            this.loading = false
        },
        onFieldChange(event: any) {
            this.selectedBusinessModel[event.fieldName] = event.value
            this.touched = true
            this.$emit('touched')
        },
        setDirty() {
            this.$emit('touched')
        },
        closeTemplate() {
            // this.$router.push('')
            this.$emit('closed')
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
