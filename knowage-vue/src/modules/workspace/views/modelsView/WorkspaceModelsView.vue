<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-d-flex p-flex-row">
        <template #left>
            {{ $t('workspace.myModels.title') }}
        </template>

        <template #right> </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="p-d-flex p-flex-row p-ai-center">
        <InputText id="model-search" class="kn-material-input p-m-3" v-model="searchWord" :placeholder="$t('common.search')" @input="searchItems" />
        <SelectButton id="model-select-buttons" v-model="tableMode" :options="workspaceModelsViewDescriptor.selectButtonOptions" />
    </div>
    <WorkspaceModelsTable class="p-m-2" :propItems="tableItems" :tableMode="tableMode"></WorkspaceModelsTable>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { IBusinessModel, IFederatedDataset } from '../../Workspace'
import SelectButton from 'primevue/selectbutton'
import WorkspaceModelsTable from './tables/WorkspaceModelsTable.vue'
import workspaceModelsViewDescriptor from './WorkspaceModelsViewDescriptor.json'

export default defineComponent({
    name: 'workspace-models-view',
    components: { SelectButton, WorkspaceModelsTable },
    data() {
        return {
            workspaceModelsViewDescriptor,
            businessModels: [] as IBusinessModel[],
            federatedDatasets: [] as IFederatedDataset[],
            filteredItems: [] as IBusinessModel[] | IFederatedDataset[],
            tableMode: 'Business',
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
