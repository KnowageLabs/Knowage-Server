<template>
    <div class="kn-page">
        <DataPreparationDialog v-model:transformation="selectedTransformation" @send-transformation="handleTransformation" :columns="columns" v-model:col="col" />
        <DataPreparationSaveDialog v-model:visibility="showSaveDialog" v-model:dataset="dataset" />
        <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
            <template #left> {{ $t('managers.workspaceManagement.dataPreparation.detail') }} </template>
            <template #right>
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" @click="saveDataset" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="closeTemplate($event)" /> </template
        ></Toolbar>
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0 toolbarCustomConfig">
            <template #left>
                <span v-for="(menu, index) in getMenuForToolbar()" v-bind:key="index">
                    <Button v-if="menu !== 'divider'" :class="descriptor.css.buttonClassHeader" v-tooltip.bottom="$t(menu.label)" @click="callFunction(menu)">
                        <span v-if="menu.icon.class" :class="menu.icon.class">{{ menu.icon.name }}</span>
                        <i v-else :class="menu.icon"></i>
                    </Button>
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
                <!-- 
				<Menu v-if="dataset.config && dataset.config.transformations && dataset.config.transformations.length > 0" :model="dataset.config.transformations" :ref="sidebar" :popup="false" class="customSidebarMenu">
					<template #item="{item}">
						<span class="p-menuitem-link">
							<i :class="descriptorTransformations.filter((x) => x.type === item.type)[0].icon"></i>
							<span class=" typeAndDescription kn-truncated">
								<span class="kn-list-item">{{ $t(item.type) }} </span>
								<span class="transformationDescription kn-truncated">
									{{ getTextForSidebar(item) }}
								</span></span
							></span
						>

						<div v-if="index == dataset.config.transformations.length - 1">
							<i class="p-jc-end pi pi-times" @click="deleteTransformation(index)"></i>
						</div>
					</template>
				</Menu> -->

                <!-- 				<div class="p-grid p-m-0 p-d-flex" v-if="dataset.config && dataset.config.transformations && dataset.config.transformations.length > 0">
					<span class="p-col">
						<Button v-for="(tr, index) in dataset.config.transformations" v-bind:key="index" :disabled="index < dataset.config.transformations.length - 1" :class="'p-col ' + descriptor.css.buttonClassHeader" v-tooltip="getTextForSidebar(tr)">
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
				</div> -->

                <span v-if="dataset.config && dataset.config.transformations && dataset.config.transformations.length > 0">
                    <span v-for="(tr, index) in dataset.config.transformations" v-bind:key="index" v-tooltip="getTextForSidebar(tr)" :class="getSidebarElementClass(index)">
                        <span :class="'p-col-1 ' + descriptorTransformations.filter((x) => x.type === tr.type)[0].icon.class" v-if="descriptorTransformations.filter((x) => x.type === tr.type)[0].icon.class">{{ descriptorTransformations.filter((x) => x.type === tr.type)[0].icon.name }}</span>
                        <i v-else :class="'p-col-1 ' + descriptorTransformations.filter((x) => x.type === tr.type)[0].icon"></i>

                        <span class="p-col-10 typeAndDescription kn-truncated">
                            <span class="kn-list-item">{{ $t(descriptorTransformations.filter((x) => x.type === tr.type)[0].label) }} </span>
                            <span class="transformationDescription kn-truncated">
                                {{ getTextForSidebar(tr) }}
                            </span></span
                        >

                        <div class="p-col-1" v-if="index == dataset.config.transformations.length - 1">
                            <i class="p-jc-end pi pi-times" @click="deleteTransformation(index)"></i></div></span
                ></span>
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
                                <Button :class="descriptor.css.buttonClassHeader" @click="toggle($event, 'opType-' + colIndex)">
                                    <span v-if="descriptor.compatibilityMap[col.type].icon.class" :class="descriptor.compatibilityMap[col.type].icon.class">{{ descriptor.compatibilityMap[col.type].icon.name }}</span>
                                    <i v-else :class="descriptor.compatibilityMap[col.type].icon"></i>
                                </Button>
                            </div>
                            <div class="p-col-6 p-m-0 p-p-0 p-ai-center p-jc-center kn-truncated">
                                <span>{{ $t(col.header) }}</span>

                                <Menu :model="getCompatibilityType(col)" :ref="'opType-' + colIndex" :popup="true">
                                    <template #item="{item}">
                                        <span class="p-menuitem-link" @click="callFunction(descriptorTransformations.filter((x) => x.type === 'changeType')[0], col.header, item)">
                                            <span v-if="descriptor.compatibilityMap[item].icon.class" :class="descriptor.compatibilityMap[item].icon.class">{{ descriptor.compatibilityMap[item].icon.name }}</span>
                                            <i v-else :class="descriptor.compatibilityMap[item].icon"></i>

                                            <span class="p-ml-2"> {{ $t(item) }}</span>
                                        </span>
                                    </template>
                                </Menu>
                            </div>
                            <div class="p-col-3 p-m-0 p-p-0 p-d-flex p-jc-end p-ai-center">
                                <Button icon="pi pi-ellipsis-v" :class="descriptor.css.buttonClassHeader" @click="toggle($event, 'trOpType-' + colIndex)" />
                                <Menu :model="getTransformationsMenu(col)" :ref="'trOpType-' + colIndex" :popup="true">
                                    <template #item="{item}">
                                        <span class="p-menuitem-link" @click="callFunction(item, col.header)">
                                            <span :class="item.icon.class" v-if="item.icon.class">{{ item.icon.name }}</span>

                                            <i v-else :class="item.icon"></i> <span class="p-ml-2"> {{ $t(item.label) }}</span></span
                                        >
                                    </template>
                                </Menu>
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

    import { AxiosResponse } from 'axios'
    import Column from 'primevue/column'
    import DataTable from 'primevue/datatable'
    import DataPreparationDescriptor from './DataPreparationDescriptor.json'
    import Divider from 'primevue/divider'
    import Sidebar from 'primevue/sidebar'

    import Menu from 'primevue/menu'

    import DataPreparationDialog from '@/modules/workspace/dataPreparation/DataPreparationDialog.vue'
    import DataPreparationSaveDialog from '@/modules/workspace/dataPreparation/DataPreparationSaveDialog.vue'
    import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'

    export default defineComponent({
        name: 'data-preparation-detail',
        props: {
            id: Object
        },
        components: { Column, DataPreparationDialog, DataPreparationSaveDialog, DataTable, Divider, Sidebar, Menu },

        data() {
            return {
                descriptor: DataPreparationDescriptor,
                loading: false as boolean,
                datasetData: Array<any>(),
                displayDataPreparationDialog: false as boolean,
                selectedProduct: null,
                visibleRight: false as boolean,
                visibility: false as boolean,
                selectedTransformation: null,
                showSaveDialog: false as boolean,
                columns: [] as IDataPreparationColumn[],
                col: null,
                descriptorTransformations: Array<any>(),
                dataset: {} as any
            }
        },

        async created() {
            this.loading = true
            this.descriptorTransformations = Object.assign([], this.descriptor.transformations)

            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/datasets/' + this.id).then((response: AxiosResponse<any>) => {
                this.dataset = response.data[0]
            })
            if (this.dataset) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/datapreparation/' + this.id + '/datasetinfo').then((response: AxiosResponse<any>) => {
                    this.columns = []
                    response.data.meta.columns
                        .filter((x) => x.pname == 'Type')
                        .forEach((element) => {
                            let obj = {} as IDataPreparationColumn
                            obj.header = element.column
                            obj.type = element.pvalue
                            obj.disabled = false as boolean

                            this.columns.push(obj)
                        })

                    this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/datapreparation/' + this.id + '/preview', this.dataset).then((response: AxiosResponse<any>) => {
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
            getSidebarElementClass(index: number): string {
                let cssClass = 'p-grid p-m-0 p-p-0 p-d-flex kn-flex transformationSidebarElement p-menuitem-link'
                if (index < this.dataset.config.transformations.length - 1) cssClass += ' kn-disabled-text'

                return cssClass
            },
            getTextForSidebar(tr): string {
                let text = ''

                tr.parameters.forEach((element) => {
                    if (text !== '') text += '\n'
                    const keys = Object.keys(element)
                    let first = true
                    keys.forEach((key) => {
                        if (!first) text += '; '
                        text += key + ':' + element[key]
                        first = false
                    })
                })

                return '(' + text + ')'
            },

            getTransformationsMenu(col: IDataPreparationColumn): Array<any> {
                return this.descriptorTransformations
                    .filter((x) => x.editColumn)
                    .filter((x) => {
                        if (x.incompatibleDataTypes) return !x.incompatibleDataTypes?.includes(col.type)
                        return true
                    })
            },
            callFunction(transformation: any, col, type?): void {
                if (transformation.type === 'changeType') {
                    transformation.config.parameters[0][0].value = type
                    let toReturn = { parameters: [] as Array<any>, type: transformation.type }
                    let obj = { columns: [] as Array<any>, type: type }
                    obj.columns.push(col)

                    toReturn.parameters.push(obj)

                    this.handleTransformation(toReturn)
                } else if (transformation.type === 'deleteColumn' && col) {
                    transformation.config.parameters[0][0].value = type
                    let toReturn = { parameters: [] as Array<any>, type: transformation.type }
                    let obj = { columns: [] as Array<any> }
                    obj.columns.push(col)

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
            handleTransformation(t: any): void {
                if (t.type === 'addColumn') {
                    t.parameters[0].columns = [t.parameters[0].columns]
                }

                if (!this.dataset.config) this.dataset.config = {}
                if (!this.dataset.config.transformations) this.dataset.config.transformations = []
                this.dataset.config.transformations.push(t)
                this.loadPreviewData()
            },
            deleteTransformation(index: number): void {
                this.dataset.config.transformations.splice(index, 1)
                this.loadPreviewData()
            },
            getCompatibilityType(col: IDataPreparationColumn): void {
                return this.descriptor.compatibilityMap[col.type].values
            },
            addColumn(item): void {
                console.log(item)
            },
            toggle(event: Event, trOp: string): void {
                // eslint-disable-next-line
                // @ts-ignore
                this.$refs[trOp].toggle(event)
            },
            getMenuForToolbar(): Array<any> {
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
            saveDataset(): void {
                this.showSaveDialog = true
            },
            async loadPreviewData() {
                /* 				await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/dataset/preview', this.selectedTransformation).then((response: AxiosResponse<any>) => {
    	console.log(response)
    	this.selectedTransformation = null
    }) */

                console.log(this.dataset.config.transformations)

                this.selectedTransformation = null
            }
        }
    })
</script>

<style lang="scss">
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

    .customSidebarMenu {
        width: 100% !important;
        border: none !important;
        padding: 0px !important;
    }
</style>
