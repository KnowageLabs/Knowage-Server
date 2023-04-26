<template>
    <KnHint :title="'managers.lovsManagement.title'" :hint="'managers.lovsManagement.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'
export default defineComponent({
    name: 'lovs-management-hint',
    components: { KnHint }
})
</script>
