<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.functionsCatalog.title') }}
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showForm(null)" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
                <FunctionCatalogFilterCards class="p-m-3" :propFilters="filters" @selected="onSelectedFilter"></FunctionCatalogFilterCards>
                <div class="p-d-flex p-flex-row p-jc-center">
                    <Chip class="keyword-chip p-m-2" :class="{ 'keyword-chip-active': selectedKeyword === keyword }" v-for="(keyword, index) in keywords" :key="index" :label="keyword" @click="filterByKeyword(keyword)"></Chip>
                </div>
                <FunctionsCatalogDatatable class="p-m-3" :propLoading="loading" :items="selectedKeyword ? filteredFunctions : functions" :readonly="readonly" @selected="showForm" @deleted="deleteFunction"></FunctionsCatalogDatatable>
            </div>
        </div>

        <FunctionsCatalogDetail v-show="detailDialogVisible" :visible="detailDialogVisible" :propFunction="selectedFunction" :functionTypes="filters" :keywords="keywords" @close="detailDialogVisible = false"></FunctionsCatalogDetail>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction, iFunctionType } from './FunctionsCatalog'
import axios from 'axios'
import Chip from 'primevue/chip'
import FunctionsCatalogDatatable from './FunctionsCatalogDatatable.vue'
import FunctionsCatalogDetail from './FunctionsCatalogDetail.vue'
import FunctionCatalogFilterCards from './FunctionCatalogFilterCards.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'

export default defineComponent({
    name: 'functions-catalog',
    components: {
        Chip,
        FunctionsCatalogDatatable,
        FunctionsCatalogDetail,
        FunctionCatalogFilterCards,
        KnFabButton
    },
    data() {
        return {
            user: null as any,
            functions: [] as iFunction[],
            filteredFunctions: [] as iFunction[],
            selectedFunction: null as iFunction | null,
            filters: [] as iFunctionType[],
            selectedFilter: null as iFunctionType | null,
            keywords: [] as String[],
            selectedKeyword: '',
            detailDialogVisible: false,
            loading: false
        }
    },
    computed: {
        // TODO proveriti uslov
        readonly(): boolean {
            console.log(this.selectedFunction?.owner + ' !== ' + this.user.userId)
            return !this.user.isSuperadmin || this.selectedFunction?.owner !== this.user.userId
        }
    },
    async created() {
        this.loadUser()
        await this.loadPage()
    },
    methods: {
        async loadUser() {
            this.user = (this.$store.state as any).user
            // console.log('loadUser() LOADED USER: ', this.user)
        },
        async loadPage() {
            this.loading = true
            await this.loadFunctions('')
            await this.loadFilters()
            this.loading = false
        },
        async loadFunctions(filterValue: string) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/functions-catalog/` + filterValue).then((response) => {
                this.functions = response.data.functions
                this.keywords = response.data.keywords
            })
            // console.log('loadFunctions() LOADED FUNCTIONS: ', this.functions)
            // console.log('loadFunctions() LOADED KEYWORDS: ', this.keywords)
        },
        async loadFilters() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/FUNCTION_TYPE`).then((response) => (this.filters = response.data))
            // console.log('loadFilters() LOADED FILTERS: ', this.filters)
        },
        showForm(selectedFunction: iFunction | null) {
            console.log('showForm() clicked: ', selectedFunction)
            this.selectedFunction = selectedFunction
            this.detailDialogVisible = true
        },
        async deleteFunction(functionId: number) {
            this.loading = true
            let tempResponse = null as any
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/functions-catalog/delete/${functionId}`)
                .then((response) => (tempResponse = response))
                .finally(() => (this.loading = false))

            if (tempResponse && !tempResponse.errros) {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                await this.loadPage()
            }
        },
        async onSelectedFilter(filter: iFunctionType) {
            // console.log('SELECTED FILTER: ', filter)
            this.selectedKeyword = ''
            if (this.selectedFilter?.valueCd === filter.valueCd) {
                return
            }
            this.selectedFilter = filter
            const filterValue = this.selectedFilter.valueCd !== 'All' ? this.selectedFilter.valueCd : ''
            this.loading = true
            await this.loadFunctions(filterValue)
            this.loading = false
        },
        filterByKeyword(keyword: string) {
            // console.log('KEYWORD: ', keyword)
            if (this.selectedKeyword === keyword) {
                return
            }
            this.selectedKeyword = keyword
            this.filteredFunctions = this.functions.filter((el: iFunction) => {
                let found = false
                el.keywords.forEach((el: string) => {
                    if (el === this.selectedKeyword) {
                        found = true
                    }
                })
                return found
            })
            // console.log('FILTERED FUNCTIONS: ', this.filteredFunctions)
        }
    }
})
</script>

<style lang="scss" scoped>
.keyword-chip {
    cursor: pointer;
}

.keyword-chip-active {
    background-color: rgb(59, 103, 140);
    color: #fff;
}
</style>
