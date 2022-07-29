<template>
    <div class="widget-editor-toolbar p-d-flex p-flex-row p-ai-center">
        <WidgetEditorStyleIcon v-for="(icon, index) in icons" :key="index" class="kn-flex" :widgetModel="widgetModel" :icon="icon" :item="item" :itemIndex="itemIndex"> </WidgetEditorStyleIcon>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import WidgetEditorStyleIcon from './WidgetEditorStyleIcon.vue'

export default defineComponent({
    name: 'widget-editor-style-toolbar',
    components: { WidgetEditorStyleIcon },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, icons: { type: Array as PropType<any[]>, required: true }, settings: { type: Object, required: true }, item: { type: Object }, itemIndex: { type: Number } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss" scoped>
.widget-editor-toolbar {
    border: 1px solid #c2c2c2;
    border-radius: 3px;
    width: 100%;
    min-height: 40px;
}
</style>
