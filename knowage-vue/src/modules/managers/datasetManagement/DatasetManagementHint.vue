<template>
    <KnHint :title="$t('managers.datasetManagement.title')" :hint="$t('managers.datasetManagement.hint')"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'kpi-definition-hint',
    components: { KnHint }
})
</script>
