<template>
    <div class="widgetEditor-tabs">
        <TabView>
            <TabPanel header="Data">
                <WidgetEditorDataTab :propWidget="propWidget" :datasets="datasets" :modelDatasets="modelDatasets" @datasetSelected="$emit('datasetSelected', $event)"></WidgetEditorDataTab>
            </TabPanel>
            <TabPanel header="Settings">
                <WidgetEditorSettingsTab :propWidget="propWidget"></WidgetEditorSettingsTab>
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing sections.
 */
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../Dashboard'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import WidgetEditorDataTab from './WidgetEditorDataTab/WidgetEditorDataTab.vue'
import WidgetEditorSettingsTab from './WidgetEditorSettingsTab/WidgetEditorSettingsTab.vue'

export default defineComponent({
    name: 'widget-editor-tabs',
    components: { TabView, TabPanel, WidgetEditorDataTab, WidgetEditorSettingsTab },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array } },
    emits: ['datasetSelected'],
    data() {
        return {
            modelDatasets: [] as any[]
        }
    }
})
</script>
<style lang="scss">
.widgetEditor-tabs {
    flex: 1;
    .p-tabview {
        width: 100%;
        height: 100%;
    }
}
</style>
