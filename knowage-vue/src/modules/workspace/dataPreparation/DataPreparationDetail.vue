<template>
	<div class="kn-page">
		<Toolbar class="kn-toolbar kn-toolbar--primary p-m-0"> <template #left> Data preparation detail</template></Toolbar>
		<Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
			<template #left>
				<span v-for="(menu, index) in getMenuForToolbar()" v-bind:key="index">
					<Button v-if="menu !== 'divider'" :icon="menu.icon" class="p-button-text p-button-rounded p-button-plain headerButton" @click="toggleSort" v-tooltip.bottom="$t(menu.label)" />
					<Divider v-else layout="vertical" />
				</span>
			</template>
			<template #right><Button icon="pi pi-arrow-left" class="p-button-text p-button-rounded p-button-plain headerButton" @click="visibleRight = true"/></template>
		</Toolbar>
		<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
		<div class="kn-page-content p-grid p-m-0 managerDetail">
			<Sidebar v-model:visible="visibleRight" position="right">
				Content
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
				contextMenu
				v-model:contextMenuSelection="selectedProduct"
				@rowContextmenu="onRowContextMenu"
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

				<Column v-for="col in descriptor.column" :field="col.field" :key="col.field" :style="col.style" :exportable="col.field == 'selectionMode' ? false : ''">
					<template #header>
						<div class="p-grid p-m-0 p-d-flex kn-flex ">
							<div class="p-col-8 p-m-0 p-p-0 p-d-flex p-jc-start p-ai-center">
								<Button icon="fas fa-hashtag" class="p-button-text p-button-rounded p-button-plain headerButton" />
								<span class="p-ai-center p-jc-center">{{ $t(col.header) }}</span>
							</div>
							<div class="p-col-4 p-m-0 p-p-0 p-d-flex p-jc-end p-ai-center">
								<Button icon="pi pi-ellipsis-v" class="p-button-text p-button-rounded p-button-plain headerButton" @click="toggle" />
							</div>
						</div> </template
				></Column>
			</DataTable>
			<OverlayPanel ref="op" class="contextMenu">
				<div class="p-col-12" v-for="(menu, index) in descriptor.menu.filter((x) => x.editColumn)" v-bind:key="index"><Button :icon="menu.icon" class="p-button-text p-button-rounded p-button-plain headerButton" @click="callFunction(menu.purpose, item)" />{{ $t(menu.label) }}</div>
			</OverlayPanel>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	/* 	import axios from 'axios' */
	import Column from 'primevue/column'
	import DataTable from 'primevue/datatable'
	import DataPreparationDescriptor from './DataPreparationDescriptor.json'
	import Divider from 'primevue/divider'
	import Sidebar from 'primevue/sidebar'
	import OverlayPanel from 'primevue/overlaypanel'

	export default defineComponent({
		name: 'data-preparation-detail',
		props: {
			id: String,
			visibility: Boolean
		},
		components: { Column, DataTable, Divider, Sidebar, OverlayPanel },

		data() {
			return {
				descriptor: DataPreparationDescriptor,
				loading: false,
				datasetData: Array<any>(),
				menuModel: Array<any>(),
				selectedProduct: null,
				visibleRight: false
			}
		},

		emits: ['update:visibility'],
		created() {
			this.datasetData = this.descriptor.columnData
			this.menuModel = this.descriptor.menu.filter((x) => x.editColumn)
		},
		methods: {
			addColumn(item) {
				console.log(item)
			},
			toggle(event) {
				// eslint-disable-next-line
				// @ts-ignore
				this.$refs.op.toggle(event)
			},
			getMenuForToolbar() {
				let tmp = this.descriptor.menu
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
			onRowContextMenu(event) {
				// eslint-disable-next-line
				// @ts-ignore
				this.$refs.cm.show(event.originalEvent)
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
			viewProduct(product) {
				this.$toast.add({ severity: 'info', summary: 'Product Selected', detail: product.name })
			},
			deleteProduct(product) {
				console.log(product)
				this.$toast.add({ severity: 'error', summary: 'Product Deleted', detail: 'BANANA' })
				this.selectedProduct = null
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
</style>
