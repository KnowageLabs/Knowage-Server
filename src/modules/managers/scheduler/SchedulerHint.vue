<template>
    <KnHint :title="'managers.scheduler.title'" :hint="'managers.scheduler.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'scheduler-hint',
    components: { KnHint }
})
</script>
