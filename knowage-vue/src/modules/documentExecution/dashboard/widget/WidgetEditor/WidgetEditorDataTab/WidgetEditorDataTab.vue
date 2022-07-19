<template>
    <div class="p-d-flex">
        <Card class="kn-flex p-m-2">
            <template #content>
                <WidgetEditorDataList :datasets="datasets" :modelDatasets="modelDatasets" @datasetSelected="$emit('datasetSelected', $event)"></WidgetEditorDataList>
            </template>
        </Card>
        <Card class="kn-flex p-m-2">
            <template #content>
                <WidgetEditorDataOptions></WidgetEditorDataOptions>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'
import WidgetEditorDataOptions from './WidgetEditorDataOptions/WidgetEditorDataOptions.vue'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: { Card, WidgetEditorDataList, WidgetEditorDataOptions },
    props: { datasets: { type: Array }, modelDatasets: { type: Array } },
    emits: ['datasetSelected'],
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
