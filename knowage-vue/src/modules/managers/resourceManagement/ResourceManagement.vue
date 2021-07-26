<template>
	<div class="kn-page">
		<div class="kn-page-content p-grid p-m-0">
			<div class="p-col-4 p-sm-4 p-md-3 p-p-0">
				<Toolbar class="kn-toolbar kn-toolbar--primary">
					<template #left>
						{{ $t('managers.resourceManagement.title') }}
					</template>
				</Toolbar>
				<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
				<MetadataDialog v-model:visibility="displayMetadataDialog" v-model:id="metadataKey"></MetadataDialog>

				<Tree id="document-tree" :value="nodes" selectionMode="single" :expandedKeys="expandedKeys" :filter="true" filterMode="lenient" data-test="functionality-tree" class="kn-tree kn-flex foldersTree">
					<template #default="slotProps">
						<router-link class="kn-decoration-none" :to="{ name: 'resource-management-detail', params: { id: slotProps.node.key, relativePath: slotProps.node.relativePath } }" exact>
							<div class="p-grid" @mouseover="buttonsVisible[slotProps.node.key] = true" @mouseleave="buttonsVisible[slotProps.node.key] = false" :data-test="'tree-item-' + slotProps.node.key">
								<div class="p-col-1 p-p-0 p-d-flex p-flex-row p-ai-left"><Button v-if="expandedKeys[slotProps.node.key] == true" class="p-button-link p-button-sm p-p-0  far fa-folder-open" /> <Button v-else class="p-button-link p-button-sm p-p-0  far fa-folder" /></div>
								<div class="p-col-8 p-p-0 p-d-flex p-flex-row p-ai-left">
									<span>{{ slotProps.node.label }}</span>
								</div>
								<div v-show="buttonsVisible[slotProps.node.key]" class="p-col-3 p-p-0 p-d-flex p-flex-row p-ai-center">
									<Button v-if="slotProps.node.modelFolder" icon="fas fa-table" v-tooltip.top="$t('managers.resourceManagement.openMetadata')" class="p-button-link p-button-sm p-p-0" @click="openMetadataDialog(slotProps.node)" :data-test="'move-up-button-' + slotProps.node.key" />
									<Button icon="fa fa-download " v-tooltip.top="$t('managers.resourceManagement.download ')" class="p-button-link p-button-sm p-p-0" @click="downloadDirect(slotProps.node)" :data-test="'move-down-button-' + slotProps.node.key" />
									<Button icon="far fa-trash-alt" v-tooltip.top="$t('common.delete')" class="p-button-link p-button-sm p-p-0" @click="deleteFolder(slotProps.node)" :data-test="'delete-button-' + slotProps.node.key" />
								</div></div
						></router-link>
					</template>
				</Tree>
			</div>
			<div class="p-col p-pt-0">
				<router-view v-model:loading="loading" />
			</div>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import axios from 'axios'
	import descriptor from './ResourceManagementDescriptor.json'
	import Tree from 'primevue/tree'
	import { iFolderTemplate } from '@/modules/managers/resourceManagement/ResourceManagement'
	import { downloadDirectFromResponse } from '@/helpers/commons/fileHelper'
	import MetadataDialog from '@/modules/managers/resourceManagement/MetadataDialog.vue'

	export default defineComponent({
		name: 'resource-management',
		components: { MetadataDialog, Tree },
		data() {
			return {
				descriptor,
				displayMetadataDialog: false,
				loading: false,
				hintVisible: true,
				nodes: Array<iFolderTemplate>(),
				expandedKeys: {},
				selectedKeys: null,
				metadataKey: null,
				dirty: false,
				buttonsVisible: [],
				showHint: false,
				touched: false,
				selectedFolder: {},
				formVisible: false
			}
		},
		async created() {
			this.loadPage()
		},
		methods: {
			loadPage(): void {
				this.loading = true
				axios
					.get(process.env.VUE_APP_API_PATH + `2.0/resources/folders`)
					.then((response) => {
						this.nodes = response.data
						if (this.nodes.length > 0) this.expandedKeys['0'] = true
					})
					.finally(() => (this.loading = false))
			},
			openMetadataDialog(node): void {
				this.metadataKey = node.key
				this.displayMetadataDialog = !this.displayMetadataDialog
			},
			deleteFolder(node) {
				this.loading = true
				axios
					.delete(process.env.VUE_APP_API_PATH + `2.0/resources/folders?key=` + node.key)
					.then(() => {
						this.$store.commit('setInfo', {
							title: this.$t('common.toast.deleteTitle'),
							msg: this.$t('common.toast.deleteSuccess')
						})
					})
					.catch(() => {
						this.$store.commit('setError', {
							title: this.$t('common.toast.deleteTitle'),
							msg: this.$t('common.toast.deleteFailed')
						})
					})
					.finally(() => (this.loading = false))
			},
			downloadDirect(node) {
				this.loading = true
				let obj = {} as JSON
				obj['key'] = '' + node.key
				axios
					.post(process.env.VUE_APP_API_PATH + `2.0/resources/folders/download`, obj, {
						responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

						headers: {
							'Content-Type': 'application/json',
							Accept: 'application/zip; charset=utf-8'
						}
					})
					.then((response) => {
						downloadDirectFromResponse(response)
					})
					.finally(() => (this.loading = false))
			},
			/*Tree utilities methods*/
			expandAll() {
				for (let node of this.nodes) {
					this.expandNode(node)
				}

				this.expandedKeys = { ...this.expandedKeys }
			},
			collapseAll() {
				this.expandedKeys = {}
			},
			expandNode(node) {
				this.expandedKeys[node.key] = true
				if (node.children && node.children.length) {
					for (let child of node.children) {
						this.expandNode(child)
					}
				}
			},
			collapseNode(node) {
				this.expandedKeys[node.key] = false
				if (node.children && node.children.length) {
					for (let child of node.children) {
						this.collapseNode(child)
					}
				}
			},
			setDirty(): void {
				this.dirty = true
			},
			showForm(functionality: iFolderTemplate, relativePath: String) {
				this.showHint = false
				console.log(relativePath)
				/*             this.functionalityParentId = parentId */
				if (!this.touched) {
					this.setSelected(functionality)
				} else {
					this.$confirm.require({
						message: this.$t('common.toast.unsavedChangesMessage'),
						header: this.$t('common.toast.unsavedChangesHeader'),
						icon: 'pi pi-exclamation-triangle',
						accept: () => {
							this.touched = false
							this.setSelected(functionality)
						}
					})
				}
			},
			setSelected(functionality: iFolderTemplate) {
				this.selectedFolder = functionality
				this.formVisible = true
			}
		}
	})
</script>

<style lang="scss">
	.knTreeLabel {
		border: none;
	}
	.foldersTree {
		height: 100%;
	}
	.rightFolderIconsBar {
		align-items: center;
		display: flex;
		flex-direction: row;
		flex-wrap: nowrap;
		justify-content: flex-end;
		flex: 1;
	}
	.treeCustomElement {
		align-items: center;
		display: flex;
		flex-direction: row;
		flex-wrap: nowrap;
	}
	.rightFolderIcon {
		margin-right: 0.5rem;
	}
	.p-treenode-label {
		width: 100%;
	}
	.p-tree-toggler p-link {
		margin: 0px;
	}
	.p-treenode-icon {
		margin: 0px;
	}
	.p-tree-wrapper {
		height: 100%;
	}
	.p-tree-container {
		height: 100%;
	}
</style>
