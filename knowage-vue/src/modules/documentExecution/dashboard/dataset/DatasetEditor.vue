<template>
    <Teleport to=".dashboard-container">
        <div class="datasetEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ $t('dashboard.datasetEditor.title') }} </template>
                <template #end>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('closeDatasetEditor')" />
                </template>
            </Toolbar>
            <div class="datasetEditor-container">
                <DatasetEditorTabs :dashboardDatasetsProp="dashboardDatasets" />
                <DatasetEditorPreview :dashboardDatasetsProp="dashboardDatasets" />
            </div>
        </div>
    </Teleport>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the dataset.
 */
import { defineComponent } from 'vue'
import DatasetEditorTabs from './DatasetEditorTabs.vue'
import DatasetEditorPreview from './DatasetEditorPreview.vue'
import mainStore from '../../../../App.store'
import dashStore from '../Dashboard.store'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'dataset-editor',
    components: { DatasetEditorTabs, DatasetEditorPreview },
    props: {},
    emits: ['closeDatasetEditor'],
    data() {
        return {
            dashboardDatasets: {} as any
        }
    },
    setup() {
        const store = mainStore()
        const dashboardStore = dashStore()
        return { store, dashboardStore }
    },
    created() {
        console.log('STORE MODEL', this.dashboardStore.$state.dashboards[1])
        this.dashboardDatasets = deepcopy(this.dashboardStore.$state.dashboards[1].configuration.datasets)
    },

    methods: {}
})
</script>
<style lang="scss">
.datasetEditor {
    height: 100vh;
    width: 100%;
    top: 0;
    left: 0;
    background-color: white;
    position: absolute;
    z-index: 999;
    display: flex;
    flex-direction: column;
    .datasetEditor-container {
        flex: 1;
        display: flex;
    }
}
</style>
