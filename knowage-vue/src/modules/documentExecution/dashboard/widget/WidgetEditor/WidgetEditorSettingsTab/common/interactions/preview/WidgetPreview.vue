<template>
    <div v-if="previewModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-grid p-col-12 p-pt-4 p-ai-center">
            <div class="p-col-6 p-sm-12 p-md-6">
                <InputSwitch v-model="previewModel.enabled"></InputSwitch>
                <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.interactions.enablePreview') }}</label>
            </div>
            <div class="p-col-6 p-sm-12 p-md-6 p-d-flex p-flex-column kn-flex p-px-2">
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
            <div class="p-sm-12 p-md-6 p-px-2">
                <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                    <label class="kn-material-input-label"> {{ $t('common.dataset') }}</label>
                    <Dropdown class="kn-material-input" v-model="previewModel.dataset" :options="selectedDatasets" optionLabel="name" optionValue="id.dsId" :disabled="previewDisabled" @change="onDatasetChanged"> </Dropdown>
                </div>
            </div>
            <div v-if="previewModel.type === 'singleColumn'" class="p-sm-11 p-md-5">
                <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="previewModel.column" :options="getSelectionDatasetColumnOptions()" :disabled="previewDisabled"> </Dropdown>
                </div>
            </div>
            <div v-else-if="previewModel.type === 'icon'" class="p-sm-11 p-md-5 p-p-4">
                <WidgetEditorStyleToolbar :options="[{ type: 'icon' }]" :propModel="{ icon: previewModel.icon }" :disabled="previewDisabled" @change="onStyleToolbarChange($event)"> </WidgetEditorStyleToolbar>
            </div>
            <div class="p-sm-1 p-md-1">
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.interactions.directDownload') }}</label>
                <Checkbox v-model="previewModel.directDownload" :binary="true" :disabled="previewDisabled" />
            </div>
        </div>
        <div v-if="previewModel.parameters.length > 0" class="p-col-12 p-p-2">
            <TableWidgetPreviewParameterList
                class="kn-flex p-mr-2"
                :widgetModel="widgetModel"
                :propParameters="previewModel.parameters"
                :selectedDatasetsColumnsMap="selectedDatasetColumnNameMap"
                :drivers="drivers"
                :disabled="previewDisabled"
                @change="onParametersChanged"
            ></TableWidgetPreviewParameterList>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetParameter, IDataset, IDatasetParameter, IWidgetStyleToolbarModel, IWidgetPreview } from '@/modules/documentExecution/dashboard/Dashboard'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { emitter } from '../../../../../../DashboardHelpers'
import descriptor from '../WidgetInteractionsDescriptor.json'
import dashboardStore from '../../../../../../Dashboard.store'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import TableWidgetPreviewParameterList from './WidgetPreviewParameterList.vue'
import WidgetEditorStyleToolbar from '../../styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-preview',
    components: {
        Checkbox,
        Dropdown,
        InputSwitch,
        TableWidgetPreviewParameterList,
        WidgetEditorStyleToolbar
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array },
        dashboardId: { type: String, required: true }
    },
    data() {
        return {
            descriptor,
            previewModel: null as IWidgetPreview | null,
            dashboardModel: null as any,
            dashboardDatasets: [] as any[],
            selectedDatasetColumnIdMap: {},
            selectedDatasetColumnNameMap: {},
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
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromPreview', this.onColumnRemovedFromPreview)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromPreview', this.onColumnRemovedFromPreview)
        },
        onColumnRemovedFromPreview() {
            this.onColumnRemoved()
        },
        loadPreviewModel() {
            if (this.widgetModel?.settings?.interactions?.preview) this.previewModel = this.widgetModel.settings.interactions.preview
        },
        loadDashboardModel() {
            this.dashboardModel = this.store.getDashboard(this.dashboardId)
            this.loadDatasetsFromModel()
        },
        loadDatasetsFromModel() {
            this.dashboardDatasets = this.dashboardModel?.configuration.datasets
        },
        onColumnRemoved() {
            this.loadPreviewModel()
        },
        loadSelectedDatasetColumnNames() {
            if (!this.selectedDatasets || this.selectedDatasets.length === 0) return
            this.selectedDatasets.forEach((dataset: IDataset) => this.loadCrossSelectedDatasetColumnName(dataset))
        },
        loadCrossSelectedDatasetColumnName(dataset: IDataset) {
            this.selectedDatasetColumnNameMap[dataset.name] = []
            this.selectedDatasetColumnIdMap[dataset.id.dsId] = []
            for (let i = 0; i < dataset.metadata.fieldsMeta.length; i++) {
                this.selectedDatasetColumnIdMap[dataset.id.dsId].push(dataset.metadata.fieldsMeta[i].name)
                this.selectedDatasetColumnNameMap[dataset.name].push(dataset.metadata.fieldsMeta[i].name)
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
            return this.previewModel?.dataset && this.selectedDatasetColumnIdMap ? this.selectedDatasetColumnIdMap[this.previewModel.dataset] : []
        },
        onDatasetChanged() {
            if (!this.previewModel) return
            this.previewModel.column = ''
            this.previewModel.parameters = []
            const index = this.dashboardDatasets.findIndex((dataset: any) => dataset.id === this.previewModel?.dataset)
            if (index !== -1)
                this.previewModel.parameters = this.dashboardDatasets[index].parameters.map((tempParameter: IDatasetParameter) => {
                    return {
                        enabled: true,
                        name: tempParameter.name,
                        type: '',
                        value: ''
                    }
                })
        }
    }
})
</script>
