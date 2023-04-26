<template>
    <KnHint :title="'managers.crossNavigationManagement.title'" :hint="'managers.crossNavigationManagement.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'navigation-hint',
    components: { KnHint }
})
</script>
