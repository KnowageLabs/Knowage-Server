<template>
    <KnHint :title="'managers.schedulationAgendaManagement.title'" :hint="'managers.schedulationAgendaManagement.hint'" data-test="schedulation-agenda-hint"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'schedulation-agenda-management-hint',
    components: { KnHint }
})
</script>
