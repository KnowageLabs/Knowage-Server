<template>
    <div v-if="previewModel" class="p-grid">
        {{ previewModel }}
        <div class="p-grid p-col-12 p-pt-4 p-ai-center">
            <div class="p-col-6 p-sm-12 p-md-6">
                <InputSwitch v-model="previewModel.enabled"></InputSwitch>
                <label class="kn-material-input-label p-ml-4">{{ $t('dashboard.widgetEditor.interactions.enablePreview') }}</label>
            </div>
            <div class="p-col-6 p-sm-12 p-md-6 p-d-flex p-flex-column kn-flex p-mx-2">
                <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                <Dropdown class="kn-material-input" v-model="previewModel.type" :options="descriptor.interactionTypes" optionValue="value" :disabled="previewDisabled" @change="onInteractionTypeChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.interactionTypes, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>
        </div>
        <div class="p-grid p-col-12 p-mt-2">
            <div class="p-col-6 p-sm-12 p-md-6 p-px-2">
                <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                    <label class="kn-material-input-label"> {{ $t('common.dataset') }}</label>
                    <Dropdown class="kn-material-input" v-model="previewModel.dataset" :options="selectedDatasets" optionLabel="name" optionValue="id.dsId" :disabled="previewDisabled" @change="onDatasetChanged"> </Dropdown>
                </div>
            </div>
            <div v-if="previewModel.type === 'singleColumn'" class="p-col-6 p-sm-12 p-md-6">
                <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="previewModel.column" :options="getSelectionDatasetColumnOptions()" :disabled="previewDisabled"> </Dropdown>
                </div>
            </div>
            <div v-else-if="previewModel.type === 'icon'" class="p-col-2 p-p-4">
                <WidgetEditorStyleToolbar :options="[{ type: 'icon' }]" :propModel="{ icon: previewModel.icon }" @change="onStyleToolbarChange($event)"> </WidgetEditorStyleToolbar>
            </div>
        </div>
        <div v-if="selectedDatasetParameters.length > 0" class="p-col-12 p-p-2">
            <TableWidgetPreviewParameterList class="kn-flex p-mr-2" :widgetModel="widgetModel" :propParameters="previewModel.parameters" :datasetParameters="selectedDatasetParameters" :disabled="previewDisabled" @change="onParametersChanged"></TableWidgetPreviewParameterList>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetParameter, IDataset, IDatasetParameter, IWidgetStyleToolbarModel, ITableWidgetPreview } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import dashboardStore from '@/modules/documentExecution/Dashboard/Dashboard.store'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import TableWidgetPreviewParameterList from './TableWidgetPreviewParameterList.vue'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-preview',
    components: { Dropdown, InputSwitch, TableWidgetPreviewParameterList, WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> }
    },
    data() {
        return {
            descriptor,
            previewModel: null as ITableWidgetPreview | null,
            dashboardModel: null as any,
            dashboardDatasets: [] as any[],
            selectedDatasetParameters: [] as IDatasetParameter[],
            selectedDatasetColumnNamesMap: {},
            getTranslatedLabel
        }
    },
    computed: {
        previewDisabled() {
            return !this.previewModel || !this.previewModel.enabled
        }
    },
    setup() {
        const store = dashboardStore()
        return { store }
    },
    created() {
        this.setEventListeners()
        this.loadDashboardModel()
        this.loadPreviewModel()
        this.loadSelectedDatasetColumnNames()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromPreview', () => this.onColumnRemoved())
        },
        loadPreviewModel() {
            if (this.widgetModel?.settings?.interactions?.preview) this.previewModel = this.widgetModel.settings.interactions.preview
            this.loadSelectedDatasetParameters()
        },
        loadDashboardModel() {
            // TODO - remove hardcoded id
            this.dashboardModel = this.store.getDashboard(1)
            console.log('LOADED MODEL: ', this.dashboardModel)
            this.loadDatasetsFromModel()
        },
        loadDatasetsFromModel() {
            this.dashboardDatasets = this.dashboardModel.configuration.datasets
        },
        loadSelectedDatasetParameters() {
            this.selectedDatasetParameters = []
            const index = this.dashboardDatasets.findIndex((dataset: any) => dataset.id === this.previewModel?.dataset)
            if (index !== -1) this.selectedDatasetParameters = this.dashboardDatasets[index].parameters
        },
        onColumnRemoved() {
            this.loadPreviewModel()
        },
        loadSelectedDatasetColumnNames() {
            if (!this.selectedDatasets || this.selectedDatasets.length === 0) return
            this.selectedDatasets.forEach((dataset: IDataset) => this.loadCrossSelectedDatasetColumnName(dataset))
        },
        loadCrossSelectedDatasetColumnName(dataset: IDataset) {
            this.selectedDatasetColumnNamesMap[dataset.id.dsId] = []
            for (let i = 0; i < dataset.metadata.fieldsMeta.length; i++) {
                this.selectedDatasetColumnNamesMap[dataset.id.dsId].push(dataset.metadata.fieldsMeta[i].name)
            }
        },
        onInteractionTypeChanged() {
            if (!this.previewModel) return
            switch (this.previewModel.type) {
                case 'allRow':
                    delete this.previewModel.column
                    delete this.previewModel.icon
                    break
                case 'singleColumn':
                    delete this.previewModel.icon
                    break
                case 'icon':
                    delete this.previewModel.column
            }
        },
        onParametersChanged(parameters: ITableWidgetParameter[]) {
            if (this.previewModel) this.previewModel.parameters = parameters
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (this.previewModel) this.previewModel.icon = model.icon
        },
        getSelectionDatasetColumnOptions() {
            if (!this.previewModel) return []
            console.log('>>>>>>>>>>>>>>>>>>>> TEST 1: ', this.selectedDatasetColumnNamesMap)
            console.log('>>>>>>>>>>>>>>>>>>>> TEST 2: ', this.selectedDatasetColumnNamesMap[this.previewModel.dataset])
            return this.previewModel?.dataset && this.selectedDatasetColumnNamesMap ? this.selectedDatasetColumnNamesMap[this.previewModel.dataset] : []
        },
        onDatasetChanged() {
            if (!this.previewModel) return
            this.previewModel.column = ''
            this.loadSelectedDatasetParameters()
        }
    }
})
</script>
