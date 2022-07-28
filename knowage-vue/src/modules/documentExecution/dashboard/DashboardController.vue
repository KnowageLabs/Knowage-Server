<template>
    <div class="dashboard-container" :id="`dashboard_${model.configuration.id}`">
        <Button label="DATASET" style="position: absolute; margin-left: 250px; z-index: 999" @click="datasetEditorVisible = true" />
        <Button label="WIDGET" style="position: absolute; margin-left: 400px; z-index: 999" @click="widgetPickerVisible = true" />
        <DashboardRenderer :model="model" :datasets="datasets"></DashboardRenderer>

        <Transition name="editorEnter" appear>
            <DatasetEditor v-if="datasetEditorVisible" @closeDatasetEditor="datasetEditorVisible = false" />
        </Transition>

        <WidgetPickerDialog v-if="widgetPickerVisible" :visible="widgetPickerVisible" @closeWidgetPicker="widgetPickerVisible = false" />
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the dashboard instance and to get initializing informations needed like the theme or the datasets.
 */
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { v4 as uuidv4 } from 'uuid'
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { emitter } from './DashboardHelpers'
import DashboardRenderer from './DashboardRenderer.vue'
import WidgetPickerDialog from './widget/WidgetPicker/WidgetPickerDialog.vue'
import mock from './DashboardMock.json'
import dashboardStore from './Dashboard.store'
import mainStore from '../../../App.store'
import DatasetEditor from './dataset/DatasetEditor.vue'

export default defineComponent({
    name: 'dashboard-manager',
    components: { DashboardRenderer, WidgetPickerDialog, DatasetEditor },
    props: { sbiExecutionId: { type: String }, document: { type: Object }, reloadTrigger: { type: Boolean }, hiddenFormData: { type: Object }, filtersData: { type: Object as PropType<{ filterStatus: iParameter[]; isReadyForExecution: boolean }> } },
    data() {
        return {
            model: mock,
            widgetPickerVisible: false,
            datasetEditorVisible: false,
            datasets: [] as any[]
        }
    },
    provide() {
        return {
            dHash: uuidv4()
        }
    },
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    created() {
        this.setEventListeners()
        this.loadDatasets()
        this.loadModel()
    },

    unmounted() {
        this.store.removeDashboard({ id: (this as any).dHash as any })
    },
    methods: {
        loadModel() {
            // TODO
            this.model = mock
            this.store.setDashboard(mock)
        },
        async loadDatasets() {
            this.appStore.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/?asPagedList=true&seeTechnical=true`)
                .then((response: AxiosResponse<any>) => (this.datasets = response.data ? response.data.item : []))
                .catch(() => {})
            this.appStore.setLoading(false)
        },
        setEventListeners() {
            emitter.on('openWidgetEditor', () => {
                this.openWidgetEditorDialog()
            })
            emitter.on('openDatasetManagement', () => {
                this.openDatasetManagementDialog()
            })
        },
        openWidgetEditorDialog() {
            this.widgetPickerVisible = true
        },
        openDatasetManagementDialog() {
            this.datasetEditorVisible = true
        }
    }
})
</script>
<style lang="scss">
.dashboard-container {
    width: 100%;
    height: 100vh;
    overflow-y: auto;
    position: relative;
}
@media screen and (max-width: 600px) {
    .dashboard-container {
        height: calc(100vh - var(--kn-mainmenu-width));
    }
}
</style>
