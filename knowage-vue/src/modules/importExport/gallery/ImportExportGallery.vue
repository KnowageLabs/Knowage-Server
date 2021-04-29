<template>
	<div>
		<OverlayPanel ref="op" class="imageOverlayPanel">
			<img :src="currentImage" alt="Nature Image" />
		</OverlayPanel>
	</div>
	<Card style="height:100%">
		<template #content>
			<DataTable
				ref="dt"
				:value="templates"
				v-model:selection="selectedTemplates"
				v-model:filters="filters1"
				class="p-datatable-sm"
				dataKey="id"
				:paginator="true"
				:rows="10"
				paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
				:rowsPerPageOptions="[5, 10, 15, 20]"
				responsiveLayout="stack"
				breakpoint="960px"
				:currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}' })"
				:globalFilterFields="['name', 'type', 'tags']"
			>
				<template #header>
					<div class="table-header">
						<span class="p-input-icon-left">
							<i class="pi pi-search" />
							<InputText class="kn-material-input" type="text" v-model="filters1['global'].value" :placeholder="$t('common.search', { first: { first } })" />
						</span>
					</div>
				</template>
				<template #empty>
					{{ $t('common.info.noDataFound') }}
				</template>
				<template #loading>
					{{ $t('common.info.dataLoading') }}
				</template>

				<Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
				<Column field="name" header="Name" :sortable="true" :style="importExportDescriptor.export.gallery.column.name.style"></Column>
				<Column field="type" header="Type" :sortable="true" :style="importExportDescriptor.export.gallery.column.type.style">
					<template #body="{data}">
						<Tag :style="importExportDescriptor.iconTypesMap[data.type].style"> {{ data.type.toUpperCase() }} </Tag>
					</template>
				</Column>

				<Column field="tags" header="Tags" :sortable="true" :style="importExportDescriptor.export.gallery.column.tags.style">
					<template #body="{data}">
						<span class="p-float-label kn-material-input">
							<Tag class="importExportTags" v-for="(tag, index) in data.tags" v-bind:key="index" rounded :value="tag"> </Tag>
						</span>
					</template>
				</Column>
				<Column field="image" header="Image" :style="importExportDescriptor.export.gallery.column.image.style">
					<template #body="{data}">
						<span @click="togglePreview($event, data.id)">
							<i class="fas fa-image" v-if="data.image.length > 0" />
						</span>
					</template>
				</Column>
			</DataTable>
		</template>
	</Card>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Column from 'primevue/column'
	import DataTable from 'primevue/datatable'
	import { FilterMatchMode, FilterOperator } from 'primevue/api'
	import InputText from 'primevue/inputtext'
	import OverlayPanel from 'primevue/overlaypanel'
	import Tag from 'primevue/tag'
	import importExportDescriptor from '../ImportExportDescriptor.json'

	export default defineComponent({
		name: 'import-export-gallery',
		components: { Column, DataTable, InputText, OverlayPanel, Tag },
		data() {
			return {
				importExportDescriptor: importExportDescriptor,
				product: {},
				selectedTemplates: null,
				filters: {},
				filters1: {},
				currentImage: '',
				templates: importExportDescriptor.templates
			}
		},
		created() {
			this.filters1 = {
				global: { value: null, matchMode: FilterMatchMode.CONTAINS },
				name: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
				type: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
				tags: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }] }
			}
		},
		methods: {
			getCurrentTemplateImage(index) {
				return this.templates[index].image
			},
			togglePreview(event, id) {
				this.currentImage = ''

				for (var idx in this.templates) {
					if (this.templates[idx].id === id) {
						this.currentImage = this.templates[idx].image
						break
					}
				}

				/* 				axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/widgetgallery/image?id' +id).then(
					(response) => {
						console.log(response)
						this.currentImage  = response.data
					},
					(error) => console.error(error)
				) */
				// eslint-disable-next-line
				// @ts-ignore
				this.$refs.op.toggle(event)
			}
		}
	})
</script>

<style lang="scss" scoped>
	.importExportTags {
		&:deep(.p-inputtext) {
			border: none !important;
			border-radius: 0;
			width: 100%;
		}
	}

	.imageOverlayPanel {
		position: absolute !important;
		top: 0px !important;
		left: 0px !important;
	}
</style>
