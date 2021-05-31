<template>
	<Dialog class="kn-dialog--toolbar--primary importExportDialog" v-bind:visible="visibility" footer="footer" :header="$t('common.import')" :closable="false" modal>
		<FileUpload name="demo[]" :chooseLabel="$t('common.choose')" :customUpload="true" @uploader="onUpload" auto="true" :maxFileSize="10000000" accept="application/zip">
			<template #empty>
				<p>{{ $t('common.dragAndDropFilesHere') }}</p>
			</template>
		</FileUpload>
		<template #footer>
			<Button class="p-button-text kn-button" v-t="'common.close'" @click="closeDialog" />
			<Button class="kn-button kn-button--secondary" v-t="'common.import'" :disabled="uploadedFiles && uploadedFiles.length == 0" @click="emitImport" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Dialog from 'primevue/dialog'
	import FileUpload from 'primevue/fileupload'

	export default defineComponent({
		name: 'import-dialog',
		components: { Dialog, FileUpload },
		props: {
			visibility: Boolean
		},
		data() {
			return { uploadedFiles: [] }
		},
		emits: ['update:visibility', 'import'],
		methods: {
			closeDialog(): void {
				this.$emit('update:visibility', false)
			},
			onUpload(data) {
				// eslint-disable-next-line
				// @ts-ignore
				this.uploadedFiles = data.files[0]
			},
			emitImport(): void {
				this.$emit('import', { files: this.uploadedFiles })
			}
		}
	})
</script>
<style lang="scss">
	.importExportDialog {
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
	}
</style>
