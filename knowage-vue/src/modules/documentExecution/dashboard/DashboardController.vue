<template>
    <div class="dashboard-container" :id="`dashboard_${model.configuration.id}`">
        <Button label="DATASET" style="position: absolute; margin-left: 250px; z-index: 999" @click="datasetEditorVisible = true" />
        <Button label="WIDGET" style="position: absolute; margin-left: 400px; z-index: 999" @click="widgetPickerVisible = true" />
        <DashboardRenderer v-if="!loading" :model="model" :datasets="datasets"></DashboardRenderer>

        <Transition name="editorEnter" appear>
            <DatasetEditor v-if="datasetEditorVisible" :dashboardIdProp="dashboardId" :availableDatasetsProp="datasets" :filtersDataProp="filtersData" @closeDatasetEditor="closeDatasetEditor" @datasetEditorSaved="closeDatasetEditor" />
        </Transition>

        <WidgetPickerDialog v-if="widgetPickerVisible" :visible="widgetPickerVisible" @openNewWidgetEditor="openNewWidgetEditor" @closeWidgetPicker="widgetPickerVisible = false" />
    </div>
    <WidgetEditor
        v-if="widgetEditorVisible"
        :dashboardId="dashboardId"
        :propWidget="selectedWidget"
        :datasets="datasets"
        :documentDrivers="[]"
        :variables="model ? model.configuration.variables : []"
        @close="closeWidgetEditor"
        @widgetSaved="closeWidgetEditor"
        @widgetUpdated="closeWidgetEditor"
        data-test="widget-editor"
    ></WidgetEditor>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the dashboard instance and to get initializing informations needed like the theme or the datasets.
 */
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { v4 as uuidv4 } from 'uuid'
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { IWidget } from './Dashboard'
import { emitter } from './DashboardHelpers'
import { formatModel } from './helpers/DashboardBackwardCompatibilityHelper'
import DashboardRenderer from './DashboardRenderer.vue'
import WidgetPickerDialog from './widget/WidgetPicker/WidgetPickerDialog.vue'
import mock from './DashboardMock.json'
import dashboardStore from './Dashboard.store'
import mainStore from '../../../App.store'
import DatasetEditor from './dataset/DatasetEditor.vue'
import WidgetEditor from './widget/WidgetEditor/WidgetEditor.vue'
import mockedDashboardModel from './mockedDashboardModel.json'
import descriptor from './DashboardDescriptor.json'
import cryptoRandomString from 'crypto-random-string'
// import mock1 from './tempMocks/mock1.json'

export default defineComponent({
    name: 'dashboard-manager',
    components: { DashboardRenderer, WidgetPickerDialog, DatasetEditor, WidgetEditor },
    props: { sbiExecutionId: { type: String }, document: { type: Object }, reloadTrigger: { type: Boolean }, hiddenFormData: { type: Object }, filtersData: { type: Object as PropType<{ filterStatus: iParameter[]; isReadyForExecution: boolean }> } },
    data() {
        return {
            descriptor,
            model: mock as any,
            widgetPickerVisible: false,
            datasetEditorVisible: false,
            datasets: [] as any[],
            widgetEditorVisible: false,
            selectedWidget: null as any,
            crossNavigations: [] as any[],
            dashboardId: '',
            loading: false
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
    async created() {
        this.setEventListeners()
        await this.getData()
    },
    mounted() {
        this.loadCrossNavigations()
        this.loadOutputParameters()
    },
    unmounted() {
        // TODO - dashboardId
        this.store.removeDashboard(this.dashboardId)
        this.store.setCrosssNavigations([])
        this.store.setOutputParameters([])
    },
    methods: {
        async getData() {
            this.loading = true
            await Promise.all([this.loadDatasets(), this.loadCrossNavigations(), this.loadOutputParameters(), this.loadModel()])
            this.loading = false
        },
        async loadModel() {
            let tempModel = null
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/documentexecution/` + this.document?.id + '/templates')
                .then((response: AxiosResponse<any>) => (tempModel = response.data))
                .catch(() => {})
            console.log('>>>>>>>>>> TEEEEEEEEEEEEEEEEEEEEEST: ', tempModel)
            // TODO
            // this.model = mock
            // this.model = formatModel(mockedDashboardModel) as any
            this.model = tempModel ? (formatModel(tempModel) as any) : {}
            this.dashboardId = cryptoRandomString({ length: 16, type: 'base64' })
            // this.model = formatModel(mock1) as any
            this.store.setDashboard(this.dashboardId, this.model)
        },
        async loadDatasets() {
            this.appStore.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/?asPagedList=true&seeTechnical=true`)
                .then((response: AxiosResponse<any>) => (this.datasets = response.data ? response.data.item : []))
                .catch(() => {})
            this.appStore.setLoading(false)
        },
        async loadCrossNavigations() {
            // TODO - Remove mocked document label
            this.appStore.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/crossNavigation/Test%20Drivers/loadCrossNavigationByDocument`)
                .then((response: AxiosResponse<any>) => (this.crossNavigations = response.data))
                .catch(() => {})
            this.appStore.setLoading(false)
            this.store.setCrosssNavigations(this.crossNavigations)
        },
        loadOutputParameters() {
            console.log('>>>>>>>>>>>>>>>> LOADED DOCUMENT: ', this.document)
            // TODO - Remove Mocked Output Parameters
            const mockedParameters = descriptor.mockedOutputParameters
            this.store.setOutputParameters(mockedParameters)
        },
        setEventListeners() {
            emitter.on('openNewWidgetPicker', () => {
                this.openNewWidgetPicker()
            })
            emitter.on('openDatasetManagement', () => {
                this.openDatasetManagementDialog()
            })
            emitter.on('openWidgetEditor', (widget) => {
                this.openWidgetEditor(widget)
            })
        },
        openNewWidgetPicker() {
            this.widgetPickerVisible = true
        },
        openDatasetManagementDialog() {
            this.datasetEditorVisible = true
        },
        openWidgetEditor(widget: IWidget) {
            this.selectedWidget = widget
            this.setWidgetEditorToVisible()
        },
        openNewWidgetEditor(widget: any) {
            this.selectedWidget = { type: widget?.type, new: true }
            this.setWidgetEditorToVisible()
        },
        setWidgetEditorToVisible() {
            this.widgetPickerVisible = false
            this.widgetEditorVisible = true
            emitter.emit('widgetEditorOpened')
        },
        closeWidgetEditor() {
            this.widgetEditorVisible = false
            this.selectedWidget = null
            emitter.emit('widgetEditorClosed')
        },
        closeDatasetEditor() {
            this.datasetEditorVisible = false
            emitter.emit('datasetManagementClosed')
        }
    }
})
</script>
<style lang="scss">
.dashboard-container {
    flex: 1;
    height: 100%;
    // width: 100%;
    // height: 100vh;
    overflow-y: auto;
    position: relative;
}
@media screen and (max-width: 600px) {
    .dashboard-container {
        height: calc(100vh - var(--kn-mainmenu-width));
    }
}
</style>
