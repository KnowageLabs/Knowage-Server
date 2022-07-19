<template>
    <div>
        <WidgetEditorDataList :datasets="datasets"></WidgetEditorDataList>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: {WidgetEditorDataList},
        props: {datasets: {type: Array} },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>