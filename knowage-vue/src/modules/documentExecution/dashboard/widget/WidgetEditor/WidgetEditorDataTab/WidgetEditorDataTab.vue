<template>
    <div class="p-d-flex">
        <Card class="kn-flex p-m-2">
            <template #content>
                <WidgetEditorDataList :datasets="datasets" :modelDatasets="modelDatasets" @datasetSelected="$emit('datasetSelected', $event)"></WidgetEditorDataList>
            </template>
        </Card>
        <WidgetEditorGeneric class="kn-flex p-m-2" :widgetModel="propWidget"></WidgetEditorGeneric>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import Card from 'primevue/card'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'
import WidgetEditorGeneric from '../WidgetEditorGeneric/WidgetEditorGeneric.vue'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: { Card, WidgetEditorDataList, WidgetEditorGeneric },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array }, modelDatasets: { type: Array } },
    emits: ['datasetSelected'],
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
