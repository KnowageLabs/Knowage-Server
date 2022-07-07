<template>
    <div class="kn-page">
        <ProgressSpinner class="kn-progress-spinner" v-if="loadingVersion" />

        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.datasetManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showDetail" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <KnListBox :options="listOfDatasets" :settings="mainDescriptor.knListSettings" @click="showDetail" @clone.stop="emitCloneDataset" @delete.stop="deleteDataset" />
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <router-view
                    :scopeTypes="scopeTypes"
                    :categoryTypes="categoryTypes"
                    :datasetTypes="datasetTypes"
                    :transformationDataset="transformationDataset"
                    :scriptTypes="scriptTypes"
                    :dataSources="dataSources"
                    :businessModels="businessModels"
                    :pythonEnvironments="pythonEnvironments"
                    :rEnvironments="rEnvironments"
                    :metaSourceResource="metaSourceResource"
                    :datasetToCloneId="datasetToCloneId"
                    :availableTags="tags"
                    @loadingOlderVersion="loadingVersion = true"
                    @olderVersionLoaded="loadingVersion = false"
                    @touched="touched = true"
                    @created="onCreate"
                    @updated="onUpdate"
                    @showSavingSpinner="loadingVersion = true"
                    @hideSavingSpinner="loadingVersion = false"
                    @close="closeDetailConfirm"
                />
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import mainDescriptor from './DatasetManagementDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import ProgressSpinner from 'primevue/progressspinner'

export default defineComponent({
    name: 'dataset-management',
    components: { FabButton, KnListBox, ProgressSpinner },
    data() {
        return {
            mainDescriptor,
            loading: false,
            listOfDatasets: [] as any,
            reverseOrdering: false,
            loadingVersion: false,
            columnOrdering: '',
            touched: false,
            scopeTypes: [] as any,
            categoryTypes: [] as any,
            datasetTypes: [] as any,
            transformationDataset: {} as any,
            scriptTypes: [] as any,
            dataSources: [] as any,
            businessModels: [] as any,
            pythonEnvironments: [] as any,
            rEnvironments: [] as any,
            metaSourceResource: [] as any,
            tags: [] as any,
            datasetToCloneId: null
        }
    },
    created() {
        this.getAllPersistentData()
    },
    methods: {
        //#region ===================== Get All Data and Format ====================================================
        getEnvironmentByConfiguration(configuration: string) {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/category/${configuration}`)
        },
        buildEnvironments(environmentsArray) {
            return environmentsArray.map((environment) => ({ label: environment.label, value: environment.valueCheck }))
        },
        async getEnvironmentData() {
            this.getEnvironmentByConfiguration('PYTHON_CONFIGURATION').then((response: AxiosResponse<any>) => (this.pythonEnvironments = this.buildEnvironments(response.data)))
            this.getEnvironmentByConfiguration('R_CONFIGURATION').then((response: AxiosResponse<any>) => (this.rEnvironments = this.buildEnvironments(response.data)))
        },
        getDomainByType(type: string) {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=${type}`)
        },
        async getDomainData() {
            this.getDomainByType('DS_SCOPE').then((response: AxiosResponse<any>) => (this.scopeTypes = response.data))
            this.getDomainByType('DATASET_CATEGORY').then((response: AxiosResponse<any>) => (this.categoryTypes = response.data))
            this.getDomainByType('DATA_SET_TYPE').then(
                (response: AxiosResponse<any>) =>
                    (this.datasetTypes = response.data.filter((cd) => {
                        return cd.VALUE_CD != 'Custom' && cd.VALUE_CD != 'Federated'
                    }))
            )
            this.getDomainByType('TRANSFORMER_TYPE').then((response: AxiosResponse<any>) => (this.transformationDataset = response.data[0]))
            this.getDomainByType('SCRIPT_TYPE').then((response: AxiosResponse<any>) => (this.scriptTypes = response.data))
        },
        async getDatasources() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasources`).then((response: AxiosResponse<any>) => (this.dataSources = response.data))
        },
        async getBusinessModels() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels`).then((response: AxiosResponse<any>) => (this.businessModels = response.data))
        },
        async getMetaSourceResource() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/metaSourceResource/`).then((response: AxiosResponse<any>) => (this.metaSourceResource = response.data))
        },
        async getTags() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/tags/`).then((response: AxiosResponse<any>) => (this.tags = response.data))
        },
        async getDatasets() {
            let url = '{"reverseOrdering":false,"columnOrdering":""}'
            this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/catalog?offset=0&fetchSize=0&ordering=` + encodeURI(url))
                .then((response: AxiosResponse<any>) => (this.listOfDatasets = [...response.data.root]))
                .finally(() => (this.loading = false))
        },
        async getAllPersistentData() {
            this.loading = true
            await this.getEnvironmentData()
            await this.getDomainData()
            await this.getMetaSourceResource()
            await this.getDatasources()
            await this.getBusinessModels()
            await this.getTags()
            await this.getDatasets()
        },
        //#endregion ================================================================================================

        //#region ===================== List Functionalities ====================================================
        showDetail(event) {
            const path = event.item ? `/dataset-management/${event.item.id}` : '/dataset-management/new-dataset'
            this.dirtyCheck(path)
        },
        closeDetailConfirm() {
            const path = '/dataset-management'
            this.dirtyCheck(path)
        },
        dirtyCheck(path) {
            if (!this.touched) {
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
                    }
                })
            }
        },

        deleteDataset(event) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.uppercaseDelete'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.$http
                        .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${event.item.label}/`)
                        .then(() => {
                            this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                            this.loading = true

                            this.getDatasets()
                            if (event.item.id == this.$route.params.id) {
                                this.$router.push('/dataset-management')
                                this.touched = false
                            }
                        })
                        .catch()
                }
            })
        },
        emitCloneDataset(event) {
            this.$router.push('/dataset-management/new-dataset')
            setTimeout(() => {
                this.datasetToCloneId = event.item.id
            }, 200)
        },
        //#endregion ================================================================================================

        onCreate(event) {
            this.touched = false
            this.getDatasets()
            this.$router.push(`/dataset-management/${event.data.id}`)
        },
        onUpdate() {
            this.touched = false
            this.getDatasets()
        }
    }
})
</script>
