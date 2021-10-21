<template>
    <div class="kn-page">
        <div class="">
            <div class="p-col p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.functionsCatalog.title') }}
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showForm(null)" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />

                <FunctionsCatalogFilterCards class="p-m-3" :propFilters="filters" @selected="onSelectedFilter"></FunctionsCatalogFilterCards>
                <FunctionsCatalogDatatable class="p-m-3" :user="user" :propLoading="loading" :items="functions" @selected="showForm" @preview="onPreview" @deleted="deleteFunction"></FunctionsCatalogDatatable>
            </div>
        </div>

        <div class="kn-page-content">
            <FunctionsCatalogDetail v-show="detailDialogVisible" :visible="detailDialogVisible" :propFunction="selectedFunction" :functionTypes="filters" @close="onDetailClose" @created="onCreated"></FunctionsCatalogDetail>
            <FunctionsCatalogPreviewDialog :visible="previewDialogVisible" :propFunction="selectedFunction" :datasets="datasets" @close="onPreviewClose"></FunctionsCatalogPreviewDialog>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction, iFunctionType, iDataset } from './FunctionsCatalog'
import axios from 'axios'
import FunctionsCatalogDatatable from './FunctionsCatalogDatatable.vue'
import FunctionsCatalogDetail from './FunctionsCatalogDetail.vue'
import FunctionsCatalogFilterCards from './FunctionsCatalogFilterCards.vue'
import FunctionsCatalogPreviewDialog from './FunctionsCatalogPreviewDialog/FunctionsCatalogPreviewDialog.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'

export default defineComponent({
    name: 'functions-catalog',
    components: {
        FunctionsCatalogDatatable,
        FunctionsCatalogDetail,
        FunctionsCatalogFilterCards,
        FunctionsCatalogPreviewDialog,
        KnFabButton
    },
    data() {
        return {
            user: null as any,
            functions: [] as iFunction[],
            selectedFunction: null as iFunction | null,
            filters: [] as iFunctionType[],
            selectedFilter: null as iFunctionType | null,
            datasets: [] as iDataset[],
            detailDialogVisible: false,
            previewDialogVisible: false,
            loading: false
        }
    },
    async created() {
        this.loadUser()
        await this.loadPage()
    },
    methods: {
        async loadUser() {
            this.user = (this.$store.state as any).user
        },
        async loadPage() {
            this.loading = true
            await this.loadFunctions('')
            await this.loadFilters()
            this.loading = false
        },
        async loadFunctions(filterValue: string) {
            const url = filterValue ? process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/functions-catalog/` + filterValue : process.env.VUE_APP_API_PATH + `1.0/functioncatalog/completelist`
            await axios.get(url).then((response) => {
                this.functions = filterValue
                    ? response.data.functions.map((el: any) => {
                          el.tags = [...el.keywords]
                          el.offlineScriptTrain = { ...el.offlineScriptTrainModel }
                          el.offlineScriptUse = { ...el.offlineScriptUseModel }
                          el.benchmark = { ...el.benchmarks }
                          const props = ['keywords', 'offlineScriptTrainModel', 'offlineScriptUseModel', 'benchmarks']
                          props.forEach((property: string) => delete el[property])
                          return el
                      })
                    : response.data
            })
        },
        async loadFilters() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/FUNCTION_TYPE`).then((response) => (this.filters = response.data))
        },
        showForm(selectedFunction: iFunction | null) {
            this.selectedFunction = selectedFunction
            this.detailDialogVisible = true
        },
        async deleteFunction(functionId: number) {
            this.loading = true
            let reponseOk = false as any
            await axios
                .delete(process.env.VUE_APP_API_PATH + `1.0/functioncatalog/${functionId}`)
                .then(() => {
                    reponseOk = true
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                })
                .finally(() => (this.loading = false))

            if (reponseOk) {
                await this.loadPage()
            }
        },
        async onSelectedFilter(filter: iFunctionType) {
            if (this.selectedFilter?.valueCd === filter.valueCd) {
                return
            }
            this.selectedFilter = filter
            const filterValue = this.selectedFilter.valueCd !== 'All' ? this.selectedFilter.valueCd : ''
            this.loading = true
            await this.loadFunctions(filterValue)
            this.loading = false
        },
        onDetailClose() {
            this.detailDialogVisible = false
            this.selectedFunction = null
        },
        async onCreated() {
            this.detailDialogVisible = false
            await this.loadPage()
        },
        async onPreview(tempFunction: iFunction) {
            this.selectedFunction = tempFunction
            await this.loadPreviewData()
            this.previewDialogVisible = true
        },
        onPreviewClose() {
            this.previewDialogVisible = false
            this.selectedFunction = null
        },
        async loadPreviewData() {
            this.loading = true
            await this.loadDatasets()
            this.loading = false
        },
        async loadDatasets() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/`).then((response) => (this.datasets = response.data.root))
        }
    }
})
</script>

<style lang="scss" scoped>
.keyword-chip {
    cursor: pointer;
    text-transform: uppercase;
}

.keyword-chip-active {
    background-color: rgb(59, 103, 140);
    color: #fff;
}
</style>
