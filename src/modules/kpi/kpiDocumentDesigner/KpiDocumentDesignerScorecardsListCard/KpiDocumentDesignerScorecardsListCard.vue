<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('kpi.kpiDocumentDesigner.scorecardList') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div>
                <DataTable v-model:filters="filters" :value="scorecards" class="p-datatable-sm kn-table" data-key="name" :global-filter-fields="KpiDocumentDesignerScorecardsListCardDescriptor.globalFilterFields" responsive-layout="stack" breakpoint="960px" :scrollable="true" scroll-height="60vh">
                    <template #header>
                        <div class="table-header p-d-flex p-ai-center">
                            <span id="search-container" class="p-input-icon-left p-mr-3">
                                <i class="pi pi-search" />
                                <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" />
                            </span>
                            <Button id="kpi-edit-add-scorecard-association-button" class="kn-button kn-button--primary" :label="$t('kpi.kpiDocumentDesigner.addScorecardAssociation')" @click="addScorecardVisible = true"></Button>
                        </div>
                    </template>

                    <template #empty>{{ $t('common.info.noDataFound') }}</template>

                    <Column key="name" class="kn-truncated" field="name" :header="$t('common.name')" :sortable="true"> </Column>
                    <Column key="dateCreation" class="kn-truncated" field="creationDate" :header="$t('common.creationDate')" :sortable="true">
                        <template #body="slotProps">
                            <span>{{ getFormattedDate(slotProps.data.creationDate) }}</span>
                        </template>
                    </Column>

                    <Column :style="KpiDocumentDesignerScorecardsListCardDescriptor.iconColumnStyle">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteScorecardConfirm(slotProps.data)" />
                        </template>
                    </Column>
                </DataTable>
            </div>

            <KpiDocumentDesignerScorecardSelectDialog :scorecard-list="scorecardList" :visible="addScorecardVisible" :data-scorecards="scorecards" @close="addScorecardVisible = false" @scorecardSelected="onScorecardSelected"></KpiDocumentDesignerScorecardSelectDialog>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iScorecard } from '../KpiDocumentDesigner'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KpiDocumentDesignerScorecardsListCardDescriptor from './KpiDocumentDesignerScorecardsListCardDescriptor.json'
import KpiDocumentDesignerScorecardSelectDialog from './KpiDocumentDesignerScorecardSelectDialog.vue'

export default defineComponent({
    name: 'kpi-edit-scorecards-list-card',
    components: { Card, Column, DataTable, KpiDocumentDesignerScorecardSelectDialog },
    props: { propData: { type: Object }, scorecardList: { type: Array as PropType<iScorecard[]> } },
    emits: ['scorecardChanged'],
    data() {
        return {
            KpiDocumentDesignerScorecardsListCardDescriptor,
            scorecards: [] as iScorecard[],
            filters: { global: [filterDefault] } as Object,
            addScorecardVisible: false
        }
    },
    watch: {
        propData() {
            this.loadScorecard()
        }
    },
    created() {
        this.loadScorecard()
    },
    methods: {
        loadScorecard() {
            if (this.propData?.scorecard) {
                this.scorecards = [this.propData.scorecard]
            }
        },
        onScorecardSelected(scorecard: iScorecard) {
            this.scorecards[0] = scorecard
            this.$emit('scorecardChanged', this.scorecards[0])
            this.addScorecardVisible = false
        },
        deleteScorecardConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.scorecards = []
                    this.$emit('scorecardChanged', null)
                }
            })
        },
        getFormattedDate(date: any) {
            return formatDate(date)
        }
    }
})
</script>

<style lang="scss" scoped>
#kpi-edit-add-scorecard-association-button {
    flex: 0.15;
    height: 2.3rem;
    margin-left: auto;
    min-width: 150px;
}
</style>
