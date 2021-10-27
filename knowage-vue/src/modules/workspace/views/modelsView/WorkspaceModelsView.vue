<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-d-flex p-flex-row">
        <template #left>
            {{ $t('workspace.myModels.title') }}
        </template>

        <template #right> <KnFabButton v-if="tableMode === 'Federated'" icon="fas fa-plus" @click="createNewFederation"></KnFabButton> </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="p-d-flex p-flex-row p-ai-center">
        <InputText id="model-search" class="kn-material-input p-m-3" v-model="searchWord" :placeholder="$t('common.search')" @input="searchItems" />
        <SelectButton id="model-select-buttons" v-model="tableMode" :options="selectButtonOptions" />
    </div>
    <WorkspaceModelsTable class="p-m-2" :propItems="tableItems" :tableMode="tableMode" @openDatasetInQBEClick="openDatasetInQBE" @editDatasetClick="editDataset" @deleteDatasetClick="deleteDatasetConfirm"></WorkspaceModelsTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IBusinessModel, IFederatedDataset } from '../../Workspace'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import SelectButton from 'primevue/selectbutton'
import WorkspaceModelsTable from './tables/WorkspaceModelsTable.vue'

export default defineComponent({
    name: 'workspace-models-view',
    components: { KnFabButton, SelectButton, WorkspaceModelsTable },
    data() {
        return {
            businessModels: [] as IBusinessModel[],
            federatedDatasets: [] as IFederatedDataset[],
            filteredItems: [] as IBusinessModel[] | IFederatedDataset[],
            tableMode: 'Business',
            selectButtonOptions: ['Business'],
            searchWord: '' as string,
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
            this.$store.commit('setInfo', {
                title: 'Todo',
                msg: 'Functionality not in this sprint'
            })
        },
        editDataset(dataset: IBusinessModel | IFederatedDataset) {
            console.log('editDataset clicked! ', dataset)
            this.$store.commit('setInfo', {
                title: 'Todo',
                msg: 'Functionality not in this sprint'
            })
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
                    this.loadFederatedDatasets()
                })
                .catch(() => {})
            this.loading = false
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
