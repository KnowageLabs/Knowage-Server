<template>
    <KnHint :title="'managers.glossary.glossaryDefinition.title'" :hint="'managers.glossary.glossaryDefinition.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'glossary-definition-hint',
    components: { KnHint }
})
</script>
