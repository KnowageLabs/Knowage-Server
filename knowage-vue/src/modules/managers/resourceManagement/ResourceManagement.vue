<template>
	<div class="kn-page">
		<div class="kn-page-content p-grid p-m-0">
			<div class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column">
				<Toolbar class="kn-toolbar kn-toolbar--primary">
					<template #left>
						{{ $t('managers.resourceManagement.title') }}
					</template>
				</Toolbar>
				<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
				<ResoruceManagementMetadataDialog v-model:visibility="displayMetadataDialog" v-model:id="metadataKey"></ResoruceManagementMetadataDialog>

				<Tree id="folders-tree" :value="nodes" selectionMode="single" :expandedKeys="expandedKeys" :filter="true" filterMode="lenient" data-test="functionality-tree" class="kn-tree kn-flex foldersTree" @node-select="showForm($event)" v-model:selectionKeys="selectedKeys">
					<template #default="slotProps">
						<div class="p-d-flex p-flex-row p-ai-center p-jc-between" @mouseover="buttonsVisible[slotProps.node.key] = true" @mouseleave="buttonsVisible[slotProps.node.key] = false" :data-test="'tree-item-' + slotProps.node.key">
							<span v-if="!slotProps.node.edit" class="kn-truncated" @dblclick="toggleInput(slotProps.node)">
								{{ slotProps.node.label }}
							</span>
							<InputText class="kn-material-input fileNameInputText" type="text" v-if="slotProps.node.edit" v-model="slotProps.node.label" maxlength="50" @blur="toggleInput(slotProps.node)" @keyup.enter="toggleInput(slotProps.node)" />

							<div v-show="buttonsVisible[slotProps.node.key] && !slotProps.node.edit">
								<Button
									v-if="slotProps.node.modelFolder"
									icon="fas fa-table"
									v-tooltip.top="$t('managers.resourceManagement.openMetadata')"
									class="p-button-text p-button-sm p-button-rounded p-button-plain p-p-0"
									@click="openMetadataDialog(slotProps.node)"
									:data-test="'move-up-button-' + slotProps.node.key"
								/>
								<Button icon="fa fa-download " v-tooltip.top="$t('managers.resourceManagement.download ')" class="p-button-text p-button-sm p-button-rounded p-button-plain p-p-0" @click="downloadDirect(slotProps.node)" :data-test="'move-down-button-' + slotProps.node.key" />
								<Button icon="far fa-trash-alt" v-tooltip.top="$t('common.delete')" class="p-button-text p-button-sm p-button-rounded p-button-plain  p-p-0" @click="showDeleteDialog(slotProps.node)" :data-test="'delete-button-' + slotProps.node.key" />
							</div>
						</div>
					</template>
				</Tree>
			</div>
			<div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-height-full-vertical">
				<ResoruceManagementHint v-if="showHint" data-test="resourceManagement-hint"></ResoruceManagementHint>
				<ResoruceManagementDetail v-if="formVisible" :folder="selectedFolder" :parentKey="folderParentKey" @touched="touched = true" @close="onClose" @inserted="loadPage($event)" @folderCreated="loadPage" @closed="switchToHint()" />
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
	import ResoruceManagementMetadataDialog from '@/modules/managers/resourceManagement/ResoruceManagementMetadataDialog.vue'
	import ResoruceManagementDetail from './ResourceManagementDetail.vue'
	import ResoruceManagementHint from './ResourceManagementHint.vue'

	export default defineComponent({
		name: 'resource-management',
		components: { ResoruceManagementMetadataDialog, ResoruceManagementDetail, ResoruceManagementHint, Tree },
		data() {
			return {
				descriptor,
				displayMetadataDialog: false,
				loading: false,
				nodes: [] as iFolderTemplate[],
				expandedKeys: {},
				selectedKeys: null,
				metadataKey: null,
				dirty: false,
				buttonsVisible: [],
				showHint: true,
				touched: false,
				selectedFolder: {},
				formVisible: false
			}
		},
		async created() {
			this.loadPage()
		},
		methods: {
			toggleInput(node) {
				if (node.edit) {
					if (node.label !== node.edit) {
						console.log('cuccu')
					}
					delete node.edit
				} else node.edit = node.label
			},
			getNodes(respNode) {
				respNode.icon = this.expandedKeys[respNode.key] == true ? 'far fa-folder-open' : 'far fa-folder'
				if (respNode.children && respNode.children.length) {
					for (let child of respNode.children) {
						this.getNodes(child)
					}
				}
			},
			loadPage(): void {
				this.loading = true
				this.showHint = true
				this.formVisible = false
				axios
					.get(process.env.VUE_APP_API_PATH + `2.0/resources/folders`)
					.then((response) => {
						let data = response.data
						this.getNodes(data.root)
						this.nodes = data
						this.loading = false
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
					.delete(process.env.VUE_APP_API_PATH + `2.0/resources/folders`, {
						headers: {
							'Content-Type': 'application/json'
						},
						data: {
							key: node.key
						}
					})
					.then(() => {
						this.loadPage()
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
				obj['key'] = node.key
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

			setDirty(): void {
				this.dirty = true
			},
			showForm(functionality: iFolderTemplate) {
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
				this.showHint = false
			},
			showDeleteDialog(node) {
				this.$confirm.require({
					message: this.$t('managers.resourceManagement.deletingFolderConfirm'),
					header: this.$t('common.toast.deleteConfirmTitle'),
					icon: 'pi pi-exclamation-triangle',
					accept: () => this.deleteFolder(node)
				})
			},
			switchToHint() {
				this.formVisible = false
				this.showHint = true

				this.expandedKeys = {}
				this.selectedKeys = null
				this.metadataKey = null
				this.dirty = false

				this.touched = false
				this.selectedFolder = {}
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
		border-radius: 0;
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
