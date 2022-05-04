<template>
    <KnHint :title="'documentBrowser.title'" :hint="'documentBrowser.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'document-browser-hint',
    components: { KnHint }
})
</script>
