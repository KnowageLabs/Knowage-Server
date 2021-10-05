<template>
	<Dialog class="kn-dialog--toolbar--primary dataPreparationSaveDialog" v-bind:visible="visibility" footer="footer" :header="$t('managers.workspaceManagement.dataPreparation.parametersConfiguration')" :closable="false" modal>
		<div class="p-grid p-m-0 p-d-flex kn-flex ">
			<span class="p-col-4 p-float-label">
				<InputText class="kn-material-input" type="text" :v-model="dataset.name" /> <label class="kn-material-input-label" for="label">{{ $t(dataset.name) }}</label></span
			>
			<span class="p-col-4 p-float-label">
				<InputText class="kn-material-input" type="text" :v-model="dataset.label" /> <label class="kn-material-input-label" for="label">{{ $t(dataset.label) }}</label></span
			>
			<span class="p-col-4 p-float-label">
				<InputText class="kn-material-input" type="text" :v-model="dataset.visibility" /> <label class="kn-material-input-label" for="label">{{ $t(dataset.visibility) }}</label></span
			>
			<span class="p-col-4 p-float-label">
				<InputText class="kn-material-input" type="text" :v-model="dataset.description" /> <label class="kn-material-input-label" for="label">{{ $t(dataset.description) }}</label></span
			>
			<span class="p-col p-float-label"> <Dropdown id="type" class="kn-material-input" :v-model="dataset.refreshRateId" dataKey="id" optionLabel="name" optionValue="id" :options="descriptor.dataPreparation.refreshRate.options" @change="setType"/></span>
		</div>

		<template #footer>
			<Button class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="resetAndClose" />

			<Button class="kn-button kn-button--primary" v-t="'common.save'" @click="handleTransformation" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'

	import Dialog from 'primevue/dialog'
	import Dropdown from 'primevue/dropdown'
	import DataPreparationDescriptor from './DataPreparationDescriptor.json'

	import ITransformation from '@/modules/workspace/dataPreparation/DataPreparation'

	export default defineComponent({
		name: 'data-preparation-detail-save-dialog',
		props: {
			dataset: {},

			visibility: ITransformation
		},
		components: { Dialog, Dropdown },
		data() {
			return { descriptor: DataPreparationDescriptor }
		},
		emits: ['update:visibility', 'sendTransformation'],
		created() {},
		methods: {
			handleTransformation() {
				this.$emit('sendTransformation', this.visibility)
			},
			resetAndClose(): void {
				this.closeDialog()
			},
			closeDialog(): void {
				this.$emit('update:visibility', false)
			}
		}
	})
</script>

<style lang="scss" scoped>
	.dataPreparationSaveDialog {
		min-width: 600px;
		width: 60%;
		max-width: 1200px;
	}

	.p-dialog-content {
		height: 300px;
	}
</style>
