<template>
    <div class="widgetEditor-tabs">
        <TabView>
            <TabPanel header="Data">
                Content I
            </TabPanel>
            <TabPanel header="Settings">
                Content II
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing sections.
 */
import { defineComponent } from 'vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'widget-editor-tabs',
    components: { TabView, TabPanel },
    props: {
        widget: {
            required: true,
            type: Object
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
