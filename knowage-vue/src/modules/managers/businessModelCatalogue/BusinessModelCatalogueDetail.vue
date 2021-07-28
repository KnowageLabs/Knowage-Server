<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ selectedBusinessModel.name }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" data-test="close-button" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card" v-else>
        <TabView class="tabview-custom">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.buisnessModelCatalogue.details') }}</span>
                </template>

                <BusinessModelDetailsCard
                    :selectedBusinessModel="selectedBusinessModel"
                    :domainCategories="categories"
                    :datasourcesMeta="datasources"
                    :user="user"
                    :toGenerate="toGenerate"
                    @fieldChanged="onFieldChange"
                    @fileUploaded="uploadedFile = $event"
                    @datamartGenerated="loadPage"
                ></BusinessModelDetailsCard>
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

                <BusinessModelVersionsCard :id="selectedBusinessModel.id" :versions="businessModelVersions" @touched="setDirty" @deleted="loadVersions"></BusinessModelVersionsCard>
            </TabPanel>

            <TabPanel>
                <template #header v-if="id">
                    <span>{{ $t('managers.buisnessModelCatalogue.drivers') }}</span>
                    <Badge :value="invalidDrivers" class="p-ml-2" severity="danger" v-if="invalidDrivers > 0"></Badge>
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
import Badge from 'primevue/badge'
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
        Badge,
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
            user: null as any,
            selectedBusinessModel: {} as iBusinessModel,
            businessModelVersions: [] as iBusinessModelVersion[],
            analyticalDrivers: [],
            drivers: [] as any[],
            driversForDelete: [] as any[],
            categories: [] as any[],
            datasources: [] as any[],
            toGenerate: false,
            uploadedFile: null as any,
            loading: false,
            touched: false,
            operation: 'insert',
            uploadingError: false,
            v$: useValidate() as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.invalidDrivers > 0 || !this.selectedBusinessModel.name || !this.selectedBusinessModel.category || !this.selectedBusinessModel.dataSourceLabel
        },
        invalidDrivers(): number {
            return this.drivers.filter((driver: any) => driver.numberOfErrors > 0).length
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    async created() {
        await this.loadUser()
        await this.loadPage()
    },
    methods: {
        async loadUser() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/currentuser`).then((response) => (this.user = response.data))
        },
        async loadSelectedBusinessModelData() {
            if (this.id) {
                await this.loadselectedBusinessModel()
                await this.loadVersions()
                await this.loadDrivers()

                this.formatBusinessModelAnalyticalDriver()
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
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/versions/`).then((response) => {
                this.businessModelVersions = response.data.versions
                this.toGenerate = response.data.togenerate
            })
        },
        async loadDrivers() {
            this.drivers = []
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers`).then((response) =>
                response.data.forEach((driver: any) => {
                    this.drivers.push({ ...driver, status: 'NOT_CHANGED', numberOfErrors: 0 })
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
        formatBusinessModelAnalyticalDriver() {
            const index = this.categories.findIndex((category) => category.VALUE_ID === this.selectedBusinessModel.category)
            this.selectedBusinessModel = { ...this.selectedBusinessModel, category: this.categories[index] }
        },
        setDriversForDelete(drivers: any) {
            this.driversForDelete = drivers
        },
        async handleSubmit() {
            if (this.selectedBusinessModel.id) {
                await this.updateBusinessModel()
            } else {
                await this.saveBusinessModel()
            }

            if (this.selectedBusinessModel.id && this.uploadedFile && !this.uploadingError) {
                console.log('UPLODADED FILE', this.uploadedFile)
                await this.uploadFile()
            }

            if (this.businessModelVersions.length > 0 && !this.uploadingError) {
                const activeBusinessModelVersion = this.businessModelVersions.find((version) => version.active === true)
                this.saveActiveVersion(activeBusinessModelVersion)
            }

            this.driversForDelete.forEach((driver) => {
                if (!this.uploadingError) {
                    this.deleteDriver(driver.id)
                }
            })

            this.drivers.forEach((driver) => {
                if (driver.status === 'CHANGED' && !this.uploadingError) {
                    delete driver.status
                    delete driver.numberOfErrors
                    if (driver.id) {
                        this.updateDriver(driver)
                    } else {
                        this.saveDriver(driver)
                    }
                }
            })

            if (!this.uploadingError) {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.updateTitle'),
                    msg: this.$t('common.toast.success')
                })
            }
            this.loadPage()
            this.touched = false
            this.$emit('inserted')
            this.uploadingError = false
        },
        setUploadingError(title: string, message: string) {
            this.uploadingError = true
            this.$store.commit('setError', { title: this.$t('common.toast.' + title), msg: message })
        },
        async saveBusinessModel() {
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/businessmodels/', { ...this.selectedBusinessModel, modelLocker: this.user.fullName }).then((response) => {
                if (response.data.errors) {
                    this.setUploadingError('createTitle', response.data.errors[0].message)
                } else {
                    this.selectedBusinessModel = response.data
                }
            })
        },
        async updateBusinessModel() {
            if (this.selectedBusinessModel.category.VALUE_ID) {
                this.selectedBusinessModel.category = this.selectedBusinessModel.category.VALUE_ID
            }
            await axios
                .put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.selectedBusinessModel.id}`, this.selectedBusinessModel)
                .then((response) => {
                    if (response.data.errors) {
                        this.setUploadingError('updateTitle', response.data.errors[0].message)
                    } else {
                        this.selectedBusinessModel = response.data
                    }
                })
                .finally(() => this.formatBusinessModelAnalyticalDriver())
        },
        saveActiveVersion(businessModelVersion) {
            axios.put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/versions/${businessModelVersion.id}/`).then((response) => {
                if (response.data.errors) {
                    this.setUploadingError('updateTitle', response.data.errors[0].message)
                }
            })
        },
        async uploadFile() {
            const formData = new FormData()
            formData.append('file', this.uploadedFile)
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.selectedBusinessModel.id}/versions`, formData, { headers: { 'Content-Type': 'multipart/form-data' } }).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: this.$t('managers.buisnessModelCatalogue.toast.uploadFile'), msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { title: this.$t('managers.buisnessModelCatalogue.uploadFile'), msg: this.$t('managers.buisnessModelCatalogue.uploadFileSuccess') })
                    this.uploadedFile = null
                }
            })
        },
        saveDriver(driver: any) {
            axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers`, { ...driver, parID: driver.parameter.id }).then((response) => {
                if (response.data.errors) {
                    this.setUploadingError('saveTitle', response.data.errors[0].message)
                }
            })
        },
        updateDriver(driver: any) {
            axios.put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers/${driver.id}`, { ...driver, parID: driver.parameter.id }).then((response) => {
                if (response.data.errors) {
                    this.setUploadingError('updateTitle', response.data.errors[0].message)
                }
            })
        },
        deleteDriver(driverId: number) {
            axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers/${driverId}`).then((response) => {
                if (response.data.errors) {
                    this.setUploadingError('deleteTitle', response.data.errors[0].message)
                }
            })
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
            this.$router.push('/business-model-catalogue')
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
