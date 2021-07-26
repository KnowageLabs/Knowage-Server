<template>
	<Dialog class="kn-dialog--toolbar--primary exportDialog" v-bind:visible="visibility" :header="$t('common.export')" :closable="false" modal>
		<div class="exportDialogContent">
			<span class="p-float-label">
				<InputText class="kn-material-input fileNameInputText" type="text" v-model="fileName" maxlength="50" />
				<label class="kn-material-input-label" for="label">{{ $t('importExport.filenamePlaceholder') }}</label>
			</span>
		</div>
		<template #footer>
			<Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="closeDialog" />
			<Button class="kn-button kn-button--primary" :label="$t('common.export')" autofocus :disabled="fileName && fileName.length == 0" @click="emitExport" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Dialog from 'primevue/dialog'

	export default defineComponent({
		name: 'export-dialog',
		components: { Dialog },
		props: {
			visibility: Boolean
		},
		data() {
			return { fileName: '' }
		},
		created() {},
		emits: ['update:visibility', 'export'],
		methods: {
			closeDialog(): void {
				this.$emit('update:visibility', false)
			},
			emitExport(): void {
				this.$emit('export', this.fileName + '.json')
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
	.fileNameInputText {
		width: 100%;
	}
	.exportDialogContent {
		padding: 16px;
	}
</style>
