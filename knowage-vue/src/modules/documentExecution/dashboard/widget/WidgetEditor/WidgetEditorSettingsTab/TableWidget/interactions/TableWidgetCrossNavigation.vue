<template>
    <div v-if="crossNavigationModel">
        {{ crossNavigationModel }}
        {{ 'OUTPUT PARS: ' }}
        {{ outputParameters }}
        <div class="p-d-flex p-flex-row p-ai-center">
            <div class="kn-flex p-mx-4 p-my-2">
                <InputSwitch v-model="crossNavigationModel.enabled"></InputSwitch>
                <label class="kn-material-input-label p-ml-4">{{ $t('dashboard.widgetEditor.interactions.enableCrossNavigation') }}</label>
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-m-2 value-type-dropdown">
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
        <div class="p-d-flex p-flex-row p-ai-center">
            <div class="p-d-flex p-flex-row p-ai-center p-m-3">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="crossNavigationModel.column" :options="widgetModel.columns" optionLabel="alias" optionValue="id"> </Dropdown>
                </div>
            </div>
            <div v-if="outputParameters.length > 0" class="p-d-flex p-flex-row p-ai-center p-m-3">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.interactions.outputParameter') }}</label>
                    <Dropdown class="kn-material-input" v-model="crossNavigationModel.parameter" :options="outputParameters" optionValue="name" optionLabel="name"> </Dropdown>
                </div>
            </div>
            <div class="p-d-flex p-flex-row p-ai-center p-m-3">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.interactions.crossNavigationName') }}</label>
                    <Dropdown class="kn-material-input" v-model="crossNavigationModel.name" :options="crossNavigationOptions"> </Dropdown>
                </div>
            </div>
        </div>
        <div v-if="crossNavigationModel.parameters" class="p-d-flex p-flex-row p-ai-center">
            <TableWidgetOutputParametersList :widgetModel="widgetModel" :propParameters="parameterList"></TableWidgetOutputParametersList>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetCorssNavigation, ITableWidgetParameter, IDataset } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import dashboardStore from '@/modules/documentExecution/Dashboard/Dashboard.store'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import TableWidgetOutputParametersList from './TableWidgetOutputParametersList.vue'

export default defineComponent({
    name: 'table-widget-cross-navigation',
    components: { Dropdown, InputSwitch, TableWidgetOutputParametersList },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> }
    },
    data() {
        return {
            descriptor,
            crossNavigationModel: null as ITableWidgetCorssNavigation | null,
            crossNavigationOptions: [] as string[],
            outputParameters: [] as any[],
            parameterList: [] as ITableWidgetParameter[],
            selectedDatasetsColumnNames: [] as string[],
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
        this.loadCrossNavigationModel()
        this.loadCrossNavigationOptions()
        this.loadOutputParameters()
        this.loadParameterList()
        // this.loadSelectedDatasetColumnNames()
    },
    methods: {
        loadCrossNavigationModel() {
            if (this.widgetModel?.settings?.interactions?.crosssNavigation) this.crossNavigationModel = this.widgetModel.settings.interactions.crosssNavigation
        },
        loadCrossNavigationOptions() {
            const temp = this.store.getCrossNavigations()
            if (temp) this.crossNavigationOptions = temp.map((crossNavigation: any) => crossNavigation.crossName)
        },
        loadOutputParameters() {
            this.outputParameters = this.store.getOutputParameters() ?? []
        },
        loadParameterList() {
            console.log('>>>>>>>>>>>>>> OUTPUT PARAMETERS: ', this.outputParameters)
            console.log('>>>>>>>>>>>>>> OUTPUT PARAMETERS: ', this.crossNavigationModel?.parameters)
            if (!this.crossNavigationModel) return
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
                    if (modelParameter.dataset) temp.column = modelParameter.dataset
                }
                this.parameterList.push(temp)
            }
        },
        // loadSelectedDatasetColumnNames() {
        //     this.selectedDatasetsColumnNames = []
        //     if (!this.selectedDatasets || this.selectedDatasets.length === 0) return

        //     const index = this.selectedDatasets.findIndex((dataset: any) => dataset.id?.dsId === this.selectedDataset?.id)
        //     if (index !== -1) this.loadCroaddSelectedDatasetColumnName(this.selectedDatasets[index].metadata.fieldsMeta)
        // },
        // loadCroaddSelectedDatasetColumnName(fieldsMeta: any[]) {
        //     for (let i = 0; i < fieldsMeta.length; i++) {
        //         this.selectedDatasetColumns.push({ ...fieldsMeta[i], dataset: this.selectedDataset?.id })
        //     }
        // },
        onInteractionTypeChanged() {}
    }
})
</script>
