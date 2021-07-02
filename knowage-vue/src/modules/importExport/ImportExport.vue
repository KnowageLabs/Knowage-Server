<template>
	<div class="kn-importExport kn-page">
		<ImportDialog v-model:visibility="displayImportDialog"></ImportDialog>
		<ExportDialog v-model:visibility="displayExportDialog" @export="startExport"></ExportDialog>
		<Toolbar class="kn-toolbar kn-toolbar--primary">
			<template #left>
				{{ $t('importExport.title') }}
			</template>
			<template #right>
				<Button class="kn-button p-button-text" @click="openImportDialog">{{ $t('common.import') }}</Button>
				<Button class="kn-button p-button-text" @click="openExportDialog" :disabled="isExportDisabled()">{{ $t('common.export') }}</Button>
			</template>
		</Toolbar>
		<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
		<div class="kn-page-content p-grid p-m-0">
			<div v-if="importExportDescriptor.functionalities.length > 1" class="functionalities-container p-col-3 p-sm-3 p-md-2">
				<KnTabCard :element="functionality" :selected="functionality.route === $route.path" v-for="(functionality, index) in importExportDescriptor.functionalities" v-bind:key="index" @click="selectType(functionality)" :badge="selectedItems[functionality.type].length"> </KnTabCard>
			</div>
			<div class="p-col p-pt-0">
				<router-view v-model:loading="loading" @onItemSelected="getSelectedItems($event)" />
			</div>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import axios from 'axios'
	import importExportDescriptor from './ImportExportDescriptor.json'
	import ExportDialog from './ExportDialog.vue'
	import ImportDialog from './ImportDialog.vue'
	import ProgressBar from 'primevue/progressbar'
	import KnTabCard from '@/components/UI/KnTabCard.vue'
	import { downloadDirect } from '@/helpers/commons/fileHelper'

	export default defineComponent({
		name: 'import-export',
		components: { ExportDialog, KnTabCard, ImportDialog, ProgressBar },
		data() {
			return {
				importExportDescriptor: importExportDescriptor,
				displayImportDialog: false,
				displayExportDialog: false,
				fileName: '',
				loading: false,
				selectedItems: {
					gallery: [],
					catalogFunction: []
				}
			}
		},
		emits: ['onItemSelected'],
		methods: {
			getSelectedItems(e) {
				this.selectedItems[e.functionality] = e.items
			},
			isExportDisabled() {
				for (var index in this.selectedItems) {
					if (this.selectedItems[index].length > 0) return false
				}
				return true
			},
			selectType(type): void {
				this.$router.push(type.route)
			},
			openImportDialog(): void {
				this.displayImportDialog = !this.displayImportDialog
			},
			openExportDialog(): void {
				this.displayExportDialog = !this.displayExportDialog
			},
			async startExport(fileName: string) {
				await axios
					.post(process.env.VUE_APP_API_PATH + '1.0/widgetgallery-ee/export/bulk', this.streamlineSelectedItemsArray(fileName), {
						responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

						headers: {
							'Content-Type': 'application/json',
							Accept: 'application/zip; charset=utf-8'
						}
					})
					.then(
						(response) => {
							if (response.data.errors) {
								this.$store.commit('setError', { title: this.$t('common.error.downloading'), msg: this.$t('importExport.export.completedWithErrors') })
							} else {
								var contentDisposition = response.headers['content-disposition']
								var fileAndExtension = contentDisposition.match(/(?!([\b attachment;filename= \b])).*(?=)/g)[0]
								var completeFileName = fileAndExtension.replaceAll('"', '')
								downloadDirect(response.data, completeFileName, 'application/zip; charset=utf-8')
								this.$store.commit('setInfo', { title: this.$t('common.downloading'), msg: this.$t('importExport.export.successfullyCompleted') })
							}

							this.selectedItems = {
								gallery: [],
								catalogFunction: []
							}
							/* closing dialog */
							this.openExportDialog()
						},
						(error) => this.$store.commit('setError', { title: this.$t('common.error.downloading'), msg: this.$t(error) })
					)
			},

			streamlineSelectedItemsArray(fileName): JSON {
				let selectedItemsToBE = {} as JSON
				selectedItemsToBE['fileName'] = fileName
				selectedItemsToBE['knowageVersion'] = process.env.VUE_APP_VERSION
				selectedItemsToBE['datetime'] = new Date()
				for (var category in this.selectedItems) {
					for (var k in this.selectedItems[category]) {
						if (!selectedItemsToBE[category]) {
							selectedItemsToBE[category] = []
						}
						let obj = { id: this.selectedItems[category][k].id }
						selectedItemsToBE[category].push(obj)
					}
				}

				return selectedItemsToBE
			}
		}
	})
</script>

<style lang="scss" scoped></style>
