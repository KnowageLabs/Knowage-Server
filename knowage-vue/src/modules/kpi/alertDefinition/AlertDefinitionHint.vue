<template>
    <KnHint :title="'kpi.alert.title'" :hint="'kpi.alert.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'alert-hint',
    components: { KnHint }
})
</script>
