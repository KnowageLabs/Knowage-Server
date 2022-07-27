<template>
    <AssociationsList :dashboardAssociationsProp="dashboardAssociationsProp" />
    <AssociationsDetail class="p-my-2" :dashboardAssociationsProp="dashboardAssociationsProp" :selectedDatasetsProp="selectedDatasetsProp" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import AssociationsList from './DatasetEditorAssociationsList/DatasetEditorAssociationsList.vue'
import AssociationsDetail from './DatasetEditorAssociationsDetail/DatasetEditorAssociationsDetail.vue'

export default defineComponent({
    name: 'dataset-editor-data-tab',
    components: { AssociationsList, AssociationsDetail },
    props: { dashboardAssociationsProp: { required: true, type: Array }, selectedDatasetsProp: { required: true, type: Array } },
    emits: [],
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
