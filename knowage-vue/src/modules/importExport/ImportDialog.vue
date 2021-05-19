<template>
	<Dialog class="kn-dialog--toolbar--primary importExportDialog" v-bind:visible="visibility" footer="footer" :header="$t('common.import')" :closable="false" modal>
		<!-- 		<FileUpload name="demo[]" :customUpload="true" @uploader="onUpload" auto="true" :maxFileSize="1000000" accept="application/zip">
			<template #empty>
				<p>Drag and drop files to here to upload.</p>
			</template>
		</FileUpload> -->
		<input ref="inputFile" id="inputFile" type="file" @change="onUpload" accept="application/zip" />
		<template #footer>
			<Button class="p-button-text kn-button" v-t="'common.close'" @click="closeDialog" />
			<Button class="kn-button kn-button--primary" v-t="'common.import'" @click="emitImport" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Dialog from 'primevue/dialog'

	export default defineComponent({
		name: 'import-dialog',
		components: { Dialog },
		props: {
			visibility: Boolean
		},
		data() {
			return { uploadedFiles: [] }
		},
		created() {},
		emits: ['update:visibility', 'import'],
		methods: {
			closeDialog(): void {
				this.$emit('update:visibility', false)
			},
			onUpload() {
				// eslint-disable-next-line
				// @ts-ignore
				this.uploadedFiles = this.$refs.inputFile.files[0]
				console.log(this.$refs.inputFile)
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
		}
	}
</style>
