<template>
    <div v-if="settings.paginationOption">
        <WidgetEditorPaginationOptions :widgetType="widgetType"></WidgetEditorPaginationOptions>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from './WidgetEditorDataOptionsDescriptor.json'
import WidgetEditorPaginationOptions from './options/WidgetEditorPaginationOptions.vue'

export default defineComponent({
    name: 'widget-editor-data-options',
    components: { WidgetEditorPaginationOptions },
    props: { widgetType: { type: String } },
    data() {
        return {
            descriptor,
            settings: {} as any
        }
    },
    created() {
        this.loadSettings()
    },
    methods: {
        loadSettings() {
            this.settings = this.widgetType && descriptor[this.widgetType] ? descriptor[this.widgetType] : descriptor['default']
            console.log('loadSettings() - settings: ', this.settings)
        }
    }
})
</script>
