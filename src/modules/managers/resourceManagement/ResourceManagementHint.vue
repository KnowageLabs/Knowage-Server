<template>
	<KnHint :title="'managers.resourceManagement.title'" :hint="'managers.resourceManagement.hint'"></KnHint>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import KnHint from '@/components/UI/KnHint.vue'

	export default defineComponent({
		name: 'news-hint',
		components: { KnHint }
	})
</script>
