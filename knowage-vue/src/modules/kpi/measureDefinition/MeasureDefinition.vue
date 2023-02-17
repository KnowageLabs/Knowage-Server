<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('kpi.measureDefinition.title') }}
            </template>
            <template #end>
                <KnFabButton icon="fas fa-plus" data-test="new-button" @click="showForm(null, false)" />
            </template>
        </Toolbar>
        <div class="kn-page-content">
            <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
            <KnHint v-if="measuresList.length === 0 && !loading" :title="'kpi.measureDefinition.title'" :hint="'kpi.measureDefinition.hint'" data-test="measure-hint"></KnHint>
            <DataTable
                v-else
                v-model:filters="filters"
                :value="measuresList"
                row-group-mode="rowspan"
                group-rows-by="rule"
                :paginator="true"
                :rows="15"
                :loading="loading"
                class="p-datatable-sm kn-table"
                data-key="id"
                :global-filter-fields="measureDefinitionDescriptor.globalFilterFields"
                responsive-layout="stack"
                breakpoint="960px"
                data-test="measures-table"
                @rowClick="showForm($event.data, false)"
            >
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>
                <template #header>
                    <div class="table-header p-d-flex">
                        <span class="p-input-icon-left p-mr-3">
                            <i class="pi pi-search" />
                            <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                        </span>
                    </div>
                </template>
                <Column v-for="col of measureDefinitionDescriptor.columns" :key="col.field" class="kn-truncated" :style="col.style" :field="col.field" :header="$t(col.header)" :sortable="true"> </Column>
                <Column :style="measureDefinitionDescriptor.table.iconColumn.style">
                    <template #body="slotProps">
                        <Button icon="pi pi-copy" class="p-button-link" data-test="clone-button" @click="cloneKpiConfirm(slotProps.data)" />
                        <Button icon="pi pi-trash" class="p-button-link" :data-test="'delete-button-' + slotProps.data.id" @click="deleteMeasureConfirm(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMeasure } from './MeasureDefinition'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { AxiosResponse } from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnHint from '@/components/UI/KnHint.vue'
import measureDefinitionDescriptor from './MeasureDefinitionDescriptor.json'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'measure-definition',
    components: {
        Column,
        DataTable,
        KnFabButton,
        KnHint
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            measureDefinitionDescriptor,
            measuresList: [] as iMeasure[],
            filters: { global: [filterDefault] } as Object,
            loading: false
        }
    },
    async created() {
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadMeasures()
            this.loading = false
        },
        async loadMeasures() {
            this.measuresList = []
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/kpi/listMeasure').then((response: AxiosResponse<any>) =>
                response.data.forEach((measure) => {
                    if (measure.category) {
                        measure.categoryName = measure.translatedValueName
                    }
                    this.measuresList.push(measure)
                })
            )
        },
        showForm(measure: iMeasure, clone: boolean) {
            const path = measure ? `/measure-definition/edit?id=${measure.ruleId}&ruleVersion=${measure.ruleVersion}&clone=${clone}` : '/measure-definition/new-measure-definition'
            this.$router.push(path)
        },
        cloneKpiConfirm(measure: iMeasure) {
            this.$confirm.require({
                header: this.$t('common.toast.cloneConfirmTitle'),
                accept: () => this.showForm(measure, true)
            })
        },
        deleteMeasureConfirm(measure: iMeasure) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteMeasure(measure)
            })
        },
        async deleteMeasure(measure: iMeasure) {
            await this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpi/${measure.ruleId}/${measure.ruleVersion}/deleteRule`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.loadPage()
                })
                .catch(() => {})
        }
    }
})
</script>
