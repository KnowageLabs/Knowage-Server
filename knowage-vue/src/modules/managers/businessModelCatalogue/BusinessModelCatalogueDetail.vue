<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #start>{{ selectedBusinessModel.name }} </template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" data-test="close-button" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

    <TabView class="tabview-custom kn-page-content" v-else>
        <TabPanel>
            <template #header>
                <span>{{ $t('managers.businessModelManager.details') }}</span>
            </template>

            <BusinessModelDetailsCard
                :selectedBusinessModel="selectedBusinessModel"
                :domainCategories="categories"
                :datasourcesMeta="datasources"
                :user="user"
                :toGenerate="toGenerate"
                :readonly="readonly"
                :businessModelVersions="businessModelVersions"
                @fieldChanged="onFieldChange"
                @fileUploaded="uploadedFile = $event"
                @datamartGenerated="loadPage"
                @modelGenerated="loadVersions"
            ></BusinessModelDetailsCard>
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.businessModelManager.metadata') }}</span>
            </template>

            <MetadataCard v-if="businessModelVersions.length > 0 && !readonly" :id="selectedBusinessModel.id"></MetadataCard>
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.businessModelManager.savedVersions') }}</span>
            </template>

            <BusinessModelVersionsCard :id="selectedBusinessModel.id" :versions="businessModelVersions" :readonly="readonly" @touched="setDirty" @deleted="loadVersions"></BusinessModelVersionsCard>
        </TabPanel>

        <TabPanel>
            <template #header v-if="id">
                <span>{{ $t('managers.businessModelManager.drivers') }}</span>
                <Badge :value="invalidDrivers" class="p-ml-2" severity="danger" v-if="invalidDrivers > 0"></Badge>
            </template>

            <BusinessModelDriversCard v-if="id" :id="selectedBusinessModel.id" :drivers="drivers" :driversOptions="analyticalDrivers" :readonly="readonly" @delete="setDriversForDelete"></BusinessModelDriversCard>
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { iBusinessModel, iBusinessModelVersion } from './BusinessModelCatalogue'
    import { AxiosResponse } from 'axios'
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
            type: Number,
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
            uploadingError: false,
            businessModelSavingError: false,
            v$: useValidate() as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.invalidDrivers > 0 || !this.selectedBusinessModel.name || !this.selectedBusinessModel.category || !this.selectedBusinessModel.dataSourceLabel || this.readonly
        },
        props: {
            id: {
                type: Number,
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
                uploadingError: false,
                v$: useValidate() as any
            }
        },
        async loadSelectedBusinessModelData() {
            if (this.id) {
                await this.loadSelectedBusinessModel()
                await this.loadVersions()
                await this.loadDrivers()
            } else {
                this.selectedBusinessModel = { modelLocked: false, smartView: false } as iBusinessModel
                this.businessModelVersions = []
                this.drivers = []
                this.analyticalDrivers = []
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
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/currentuser`).then((response: AxiosResponse<any>) => (this.user = response.data))
            },
            async loadSelectedBusinessModelData() {
                if (this.id) {
                    await this.loadSelectedBusinessModel()
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
            async loadSelectedBusinessModel() {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}`).then((response: AxiosResponse<any>) => (this.selectedBusinessModel = response.data))
            },
            async loadVersions() {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/versions/`).then((response: AxiosResponse<any>) => {
                    this.businessModelVersions = response.data.versions
                    this.toGenerate = response.data.togenerate
                })
            },
            async loadDrivers() {
                this.drivers = []
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers`).then((response: AxiosResponse<any>) =>
                    response.data.forEach((driver: any) => {
                        this.drivers.push({ ...driver, status: 'NOT_CHANGED', numberOfErrors: 0 })
                    })
                )
            },
            async loadAnalyticalDrivers() {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/').then((response: AxiosResponse<any>) => (this.analyticalDrivers = response.data))
            },
            async loadCategories() {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'domains/listValueDescriptionByType?DOMAIN_TYPE=BM_CATEGORY').then((response: AxiosResponse<any>) => (this.categories = response.data))
            },
            async loadDatasources() {
                this.datasources = []
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasources/?type=meta').then((response: AxiosResponse<any>) => response.data.forEach((datasource) => this.datasources.push(datasource.label)))
            },
            formatBusinessModelAnalyticalDriver() {
                const index = this.categories.findIndex((category) => category.VALUE_ID === this.selectedBusinessModel.category)
                this.selectedBusinessModel = { ...this.selectedBusinessModel, category: this.categories[index] }
            },
            setDriversForDelete(drivers: any) {
                this.driversForDelete = drivers
            },
            async handleSubmit() {
                this.loading = true
                if (this.selectedBusinessModel.id) {
                    await this.updateBusinessModel()
                } else {
                    await this.saveBusinessModel()
                }

            if (this.businessModelSavingError) {
                this.businessModelSavingError = false
                this.loading = false
                return
            }

            if (this.selectedBusinessModel.id && this.uploadedFile && !this.uploadingError) {
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
                    this.$router.replace(`/business-model-catalogue/${this.selectedBusinessModel.id}`)
                }
                this.loadPage()
                this.touched = false
                this.$emit('inserted')
                this.uploadingError = false
                this.loading = false
            },
            setUploadingError(title: string, message: string) {
                this.uploadingError = true
                this.$store.commit('setError', { title: this.$t('common.toast.' + title), msg: message })
            },
            async saveBusinessModel() {
                await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/businessmodels/', { ...this.selectedBusinessModel, modelLocker: this.user.userId }).then((response: AxiosResponse<any>) => {
                    if (response.data.errors) {
                        this.setUploadingError('createTitle', response.data.errors[0].message)
                    } else {
                        this.selectedBusinessModel = response.data
                    }
                })
                this.$router.replace(`/business-model-catalogue/${this.selectedBusinessModel.id}`)
            }
            this.loadPage()
            this.touched = false
            this.$emit('inserted')
            this.uploadingError = false
            this.loading = false
        },
        setUploadingError(title: string, message: string) {
            this.uploadingError = true
            this.$store.commit('setError', { title: this.$t('common.toast.' + title), msg: message })
        },
        async saveBusinessModel() {
            if (this.selectedBusinessModel.category.VALUE_ID) {
                this.selectedBusinessModel.category = this.selectedBusinessModel.category.VALUE_ID
            }
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/businessmodels/', { ...this.selectedBusinessModel, modelLocker: this.user.userId })
                .then((response: AxiosResponse<any>) => {
                    if (response.data.errors) {
                        this.setUploadingError('createTitle', response.data.errors[0].message)
                    } else {
                        this.selectedBusinessModel = response.data
                    }
                })
                .catch(() => (this.businessModelSavingError = true))
                .finally(() => this.formatBusinessModelAnalyticalDriver())
        },
        async updateBusinessModel() {
            if (this.selectedBusinessModel.category.VALUE_ID) {
                this.selectedBusinessModel.category = this.selectedBusinessModel.category.VALUE_ID
            }
            await this.$http
                .put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.selectedBusinessModel.id}`, { ...this.selectedBusinessModel, modelLocker: this.user.userId })
                .then((response: AxiosResponse<any>) => {
                    if (response.data.errors) {
                        this.setUploadingError('updateTitle', response.data.errors[0].message)
                    }
                })
            },
            async uploadFile() {
                const formData = new FormData()
                formData.append('file', this.uploadedFile)
                await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.selectedBusinessModel.id}/versions`, formData, { headers: { 'Content-Type': 'multipart/form-data' } }).then((response: AxiosResponse<any>) => {
                    if (response.data.errors) {
                        this.$store.commit('setError', { title: this.$t('managers.businessModelManager.toast.uploadFile'), msg: response.data.errors })
                    } else {
                        this.$store.commit('setInfo', { title: this.$t('managers.businessModelManager.uploadFile'), msg: this.$t('managers.businessModelManager.uploadFileSuccess') })
                        this.uploadedFile = null
                    }
                })
                .catch(() => (this.businessModelSavingError = true))
                .finally(() => this.formatBusinessModelAnalyticalDriver())
        },
        saveActiveVersion(businessModelVersion) {
            this.$http.put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/versions/${businessModelVersion.id}/`).then((response: AxiosResponse<any>) => {
                if (response.data.errors) {
                    this.setUploadingError('updateTitle', response.data.errors[0].message)
                }
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
