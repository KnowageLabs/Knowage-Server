<template>
    <KnHint :title="'managers.buisnessModelCatalogue.drivers'" :hint="'managers.buisnessModelCatalogue.noDriverSelected'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'
export default defineComponent({
    name: 'news-hint',
    components: { KnHint }
})
</script>
