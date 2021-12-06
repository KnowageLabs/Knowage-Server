<template>
    <KnHint :title="'kpi.targetDefinition.title'" :hint="'kpi.targetDefinition.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'target-definition-hint',
    components: { KnHint }
})
</script>
