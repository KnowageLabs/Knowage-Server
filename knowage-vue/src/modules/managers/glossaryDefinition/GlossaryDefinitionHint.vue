<template>
    <KnHint :title="'managers.glossaryDefinition.title'" :hint="'managers.glossaryDefinition.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'kpi-definition-hint',
    components: { KnHint }
})
</script>
