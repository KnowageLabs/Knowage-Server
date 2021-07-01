<template>
	<div :class="getVisibility">
		<ProgressSpinner />
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import ProgressSpinner from 'primevue/progressspinner'

	export default defineComponent({
		name: 'kn-spinner-panel',
		components: { ProgressSpinner },
		props: { visibility: Boolean },
		computed: {
			getVisibility(): String {
				return this.visibility ? 'overlappingPanel' : 'kn-hide'
			}
		}
	})
</script>

<style lang="scss"></style>
