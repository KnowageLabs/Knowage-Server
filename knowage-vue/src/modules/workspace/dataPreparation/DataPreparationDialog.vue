<template>
	<Dialog class="kn-dialog--toolbar--primary dataPreparationDialog" v-bind:visible="visibility" footer="footer" :header="$t('managers.workspaceManagement.dataPreparation.saveDataset')" :closable="false" modal>
		<div class="p-grid p-m-0 p-d-flex kn-flex ">
			<span v-for="(field, index) in visibility.config.parameters" v-bind:key="index" class="p-float-label">
				<div :class="'p-col-' + 12 / visibility.config.parameters.length">
					<span v-if="field.type == 'string'" class="p-float-label">
						<InputText class="kn-material-input" type="text" v-model="field.value" /> <label class="kn-material-input-label" for="label">{{ $t(field.name) }}</label></span
					>
					<span v-if="field.type === 'calendar'"><Calendar v-model="field.value"/></span>
					<span v-if="field.type === 'boolean'">
						<InputSwitch v-model="field.value" />
						<label :for="field.value">{{ field.name }}</label>
					</span>
					<span v-if="field.type === 'dropdown'">
						<Dropdown v-model="field.value" :options="field.options" />
					</span>
				</div>
			</span>
		</div>

		<template #footer>
			<Button class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="resetAndClose" />

			<Button class="kn-button kn-button--primary" v-t="'common.save'" @click="handleTransformation" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'

	import Calendar from 'primevue/calendar'
	import Dialog from 'primevue/dialog'
	import Dropdown from 'primevue/dropdown'
	import InputSwitch from 'primevue/inputswitch'
	import ITransformation from '@/modules/workspace/dataPreparation/DataPreparation'

	export default defineComponent({
		name: 'data-preparation-detail-dialog',
		props: {
			visibility: ITransformation
		},
		components: { Calendar, Dialog, Dropdown, InputSwitch },
		data() {
			return {}
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
	.dataPreparationDialog {
		min-width: 600px;
		width: 60%;
		max-width: 1200px;
	}

	.p-dialog-content {
		height: 300px;
	}
</style>
