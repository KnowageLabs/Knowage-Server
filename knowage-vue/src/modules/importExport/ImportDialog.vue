<template>
	<Dialog class="kn-dialog--toolbar--primary importExportDialog" v-bind:visible="visibility" footer="footer" :header="$t('common.import')" :closable="false" modal>
		<div v-if="step == 0">
			<FileUpload name="demo[]" :chooseLabel="$t('common.choose')" :customUpload="true" @uploader="onUpload" @remove="onDelete" auto="true" :maxFileSize="10000000" accept="application/zip, application/x-zip-compressed" :multiple="false" :fileLimit="1">
				<template #empty>
					<p>{{ $t('common.dragAndDropFileHere') }}</p>
				</template>
			</FileUpload>
		</div>
		<div v-if="step == 1" class="importExportImport">
			<TabView @change="resetSearchFilter">
				<TabPanel v-for="functionality in importExportDescriptor.functionalities" :key="functionality.label">
					<template #header>
						{{ $t(functionality.label).toUpperCase() }}

						<Badge class="p-ml-1" v-if="selectedItems[functionality.type].length && selectedItems[functionality.type].length > 0" :value="selectedItems[functionality.type].length"></Badge>
					</template>
					<DataTable
						ref="dt"
						:value="packageItems[functionality.type]"
						v-model:selection="selectedItems[functionality.type]"
						v-model:filters="filters"
						class="p-datatable-sm kn-table functionalityTable"
						dataKey="id"
						:paginator="true"
						:rows="10"
						paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
						responsiveLayout="stack"
						breakpoint="960px"
						:currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
						:globalFilterFields="['name', 'type', 'tags']"
						:loading="loading"
					>
						<template #header>
							<div class="table-header">
								<span class="p-input-icon-left">
									<i class="pi pi-search" />
									<InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" />
								</span>
							</div>
						</template>
						<template #empty>
							{{ $t('common.info.noDataFound') }}
						</template>
						<template #loading>
							{{ $t('common.info.dataLoading') }}
						</template>

						<Column v-for="col in getData(functionality.type)" :field="col.field" :header="$t(col.header)" :key="col.field" :style="col.style" :selectionMode="col.field == 'selectionMode' ? 'multiple' : ''" :exportable="col.field == 'selectionMode' ? false : ''">
							<template #body="{data}" v-if="col.displayType">
								<span class="p-float-label kn-material-input">
									<div v-if="col.displayType == 'tags'">
										<Tag class="importExportTags p-mr-1" v-for="(tag, index) in data.tags" v-bind:key="index" rounded :value="tag"> </Tag>
									</div>
									<div v-if="col.displayType == 'widgetGalleryType'">
										<Tag :style="importExportDescriptor.iconTypesMap[data.type].style"> {{ data.type.toUpperCase() }} </Tag>
									</div>
								</span>
							</template>
						</Column>
					</DataTable>
				</TabPanel>
			</TabView>
		</div>

		<template #footer>
			<Button v-bind:visible="visibility" class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="resetAndClose" />

			<Button v-if="step == 0" v-bind:visible="visibility" class="kn-button kn-button--primary" v-t="'common.next'" :disabled="uploadedFiles && uploadedFiles.length == 0" @click="goToChooseElement(uploadedFiles)" />
			<span v-if="step == 1">
				<Button v-bind:visible="visibility" class="kn-button kn-button--secondary" v-t="'common.back'" @click="resetToFirstStep"/>
				<Button v-bind:visible="visibility" class="kn-button kn-button--primary" v-t="'common.import'" :disabled="isImportDisabled()" @click="startImport"
			/></span>
		</template>
	</Dialog>
</template>

<script lang="ts">
	import axios from 'axios'
	import { defineComponent } from 'vue'
	import { FilterMatchMode, FilterOperator } from 'primevue/api'
	import { ITableColumn } from '../commons/ITableColumn'
	import Badge from 'primevue/badge'
	import Column from 'primevue/column'
	import DataTable from 'primevue/datatable'
	import Dialog from 'primevue/dialog'
	import FileUpload from 'primevue/fileupload'
	import importExportDescriptor from './ImportExportDescriptor.json'
	import TabPanel from 'primevue/tabpanel'
	import TabView from 'primevue/tabview'
	import Tag from 'primevue/tag'

	export default defineComponent({
		name: 'import-dialog',
		components: { Badge, Column, DataTable, Dialog, FileUpload, TabPanel, TabView, Tag },
		props: {
			visibility: Boolean
		},
		data() {
			return {
				importExportDescriptor: importExportDescriptor,
				uploadedFiles: [],
				fileName: '',
				filters: {},
				loading: false,
				packageItems: {
					gallery: [],
					catalogFunction: []
				},
				selectedItems: {
					gallery: [],
					catalogFunction: []
				},
				step: 0,
				token: ''
			}
		},
		emits: ['update:visibility', 'import'],
		created() {
			this.filters = {
				global: { value: null, matchMode: FilterMatchMode.CONTAINS },
				name: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
				type: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
				tags: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }] }
			}
		},
		methods: {
			async cleanTempDirectory() {
				if (this.token != '') {
					this.uploadedFiles = []
					await axios.get(process.env.VUE_APP_API_PATH + '1.0/import/cleanup', { params: { token: this.token } }).then(
						(response) => {
							if (!response.data.errors) {
								this.token = ''
								this.packageItems = {
									gallery: [],
									catalogFunction: []
								}
							}
						},
						(error) => console.log(error)
					)
				}
			},
			closeDialog(): void {
				this.$emit('update:visibility', false)
			},
			emitImport(): void {
				this.$emit('import', { files: this.uploadedFiles })
			},
			getData(type): Array<ITableColumn> {
				let columns = this.importExportDescriptor['import'][type]['column']
				columns.sort(function(a, b) {
					if (a.position > b.position) return 1
					if (a.position < b.position) return -1
					return 0
				})
				return columns
			},
			getPackageItems(e) {
				this.packageItems[e.functionality] = e.items
			},
			getSelectedItems(e) {
				this.selectedItems[e.functionality] = e.items
			},
			async goToChooseElement(uploadedFiles) {
				if (this.uploadedFiles.length == 1) {
					this.loading = true
					this.step = 1

					var formData = new FormData()
					formData.append('file', uploadedFiles[0])
					await axios
						.post(process.env.VUE_APP_API_PATH + '1.0/import/upload', formData, {
							headers: {
								'Content-Type': 'multipart/form-data'
							}
						})
						.then(
							(response) => {
								if (response.data.errors) {
									this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('importExport.import.completedWithErrors') })
								} else {
									this.packageItems = response.data.entries
									this.token = response.data.token
									this.step = 1
								}
							},
							(error) => this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t(error) })
						)
					this.loading = false
				} else {
					this.$store.commit('setWarning', { title: this.$t('common.uploading'), msg: this.$t('managers.widgetGallery.noFileProvided') })
				}
			},
			isImportDisabled(): Boolean {
				for (var idx in this.selectedItems) {
					if (this.selectedItems[idx].length > 0) return false
				}
				return true
			},
			onDelete(idx) {
				this.uploadedFiles.splice(idx)
			},
			onUpload(data) {
				// eslint-disable-next-line
				// @ts-ignore
				this.uploadedFiles[0] = data.files[0]
			},
			resetAndClose(): void {
				this.resetToFirstStep()
				this.closeDialog()
			},
			resetSearchFilter(): void {
				this.filters['global'].value = ''
			},
			async resetToFirstStep() {
				this.step = 0
				this.selectedItems = {
					gallery: [],
					catalogFunction: []
				}
				this.packageItems = {
					gallery: [],
					catalogFunction: []
				}
				this.cleanTempDirectory()
			},

			startImport() {
				this.$store.commit('setLoading', true)
				axios
					.post(process.env.VUE_APP_API_PATH + '1.0/import/bulk', this.streamlineSelectedItemsArray(), {
						headers: {
							// Overwrite Axios's automatically set Content-Type
							'Content-Type': 'application/json'
						}
					})
					.then(
						(response) => {
							if (response.data.errors) {
								this.$store.commit('setError', { title: this.$t('common.error.import'), msg: this.$t('importExport.import.completedWithErrors') })
							} else {
								this.$store.commit('setInfo', { title: this.$t('common.import'), msg: this.$t('importExport.import.successfullyCompleted') })
							}

							this.$store.commit('setLoading', false)
						},
						(error) => this.$store.commit('setError', { title: this.$t('common.error.import'), msg: this.$t(error) })
					)
				this.token = ''
				this.resetAndClose()
			},

			streamlineSelectedItemsArray(): JSON {
				let selectedItemsToBE = {} as JSON
				selectedItemsToBE['selectedItems'] = {}
				for (var category in this.selectedItems) {
					for (var k in this.selectedItems[category]) {
						if (!selectedItemsToBE['selectedItems'][category]) {
							selectedItemsToBE['selectedItems'][category] = []
						}

						selectedItemsToBE['selectedItems'][category].push(this.selectedItems[category][k].id)
					}
				}

				selectedItemsToBE['token'] = this.token

				return selectedItemsToBE
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

		.functionalityTable {
			min-height: 400px;
			height: 40%;
		}
	}
	.importExportTags {
		background-color: $color-default;
	}
	.thirdButton {
		float: left;
	}
</style>
