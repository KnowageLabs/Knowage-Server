<template>
    <KnHint :title="$t('managers.layersManagement.title')" :hint="$t('managers.layersManagement.hint')"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'layers-management-hint',
    components: { KnHint }
})
</script>
