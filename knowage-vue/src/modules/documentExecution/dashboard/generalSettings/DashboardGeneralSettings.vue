<template>
    <div class="dashboardEditor">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start> {{ $t('dashboard.generalSettings.title') }} </template>
            <template #end>
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="saveGeneralSettings" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('closeGeneralSettings')" />
            </template>
        </Toolbar>

        <div class="p-grid p-m-0 p-p-0 kn-overflow">
            <DashboardGeneralSettingsList class="p-col-3 p-pr-2" @selectedOption="setSelectedOption"></DashboardGeneralSettingsList>
            <DashboardVariables v-if="selectedOption === 'Variables'" class="p-col-9 p-pl-2" :propVariables="variables" :selectedDatasets="selectedDatasets" :selectedDatasetsColumnsMap="selectedDatasetColumnsMap" :drivers="documentDrivers" :profileAttributes="profileAttributes"></DashboardVariables>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVariable, IDataset } from '@/modules/documentExecution/dashboard/Dashboard'
import { mapActions } from 'pinia'
import { getVariableValueFromDatasetColumn } from './VariablesHelper'
import DashboardGeneralSettingsList from './DashboardGeneralSettingsList.vue'
import DashboardVariables from './DashboardVariables.vue'
import store from '@/modules/documentExecution/dashboard/Dashboard.store'
import mainStore from '@/App.store'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'dashboard-general-settings',
    components: { DashboardGeneralSettingsList, DashboardVariables },
    props: { dashboardId: { type: String, required: true }, datasets: { type: Array as PropType<IDataset[]>, required: true }, documentDrivers: { type: Array, required: true }, profileAttributes: { type: Array as PropType<{ name: string; value: string }[]>, required: true } },
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
        saveGeneralSettings() {
            this.dashboardModel.configuration.variables = this.variables
            this.$emit('closeGeneralSettings')
        }
    }
})
</script>
<style lang="scss">
.dashboardEditor {
    height: 95%;
    width: 100%;
    top: 0;
    left: 0;
    background-color: white;
    position: absolute;
    z-index: 999;
    display: flex;
    flex-direction: column;
}
</style>
