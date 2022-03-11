<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('kpi.measureDefinition.title') }}
            </template>
            <template #end>
                <KnFabButton icon="fas fa-plus" @click="showForm(null, false)" data-test="new-button" />
            </template>
        </Toolbar>
        <div class="kn-page-content">
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <KnHint v-if="measuresList.length === 0 && !loading" :title="'kpi.measureDefinition.title'" :hint="'kpi.measureDefinition.hint'" data-test="measure-hint"></KnHint>
            <DataTable
                v-else
                :value="measuresList"
                rowGroupMode="rowspan"
                groupRowsBy="rule"
                :paginator="true"
                :rows="15"
                :loading="loading"
                class="p-datatable-sm kn-table"
                dataKey="id"
                v-model:filters="filters"
                :globalFilterFields="measureDefinitionDescriptor.globalFilterFields"
                responsiveLayout="stack"
                breakpoint="960px"
                @rowClick="showForm($event.data, false)"
                data-test="measures-table"
            >
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>
                <template #header>
                    <div class="table-header p-d-flex">
                        <span class="p-input-icon-left p-mr-3">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                        </span>
                    </div>
                </template>
                <Column class="kn-truncated" :style="col.style" v-for="col of measureDefinitionDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"> </Column>
                <Column :style="measureDefinitionDescriptor.table.iconColumn.style">
                    <template #body="slotProps">
                        <Button icon="pi pi-copy" class="p-button-link" @click="cloneKpiConfirm(slotProps.data)" data-test="clone-button" />
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteMeasureConfirm(slotProps.data)" :data-test="'delete-button-' + slotProps.data.id" />
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

    export default defineComponent({
        name: 'measure-definition',
        components: {
            Column,
            DataTable,
            KnFabButton,
            KnHint
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
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listMeasure').then((response: AxiosResponse<any>) =>
                    response.data.forEach((measure) => {
                        if (measure.category) {
                            measure.categoryName = measure.translatedValueName
                        }
                        this.measuresList.push(measure)
                    })
                )
            },
            showForm(measure: iMeasure, clone: Boolean) {
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
                    .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${measure.ruleId}/${measure.ruleVersion}/deleteRule`)
                    .then(() => {
                        this.$store.commit('setInfo', {
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
