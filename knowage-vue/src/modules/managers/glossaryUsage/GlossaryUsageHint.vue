<template>
    <KnHint :title="'managers.glossaryUsage.title'" :hint="'managers.glossaryUsage.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'
export default defineComponent({
    name: 'glossary-usage-hint',
    components: { KnHint }
})
</script>
