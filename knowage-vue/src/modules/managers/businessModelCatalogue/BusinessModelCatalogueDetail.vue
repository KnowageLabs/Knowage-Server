<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ selectedBusinessModel.name }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" :disabled="buttonDisabled" />
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

                <BusinessModelDetailsCard :selectedBusinessModel="selectedBusinessModel" :domainCategories="categories" :datasourcesMeta="datasources"></BusinessModelDetailsCard>
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

                <BusinessModelDriversCard v-if="id" :id="selectedBusinessModel.id" :drivers="drivers" :driversOptions="analyticalDrivers"></BusinessModelDriversCard>
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
            drivers: [],
            categories: [] as any[],
            datasources: [] as any[],
            loading: false,
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
            await this.loadSelectedBusinessModelData()
        }
    },
    async created() {
        this.loading = true
        await this.loadAnalyticalDrivers()
        await this.loadCategories()
        await this.loadDatasources()
        await this.loadSelectedBusinessModelData()
        this.loading = false
        console.log('SELECTED BUSINESS MODEL: ', this.selectedBusinessModel)
        console.log('SELECTED BUSINESS VERSIONS: ', this.businessModelVersions)
        console.log('CATEGORIES: ', this.categories)
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
                this.selectedBusinessModel = {} as iBusinessModel
                this.businessModelVersions = []
                this.drivers = []
                this.analyticalDrivers = []
                this.categories = []
            }
        },
        async loadselectedBusinessModel() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}`).then((response) => (this.selectedBusinessModel = response.data))
        },
        async loadVersions() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/versions/`).then((response) => (this.businessModelVersions = response.data.versions))
        },
        async loadDrivers() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.id}/drivers`).then((response) => (this.drivers = response.data))
        },
        async loadAnalyticalDrivers() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/').then((response) => (this.analyticalDrivers = response.data))
        },
        async loadCategories() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'domains/listValueDescriptionByType?DOMAIN_TYPE=BM_CATEGORY').then((response) => (this.categories = response.data))
        },
        async loadDatasources() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasources/?type=meta').then((response) => (this.datasources = response.data))
        },
        async handleSubmit() {
            console.log('handleSubmit()')
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
