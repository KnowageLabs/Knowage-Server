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
                <FunctionsCatalogDatatable class="p-m-3" :propLoading="loading" :items="functions" @selected="showForm" @deleted="deleteFunction"></FunctionsCatalogDatatable>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction, iFilter } from './FunctionsCatalog'
import axios from 'axios'
import FunctionsCatalogDatatable from './FunctionsCatalogDatatable.vue'
import FunctionCatalogFilterCards from './FunctionCatalogFilterCards.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'

export default defineComponent({
    name: 'functions-catalog',
    components: { FunctionsCatalogDatatable, FunctionCatalogFilterCards, KnFabButton },
    data() {
        return {
            user: null as any,
            functions: [] as iFunction[],
            filters: [] as iFilter[],
            loading: false
        }
    },
    computed: {
        // TODO dodati provere kao (isAdmin || (isDev && shownFunction.owner==ownerUserName))
        isAdmin() {
            return true
        },
        isDev() {
            return true
        }
    },
    async created() {
        this.loadUser()
        await this.loadPage()
    },
    methods: {
        async loadUser() {
            this.user = (this.$store.state as any).user
            console.log('loadUser() LOADED USER: ', this.user)
        },
        async loadPage() {
            this.loading = true
            await this.loadFunctions('')
            await this.loadFilters()
            this.loading = false
        },
        async loadFunctions(filterValue: string) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/functions-catalog/` + filterValue).then((response) => (this.functions = response.data.functions))
            console.log('loadFunctions() LOADED FUNCTIONS: ', this.functions)
        },
        async loadFilters() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/FUNCTION_TYPE`).then((response) => (this.filters = response.data))
            console.log('loadFilters() LOADED FILTERS: ', this.filters)
        },
        showForm(selectedFunction: iFunction | null) {
            console.log('showForm() clicked: ', selectedFunction)
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
        async onSelectedFilter(filter: iFilter) {
            console.log('SELECTED FILTER: ', filter)
            const filterValue = filter.valueCd !== 'All' ? filter.valueCd : ''
            this.loading = true
            await this.loadFunctions(filterValue)
            this.loading = false
        }
    }
})
</script>
