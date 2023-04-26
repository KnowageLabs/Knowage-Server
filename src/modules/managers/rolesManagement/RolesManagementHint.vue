<template>
    <KnHint :title="'managers.rolesManagement.title'" :hint="'managers.rolesManagement.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'roles-management-hint',
    components: { KnHint }
})
</script>
