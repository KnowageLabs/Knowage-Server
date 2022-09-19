<template>
    <div v-if="crossNavigationModel">
        <div class="p-d-flex p-flex-row p-ai-center">
            <div class="kn-flex p-mx-4 p-my-2">
                <InputSwitch v-model="crossNavigationModel.enabled"></InputSwitch>
                <label class="kn-material-input-label p-ml-4">{{ $t('dashboard.widgetEditor.interactions.enableCrossNavigation') }}</label>
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-m-2">
                <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                <Dropdown class="kn-material-input" v-model="crossNavigationModel.type" :options="descriptor.interactionTypes" optionValue="value" :disabled="crossNavigationDisabled" @change="onInteractionTypeChanged">
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
        <div class="p-d-flex p-flex-row p-ai-center p-mt-2">
            <div class="p-d-flex p-flex-row p-ai-center kn-flex p-mx-2">
                <div class="p-d-flex p-flex-column kn-flex">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="crossNavigationModel.column" :options="widgetModel.columns" optionLabel="alias" optionValue="id" :disabled="crossNavigationDisabled"> </Dropdown>
                </div>
            </div>
            <div class="p-d-flex p-flex-row p-ai-center kn-flex">
                <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                    <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.interactions.crossNavigationName') }}</label>
                    <Dropdown class="kn-material-input" v-model="crossNavigationModel.name" :options="crossNavigationOptions" :disabled="crossNavigationDisabled"> </Dropdown>
                </div>
            </div>
            <div v-if="crossNavigationModel.type === 'icon'" class="p-m-4">
                <WidgetEditorStyleToolbar :options="[{ type: 'icon' }]" :propModel="{ icon: crossNavigationModel.icon }" :disabled="crossNavigationDisabled" @change="onStyleToolbarChange($event)"> </WidgetEditorStyleToolbar>
            </div>
        </div>
        <div v-if="crossNavigationModel.parameters" class="p-d-flex p-flex-row p-ai-center p-m-2">
            <TableWidgetOutputParametersList class="kn-flex p-mr-2" :widgetModel="widgetModel" :propParameters="parameterList" :selectedDatasetsColumnsMap="selectedDatasetsColumnsMap" :disabled="crossNavigationDisabled" @change="onParametersChanged"></TableWidgetOutputParametersList>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetCrossNavigation, ITableWidgetParameter, IDataset, IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { emitter } from '../../../../../../DashboardHelpers'
import descriptor from '../../TableWidgetSettingsDescriptor.json'
import dashboardStore from '@/modules/documentExecution/Dashboard/Dashboard.store'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import TableWidgetOutputParametersList from './TableWidgetOutputParametersList.vue'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-cross-navigation',
    components: { Dropdown, InputSwitch, TableWidgetOutputParametersList, WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> }
    },
    data() {
        return {
            descriptor,
            crossNavigationModel: null as ITableWidgetCrossNavigation | null,
            crossNavigationOptions: [] as string[],
            outputParameters: [] as any[],
            parameterList: [] as ITableWidgetParameter[],
            selectedDatasetsColumnsMap: {},
            getTranslatedLabel
        }
    },
    computed: {
        crossNavigationDisabled() {
            return !this.crossNavigationModel || !this.crossNavigationModel.enabled
        }
    },
    setup() {
        const store = dashboardStore()
        return { store }
    },
    created() {
        this.setEventListeners()
        this.loadCrossNavigationModel()
        this.loadCrossNavigationOptions()
        this.loadOutputParameters()
        this.loadParameterList()
        this.loadSelectedDatasetColumnNames()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromCrossNavigation', () => this.onColumnRemoved())
        },
        loadCrossNavigationModel() {
            if (this.widgetModel?.settings?.interactions?.crosssNavigation) this.crossNavigationModel = this.widgetModel.settings.interactions.crosssNavigation
        },
        loadCrossNavigationOptions() {
            const temp = this.store.getCrossNavigations()
            if (temp) this.crossNavigationOptions = temp.map((crossNavigation: any) => crossNavigation.crossName)
        },
        onColumnRemoved() {
            this.loadCrossNavigationModel()
            this.loadParameterList()
        },
        loadOutputParameters() {
            console.log('AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA >  ', this.store.getOutputParameters())
            this.outputParameters = this.store.getOutputParameters() ?? []
        },
        loadParameterList() {
            if (!this.crossNavigationModel) return
            console.log('OUTPUT PARAMETERS: ', this.outputParameters)
            this.parameterList = []
            for (let i = 0; i < this.outputParameters.length; i++) {
                const outputParameter = this.outputParameters[i]
                const temp = { enabled: false, name: outputParameter.name, type: '' } as ITableWidgetParameter
                const index = this.crossNavigationModel.parameters.findIndex((parameter: ITableWidgetParameter) => parameter.name === outputParameter.name)
                if (index !== -1) {
                    const modelParameter = this.crossNavigationModel?.parameters[index]
                    temp.enabled = modelParameter.enabled
                    temp.type = modelParameter.type
                    temp.value = modelParameter.value
                    if (modelParameter.column) temp.column = modelParameter.column
                    if (modelParameter.dataset) temp.dataset = modelParameter.dataset
                }
                this.parameterList.push(temp)
            }
        },
        loadSelectedDatasetColumnNames() {
            if (!this.selectedDatasets || this.selectedDatasets.length === 0) return

            this.selectedDatasets.forEach((dataset: IDataset) => this.loadCrossSelectedDatasetColumnName(dataset))
        },
        loadCrossSelectedDatasetColumnName(dataset: IDataset) {
            this.selectedDatasetsColumnsMap[dataset.name] = []
            for (let i = 0; i < dataset.metadata.fieldsMeta.length; i++) {
                this.selectedDatasetsColumnsMap[dataset.name].push(dataset.metadata.fieldsMeta[i].name)
            }
        },
        onInteractionTypeChanged() {
            if (this.crossNavigationModel && this.crossNavigationModel.type !== 'icon') delete this.crossNavigationModel.icon
        },
        onParametersChanged(parameters: ITableWidgetParameter[]) {
            if (this.crossNavigationModel) this.crossNavigationModel.parameters = parameters
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (this.crossNavigationModel) this.crossNavigationModel.icon = model.icon
        }
    }
})
</script>
