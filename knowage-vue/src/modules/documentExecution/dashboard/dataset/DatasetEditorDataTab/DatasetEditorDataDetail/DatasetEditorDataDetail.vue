<template>
    <div v-if="selectedDatasetProp.id" id="dataset-editor-data-detail-container" class="kn-flex p-mt-3 p-mr-3">
        <InfoCard :selectedDatasetProp="selectedDatasetProp" />
        <ParamsCard v-if="selectedDatasetProp.parameters.length > 0" :selectedDatasetProp="selectedDatasetProp" :dashboardDatasetsProp="dashboardDatasetsProp" :documentDriversProp="documentDriversProp" />
        <IndexesCard v-if="selectedDatasetProp.modelCache" :selectedDatasetProp="selectedDatasetProp" />
    </div>
    <div v-else>
        <KnHint class="p-as-center" :title="'documentExecution.dossier.title'" :hint="'documentExecution.dossier.hint'"></KnHint>
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
    props: { selectedDatasetProp: { required: true, type: Object }, dashboardDatasetsProp: { required: true, type: Array as any }, documentDriversProp: { type: Array as any } },
    emits: [],
    data() {
        return {}
    },
    setup() {},
    async created() {},
    methods: {}
})
</script>
