<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.scorecards.title') }}
            </template>
            <template #end>
                <KnFabButton icon="fas fa-plus" @click="openScorecardDetail()"></KnFabButton>
            </template>
        </Toolbar>

        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col">
                <DataTable
                    v-model:filters="filters"
                    :value="scorecards"
                    :paginator="scorecards.length > 20"
                    :rows="20"
                    class="p-datatable-sm kn-table"
                    data-key="id"
                    :global-filter-fields="scorecardsDescriptor.globalFilterFields"
                    responsive-layout="stack"
                    breakpoint="960px"
                    @rowClick="openScorecardDetail($event.data)"
                >
                    <template #header>
                        <div class="table-header">
                            <span class="p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText v-model="filters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                            </span>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <Column class="kn-truncated" field="name" :header="$t('common.name')" :sortable="true"></Column>
                    <Column class="kn-truncated" field="creationDate" :header="$t('common.creationDate')" :sortable="true">
                        <template #body="slotProps">
                            <span>{{ getFormattedDate(slotProps.data.creationDate) }}</span>
                        </template>
                    </Column>
                    <Column class="kn-truncated" field="author" :header="$t('common.author')" :sortable="true"></Column>
                    <Column :style="scorecardsDescriptor.iconColumnStyle">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteScorecardConfirm(slotProps.data)" />
                        </template>
                    </Column>
                </DataTable>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iScorecard } from './Scorecards'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import moment from 'moment'
import scorecardsDescriptor from './ScorecardsDescriptor.json'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'scorecards',
    components: { Column, DataTable, KnFabButton },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            scorecardsDescriptor,
            scorecards: [] as iScorecard[],
            filters: { global: [filterDefault] }
        }
    },
    computed: {},
    async created() {
        await this.loadScorecards()
    },
    methods: {
        async loadScorecards() {
            this.store.setLoading(true)
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/listScorecard`).then((response: AxiosResponse<any>) => (this.scorecards = response.data))
            this.store.setLoading(false)
        },
        getFormattedDate(date: number) {
            const tempDate = moment(date).format('DD/MM/YYYY')
            return formatDate(tempDate, '', 'DD/MM/YYYY')
        },
        openScorecardDetail(scorecard: iScorecard | null = null) {
            const path = scorecard ? `/scorecards/${scorecard.id}` : '/scorecards/new-scorecard'
            this.$router.push(path)
        },
        deleteScorecardConfirm(scorecard: iScorecard) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteScorecard(scorecard)
            })
        },
        async deleteScorecard(scorecard: iScorecard) {
            await this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/${scorecard.id}/deleteScorecard`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.removeScorecard(scorecard)
                })
                .catch(() => {})
        },
        removeScorecard(scorecard: iScorecard) {
            const index = this.scorecards.findIndex((tempScorecard: iScorecard) => tempScorecard.id === scorecard.id)
            if (index !== -1) this.scorecards.splice(index, 1)
        }
    }
})
</script>
