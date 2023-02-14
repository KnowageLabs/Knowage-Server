<template>
    <div class="dashboardEditor">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start> {{ $t('dashboard.generalSettings.title') }} </template>
            <template #end>
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="saveGeneralSettings" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('closeGeneralSettings')" />
            </template>
        </Toolbar>

        <div class="datasetEditor-container kn-overflow">
            <DashboardGeneralSettingsList @selectedOption="setSelectedOption"></DashboardGeneralSettingsList>
            <DashboardVariables v-if="selectedOption === 'Variables'" :propVariables="variables" :selectedDatasets="selectedDatasets" :selectedDatasetsColumnsMap="selectedDatasetColumnsMap" :profileAttributes="profileAttributes"></DashboardVariables>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVariable, IDataset } from '@/modules/documentExecution/dashboard/Dashboard'
import { mapActions } from 'pinia'
import DashboardGeneralSettingsList from './DashboardGeneralSettingsList.vue'
import DashboardVariables from './DashboardVariables.vue'
import store from '@/modules/documentExecution/dashboard/Dashboard.store'
import mainStore from '@/App.store'
import deepcopy from 'deepcopy'
import { setVariableValueFromDataset } from './VariablesHelper'

export default defineComponent({
    name: 'dashboard-general-settings',
    components: { DashboardGeneralSettingsList, DashboardVariables },
    props: {
        dashboardId: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        profileAttributes: { type: Array as PropType<{ name: string; value: string }[]>, required: true }
    },
    emits: ['closeGeneralSettings'],
    data() {
        return {
            selectedOption: '' as string,
            dashboardModel: null as any,
            variables: [] as IVariable[],
            selectedDatasets: [] as IDataset[],
            selectedDatasetColumnsMap: {}
        }
    },
    watch: {},
    computed: {},
    created() {
        this.loadDashboardModel()
        this.loadVariables()
        this.loadSelectedDatasets()
        this.loadSelectedDatasetColumnNames()
    },
    methods: {
        ...mapActions(store, ['getDashboard']),
        ...mapActions(mainStore, ['getUser']),
        loadDashboardModel() {
            this.dashboardModel = this.getDashboard(this.dashboardId)
        },
        loadVariables() {
            if (this.dashboardModel && this.dashboardModel.configuration) this.variables = deepcopy(this.dashboardModel.configuration.variables)
        },
        loadSelectedDatasets() {
            this.selectedDatasets = [] as IDataset[]
            if (this.dashboardModel && this.dashboardModel.configuration) {
                const tempModelDatasets = deepcopy(this.dashboardModel.configuration.datasets)
                for (let i = 0; i < tempModelDatasets.length; i++) {
                    const tempDataset = tempModelDatasets[i]
                    const index = this.datasets.findIndex((dataset: any) => dataset.id.dsId === tempDataset.id)
                    if (index !== -1)
                        this.selectedDatasets.push({
                            ...this.datasets[index],
                            cache: tempDataset.cache,
                            indexes: tempDataset.indexes ?? [],
                            parameters: tempDataset.parameters as any[],
                            drivers: tempDataset.drivers ?? []
                        })
                }
            }
        },
        loadSelectedDatasetColumnNames() {
            if (!this.selectedDatasets || this.selectedDatasets.length === 0) return
            this.selectedDatasets.forEach((dataset: IDataset) => this.loadSelectedDatasetColumnName(dataset))
        },
        loadSelectedDatasetColumnName(dataset: IDataset) {
            this.selectedDatasetColumnsMap[dataset.id.dsId] = { name: dataset.name, columns: [] }
            for (let i = 0; i < dataset.metadata.fieldsMeta.length; i++) {
                this.selectedDatasetColumnsMap[dataset.id.dsId].columns.push(dataset.metadata.fieldsMeta[i].name)
            }
        },
        setSelectedOption(option: string) {
            this.selectedOption = option
        },
        async saveGeneralSettings() {
            for (let i = 0; i < this.variables.length; i++) {
                if (this.variables[i].type === 'dataset') await setVariableValueFromDataset(this.variables[i], this.datasets, this.$http)
            }

            this.dashboardModel.configuration.variables = this.variables
            this.$emit('closeGeneralSettings')
        }
    }
})
</script>
