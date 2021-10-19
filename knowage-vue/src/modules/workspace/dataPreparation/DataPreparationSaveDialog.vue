<template>
	<Dialog class="kn-dialog--toolbar--primary dataPreparationSaveDialog" v-bind:visible="visibility" footer="footer" :header="$t('managers.workspaceManagement.dataPreparation.savePreparedDataset')" :closable="false" modal>
		<div class="p-grid p-m-0 p-d-flex elementClass">
			<span class="p-col-4 p-float-label">
				<InputText
					class="kn-material-input"
					type="text"
					v-model="localDataset.name"
					v-model.trim="v$.localDataset.name.$model"
					:class="{
						'p-invalid': v$.localDataset.name.$invalid && v$.localDataset.name.$dirty
					}"
					maxLength="100"
				/>
				<label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.name') }}</label></span
			>
			<KnValidationMessages
				:vComp="v$.localDataset.name"
				:additionalTranslateParams="{
					fieldName: $t('managers.configurationManagement.headers.name')
				}"
			></KnValidationMessages>
			<span class="p-col-4 p-float-label">
				<InputText
					class="kn-material-input"
					type="text"
					v-model="localDataset.label"
					v-model.trim="v$.localDataset.label.$model"
					:class="{
						'p-invalid': v$.localDataset.label.$invalid && v$.localDataset.label.$dirty
					}"
					maxLength="100"
				/>
				<label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.label') }}</label></span
			>
			<KnValidationMessages
				:vComp="v$.localDataset.label"
				:additionalTranslateParams="{
					fieldName: $t('managers.configurationManagement.headers.label')
				}"
			></KnValidationMessages>
			<span class="p-col-4 p-float-label">
				<InputText
					class="kn-material-input"
					type="text"
					v-model="localDataset.visibility"
					v-model.trim="v$.localDataset.visibility.$model"
					:class="{
						'p-invalid': v$.localDataset.visibility.$invalid && v$.localDataset.visibility.$dirty
					}"
					maxLength="100"
				/>
				<label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.visibility') }}</label></span
			>
			<KnValidationMessages
				:vComp="v$.localDataset.visibility"
				:additionalTranslateParams="{
					fieldName: $t('managers.configurationManagement.headers.visibility')
				}"
			></KnValidationMessages>
			<span class="p-col-4 p-float-label">
				<InputText
					class="kn-material-input"
					type="text"
					v-model="localDataset.description"
					v-model.trim="v$.localDataset.description.$model"
					:class="{
						'p-invalid': v$.localDataset.description.$invalid && v$.localDataset.description.$dirty
					}"
					maxLength="100"
				/>
				<label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.description') }}</label></span
			>
			<KnValidationMessages
				:vComp="v$.localDataset.description"
				:additionalTranslateParams="{
					fieldName: $t('managers.configurationManagement.headers.description')
				}"
			></KnValidationMessages>
			<span class="p-col p-float-label">
				<Dropdown
					id="type"
					class="kn-material-input"
					v-model="localDataset.refreshRate"
					dataKey="id"
					optionLabel="name"
					optionValue="code"
					:options="descriptor.dataPreparation.refreshRate.options"
					:placeholder="$t('managers.workspaceManagement.dataPreparation.dataset.refreshRate.label')"
					v-model.trim="v$.localDataset.refreshRate.$model"
					:class="{
						'p-invalid': v$.localDataset.refreshRate.$invalid && v$.localDataset.refreshRate.$dirty
					}"
					maxLength="100"
			/></span>
			<KnValidationMessages
				:vComp="v$.localDataset.refreshRate"
				:additionalTranslateParams="{
					fieldName: $t('managers.configurationManagement.headers.refreshRate')
				}"
			></KnValidationMessages>
		</div>

		<template #footer>
			<Button class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="resetAndClose" />

			<Button class="kn-button kn-button--primary" v-t="'common.save'" :disabled="buttonDisabled" @click="handleTransformation()" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent, PropType } from 'vue'
	import { createValidations } from '@/helpers/commons/validationHelper'

	import axios from 'axios'
	import Dialog from 'primevue/dialog'
	import Dropdown from 'primevue/dropdown'
	import DataPreparationDescriptor from './DataPreparationDescriptor.json'
	import DataPreparationValidationDescriptor from './DataPreparationValidationDescriptor.json'
	import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
	import useValidate from '@vuelidate/core'

	import { IDataPreparationDataset } from '@/modules/workspace/dataPreparation/DataPreparation'

	export default defineComponent({
		name: 'data-preparation-detail-save-dialog',
		props: {
			dataset: {} as PropType<IDataPreparationDataset>,
			visibility: Boolean
		},
		components: { Dialog, Dropdown, KnValidationMessages },
		data() {
			return { descriptor: DataPreparationDescriptor, localDataset: {} as any, v$: useValidate() as any, validationDescriptor: DataPreparationValidationDescriptor }
		},
		validations() {
			return {
				localDataset: createValidations('localDataset', this.validationDescriptor.validations.configuration)
			}
		},
		emits: ['update:visibility'],

		methods: {
			handleTransformation() {
				let data = this.createDataToSend()
				axios
					.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'selfservicedataset/save?SBI_EXECUTION_ID=-1&isTech=false&showDerivedDataset=false&showOnlyOwner=true', data, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/x-www-form-urlencoded' } })
					.then((response) => {
						console.log(response)
					})
			},
			createDataToSend() {
				let ds = this.localDataset
				if (ds.config) ds.config = JSON.stringify(ds.config)
				ds.dsDerivedId = this.localDataset.id
				ds.id = null
				ds.type = 'PreparedDataset'

				var data = new URLSearchParams()
				const keys = Object.keys(ds)
				keys.forEach((key) => {
					let value = ds[key]
					if (!value) value = ''
					if (value instanceof Object) value = JSON.stringify(value)
					data.append(key, value)
				})
				return data
			},
			resetAndClose(): void {
				this.closeDialog()
			},
			closeDialog(): void {
				this.$emit('update:visibility', false)
			},
			loadTranslations() {
				this.descriptor.dataPreparation.refreshRate.options.forEach((element) => {
					element.name = this.$t(element.name)
				})
			}
		},

		created() {
			this.loadTranslations()
		},
		updated() {
			if (this.dataset) {
				this.localDataset = this.dataset
			}
		}
	})
</script>

<style lang="scss" scoped>
	.dataPreparationSaveDialog {
		min-width: 600px !important;
		width: 50vw;
		max-width: 1200px !important;
		&:deep(.p-dialog-content) {
			height: 30vw;
		}
	}
</style>
