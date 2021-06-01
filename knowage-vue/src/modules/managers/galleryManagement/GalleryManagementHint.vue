<template>
    <KnHint :title="'managers.widgetGallery.title'" :hint="'managers.widgetGallery.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'gallery-hint',
    components: { KnHint }
})
</script>
