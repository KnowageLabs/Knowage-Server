<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-d-flex p-flex-row">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.myModels.title') }}
        </template>
        <template #right>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <KnFabButton icon="fas fa-plus" @click="createNewFederation" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="p-d-flex p-flex-row p-ai-center">
        <InputText id="model-search" class="kn-material-input p-m-3" v-model="searchWord" :placeholder="$t('common.search')" @input="searchItems" data-test="search-input" />
        <SelectButton id="model-select-buttons" v-model="tableMode" :options="selectButtonOptions" @click="onTableModeChange" />
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
                    @openDatasetInQBE="openDatasetInQBE"
                    @editDataset="editDataset"
                    @deleteDataset="deleteDatasetConfirm"
                />
            </template>
        </div>
    </div>

    <DetailSidebar
        :visible="showDetailSidebar"
        :viewType="selectedModel && selectedModel.federation_id ? 'federationDataset' : 'businessModel'"
        :document="selectedModel"
        @openDatasetInQBE="openDatasetInQBE"
        @editDataset="editDataset"
        @deleteDataset="deleteDatasetConfirm"
        @close="showDetailSidebar = false"
        data-test="detail-sidebar"
    />
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

export default defineComponent({
    name: 'workspace-models-view',
    components: { DetailSidebar, KnFabButton, Message, SelectButton, WorkspaceModelsTable, WorkspaceCard },
    emits: ['showMenu', 'toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    data() {
        return {
            mainDescriptor,
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
            loading: false
        }
    },
    computed: {
        hasEnableFederatedDatasetFunctionality(): boolean {
            return this.user.functionalities.includes('EnableFederatedDataset')
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
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/?fileExtension=jar`).then((response: AxiosResponse<any>) => {
                this.businessModels = response.data
                this.businessModels = this.businessModels.map((el: any) => {
                    return { ...el, type: 'businessModel' }
                })
            })
            this.loading = false
        },
        async loadFederatedDatasets() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `federateddataset/`).then((response: AxiosResponse<any>) => {
                this.federatedDatasets = response.data
                this.federatedDatasets = this.federatedDatasets.map((el: any) => {
                    return { ...el, type: 'federatedDataset' }
                })
            })
            this.loading = false
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
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
                    this.filteredItems = items.filter((el: any) => {
                        return el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.description?.toLowerCase().includes(this.searchWord.toLowerCase())
                    })
                }
            }, 250)
        },
        resetSearch() {
            this.searchWord = ''
        },
        openDatasetInQBE() {
            this.$store.commit('setInfo', {
                title: 'Todo',
                msg: 'Functionality not in this sprint'
            })
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
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/federateddataset/${dataset.federation_id}`)
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
        }
    }
})
</script>

<style lang="scss" scoped>
#model-select-buttons {
    margin: 2rem 2rem 2rem auto;
}

#model-search {
    flex: 0.3;
}
</style>
