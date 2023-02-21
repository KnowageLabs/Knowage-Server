<template>
    <div v-if="selectedDatasetProp.id" id="dataset-editor-data-detail-container" class="kn-flex p-mt-3 kn-overflow-y kn-overflow-x-hidden dashboard-scrollbar">
        <InfoCard :selected-dataset-prop="selectedDatasetProp" />
        <ParamsCard v-if="selectedDatasetProp.parameters?.length > 0 || selectedDatasetProp.drivers?.length > 0" :selected-dataset-prop="selectedDatasetProp" :dashboard-datasets-prop="dashboardDatasetsProp" :document-drivers-prop="documentDriversProp" :dashboard-id="dashboardId" />
        <IndexesCard v-if="selectedDatasetProp.modelCache" :selected-dataset-prop="selectedDatasetProp" />
    </div>
    <div v-else>
        <KnHint class="p-as-center" :title="'dashboard.datasetEditor.dataDetailTitle'" :hint="'dashboard.datasetEditor.dataDetailMsg'" data-test="dataset-hint"></KnHint>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import InfoCard from './DatasetEditorDataDetailInfo.vue'
import ParamsCard from './DatasetEditorDataDetailPrameters.vue'
import IndexesCard from './DatasetEditorDataDetailIndexes.vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'dataset-editor-data-detail',
    components: { InfoCard, ParamsCard, IndexesCard, KnHint },
    props: { selectedDatasetProp: { required: true, type: Object }, dashboardDatasetsProp: { required: true, type: Array as any }, documentDriversProp: { type: Array as any }, dashboardId: { type: String, required: true } },
    emits: [],
    setup() {},
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
