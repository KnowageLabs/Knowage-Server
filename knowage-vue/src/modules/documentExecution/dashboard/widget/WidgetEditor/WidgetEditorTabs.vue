<template>
    <TabView :activeIndex="activeIndex">
        <TabPanel v-if="propWidget && propWidget.type !== 'selection'" :header="$t('common.data')">
            <WidgetEditorDataTab :propWidget="propWidget" :datasets="datasets" :selectedDatasets="selectedDatasets" @datasetSelected="$emit('datasetSelected', $event)" data-test="data-tab"></WidgetEditorDataTab>
        </TabPanel>
        <TabPanel :header="$t('common.settings')">
            <WidgetEditorSettingsTab :propWidget="propWidget" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers" :variables="variables" :dashboardId="dashboardId" @settingChanged="$emit('settingChanged', $event)"></WidgetEditorSettingsTab>
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing sections.
 */
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable, IDashboardDriver } from '../../Dashboard'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import WidgetEditorDataTab from './WidgetEditorDataTab/WidgetEditorDataTab.vue'
import WidgetEditorSettingsTab from './WidgetEditorSettingsTab/WidgetEditorSettingsTab.vue'

export default defineComponent({
    name: 'widget-editor-tabs',
    components: { TabView, TabPanel, WidgetEditorDataTab, WidgetEditorSettingsTab },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array as PropType<IDashboardDriver[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    emits: ['datasetSelected', 'settingChanged'],
    data() {
        return {
            activeIndex: 0
        }
    }
})
</script>
