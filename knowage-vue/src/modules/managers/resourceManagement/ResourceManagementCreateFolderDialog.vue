<template>
	<Dialog class="kn-dialog--toolbar--primary createFolderDialog" v-bind:visible="visibility" footer="footer" :header="$t('managers.resourceManagement.createFolder')" :closable="false" modal>
		<Breadcrumb :home="home" :model="items"> </Breadcrumb>
		<div class="createFolderDialogContent">
			<span class="p-float-label">
				<InputText class="folderNameInputText" type="text" v-model="folderName" />
				<label class="kn-material-input-label" for="label">{{ $t('managers.resourceManagement.foldernamePlaceholder') }}</label>
			</span>
		</div>
		<template #footer>
			<Button class="kn-button kn-button--secondary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
			<Button class="kn-button kn-button--primary" :disabled="folderName && folderName.length == 0" @click="emitCreateFolder"> {{ $t('common.save') }}</Button>
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Breadcrumb from 'primevue/breadcrumb'
	import Dialog from 'primevue/dialog'
	import resourceManagementDescriptor from './ResourceManagementDescriptor.json'

	export default defineComponent({
		name: 'import-file-dialog',
		components: { Breadcrumb, Dialog },
		props: {
			visibility: Boolean,
			path: String
		},
		emits: ['update:visibility', 'createFolder'],
		data() {
			return {
				descriptor: resourceManagementDescriptor,
				home: { icon: 'pi pi-home' },
				items: [] as Array<{ label: String }>,
				folderName: '',
				loading: false
			}
		},
		mounted() {
			this.setBreadcrumbs()
		},
		methods: {
			closeDialog(): void {
				this.folderName = ''
				this.$emit('update:visibility', false)
			},
			emitCreateFolder(): void {
				this.$emit('createFolder', this.folderName)
			},
			setBreadcrumbs() {
				this.items = []

				if (this.path) {
					let pathFolders = this.path.split('\\', -1)
					pathFolders.forEach((element) => {
						let obj = { label: element }
						this.items.push(obj)
					})
				}
			}
		},
		watch: {
			path(oldPath, newPath) {
				if (oldPath != newPath) this.setBreadcrumbs()
			}
		}
	})
</script>

<style lang="scss">
	.createFolderDialog {
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
	.folderNameInputText {
		width: 100%;
	}
	.createFolderDialogContent {
		padding: 16px;
	}
	.p-breadcrumb {
		border: none;
		border-radius: 0;
		border-bottom: 1px solid $list-border-color;
	}
</style>
