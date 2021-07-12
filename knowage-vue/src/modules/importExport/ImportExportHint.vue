<template>
	<KnHint :title="'managers.catalogFunction.title'" :hint="'managers.catalogFunction.hint'"></KnHint>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import KnHint from '@/components/UI/KnHint.vue'

	export default defineComponent({
		name: 'import-export-hint',
		components: { KnHint }
	})
</script>
