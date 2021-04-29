<template>
	<div class="kn-importExport kn-page">
		<Toolbar class="kn-toolbar kn-toolbar--primary">
			<template #left>
				{{ $t('importExport.title') }}
			</template>
			<template #right>
				<Button class="kn-button p-button-text">{{ $t('common.import') }}</Button>
				<Button class="kn-button p-button-text">{{ $t('common.export') }}</Button>
			</template>
		</Toolbar>
		<div class="kn-page-content p-grid p-m-0">
			<div class="functionalities-container p-col-3 p-sm-3 p-md-2">
				<KnTabCard :element="type" v-for="(type, index) in importExportDescriptor.functionalities" v-bind:key="index" @click="selectType(type)"></KnTabCard>
			</div>
			<div class="p-col-9 p-sm-9 p-md-10 p-p-0 p-m-0">
				<router-view />
			</div>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import importExportDescriptor from './ImportExportDescriptor.json'
	import KnTabCard from '@/components/UI/KnTabCard.vue'

	export default defineComponent({
		name: 'import-export',
		components: { KnTabCard },
		data() {
			return {
				importExportDescriptor: importExportDescriptor
			}
		},
		created() {},
		methods: {
			selectType(type) {
				this.$router.push(type.route)
			}
		}
	})
</script>

<style lang="scss" scoped>
	.functionalities-container {
		flex: 1;
	}
</style>
