<template>
    <WidgetEditorSettingsList v-if="descriptor" :widgetModel="propWidget" :options="descriptor.settingsListOptions" @itemClicked="onItemClicked"></WidgetEditorSettingsList>
    <div v-if="propWidget" class="p-d-flex kn-flex kn-overflow">
        <TableWidgetSettingsContainer
            v-if="propWidget.type === 'table'"
            class="model-div kn-flex kn-overflow p-my-3 p-mr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :drivers="drivers"
            :variables="variables"
            :dashboardId="dashboardId"
        ></TableWidgetSettingsContainer>
        <SelectorWidgetSettingsContainer
            v-else-if="propWidget.type === 'selector'"
            class="model-div kn-flex kn-overflow p-my-3 p-mr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :drivers="drivers"
            :variables="variables"
        ></SelectorWidgetSettingsContainer>
        <SelectionsWidgetSettingsContainer
            v-else-if="propWidget.type === 'selection'"
            class="model-div kn-flex kn-overflow p-my-3 p-mr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :drivers="drivers"
            :variables="variables"
        ></SelectionsWidgetSettingsContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '../../../Dashboard'
import tableDescriptor from './TableWidget/TableWidgetSettingsDescriptor.json'
import TableWidgetSettingsContainer from './TableWidget/TableWidgetSettingsContainer.vue'
import SelectorWidgetSettingsContainer from './SelectorWidget/SelectorWidgetSettingsContainer.vue'
import SelectionsWidgetSettingsContainer from './SelectionsWidget/SelectionsWidgetSettingsContainer.vue'
import selectorDescriptor from './SelectorWidget/SelectorWidgetSettingsDescriptor.json'
import selectionsDescriptor from './SelectionsWidget/SelectionsWidgetSettingsDescriptor.json'
import WidgetEditorSettingsList from './WidgetEditorSettingsList.vue'

export default defineComponent({
    name: 'widget-editor-settings-tab',
    components: { TableWidgetSettingsContainer, WidgetEditorSettingsList, SelectorWidgetSettingsContainer, SelectionsWidgetSettingsContainer },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array },
        variables: { type: Array as PropType<IVariable[]> },
        dashboardId: { type: String, required: true }
    },
    emits: [],
    data() {
        return {
            descriptor: null as any,
            selectedDescriptor: {},
            selectedSetting: ''
        }
    },
    created() {
        this.loadDescriptor()
    },
    methods: {
        loadDescriptor() {
            switch (this.propWidget.type) {
                case 'table':
                    this.descriptor = tableDescriptor
                    break
                case 'selector':
                    this.descriptor = selectorDescriptor
                    break
                case 'selection':
                    this.descriptor = selectionsDescriptor
            }
        },
        onItemClicked(item: any) {
            this.selectedSetting = item.value
            this.selectedDescriptor = { table: item.descriptor }
        }
    }
})
</script>
