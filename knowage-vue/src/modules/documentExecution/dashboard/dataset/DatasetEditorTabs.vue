<template>
    <div class="datasetEditor-tabs">
        <TabView>
            <TabPanel :header="$t('dashboard.datasetEditor.dataTabTitle')">
                <DataTab id="dataset-editor-list-card" :dashboardDatasetsProp="dashboardDatasetsProp" :availableDatasetsProp="availableDatasetsProp" />
            </TabPanel>
            <TabPanel :header="$t('dashboard.datasetEditor.associationsTabTitle')"> Associations </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
/**
 * ! this component is in charge of controlling the tabview of the dataset
 */
import { defineComponent } from 'vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DataTab from './DatasetEditorDataTab/DatasetEditorDataTab.vue'

export default defineComponent({
    name: 'widget-editor-tabs',
    components: { TabView, TabPanel, DataTab },
    props: { dashboardDatasetsProp: { required: true, type: Array }, availableDatasetsProp: { required: true, type: Array } },
    emits: [],
    data() {
        return {}
    }
})
</script>
<style lang="scss">
.datasetEditor-tabs {
    flex: 1;
    .p-tabview {
        width: 100%;
        height: 100%;
        .p-tabview-panels {
            padding: 0;
        }
    }
}
</style>
