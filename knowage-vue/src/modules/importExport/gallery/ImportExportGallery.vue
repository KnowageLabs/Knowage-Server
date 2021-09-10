<template>
	<span>
		<OverlayPanel ref="op" class="imageOverlayPanel">
			<img :src="currentImage" />
		</OverlayPanel>
	</span>
	<DataTable
		ref="dt"
		:value="templates"
		v-model:selection="selectedGalleryItems"
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

		<Column selectionMode="multiple" :exportable="false" :style="importExportDescriptor.export.gallery.column.selectionMode.style"></Column>
		<Column field="name" :header="$t(importExportDescriptor.export.gallery.column.name.header)" :sortable="true" :style="importExportDescriptor.export.gallery.column.name.style"></Column>
		<Column field="type" :header="$t(importExportDescriptor.export.gallery.column.type.header)" :sortable="true" :style="importExportDescriptor.export.gallery.column.type.style">
			<template #body="{data}">
				<Tag :style="importExportDescriptor.iconTypesMap[data.type].style"> {{ data.type.toUpperCase() }} </Tag>
			</template>
		</Column>

		<Column field="tags" :header="$t(importExportDescriptor.export.gallery.column.tags.header)" :sortable="true" :style="importExportDescriptor.export.gallery.column.tags.style">
			<template #body="{data}">
				<span class="p-float-label kn-material-input">
					<Tag class="importExportTags p-mr-1" v-for="(tag, index) in data.tags" v-bind:key="index" rounded :value="tag"> </Tag>
				</span>
			</template>
		</Column>
		<Column field="image" :header="$t(importExportDescriptor.export.gallery.column.image.header)" :style="importExportDescriptor.export.gallery.column.image.style">
			<template #body="{data}">
				<span @click="togglePreview($event, data.id)">
					<i class="fas fa-image" v-if="data.image && data.image.length > 0" />
				</span>
			</template>
		</Column>
	</DataTable>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import axios from 'axios'
	import Column from 'primevue/column'
	import DataTable from 'primevue/datatable'
	import { FilterMatchMode, FilterOperator } from 'primevue/api'
	import InputText from 'primevue/inputtext'
	import OverlayPanel from 'primevue/overlaypanel'

	import Tag from 'primevue/tag'
	import importExportDescriptor from '../ImportExportDescriptor.json'
	import { IGalleryTemplate } from '@/modules/managers/galleryManagement/GalleryManagement'

	export default defineComponent({
		name: 'import-export-gallery',
		components: { Column, DataTable, InputText, OverlayPanel, Tag },
		props: { selectedItems: Object },
		data() {
			return {
				currentImage: '',
				filters: {},
				importExportDescriptor: importExportDescriptor,
				product: {},
				selectedGalleryItems: [],
				templates: [] as Array<IGalleryTemplate>,
				FUNCTIONALITY: 'gallery'
			}
		},
		created() {
			this.filters = {
				global: { value: null, matchMode: FilterMatchMode.CONTAINS },
				name: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
				type: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
				tags: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }] }
			}
			this.loadAllTemplates()
		},
		emits: ['onItemSelected', 'update:loading'],
		methods: {
			loadAllTemplates(): void {
				this.$emit('update:loading', true)
				this.axios
					.get(process.env.VUE_APP_API_PATH + '1.0/widgetgallery')
					.then((response) => {
						this.templates = response.data
						if (this.selectedItems) {
							this.selectedGalleryItems = this.selectedItems[this.FUNCTIONALITY].filter((element) => {
								return this.templates.filter((el) => el.id === element.id).length == 1
							})
						}
					})
					.catch((error) => console.error(error))
					.finally(() => {
						this.$emit('update:loading', false)
					})
			},
			togglePreview(event, id) {
				this.currentImage = ''

				axios.get(process.env.VUE_APP_API_PATH + '1.0/widgetgallery/image/' + id).then(
					(response) => {
						console.log(response)
						this.currentImage = response.data
					},
					(error) => console.error(error)
				)
				// eslint-disable-next-line
				// @ts-ignore
				this.$refs.op.toggle(event)
			}
		},
		watch: {
			selectedGalleryItems(newSelectedGalleryItems, oldSelectedGalleryItems) {
				if (oldSelectedGalleryItems != newSelectedGalleryItems) {
					this.$emit('onItemSelected', { items: this.selectedGalleryItems, functionality: this.FUNCTIONALITY })
				}
			}
		}
	})
</script>

<style lang="scss" scoped>
	.imageOverlayPanel {
		position: absolute !important;
		top: 0px !important;
		left: 0px !important;
	}

	.importExportTags {
		background-color: $color-default;
	}

	.p-paginator p-component p-paginator-bottom {
		height: 50px;
	}
</style>
