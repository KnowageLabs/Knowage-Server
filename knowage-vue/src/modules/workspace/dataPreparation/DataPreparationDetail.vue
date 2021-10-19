<template>
	<div class="kn-page">
		<DataPreparationDialog v-model:transformation="selectedTransformation" @send-transformation="handleTransformation" :columns="columns" v-model:col="col" />
		<DataPreparationSaveDialog v-model:visibility="showSaveDialog" v-model:dataset="dataset" />
		<Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
			<template #left> {{ $t('managers.workspaceManagement.dataPreparation.detail') }} </template>
			<template #right>
				<Button icon="pi pi-save" class="kn-button p-button-text" v-tooltip.bottom="$t('common.save')" @click="saveDataset" />
				<Button icon="pi pi-times" class="kn-button p-button-text" v-tooltip.bottom="$t('common.close')" @click="closeTemplate($event)" /> </template
		></Toolbar>
		<Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0 toolbarCustomConfig">
			<template #left>
				<span v-for="(menu, index) in getMenuForToolbar()" v-bind:key="index">
					<Button v-if="menu !== 'divider'" :icon="menu.icon" :class="descriptor.css.buttonClassHeader" v-tooltip.bottom="$t(menu.label)" @click="callFunction(menu)" />
					<Divider v-else layout="vertical" />
				</span>
			</template>
			<template #right><Button icon="pi pi-arrow-left" :class="descriptor.css.buttonClassHeader" @click="visibleRight = true"/></template>
		</Toolbar>
		<Divider class="p-m-0 p-p-0 dividerCustomConfig" />
		<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
		<div class="kn-page-content p-grid p-m-0 managerDetail">
			<Sidebar v-model:visible="visibleRight" position="right">
				<span>{{ $t('managers.workspaceManagement.dataPreparation.transformations.label') }}</span>
				<div class="p-grid p-m-0 p-d-flex" v-if="dataset.config && dataset.config.transformations && dataset.config.transformations.length > 0">
					<span class="p-col">
						<Button v-for="(tr, index) in dataset.config.transformations" v-bind:key="index" :disabled="index < dataset.config.transformations.length - 1" :class="'p-col ' + descriptor.css.buttonClassHeader" v-tooltip.bottom="getTextForSidebar(tr)">
							<div class="p-grid p-m-0 p-p-0 p-d-flex kn-flex transformationSidebarElement">
								<i :class="'p-col-1 ' + descriptorTransformations.filter((x) => x.type === tr.type)[0].icon"></i>
								<span class="p-col-9 typeAndDescription kn-truncated">
									<span class="kn-list-item">{{ $t(tr.type) }} </span>
									<span class="transformationDescription kn-truncated">
										{{ getTextForSidebar(tr) }}
									</span></span
								>

								<div class="p-col" v-if="index == dataset.config.transformations.length - 1">
									<i class="p-jc-end pi pi-times" @click="deleteTransformation(index)"></i>
								</div>
							</div>
						</Button>
					</span>
				</div>
			</Sidebar>
			<DataTable
				ref="dt"
				:value="datasetData"
				class="p-datatable-sm kn-table functionalityTable"
				dataKey="id"
				:paginator="true"
				:rows="10"
				paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
				breakpoint="960px"
				:currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
				:loading="loading"
				:resizableColumns="true"
				columnResizeMode="expand"
				showGridlines
				responsiveLayout="scroll"
			>
				<template #empty>
					{{ $t('common.info.noDataFound') }}
				</template>
				<template #loading>
					{{ $t('common.info.dataLoading') }}
				</template>

				<Column v-for="(col, colIndex) in columns" :field="col.header" :key="colIndex" :style="col.style">
					<template #header>
						<div class="p-grid p-m-0 p-d-flex kn-flex ">
							<div class="p-col-3 p-m-0 p-p-0 p-jc-start p-ai-center">
								<Button :icon="descriptor.compatibilityMap[col.type].icon" :class="descriptor.css.buttonClassHeader" @click="toggle($event, 'opType-' + colIndex)" />
							</div>
							<div class="p-col-6 p-m-0 p-p-0 p-ai-center p-jc-center kn-truncated">
								{{ $t(col.header) }}

								<OverlayPanel :ref="'opType-' + colIndex" class="op">
									<div class="p-col-12 p-m-0 p-p-0" v-for="(type, index) in getCompatibilityType(col)" v-bind:key="index">
										<Button :icon="descriptor.compatibilityMap[type].icon" :class="descriptor.css.buttonClassHeader" @click="callFunction(descriptorTransformations.filter((x) => x.type === 'changeType')[0], col.header, type)" :label="$t(type)" />
									</div>
								</OverlayPanel>
							</div>
							<div class="p-col-3 p-m-0 p-p-0 p-d-flex p-jc-end p-ai-center">
								<Button icon="pi pi-ellipsis-v" :class="descriptor.css.buttonClassHeader" @click="toggle($event, 'trOpType-' + colIndex)" />
								<OverlayPanel :ref="'trOpType-' + colIndex" class="transformationsOverlayPanel">
									<div class="p-col-12 p-m-0 p-p-0" v-for="(menu, index) in getTransformationsMenu(col)" v-bind:key="index"><Button :icon="menu.icon" :class="descriptor.css.buttonClassHeader" :label="$t(menu.label)" @click="callFunction(menu, col.header)" /></div>
								</OverlayPanel>
							</div>
						</div> </template
				></Column>
			</DataTable>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'

	import axios from 'axios'
	import Column from 'primevue/column'
	import DataTable from 'primevue/datatable'
	import DataPreparationDescriptor from './DataPreparationDescriptor.json'
	import Divider from 'primevue/divider'
	import Sidebar from 'primevue/sidebar'
	import OverlayPanel from 'primevue/overlaypanel'
	/* import Listbox from 'primevue/listbox' */

	/* import ITransformation from '@/modules/workspace/dataPreparation/DataPreparation' */

	import DataPreparationDialog from '@/modules/workspace/dataPreparation/DataPreparationDialog.vue'
	import DataPreparationSaveDialog from '@/modules/workspace/dataPreparation/DataPreparationSaveDialog.vue'
	import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'

	export default defineComponent({
		name: 'data-preparation-detail',
		props: {
			id: Object
		},
		components: { Column, DataPreparationDialog, DataPreparationSaveDialog, DataTable, Divider, Sidebar, OverlayPanel },

		data() {
			return {
				descriptor: DataPreparationDescriptor,
				loading: false,
				datasetData: Array<any>(),
				displayDataPreparationDialog: false,
				selectedProduct: null,
				visibleRight: false,
				visibility: false,
				selectedTransformation: null,
				showSaveDialog: false,
				columns: [] as IDataPreparationColumn[],
				col: null,
				descriptorTransformations: Array<any>(),
				dataset: {} as any
			}
		},

		async created() {
			this.loading = true
			this.descriptorTransformations = Object.assign([], this.descriptor.transformations)

			await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/datasets/' + this.id).then((response) => {
				this.dataset = response.data[0]
			})
			if (this.dataset) {
				await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/datapreparation/' + this.id + '/datasetinfo').then((response) => {
					this.columns = []
					response.data.meta.columns
						.filter((x) => x.pname == 'Type')
						.forEach((element) => {
							let obj = {} as IDataPreparationColumn
							obj.header = element.column
							obj.type = element.pvalue
							obj.disabled = false

							this.columns.push(obj)
						})

					axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/datapreparation/' + this.id + '/preview', this.dataset).then((response) => {
						this.datasetData = []

						response.data.rows.forEach((element) => {
							let obj = {}
							const keys = Object.keys(element)
							keys.forEach((key) => {
								let index = parseInt(key.replace('column_', ''), 10) - 1
								if (index >= 0 && index < this.columns.length) {
									let v = this.columns[index] as IDataPreparationColumn

									if (v) obj[v.header] = element[key]
								}
							})
							this.datasetData.push(obj)

							this.loading = false
						})
					})
				})
			}
		},
		methods: {
			getTextForSidebar(tr) {
				let text = ''

				tr.parameters.forEach((element) => {
					const keys = Object.keys(element)
					keys.forEach((key) => {
						text += key + ':' + element[key] + '; '
					})
				})

				return '(' + text + ')'
			},

			getTransformationsMenu(col) {
				return this.descriptorTransformations
					.filter((x) => x.editColumn)
					.filter((x) => {
						if (x.incompatibleDataTypes) return !x.incompatibleDataTypes?.includes(col.type)
						return true
					})
			},
			callFunction(transformation, col, type?) {
				if (transformation.type === 'changeType') {
					transformation.config.parameters[0][0].value = type
					let toReturn = { parameters: [] as Array<any>, type: transformation.type }
					let obj = { columns: [] as Array<any> }
					obj.columns.push({ columns: col, type: type })
					toReturn.parameters.push(obj)

					this.handleTransformation(toReturn)
				} else {
					let requiresValues = false
					for (var i = 0; i < transformation.config.parameters.length; i++) {
						let element = transformation.config.parameters[i]
						requiresValues = element.filter((x) => !x.value).length > 0

						if (requiresValues) break
					}

					if (requiresValues) {
						this.selectedTransformation = transformation
						this.col = col
					} else {
						this.handleTransformation(transformation)
					}
				}
			},
			handleTransformation(t) {
				if (!this.dataset.config) this.dataset.config = {}
				if (!this.dataset.config.transformations) this.dataset.config.transformations = []
				this.dataset.config.transformations.push(t)
				this.loadPreviewData()
			},
			deleteTransformation(index) {
				this.dataset.config.transformations.splice(index, 1)
				this.loadPreviewData()
			},
			getCompatibilityType(col) {
				return this.descriptor.compatibilityMap[col.type].values
			},
			addColumn(item) {
				console.log(item)
			},
			toggle(event, trOp) {
				// eslint-disable-next-line
				// @ts-ignore
				this.$refs[trOp].toggle(event)
			},
			getMenuForToolbar() {
				let tmp = this.descriptorTransformations
					.filter((x) => x.toolbar)
					.sort(function(a, b) {
						if (a.position > b.position) return 1
						if (a.position < b.position) return -1
						return 0
					})

				let menu = [] as Array<any>
				if (tmp.length > 0) {
					let type = tmp[0].category
					menu.push(tmp[0])

					for (let i = 1; i < tmp.length; i++) {
						if (type !== tmp[i].category) {
							type = tmp[i].category
							menu.push('divider')
						}
						menu.push(tmp[i])
					}
				}
				return menu
			},
			saveDataset() {
				this.showSaveDialog = true
			},
			async loadPreviewData() {
				/* 				await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/dataset/preview', this.selectedTransformation).then((response) => {
					console.log(response)
					this.selectedTransformation = null
				}) */

				console.log(this.dataset.config.transformations)

				this.selectedTransformation = null
			}
		}
	})
</script>

<style lang="scss" scoped>
	.image {
		position: relative;
	}
	.imageH2 {
		position: absolute;
		top: 10px;
		left: 0;
		width: 100%;
	}
	.toolbarCustomConfig {
		background-color: white !important;
	}
	.dividerCustomConfig {
		border: 1px solid;
		border-color: $color-borders;
	}
	.p-overlaypanel-content {
		padding: 0px !important;
	}
	.transformationDescription {
		color: $list-item-text-secondary-color;
		font-size: $list-item-text-secondary-font-size;
	}

	.typeAndDescription {
		flex-direction: column;
		display: flex;
		align-items: flex-start;
	}

	.p-sidebar-content {
		height: 100vw;
	}

	.transformationSidebarElement {
		align-items: center;
	}
</style>
