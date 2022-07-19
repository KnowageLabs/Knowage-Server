<template>
    <div class="p-d-flex">
        <WidgetEditorDataList class="kn-flex" :datasets="datasets" :modelDatasets="modelDatasets" @datasetSelected="$emit('datasetSelected', $event)"></WidgetEditorDataList>
        <div class="kn-flex">Other Content</div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: { WidgetEditorDataList },
    props: { datasets: { type: Array }, modelDatasets: { type: Array } },
    emits: ['datasetSelected'],
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
