<template>
    <div class="kn-page kn-data-preparation">
        <DataPreparationDialog v-model:transformation="selectedTransformation" @send-transformation="handleTransformation" :columns="columns" v-model:col="col" />
        <DataPreparationSaveDialog v-model:visibility="showSaveDialog" v-model:dataset="dataset" />
        <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
            <template #start> {{ $t('managers.workspaceManagement.dataPreparation.label') }} ({{ $t('managers.workspaceManagement.dataPreparation.originalDataset') }}: {{ dataset.label }})</template>
            <template #end>
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" @click="saveDataset" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="closeTemplate()" /> </template
        ></Toolbar>
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0 toolbarCustomConfig">
            <template #start>
                <span v-for="(menu, index) in getMenuForToolbar()" v-bind:key="index">
                    <Button v-if="menu !== 'divider'" :class="descriptor.css.buttonClassHeader" v-tooltip.bottom="$t(menu.label)" @click="callFunction(menu)" :disabled="calculateDisabledProperty(menu)">
                        <span v-if="menu.icon.class" :class="menu.icon.class">{{ menu.icon.name }}</span>
                        <i v-else :class="menu.icon"></i>
                    </Button>
                    <Divider v-else layout="vertical" />
                </span>
            </template>
            <template #end>
                <div class="arrow-button-container">
                    <Button icon="pi pi-arrow-left" :class="descriptor.css.buttonClassHeader" style="overflow: visible" @click="visibleRight = true" />
                    <Badge class="arrow-badge" v-if="dataset.config && dataset.config.transformations && dataset.config.transformations.length > 0" :value="dataset.config && dataset.config.transformations && dataset.config.transformations.length"></Badge>
                </div>
            </template>
        </Toolbar>
        <Divider class="kn-divider" />
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <div class="kn-page-content p-grid p-m-0 managerDetail">
            <Sidebar v-model:visible="visibleRight" position="right" class="kn-data-preparation-sidenav">
                <div class="info-container">
                    <div class="original-dataset">
                        <i class="fa fa-database"></i><span>{{ $t('managers.workspaceManagement.dataPreparation.originalDataset') }}</span
                        >: {{ dataset.label }}
                    </div>
                    <div class="original-dataset" v-if="dataset.refreshRate">
                        <i class="fas fa-stopwatch"></i><span>{{ $t('managers.workspaceManagement.dataPreparation.dataset.refreshRate.label') }}</span
                        >: {{ dataset.refreshRate }}
                    </div>
                </div>
                <Divider class="p-m-0 p-p-0 dividerCustomConfig" />
                <div class="kn-truncated">{{ $t('managers.workspaceManagement.dataPreparation.transformations.label') }}</div>

                <div v-if="dataset.config && dataset.config.transformations && dataset.config.transformations.length > 0" class="sidebarClass">
                    <div v-for="(tr, index) in dataset.config.transformations.reverse()" v-bind:key="index" :class="getSidebarElementClass(index)" class="sidenav-transformation">
                        <span class="transformation-icon" :class="descriptorTransformations.filter((x) => x.name === tr.type)[0].icon.class" v-if="descriptorTransformations.filter((x) => x.name === tr.type)[0].icon.class">{{
                            descriptorTransformations.filter((x) => x.name === tr.type)[0].icon.name
                        }}</span>
                        <i v-else class="transformation-icon" :class="descriptorTransformations.filter((x) => x.name === tr.type)[0].icon"></i>

                        <span class="typeAndDescription kn-truncated kn-flex">
                            <span class="kn-list-item" :title="$t(descriptorTransformations.filter((x) => x.name === tr.type)[0].label)">{{ $t(descriptorTransformations.filter((x) => x.name === tr.type)[0].label) }} </span>
                            <span class="transformationDescription kn-truncated" :title="getTextForSidebar(tr)">
                                {{ getTextForSidebar(tr) }}
                            </span>
                        </span>

                        <Button v-if="index == 0" icon="p-jc-end pi pi-trash" :class="descriptor.css.buttonClassHeader" @click="deleteTransformation(index)" v-tooltip="$t('common.delete')" />
                    </div>
                </div>
            </Sidebar>
            <DataTable
                ref="dt"
                :value="datasetData"
                class="p-datatable-sm kn-table data-prep-table"
                dataKey="id"
                :paginator="datasetData.length > 20"
                :rows="20"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                breakpoint="960px"
                :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                :loading="loading"
                :resizableColumns="true"
                columnResizeMode="expand"
                showGridlines
                responsiveLayout="scroll"
                :scrollable="true"
                scrollDirection="both"
            >
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>

                <Column v-for="(col, colIndex) in columns" :field="col.header" :key="colIndex" :style="{ width: '200px' }">
                    <template #header>
                        <Button v-if="col.fieldType" :class="descriptor.css.buttonClassHeader" @click="toggle($event, 'opType-' + colIndex)">
                            <i :class="descriptor.roles.filter((x) => x.code === col.fieldType)[0].icon"></i>
                        </Button>
                        <OverlayPanel :ref="'opType-' + colIndex" :popup="true">
                            <span class="p-float-label">
                                <Dropdown v-model="col.fieldType" :options="translateRoles()" optionLabel="label" optionValue="code" class="kn-material-input" />
                            </span>
                        </OverlayPanel>
                        <div style="display: flex; flex-direction: column; flex:1;">
                            <input class="kn-input-text-sm" type="text" v-model="col.fieldAlias" v-if="col.editing" @blur="changeAlias(col)" @keydown.enter="changeAlias(col)" />
                            <span v-else class="kn-clickable" @click="changeAlias(col)">{{ col.fieldAlias }}</span>
                            <span class="kn-list-item-text-secondary kn-truncated roleType">{{ $t(removePrefixFromType(col.Type)) }}</span>
                        </div>
                        <Button icon="pi pi-ellipsis-v" :class="descriptor.css.buttonClassHeader" @click="toggle($event, 'trOpType-' + colIndex)" />
                        <Menu :model="getTransformationsMenu(col)" :ref="'trOpType-' + colIndex" :popup="true">
                            <template #item="{item}">
                                <span class="p-menuitem-link" @click="callFunction(item, col)">
                                    <span :class="item.icon.class" v-if="item.icon.class">{{ item.icon.name }}</span>
                                    <i v-else :class="item.icon"></i> <span class="p-ml-2"> {{ $t(item.label) }}</span></span
                                >
                            </template>
                        </Menu>
                    </template></Column
                >
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

import { AxiosResponse } from 'axios'
import Badge from 'primevue/badge'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import DataPreparationDescriptor from './DataPreparationDescriptor.json'
import Divider from 'primevue/divider'
import Dropdown from 'primevue/dropdown'
import Sidebar from 'primevue/sidebar'
import OverlayPanel from 'primevue/overlaypanel'

import Menu from 'primevue/menu'

import DataPreparationDialog from '@/modules/workspace/dataPreparation/DataPreparationDialog.vue'
import DataPreparationSaveDialog from '@/modules/workspace/dataPreparation/DataPreparationSaveDialog.vue'
import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'

import DataPreparationSimpleDescriptor from '@/modules/workspace/dataPreparation/DataPreparationSimple/DataPreparationSimpleDescriptor.json'
import DataPreparationCustomDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationCustomDescriptor.json'


import { Client } from '@stomp/stompjs';

export default defineComponent({
    name: 'data-preparation-detail',
    props: {
        id: Object
    },
    components: { Badge, Column, DataPreparationDialog, DataPreparationSaveDialog, DataTable, Divider, Dropdown, OverlayPanel, Sidebar, Menu },

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
            dataset: {} as any,
            simpleDescriptor: DataPreparationSimpleDescriptor,
            customDescriptor: DataPreparationCustomDescriptor,
            client: {} as any
        }
    },

    async created() {
        this.loading = true
        this.descriptorTransformations = Object.assign([], this.descriptor.transformations)

        await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/datasets/' + this.id).then((response: AxiosResponse<any>) => {
            this.dataset = response.data[0]
        })
        if (this.dataset) {
            await this.initWebsocket()

            this.client.onConnect = (frame)=> {
                // Do something, all subscribes must be done is this callback
                // This is needed because this will be executed after a (re)connect
                console.log(frame);

                this.client.subscribe("/user/queue/preview",(message)=> {
                    // called when the client receives a STOMP message from the server
                    if (message.body) {
                     this.updateTable(message.body)
                    } else {
                        console.log("got empty message");
                    }
                    this.loading = false
                },
                {
                    "dsLabel": this.dataset.label
                });
                
                this.client.subscribe("/user/queue/error", (error) => {
                    // called when the client receives a STOMP message from the server
                    if (error.body) {
                        let message = JSON.parse(error.body)
                        this.$store.commit('setError', { title: "Error", msg: message.message })
                    } else {
                        this.$store.commit('setError', { title: "Error"})
                    }
                    this.loading = false
                });
            };

            this.client.onStompError = function (frame) {
                // Will be invoked in case of error encountered at Broker
                // Bad login/passcode typically will cause an error
                // Complaint brokers will set `message` header with a brief message. Body may contain details.
                // Compliant brokers will terminate the connection after any error
                console.log('Broker reported error: ' + frame.headers['message']);
                console.log('Additional details: ' + frame.body);
            };
            this.client.activate();
        }
    },
    methods: {
        calculateDisabledProperty(menu): Boolean {
            let disabled = false
            if (menu.type === 'advancedFilter') {
                if (!this.dataset.config) disabled = true
                else disabled = this.dataset.config.transformations.filter((x) => x.type === 'filter').length < 2
            }
            return disabled
        },
        changeAlias(col): void {
            if (col.editing) delete col.editing
            else col.editing = true
        },
        closeTemplate(): void {
            this.$router.push({ name: 'data-preparation' })
        },
        getSidebarElementClass(index: number): string {
            let cssClass = 'p-grid p-m-0 p-p-0 p-d-flex kn-flex transformationSidebarElement p-menuitem-link'
            if (index > 0) cssClass += ' kn-disabled-text'

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
                    if (x.incompatibleDataTypes) return !x.incompatibleDataTypes?.includes(col.Type)
                    return true
                })
        },
        initWebsocket(): void {
            let url = process.env.VUE_APP_HOST_URL.replace('http', 'ws') + '/knowage-data-preparation/ws?' + process.env.VUE_APP_DEFAULT_AUTH_HEADER + '=' + localStorage.getItem('token')
            this.client = new Client({
                brokerURL: url,
                connectHeaders: {
                },
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });
        },
        getColHeader(metadata: Array<any>, idx: Number): string {
            let columnMapping = 'Column_' + idx
            let toReturn = metadata.filter((x) => x.mappedTo == columnMapping)[0].alias
            return toReturn
        },
        callFunction(transformation: any, col): void {
            if (transformation.name === 'changeType' || transformation.name === 'splitColumn') {
                let parsArray = transformation.name === 'changeType' ? this.simpleDescriptor[transformation.name].parameters : this.customDescriptor[transformation.name].parameters
                for (var i = 0; i < parsArray.length; i++) {
                    let element = parsArray[i]
                    if (element.name === 'destType' || element.name === 'destType1' || element.name === 'destType2') {
                        element.availableOptions = col ? this.getCompatibilityType(col) : this.descriptor.compatibilityMap['all'].values

                        element.availableOptions.forEach((element) => {
                            element.label = this.removePrefixFromType(element.label)
                        })
                    }
                }

                /* this.handleTransformation(transformation) */
                this.selectedTransformation = transformation
                if (col) this.col = col.header
            } else if (transformation.name === 'drop' && col) {
                this.$confirm.require({
                    message: this.$t('common.toast.deleteMessage'),
                    header: this.$t('common.toast.deleteTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        let par = this.simpleDescriptor[transformation.name].parameters[0]
                        par.value = col.header
                        transformation.parameters = []
                        transformation.parameters.push(par)
                        let toReturn = { parameters: [] as Array<any>, type: 'drop' }
                        let obj = { columns: [] as Array<any> }
                        obj.columns.push(col.header)

                        toReturn.parameters.push(obj)

                        this.handleTransformation(toReturn)
                    }
                })
            } else {
                this.selectedTransformation = transformation
                if (col) this.col = col.header
            }
        },
        handleTransformation(t: any): void {
            if (!this.dataset.config) this.dataset.config = {}
            if (!this.dataset.config.transformations) this.dataset.config.transformations = []

            if (t.type === 'addColumn') {
                t.parameters[0].columns = [t.parameters[0].columns]
            }

            this.dataset.config.transformations.push(t)
            this.loading = true
            this.client.publish({ destination: "/app/preview", headers: {"dsLabel": this.dataset.label},body: JSON.stringify(this.dataset.config.transformations)});
        },
        deleteTransformation(index: number): void {
            this.dataset.config.transformations.splice(index, 1)
            this.loading = true
            this.client.publish({ destination: "/app/preview", headers: {"dsLabel": this.dataset.label},body: JSON.stringify(this.dataset.config.transformations)});
        },
        getCompatibilityType(col: IDataPreparationColumn): void {
            return this.descriptor.compatibilityMap[col.Type].values
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
        removePrefixFromType(type: String): String {
            let splitted = type.split('.', -1)

            return splitted.length > 0 ? splitted[splitted.length - 1] : splitted[0]
        },
        saveDataset(): void {
            this.showSaveDialog = true
        },
        translateRoles() {
            let translatedRoles = this.descriptor.roles
            translatedRoles.forEach((x) => (x.label = this.$t(x.label)))
            return translatedRoles
        },
        switchEditMode(col) {
            col.edit = !col.edit
        },
        updateTable(message) {
               let response = JSON.parse(message);
                        // set headers
                        let metadata = response.metadata.columns
                        this.columns = []
                        for (let i = 0; i < metadata.length; i++) {
                            let obj = {} as IDataPreparationColumn
                            obj.Type = metadata[i].type
                            obj.disabled = false
                            obj.fieldAlias = metadata[i].alias
                            obj.fieldType = metadata[i].fieldType
                            obj.header = metadata[i].name
                            this.columns.push(obj)
                        }
                        //set data rows
                        this.datasetData = []
                        response.rows.forEach((row) => {
                            let obj = {}
                            for (let i = 0; i < row.length; i++) {
                                let colHeader = this.getColHeader(metadata, i)
                                obj[colHeader] = row[i];
                            }
                            this.datasetData.push(obj)
                        })
        }
    }
})
</script>

<style lang="scss">
.kn-data-preparation {
    .arrow-button-container {
        position: relative;
        left: -10px;
        .arrow-badge {
            position: absolute;
            top: 0;
            left: 25px;
        }
    }

    .managerDetail {
        width: calc(100vw - $mainmenu-width);
    }

    .p-datatable.p-datatable-sm.data-prep-table {
        width: 100%;
        .p-datatable-thead {
            tr {
                th {
                    background-color: $table-header-background-color;
                }
            }
        }
        .p-column-header-content {
            flex: 1;
            .p-button.p-button-icon-only.p-button-rounded {
                min-width: 2.25rem;
            }
        }
        .p-datatable-tbody > tr > td {
            padding: 0.1rem;
            font-size: 0.9rem;
        }
    }
}
.kn-data-preparation-sidenav {
    .info-container {
        border: 1px dashed $color-borders;
        border-radius: 4px;
        padding: 4px;
        margin-bottom: 8px;
        .original-dataset {
            height: 32px;
            justify-content: flex-start;
            align-items: center;
            display: flex;
            span {
                margin-left: 4px;
                text-transform: uppercase;
                font-size: 0.9rem;
            }
        }
    }

    .sidenav-transformation {
        display: flex;
        width: 100%;
        align-items: center;
        .transformation-icon {
            min-width: 24px;
        }
    }
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

.customSidebarMenu {
    width: 100% !important;
    border: none !important;
    padding: 0px !important;
}

.roleType {
    font-size: 0.67em;
}
.sidebarClass {
    flex-direction: column-reverse;
}
</style>
