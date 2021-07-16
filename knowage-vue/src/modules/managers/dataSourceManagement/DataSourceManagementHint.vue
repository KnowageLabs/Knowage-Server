<template>
    <KnHint :title="'managers.dataSourceManagement.title'" :hint="'managers.dataSourceManagement.hint'" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'datasource-management-hint',
    components: { KnHint }
})
</script>
