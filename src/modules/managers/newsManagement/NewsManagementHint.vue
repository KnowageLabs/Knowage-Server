<template>
    <KnHint :title="'managers.newsManagement.title'" :hint="'managers.newsManagement.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'news-hint',
    components: { KnHint }
})
</script>
