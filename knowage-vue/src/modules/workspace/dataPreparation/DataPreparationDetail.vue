<template>
	<div class="kn-page">
		<DataPreparationDialog v-model:visibility="selectedTransformation" @sendTrasformation="handleTransformation" />
		<DataPreparationSaveDialog v-model:visibility="showSaveDialog" :dataset="dataset" />
		<Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
			<template #left> {{ $t('managers.workspaceManagement.dataPreparation.detail') }} </template>
			<template #right>
				<Button icon="pi pi-save" class="kn-button p-button-text" v-tooltip.bottom="$t('common.save')" @click="saveDataset" />
				<Button icon="pi pi-times" class="kn-button p-button-text" v-tooltip.bottom="$t('common.close')" @click="closeTemplate($event)" /> </template
		></Toolbar>
		<Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0 toolbarCustomConfig">
			<template #left>
				<span v-for="(menu, index) in getMenuForToolbar()" v-bind:key="index">
					<Button v-if="menu !== 'divider'" :icon="menu.icon" :class="buttonDefaultClass + ' headerButton'" v-tooltip.bottom="$t(menu.label)" @click="callFunction(menu)" />
					<Divider v-else layout="vertical" />
				</span>
			</template>
			<template #right><Button icon="pi pi-arrow-left" :class="buttonDefaultClass + ' headerButton'" @click="visibleRight = true"/></template>
		</Toolbar>
		<Divider class="p-m-0 p-p-0 dividerCustomConfig" />
		<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
		<div class="kn-page-content p-grid p-m-0 managerDetail">
			<Sidebar v-model:visible="visibleRight" position="right">
				{{ $t('modules.workspace.dataPreparation.transformations') }}
				<span v-for="(mutation, index) in descriptor.mutations" v-bind:key="index"> <Chip :label="mutation.type" :icon="descriptor.transformations.filter((x) => x.type === mutation.type)[0].icon"> </Chip></span>
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

				<Column v-for="(col, colIndex) in columns" :field="col.name" :key="colIndex" :style="col.style">
					<template #header>
						<div class="p-grid p-m-0 p-d-flex kn-flex ">
							<div class="p-col-3 p-m-0 p-p-0 p-jc-start p-ai-center">
								<Button :icon="descriptor.compatibilityMap[col.type].icon" :class="buttonDefaultClass + ' headerButton'" @click="toggle($event, 'opType-' + colIndex)" />
							</div>
							<div class="p-col-6 p-m-0 p-p-0 p-ai-center p-jc-center kn-truncated">
								{{ $t(col.header) }}

								<OverlayPanel :ref="'opType-' + colIndex" class="op">
									<div class="p-col-12 p-m-0 p-p-0" v-for="(type, index) in getCompatibilityType(col)" v-bind:key="index"><Button :icon="descriptor.compatibilityMap[type].icon" :class="buttonDefaultClass + ' headerButton'" @click="callFunction(menu)" />{{ $t(type) }}</div>
								</OverlayPanel>
							</div>
							<div class="p-col-3 p-m-0 p-p-0 p-d-flex p-jc-end p-ai-center">
								<Button icon="pi pi-ellipsis-v" :class="buttonDefaultClass + ' headerButton'" @click="toggle($event, 'trOpType-' + colIndex)" />
								<OverlayPanel :ref="'trOpType-' + colIndex" class="transformationsOverlayPanel">
									<div class="p-col-12 p-m-0 p-p-0" v-for="(menu, index) in getTransformationsMenu(col)" v-bind:key="index"><Button :icon="menu.icon" :class="buttonDefaultClass + ' headerButton'" @click="callFunction(menu)" />{{ $t(menu.label) }}</div>
								</OverlayPanel>
							</div>
						</div>
					</template></Column
				>
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
	import Chip from 'primevue/chip'
	/* import ITransformation from '@/modules/workspace/dataPreparation/DataPreparation' */

	import DataPreparationDialog from '@/modules/workspace/dataPreparation/DataPreparationDialog.vue'
	import DataPreparationSaveDialog from '@/modules/workspace/dataPreparation/DataPreparationSaveDialog.vue'

	export default defineComponent({
		name: 'data-preparation-detail',
		props: {
			id: String
		},
		components: { Chip, Column, DataPreparationDialog, DataPreparationSaveDialog, DataTable, Divider, Sidebar, OverlayPanel },

		data() {
			return {
				descriptor: DataPreparationDescriptor,
				loading: false,
				dataset: {},
				datasetData: Array<any>(),
				displayDataPreparationDialog: false,
				transformationsModel: Array<any>(),
				selectedProduct: null,
				visibleRight: false,
				visibility: false,
				selectedTransformation: null,
				showSaveDialog: false,
				columns: [],

				// CSS
				buttonDefaultClass: 'p-button-text p-button-rounded p-button-plain'
			}
		},

		emits: ['sendTransformation'],
		created() {
			this.datasetData = this.descriptor.columnData

			axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasets/' + this.id + '/preview', { start: 0, limit: 15 }).then((response) => {
				this.datasetData = response.data.rows
				this.dataset = { name: 'knowage', description: 'FERFAEFAWFAEF', label: 'LABEL', visibility: 'TOUT LE MONDE', refreshRateId: '1' }
				this.columns = response.data.metaData.fields.filter((x) => x.dataIndex)
			})

			this.transformationsModel = this.descriptor.transformations.filter((x) => x.editColumn)
		},
		methods: {
			getTransformationsMenu(col) {
				return this.descriptor.transformations
					.filter((x) => x.editColumn)
					.filter((x) => {
						if (x.incompatibleDataTypes) return !x.incompatibleDataTypes?.includes(col.type)
						return true
					})
			},
			callFunction(transformation) {
				if (transformation.config.parameters && transformation.config.parameters.filter((x) => !x.value).length > 0) {
					this.selectedTransformation = transformation
				} else {
					let t = transformation
					this.handleTransformation(t)
				}
			},
			handleTransformation(t) {
				console.log(t)

				this.selectedTransformation = null

				/* 									await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/dataPreparation', t).then((response) => {
						console.log(response)
					}) */
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
				let tmp = this.descriptor.transformations
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
			getData(o) {
				console.log(o)
				this.descriptor.column
			},
			search(e) {
				console.log(e)
			},
			filter(e) {
				console.log(e)
			},
			saveDataset() {
				this.showSaveDialog = true
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
</style>
