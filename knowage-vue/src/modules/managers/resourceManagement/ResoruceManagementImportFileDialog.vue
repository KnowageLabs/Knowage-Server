<template>
	<Dialog class="kn-dialog--toolbar--primary importFileDialog" v-bind:visible="visibility" footer="footer" :header="$t('common.import')" :closable="false" modal>
		<FileUpload name="demo[]" :chooseLabel="$t('common.choose')" :customUpload="true" @uploader="onUpload" @remove="onDelete" auto="true" :maxFileSize="10000000" :multiple="false" :fileLimit="1">
			<template #empty>
				<p>{{ $t('common.dragAndDropFileHere') }}</p>
			</template>
		</FileUpload>

		<span class="inputFileToggle" v-if="this.uploadedFiles.length > 0">
			<label for="active" class="kn-material-input-label p-ml-3"> {{ $t('managers.resourceManagement.extract') }}</label>
			<InputSwitch v-model="checked" />
		</span>
		<template #footer>
			<Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="closeDialog" />
			<Button class="kn-button kn-button--primary" v-t="'common.import'" :disabled="uploadedFiles && uploadedFiles.length == 0" @click="startImportFile" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import axios from 'axios'
	import { defineComponent } from 'vue'
	import Dialog from 'primevue/dialog'
	import FileUpload from 'primevue/fileupload'
	import InputSwitch from 'primevue/inputswitch'
	import resourceManagementDescriptor from './ResourceManagementDescriptor.json'

	export default defineComponent({
		name: 'import-file-dialog',
		components: { Dialog, FileUpload, InputSwitch },
		props: {
			path: String,
			visibility: Boolean
		},
		data() {
			return { checked: false, descriptor: resourceManagementDescriptor, uploadedFiles: [], loading: false }
		},
		emits: ['update:visibility', 'fileUploaded'],
		methods: {
			closeDialog(): void {
				this.$emit('update:visibility', false)
				this.$emit('fileUploaded', true)
			},
			onDelete(idx) {
				this.uploadedFiles.splice(idx)
			},
			onUpload(data) {
				// eslint-disable-next-line
				// @ts-ignore
				this.uploadedFiles[0] = data.files[0]
			},
			async startImportFile() {
				if (this.uploadedFiles[0] && this.path) {
					this.loading = true

					var formData = new FormData()
					formData.append('file', this.uploadedFiles[0])
					formData.append('key', this.path)
					let checkedAsString = this.checked ? 'true' : 'false'
					formData.append('extract', checkedAsString)
					await axios
						.post(process.env.VUE_APP_API_PATH + '2.0/resources/files/uploadFile', formData, {
							headers: {
								'Content-Type': 'multipart/form-data'
							}
						})
						.then(
							(response) => {
								if (response.data.errors) {
									this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('managers.resourceManagement.upload.completedWithErrors') })
								} else {
									this.$store.commit('setInfo', { title: this.$t('common.error.uploading'), msg: this.$t('managers.resourceManagement.upload.completedWithErrors') })
								}
							},
							(error) => this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t(error) })
						)

						.finally(() => {
							this.loading = false
							this.closeDialog()
							this.uploadedFiles = []
						})
				} else {
					this.$store.commit('setWarning', { title: this.$t('common.uploading'), msg: this.$t('managers.widgetGallery.noFileProvided') })
				}
			}
		}
	})
</script>
<style lang="scss">
	.importFileDialog {
		min-width: 600px;
		width: 60%;
		max-width: 1200px;

		.p-fileupload-buttonbar {
			border: none;

			.p-button:not(.p-fileupload-choose) {
				display: none;
			}

			.p-fileupload-choose {
				@extend .kn-button--primary;
			}
		}

		.functionalityTable {
			min-height: 400px;
			height: 40%;
		}
	}

	.inputFileToggle {
		right: 20px;
		position: absolute;
		top: 20px;
	}
</style>
