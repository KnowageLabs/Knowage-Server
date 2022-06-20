<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-d-flex p-flex-row">
        <template #start>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.myModels.title') }}
        </template>
        <template #end>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <KnFabButton v-if="tableMode === 'Federated'" icon="fas fa-plus" @click="createNewFederation" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

    <div class="p-d-flex p-flex-row p-ai-center p-flex-wrap">
        <InputText class="kn-material-input p-m-3 model-search" v-model="searchWord" :placeholder="$t('common.search')" @input="searchItems" data-test="search-input" />
        <span class="p-float-label p-mr-auto model-search">
            <MultiSelect class="kn-material-input kn-width-full" :style="mainDescriptor.style.multiselect" v-model="selectedCategories" :options="modelCategories" optionLabel="VALUE_CD" @change="searchItems" :filter="true" />
            <label class="kn-material-input-label"> {{ $t('common.type') }} </label>
        </span>
        <SelectButton class="p-mx-2" v-model="tableMode" :options="selectButtonOptions" @click="onTableModeChange" />
    </div>

    <div class="p-m-2 kn-overflow">
        <WorkspaceModelsTable v-if="!toggleCardDisplay" :propItems="filteredItems" @selected="setSelectedModel" @openDatasetInQBEClick="openDatasetInQBE" @editDatasetClick="editDataset" @deleteDatasetClick="deleteDatasetConfirm" data-test="models-table"></WorkspaceModelsTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2" data-test="card-container">
            <Message v-if="filteredItems.length === 0" class="kn-flex p-m-2" severity="info" :closable="false" :style="mainDescriptor.style.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <template v-else>
                <WorkspaceCard
                    v-for="(document, index) of filteredItems"
                    :key="index"
                    :viewType="document && document.federation_id ? 'federationDataset' : 'businessModel'"
                    :document="document"
                    @openSidebar="setSelectedModel"
                    @openDatasetInQBE="openDatasetInQBE($event)"
                    @editDataset="editDataset"
                    @deleteDataset="deleteDatasetConfirm"
                    @monitoring="showMonitoring = !showMonitoring"
                />
            </template>
        </div>
    </div>

    <DetailSidebar
        :visible="showDetailSidebar"
        :viewType="selectedModel && selectedModel.federation_id ? 'federationDataset' : 'businessModel'"
        :document="selectedModel"
        @openDatasetInQBE="openDatasetInQBE($event)"
        @editDataset="editDataset"
        @deleteDataset="deleteDatasetConfirm"
        @monitoring="showMonitoring = !showMonitoring"
        @close="showDetailSidebar = false"
        data-test="detail-sidebar"
    />

    <QBE v-if="qbeVisible" :visible="qbeVisible" :dataset="selectedQbeDataset" @close="closeQbe" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IBusinessModel, IFederatedDataset } from '../../Workspace'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import Message from 'primevue/message'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import SelectButton from 'primevue/selectbutton'
import WorkspaceModelsTable from './tables/WorkspaceModelsTable.vue'
import { AxiosResponse } from 'axios'
import QBE from '@/modules/qbe/QBE.vue'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'workspace-models-view',
    components: { MultiSelect, DetailSidebar, KnFabButton, Message, SelectButton, WorkspaceModelsTable, WorkspaceCard, QBE },
    emits: ['showMenu', 'toggleDisplayView', 'showQbeDialog'],
    props: { toggleCardDisplay: { type: Boolean } },
    data() {
        return {
            mainDescriptor,
            selectedCategories: [] as any,
            selectedCategoryIds: [] as any,
            businessModels: [] as IBusinessModel[],
            federatedDatasets: [] as IFederatedDataset[],
            allItems: [] as (IBusinessModel | IFederatedDataset)[],
            filteredItems: [] as (IBusinessModel | IFederatedDataset)[],
            tableMode: 'All',
            selectButtonOptions: ['Business'],
            selectedModel: null as IBusinessModel | IFederatedDataset | null,
            searchWord: '' as string,
            showDetailSidebar: false,
            user: null as any,
            loading: false,
            datasetDrivers: null as any,
            datasetName: '',
            qbeVisible: false,
            selectedQbeDataset: null,
            modelCategories: [] as any
        }
    },
    computed: {
        hasEnableFederatedDatasetFunctionality(): boolean {
            if (this.user && this.user.functionalities) return this.user.functionalities.includes('EnableFederatedDataset')
            else return false
        }
    },
    watch: {
        tableMode() {
            this.resetSearch()
            this.selectedModel = null
        }
    },
    async created() {
        this.user = (this.$store.state as any).user
        await this.getModelCategories()
        await this.loadBusinessModels()
        if (this.hasEnableFederatedDatasetFunctionality) {
            await this.loadFederatedDatasets()
            this.selectButtonOptions.push('Federated')
            this.selectButtonOptions.push('All')
        }
        this.loadAllItems()
    },
    methods: {
        loadAllItems() {
            this.allItems = [...this.businessModels, ...this.federatedDatasets] as (IBusinessModel | IFederatedDataset)[]
            this.filteredItems = [...this.allItems] as (IBusinessModel | IFederatedDataset)[]
        },
        async loadBusinessModels() {
            this.loading = true
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/?fileExtension=jar`).then((response: AxiosResponse<any>) => {
                this.businessModels = response.data
                this.businessModels = this.businessModels.map((el: any) => {
                    return { ...el, type: 'businessModel' }
                })
            })
            this.loading = false
        },
        async loadFederatedDatasets() {
            this.loading = true
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `federateddataset/`).then((response: AxiosResponse<any>) => {
                this.federatedDatasets = response.data
                this.federatedDatasets = this.federatedDatasets.map((el: any) => {
                    return { ...el, type: 'federatedDataset' }
                })
            })
            this.loading = false
        },
        async getModelCategories() {
            this.loading = true
            return this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `domainsforfinaluser/bm-categories`).then((response: AxiosResponse<any>) => {
                this.modelCategories = [...response.data]
            })
        },
        searchItems(event?) {
            setTimeout(() => {
                if (event?.value) {
                    this.selectedCategoryIds = [] as any
                    event.value.forEach((el) => {
                        this.selectedCategoryIds.push(el.VALUE_ID)
                    })
                }
                if (!this.searchWord.trim().length && this.selectedCategoryIds.length == 0) {
                    this.filteredItems = [...this.allItems] as (IBusinessModel | IFederatedDataset)[]
                } else {
                    let items = [] as (IBusinessModel | IFederatedDataset)[]
                    if (this.tableMode === 'Business') {
                        items = this.businessModels as (IBusinessModel | IFederatedDataset)[]
                    } else if (this.tableMode === 'Federated') {
                        items = this.federatedDatasets as (IBusinessModel | IFederatedDataset)[]
                    } else {
                        items = this.allItems
                    }

                    if (this.selectedCategoryIds.length > 0) {
                        this.filteredItems = items.filter((el: any) => {
                            return this.selectedCategoryIds.includes(el.category) && (el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.description?.toLowerCase().includes(this.searchWord.toLowerCase()))
                        })
                    } else {
                        this.filteredItems = items.filter((el: any) => {
                            return el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.description?.toLowerCase().includes(this.searchWord.toLowerCase())
                        })
                    }
                }
            }, 250)
        },
        resetSearch() {
            this.searchWord = ''
        },
        openDatasetInQBE(dataset: any) {
            if (import.meta.env.VUE_APP_USE_OLD_QBE_IFRAME == 'true') {
                this.$emit('showQbeDialog', dataset)
            } else {
                this.selectedQbeDataset = dataset
                this.qbeVisible = true
            }
        },
        createNewFederation() {
            this.$router.push('models/federation-definition/new-federation')
        },
        editDataset(dataset: IFederatedDataset) {
            this.$router.push(`models/federation-definition/${dataset.federation_id}`)
        },
        deleteDatasetConfirm(dataset: IFederatedDataset) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => await this.deleteDataset(dataset)
            })
        },
        async deleteDataset(dataset: IFederatedDataset) {
            this.loading = true
            await this.$http
                .delete(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/federateddataset/${dataset.federation_id}`)
                .then(async () => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    await this.reloadFederatedDatasets()
                })
                .catch(() => {})
            this.loading = false
        },
        async reloadFederatedDatasets() {
            await this.loadFederatedDatasets()
            this.loadAllItems()
            this.filteredItems = [...this.federatedDatasets]
        },
        setSelectedModel(model: IBusinessModel | IFederatedDataset) {
            this.selectedModel = model
            this.showDetailSidebar = true
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
        },
        onTableModeChange() {
            this.selectedCategoryIds = [] as any
            this.selectedCategories = [] as any
            switch (this.tableMode) {
                case 'Business':
                    this.filteredItems = [...this.businessModels]
                    break
                case 'Federated':
                    this.filteredItems = [...this.federatedDatasets]
                    break
                case 'All':
                    this.filteredItems = [...this.allItems]
            }
        },
        closeQbe() {
            this.qbeVisible = false
            this.selectedQbeDataset = null
        }
    }
})
</script>

<style lang="scss" scoped>
.model-search {
    flex: 0.3;
}
</style>
