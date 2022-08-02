<template>
    <KnHint title="dashboard.widgetEditor.title" hint="dashboard.widgetEditor.hint"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'widget-editor-hint',
    components: { KnHint }
})
</script>
