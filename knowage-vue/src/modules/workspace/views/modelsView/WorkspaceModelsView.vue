<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-d-flex p-flex-row">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.myModels.title') }}
        </template>
        <template #right>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <KnFabButton v-if="tableMode === 'Federated'" icon="fas fa-plus" @click="createNewFederation" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="p-d-flex p-flex-row p-ai-center">
        <InputText id="model-search" class="kn-material-input p-m-3" v-model="searchWord" :placeholder="$t('common.search')" @input="searchItems" />
        <SelectButton id="model-select-buttons" v-model="tableMode" :options="selectButtonOptions" />
    </div>

    <div class="p-m-2 overflow">
        <WorkspaceModelsTable class="p-m-2" v-if="!toggleCardDisplay" :propItems="tableItems" :tableMode="tableMode" @selected="setSelectedModel" @openDatasetInQBEClick="openDatasetInQBE" @editDatasetClick="editDataset" @deleteDatasetClick="deleteDatasetConfirm"></WorkspaceModelsTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2">
            <WorkspaceCard v-for="(document, index) of tableItems" :key="index" :viewType="selectedModel && selectedModel.federation_id ? 'federationDataset' : 'businessModel'" :document="document" />
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
    />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IBusinessModel, IFederatedDataset } from '../../Workspace'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import SelectButton from 'primevue/selectbutton'
import WorkspaceModelsTable from './tables/WorkspaceModelsTable.vue'

export default defineComponent({
    name: 'workspace-models-view',
    components: { DetailSidebar, KnFabButton, SelectButton, WorkspaceModelsTable, WorkspaceCard },
    emits: ['showMenu', 'toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    data() {
        return {
            businessModels: [] as IBusinessModel[],
            federatedDatasets: [] as IFederatedDataset[],
            filteredItems: [] as IBusinessModel[] | IFederatedDataset[],
            tableMode: 'Business',
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
        },
        tableItems(): IBusinessModel[] | IFederatedDataset[] {
            if (this.searchWord !== '') {
                return this.filteredItems
            } else {
                return this.tableMode === 'Business' ? this.businessModels : this.federatedDatasets
            }
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
        }
    },
    methods: {
        async loadBusinessModels() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/?fileExtension=jar`).then((response) => (this.businessModels = response.data))
            this.loading = false
            // console.log('LOADED BUSINESS MODELS: ', this.businessModels)
        },
        async loadFederatedDatasets() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `federateddataset/`).then((response) => (this.federatedDatasets = response.data))
            this.loading = false
            // console.log('LOADED FEDERATED DATASETS: ', this.federatedDatasets)
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredItems = this.tableMode === 'Business' ? [...this.businessModels] : ([...this.federatedDatasets] as IBusinessModel[] | IFederatedDataset[])
                } else {
                    this.filterItems()
                }
            }, 250)
        },
        filterItems() {
            if (this.tableMode === 'Business') {
                this.filteredItems = this.businessModels.filter((el: any) => {
                    return el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.description?.toLowerCase().includes(this.searchWord.toLowerCase())
                })
            } else {
                this.filteredItems = this.federatedDatasets.filter((el: any) => {
                    return el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.label?.toLowerCase().includes(this.searchWord.toLowerCase())
                })
            }
        },
        resetSearch() {
            this.searchWord = ''
        },
        openDatasetInQBE(dataset: IBusinessModel | IFederatedDataset) {
            console.log('openDatasetInQBE clicked! ', dataset)
            this.$store.commit('setInfo', {
                title: 'Todo',
                msg: 'Functionality not in this sprint'
            })
        },
        createNewFederation() {
            this.$router.push('models/federation-definition/new-federation')
        },
        editDataset(dataset: IFederatedDataset) {
            console.log('editDataset clicked! ', dataset)
            this.$router.push(`models/federation-definition/${dataset.federation_id}`)
        },
        deleteDatasetConfirm(dataset: IFederatedDataset) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDataset(dataset)
            })
        },
        async deleteDataset(dataset: IFederatedDataset) {
            this.loading = true
            await this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/federateddataset/${dataset.federation_id}`)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.loadFederatedDatasets()
                })
                .catch(() => {})
            this.loading = false
        },
        setSelectedModel(model: IBusinessModel | IFederatedDataset) {
            this.selectedModel = model
            this.showDetailSidebar = true
            console.log('SELECTED MODEL: ', this.selectedModel)
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
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
