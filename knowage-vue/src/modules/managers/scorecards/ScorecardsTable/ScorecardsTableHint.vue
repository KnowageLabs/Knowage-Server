<template>
    <KnHint class="p-my-2" :title="'managers.scorecards.scorecardDesigner'" :hint="hint"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'scorecards-table-hint',
    components: { KnHint },
    props: { hint: { type: String } }
})
</script>
