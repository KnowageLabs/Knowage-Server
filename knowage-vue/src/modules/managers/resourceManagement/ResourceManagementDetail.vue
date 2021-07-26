<template>
	<Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
		<template #left>{{ $t('managers.resourceManagement.detailTitle') }}</template>
	</Toolbar>
	<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
	<Breadcrumb :home="home" :model="items"> </Breadcrumb>
	<div v-if="selectedFiles.length > 0">
		<Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
			<template #left>{{ $t('managers.resourceManagement.selectedFiles', { num: selectedFiles.length }) }}</template>
			<template #right>
				<Button icon="fas fa-download" class="p-button-text p-button-rounded p-button-plain" @click="downloadFiles" :disabled="invalid" />
				<Button icon="fas fa-trash" class="p-button-text p-button-rounded p-button-plain" @click="deleteFiles" />
			</template>
		</Toolbar>
	</div>

	<Dialog class="kn-dialog--toolbar--primary" v-bind:visible="folderCreation" footer="footer" :header="$t('language.languageSelection')" :closable="false" modal>
		<div class="folderCreationDialogContent">
			<span class="p-float-label">
				<InputText class="" type="text" v-model="folderName" @change="setDirty" />
				<label class="kn-material-input-label" for="label">{{ $t('importExport.filenamePlaceholder') }}</label>
			</span>
		</div>
		<template #footer>
			<Button class="kn-button kn-button--primary" :disabled="folderName && folderName.length == 0" @click="createFolder"> {{ $t('common.save') }}</Button>
			<Button class="kn-button kn-button--secondary" @click="openCreationFolderDialog"> {{ $t('common.cancel') }}</Button>
		</template>
	</Dialog>

	<div class="managerDetail">
		<div class="p-grid p-m-0 p-fluid">
			<div class="p-col-12">
				<DataTable
					ref="dt"
					:value="files"
					:loading="loading"
					v-model:selection="selectedFiles"
					v-model:filters="filters"
					class="p-datatable-sm kn-table"
					dataKey="id"
					:paginator="true"
					:rows="10"
					paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
					:rowsPerPageOptions="[10, 15, 20]"
					responsiveLayout="stack"
					breakpoint="960px"
					:currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
					:globalFilterFields="['name', 'type', 'tags']"
				>
					<template #header>
						<div class="p-grid">
							<div class="p-col-11">
								<i class="pi pi-search" />
								<InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" />
							</div>
							<div v-if="selectedFiles.length == 0" class="p-col">
								<Button icon="fas fa-folder-plus" class="p-button-text p-button-rounded p-button-plain" @click="openCreationFolderDialog" :disabled="invalid" />
								<Button icon="fas fa-upload" class="p-button-text p-button-rounded p-button-plain" @click="upload" />
							</div>
						</div>
					</template>
					<template #empty>
						{{ $t('common.info.noDataFound') }}
					</template>
					<template #loading>
						{{ $t('common.info.dataLoading') }}
					</template>

					<Column v-for="col in getOrderedColumns()" :field="col.field" :header="$t(col.header)" :key="col.field" :style="col.style" :selectionMode="col.field == 'selectionMode' ? 'multiple' : ''" :exportable="col.field == 'selectionMode' ? false : ''">
						<template #body="{data}" v-if="col.displayType">
							<span class="p-float-label kn-material-input">
								<div v-if="col.displayType == 'fileSize'">
									{{ getDataValue(data.size) }}
								</div>
								<div v-if="col.displayType == 'date'">
									{{ getDate(data.lastModified) }}
								</div>
							</span>
						</template>
					</Column>
				</DataTable>
			</div>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import axios from 'axios'
	import descriptor from './ResourceManagementDescriptor.json'
	import Column from 'primevue/column'
	import DataTable from 'primevue/datatable'
	import { IFileTemplate } from '@/modules/managers/resourceManagement/ResourceManagement'
	import { FilterMatchMode, FilterOperator } from 'primevue/api'
	import { byteToHumanFriendlyFormat } from '@/helpers/commons/fileHelper'
	import { ITableColumn } from '../../commons/ITableColumn'
	import { formatDate } from '@/helpers/commons/localeHelper'
	import Breadcrumb from 'primevue/breadcrumb'
	import { downloadDirectFromResponse } from '@/helpers/commons/fileHelper'
	import Dialog from 'primevue/dialog'

	export default defineComponent({
		components: { Breadcrumb, Column, DataTable, Dialog },
		props: {
			id: String,
			relativePath: String
		},
		emits: ['touched', 'closed', 'inserted', 'folderCreated'],
		data() {
			return {
				descriptor,
				loading: false,
				touched: false,
				files: [] as Array<IFileTemplate>,
				selectedFiles: [] as Array<IFileTemplate>,
				filters: {
					global: { value: null, matchMode: FilterMatchMode.CONTAINS },
					name: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
					size: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
					lastModified: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }] }
				},
				/* 			home: { icon: 'pi pi-home', to: '/' },
				items: [] as Array<{}>, */
				home: { icon: 'pi pi-home' },
				items: [] as Array<{ label: String }>,
				folderCreation: false,
				folderName: ''
			}
		},
		computed: {},
		beforeRouteUpdate() {
			this.loadSelectedFolder()
		},
		created() {
			this.loadSelectedFolder()
		},
		mounted() {},

		methods: {
			createFolder() {
				let obj = {} as JSON
				obj['key'] = this.id
				obj['folderName'] = this.folderName
				axios
					.post(process.env.VUE_APP_API_PATH + `2.0/resources/folders`, obj, {
						responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

						headers: {
							'Content-Type': 'application/json'
						}
					})
					.then(() => {
						this.openCreationFolderDialog()
						this.loadSelectedFolder()
						this.$emit('folderCreated', true)
					})
					.finally(() => (this.loading = false))
			},
			downloadFiles() {
				let obj = {} as JSON
				obj['key'] = this.id
				obj['selectedFilesNames'] = []
				for (var idx in this.selectedFiles) {
					obj['selectedFilesNames'].push(this.selectedFiles[idx].name)
				}
				axios
					.post(process.env.VUE_APP_API_PATH + `2.0/resources/files/download`, obj, {
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
			getBreadcrumbs() {
				this.items = []
				if (this.relativePath) {
					let pathFolders = this.relativePath.split('\\')
					pathFolders.forEach((element) => {
						let obj = { label: element }
						this.items.push(obj)
					})
				}
			},
			getOrderedColumns(): Array<ITableColumn> {
				let columns = this.descriptor['column']
				columns.sort(function(a, b) {
					if (a.position > b.position) return 1
					if (a.position < b.position) return -1
					return 0
				})
				return columns
			},
			getDataValue(data) {
				return byteToHumanFriendlyFormat(data)
			},
			getDate(date) {
				return formatDate(date.expirationDate, 'LLL')
			},
			loadSelectedFolder() {
				this.loading = true
				this.files = []
				if (this.id) {
					axios.get(process.env.VUE_APP_API_PATH + `2.0/resources/files` + '?key=' + this.id).then((response) => {
						this.files = response.data
						this.getBreadcrumbs()
					})
				}
				this.loading = false
			},
			openCreationFolderDialog() {
				this.folderCreation = !this.folderCreation
			},
			setDirty() {
				this.touched = true
				this.$emit('touched')
			}
		},
		watch: {
			id(oldId, newId) {
				if (oldId != newId) this.loadSelectedFolder()
			}
		}
	})
</script>

<style scoped lang="scss"></style>
