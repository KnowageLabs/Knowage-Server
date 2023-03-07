<template>
    <div v-show="model && visible" :id="`dashboard_${model?.configuration?.id}`" class="dashboard-container">
        <Button icon="fas fa-square-check" class="p-m-3 p-button-rounded p-button-text p-button-plain" style="position: fixed; right: 0; z-index: 999; background-color: white; box-shadow: 0px 2px 3px #ccc" @click="selectionsDialogVisible = true" />
        <DashboardRenderer v-if="!loading" :model="model" :datasets="datasets" :dashboardId="dashboardId" :documentDrivers="drivers" :variables="model ? model.configuration.variables : []"></DashboardRenderer>

        <Transition name="editorEnter" appear>
            <DatasetEditor v-if="datasetEditorVisible" :dashboard-id-prop="dashboardId" :available-datasets-prop="datasets" :filters-data-prop="filtersData" @closeDatasetEditor="closeDatasetEditor" @datasetEditorSaved="closeDatasetEditor" @allDatasetsLoaded="datasets = $event" />
        </Transition>

        <Transition name="editorEnter" appear>
            <DashboardGeneralSettings
                v-if="generalSettingsVisible"
                :dashboard-id="dashboardId"
                :datasets="datasets"
                :document-drivers="drivers"
                :profile-attributes="profileAttributes"
                @closeGeneralSettings="closeGeneralSettings"
                @saveGeneralSettings="generalSettingsVisible = false"
            ></DashboardGeneralSettings>
        </Transition>

        <WidgetPickerDialog v-if="widgetPickerVisible" :visible="widgetPickerVisible" @openNewWidgetEditor="openNewWidgetEditor" @closeWidgetPicker="widgetPickerVisible = false" />
        <DashboardControllerSaveDialog v-if="saveDialogVisible" :visible="saveDialogVisible" @save="saveNewDashboard" @close="saveDialogVisible = false"></DashboardControllerSaveDialog>
        <SelectionsListDialog v-if="selectionsDialogVisible" :visible="selectionsDialogVisible" :dashboard-id="dashboardId" @close="selectionsDialogVisible = false" @save="onSelectionsRemove" />
    </div>
    <WidgetEditor
        v-if="widgetEditorVisible"
        :dashboard-id="dashboardId"
        :prop-widget="selectedWidget"
        :datasets="datasets"
        :document-drivers="drivers"
        :variables="model ? model.configuration.variables : []"
        :html-gallery-prop="htmlGallery"
        :custom-chart-gallery-prop="customChartGallery"
        data-test="widget-editor"
        @close="closeWidgetEditor"
        @widgetSaved="closeWidgetEditor"
        @widgetUpdated="closeWidgetEditor"
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
import { IDashboardDataset, ISelection, IWidget, IGalleryItem, IDataset, IDashboardCrossNavigation } from './Dashboard'
import { emitter, createNewDashboardModel, formatDashboardForSave, formatNewModel, loadDatasets, getFormattedOutputParameters } from './DashboardHelpers'
import { mapActions, mapState } from 'pinia'
import { formatModel } from './helpers/DashboardBackwardCompatibilityHelper'
import { setDatasetIntervals, clearAllDatasetIntervals } from './helpers/datasetRefresh/DatasetRefreshHelpers'
import { loadDrivers } from './helpers/DashboardDriversHelper'
import DashboardRenderer from './DashboardRenderer.vue'
import WidgetPickerDialog from './widget/WidgetPicker/WidgetPickerDialog.vue'
import dashboardStore from './Dashboard.store'
import mainStore from '../../../App.store'
import DatasetEditor from './dataset/DatasetEditor.vue'
import WidgetEditor from './widget/WidgetEditor/WidgetEditor.vue'
import descriptor from './DashboardDescriptor.json'
import cryptoRandomString from 'crypto-random-string'
import DashboardControllerSaveDialog from './DashboardControllerSaveDialog.vue'
import SelectionsListDialog from './widget/SelectorWidget/SelectionsListDialog.vue'
import DashboardGeneralSettings from './generalSettings/DashboardGeneralSettings.vue'
import deepcopy from 'deepcopy'
import { ICrossNavigationParameter } from '../main/DocumentExecution'

export default defineComponent({
    name: 'dashboard-manager',
    components: { DashboardRenderer, WidgetPickerDialog, DatasetEditor, WidgetEditor, DashboardControllerSaveDialog, SelectionsListDialog, DashboardGeneralSettings },
    provide() {
        return {
            dHash: uuidv4()
        }
    },
    props: {
        visible: { type: Boolean },
        sbiExecutionId: { type: String },
        document: { type: Object },
        reloadTrigger: { type: Boolean },
        hiddenFormData: { type: Object },
        filtersData: { type: Object as PropType<{ filterStatus: iParameter[]; isReadyForExecution: boolean }> },
        newDashboardMode: { type: Boolean }
    },
    emits: ['newDashboardSaved', 'executeCrossNavigation'],
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    data() {
        return {
            descriptor,
            model: null as any,
            widgetPickerVisible: false,
            datasetEditorVisible: false,
            datasets: [] as IDataset[],
            widgetEditorVisible: false,
            selectedWidget: null as any,
            crossNavigations: [] as any[],
            profileAttributes: [] as { name: string; value: string }[],
            drivers: [] as any[],
            internationalization: {} as any,
            dashboardId: '',
            saveDialogVisible: false,
            selectionsDialogVisible: false,
            generalSettingsVisible: false,
            loading: false,
            htmlGallery: [] as IGalleryItem[],
            customChartGallery: [] as IGalleryItem[]
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        })
    },
    async created() {
        console.log('------------- DASHBOARD CONTROLLER CREATED!!!')
        this.setEventListeners()
        await this.getData()
        this.$watch('model.configuration.datasets', (modelDatasets: IDashboardDataset[]) => {
            setDatasetIntervals(modelDatasets, this.datasets)
        })
    },
    beforeUnmount() {
        console.log('------------- DASHBOARD CONTROLLER BEFORE UNMOUNTED!!!')
        console.log('------------- this data: ', this.$data)
        this.setDashboardState(this.dashboardId, this.$data)
        console.log('------------------------ LOAD STATE 2: ', this.getDashboardState(this.dashboardId))
        this.emptyStoreValues()
        clearAllDatasetIntervals()
    },
    methods: {
        ...mapActions(dashboardStore, ['removeSelections', 'setAllDatasets', 'getSelections', 'setInternationalization', 'getInternationalization', 'setDashboardDocument', 'setDashboardDrivers', 'setProfileAttributes', 'getCrossNavigations', 'getDashboardState', 'setDashboardState']),
        async getData() {
            this.loading = true
            this.dashboardId = cryptoRandomString({ length: 16, type: 'base64' })
            console.log('------------------------ LOAD STATE: ', this.getDashboardState(this.dashboardId))
            if (this.filtersData) this.drivers = loadDrivers(this.filtersData, this.model)
            await Promise.all([this.loadProfileAttributes(), this.loadModel(), this.loadInternationalization()])
            this.setDashboardDrivers(this.dashboardId, this.drivers)
            this.loadHtmlGallery()
            this.loadCustomChartGallery()
            this.loadOutputParameters()
            await this.loadCrossNavigations()
            this.loading = false
        },
        async loadModel() {
            let tempModel = null as any
            if (this.newDashboardMode) {
                tempModel = createNewDashboardModel()
            } else {
                await this.$http
                    .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/documentexecution/` + this.document?.id + '/templates')
                    .then((response: AxiosResponse<any>) => (tempModel = response.data))
                    .catch(() => {})
            }
            this.datasets = await loadDatasets(tempModel, this.appStore, this.setAllDatasets, this.$http)
            this.model = (tempModel && this.newDashboardMode) || typeof tempModel.id != 'undefined' ? await formatNewModel(tempModel, this.datasets, this.$http) : await (formatModel(tempModel, this.document, this.datasets, this.drivers, this.profileAttributes, this.$http, this.user) as any)
            console.log('---------- LOAD MODEL: ', this.model)
            setDatasetIntervals(this.model?.configuration.datasets, this.datasets)
            this.store.setDashboard(this.dashboardId, this.model)
            this.store.setSelections(this.dashboardId, this.model.configuration.selections, this.$http)
            this.store.setDashboardDocument(this.dashboardId, this.document)
        },
        async loadInternationalization() {
            this.appStore.setLoading(true)
            const result = (this.appStore.$state as any).user.locale.split('_')
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/i18nMessages/?currCountry=${result[1]}&currLanguage=${result[0]}&currScript=`)
                .then((response: AxiosResponse<any>) => {
                    this.internationalization = response.data
                    this.setInternationalization(response.data)
                })
                .catch(() => {})

            this.appStore.setLoading(false)
        },
        async loadCrossNavigations() {
            if (this.newDashboardMode) return
            this.appStore.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/crossNavigation/${this.document?.label}/loadCrossNavigationByDocument`)
                .then((response: AxiosResponse<any>) => (this.crossNavigations = response.data))
                .catch(() => {})
            this.appStore.setLoading(false)
            //('------- loaded cross navigations: ', this.crossNavigations)
            this.store.setCrossNavigations(this.dashboardId, this.crossNavigations)
        },
        async loadHtmlGallery() {
            await this.$http
                .get(import.meta.env.VITE_API_PATH + `1.0/widgetgallery/widgets/html`)
                .then((response: AxiosResponse<any>) => (this.htmlGallery = response.data))
                .catch(() => {})
        },
        async loadCustomChartGallery() {
            await this.$http
                .get(import.meta.env.VITE_API_PATH + `1.0/widgetgallery/widgets/chart`)
                .then((response: AxiosResponse<any>) => (this.customChartGallery = response.data))
                .catch(() => {})
        },
        loadOutputParameters() {
            if (this.newDashboardMode) return
            // TODO - Remove Mocked Output Parameters
            // console.log('----- Dashboard Controller --------- document: ', this.document)
            const formattedOutputParameters = this.document ? getFormattedOutputParameters(this.document.outputParameters) : []
            this.store.setOutputParameters(this.dashboardId, formattedOutputParameters)
        },

        loadProfileAttributes() {
            this.profileAttributes = []
            const user = this.appStore.getUser()
            if (user && user.attributes) {
                Object.keys(user.attributes).forEach((key: string) => this.profileAttributes.push({ name: key, value: user.attributes[key] }))
            }
            this.setProfileAttributes(this.profileAttributes)
        },
        setEventListeners() {
            emitter.on('openNewWidgetPicker', () => this.openNewWidgetPicker())
            emitter.on('openDatasetManagement', () => this.openDatasetManagementDialog())
            emitter.on('openWidgetEditor', (widget) => this.openWidgetEditor(widget as IWidget))
            emitter.on('saveDashboard', () => this.onSaveDashboardClicked())
            emitter.on('openDashboardGeneralSettings', () => this.openGeneralSettings())
            emitter.on('executeCrossNavigation', this.executeCrossNavigation)
        },
        openNewWidgetPicker() {
            this.widgetPickerVisible = true
        },
        openDatasetManagementDialog() {
            this.datasetEditorVisible = true
            clearAllDatasetIntervals()
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
            clearAllDatasetIntervals()
        },
        emptyStoreValues() {
            this.store.removeDashboard(this.dashboardId)
            this.store.setCrossNavigations(this.dashboardId, [])
            this.store.setOutputParameters(this.dashboardId, [])
            this.store.setSelections(this.dashboardId, [], this.$http)
            this.store.setSelections(this.dashboardId, [], this.$http)
            this.setDashboardDrivers(this.dashboardId, [])
            this.setProfileAttributes([])
        },
        closeWidgetEditor() {
            this.widgetEditorVisible = false
            this.selectedWidget = null
            emitter.emit('widgetEditorClosed')
            setDatasetIntervals(this.model.configuration.datasets, this.datasets)
        },
        closeDatasetEditor() {
            this.datasetEditorVisible = false
            emitter.emit('datasetManagementClosed')
        },
        async onSaveDashboardClicked() {
            if (!this.document) return
            if (this.newDashboardMode) {
                this.saveDialogVisible = true
            } else {
                await this.saveDashboard(this.document)
            }
        },
        async saveNewDashboard(document: { name: string; label: string }) {
            await this.saveDashboard(document)
        },
        async saveDashboard(document: any) {
            this.appStore.setLoading(true)
            if (!this.document) return
            const folders = this.newDashboardMode && this.$route.query.folderId ? [this.$route.query.folderId] : []
            const postData = {
                document: {
                    name: document.name,
                    label: document.label,
                    description: document.description,
                    type: 'DOCUMENT_COMPOSITE'
                },
                customData: {
                    templateContent: this.getTemplateContent()
                },
                action: this.newDashboardMode ? 'DOC_SAVE' : 'MODIFY_COCKPIT',
                folders: folders
            }

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/saveDocument`, postData)
                .then(() => {
                    this.appStore.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.saveDialogVisible = false
                    if (this.newDashboardMode) this.$emit('newDashboardSaved', document)
                })
                .catch(() => {})

            this.appStore.setLoading(false)
        },
        getTemplateContent() {
            const dashboardModel = deepcopy(this.store.getDashboard(this.dashboardId))
            dashboardModel.configuration.selections = this.getSelections(this.dashboardId)
            formatDashboardForSave(dashboardModel)
            return dashboardModel
        },
        onSelectionsRemove(selections: ISelection[]) {
            this.selectionsDialogVisible = false
            this.removeSelections(selections, this.dashboardId)
        },
        openGeneralSettings() {
            this.generalSettingsVisible = true
        },
        closeGeneralSettings() {
            this.generalSettingsVisible = false
            emitter.emit('dashboardGeneralSettingsClosed')
        },
        executeCrossNavigation(payload: { documentCrossNavigationOutputParameters: ICrossNavigationParameter[]; crossNavigationName: string | undefined; crossNavigations: IDashboardCrossNavigation[] }) {
            // console.log('------- CROSS NAVIGATION PAYLOAD: ', payload)
            const crossNavigations = this.getCrossNavigations(this.dashboardId)
            // console.log('------- CROSS NAVIGATION crossNavigations: ', crossNavigations)
            payload.crossNavigations = crossNavigations
            this.$emit('executeCrossNavigation', payload)
        }
    }
})
</script>
<style lang="scss">
.dashboard-container {
    flex: 1;
}
@media screen and (max-width: 600px) {
    .dashboard-container {
        height: calc(100vh - var(--kn-mainmenu-width));
    }
}
</style>
