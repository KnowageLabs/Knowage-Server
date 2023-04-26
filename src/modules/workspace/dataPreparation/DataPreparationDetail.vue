<template>
    <div class="kn-page kn-data-preparation">
        <KnCalculatedField
            v-model:visibility="showCFDialog"
            v-model:template="selectedTransformation"
            :fields="columns"
            :descriptor="cfDescriptor"
            :prop-calc-field-functions="cfDescriptor.availableFunctions"
            :read-only="readOnly"
            :valid="cfType !== ''"
            @save="saveCFDialog"
            @cancel="cancelCFDialog"
            @update:readOnly="updateReadOnly"
        >
            <template #additionalInputs>
                <div class="p-col-4">
                    <span v-if="cfDescriptor.availableTypes" class="p-float-label p-field p-ml-2 kn-flex">
                        <Dropdown
                            v-model="cfType"
                            :options="cfDescriptor.availableTypes"
                            :disabled="readOnly"
                            class="kn-material-input"
                            option-label="label"
                            option-value="code"
                            :class="{
                                'p-invalid': !cfType
                            }"
                        />
                        <label class="kn-material-input-label"> {{ $t('components.knCalculatedField.type') }} </label>
                    </span>
                </div>
            </template>
        </KnCalculatedField>
        <DataPreparationDialog v-model:transformation="selectedTransformation" v-model:col="col" :columns="columns" :read-only="readOnly" @send-transformation="handleTransformation" @update:readOnly="updateReadOnly" />
        <DataPreparationSaveDialog v-model:visibility="showSaveDialog" :original-dataset="dataset" :config="dataset.config" :columns="columns" :instance-id="instanceId" :process-id="processId" :prepared-ds-meta="preparedDsMeta" @update:instanceId="updateInstanceId" @update:processId="updateprocessId" />
        <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
            <template #start> {{ $t('managers.workspaceManagement.dataPreparation.label') }} ({{ $t('managers.workspaceManagement.dataPreparation.originalDataset') }}: {{ dataset.name }})</template>
            <template #end>
                <Button v-tooltip.bottom="$t('common.refresh')" icon="pi pi-refresh" class="p-button-text p-button-rounded p-button-plain" :disabled="loading > 0" @click="refreshOriginalDataset" />
                <Button v-tooltip.bottom="$t('common.save')" icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="loading > 0" @click="saveDataset" />
                <Button v-tooltip.bottom="$t('common.close')" icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate()" /> </template
        ></Toolbar>
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0 toolbarCustomConfig">
            <template #start>
                <template v-for="(menu, index) in getMenuForToolbar()" :key="index">
                    <Button v-if="menu !== 'divider'" v-tooltip.bottom="$t(menu.label)" :class="descriptor.css.buttonClassHeader" :disabled="calculateDisabledProperty(menu)" @click="callFunction(menu)">
                        <span v-if="menu.icon.class" :class="menu.icon.class">{{ menu.icon.name }}</span>
                        <i v-else :class="menu.icon"></i>
                    </Button>
                    <Divider v-else layout="vertical" />
                </template>
            </template>
            <template #end>
                <div class="arrow-button-container">
                    <Button icon="pi pi-arrow-left" :class="descriptor.css.buttonClassHeader" style="overflow: visible" @click="toggleSidebarVisibility()" />
                    <Badge v-if="dataset.config && dataset.config.transformations && dataset.config.transformations.length > 0" class="arrow-badge" :value="dataset.config && dataset.config.transformations && dataset.config.transformations.length"></Badge>
                </div>
            </template>
        </Toolbar>
        <template v-if="loading > 0">
            <template v-if="progressMode === 'indeterminate'">
                <ProgressBar class="kn-progress-bar" :mode="progressMode" :value="getProgressValue()" />
            </template>
            <template v-else><ProgressBar class="kn-progress-bar" :value="getProgressValue()" /> </template>
        </template>
        <Divider class="kn-divider dividerCustomConfig" />
        <div class="kn-page-content p-grid p-m-0 managerDetail">
            <Sidebar v-model:visible="visibleRight" position="right" class="kn-data-preparation-sidenav">
                <div class="info-container">
                    <div class="original-dataset">
                        <i class="fa fa-database p-mr-2"></i><span>{{ $t('managers.workspaceManagement.dataPreparation.originalDataset') }}</span
                        >: {{ dataset.label }}
                    </div>
                </div>

                <div class="titleContainer">
                    <h4 class="kn-truncated">{{ $t('managers.workspaceManagement.dataPreparation.transformations.label') }}</h4>
                </div>
                <Divider class="p-m-0 p-p-0 dividerCustomConfig" />
                <Listbox class="kn-list kn-flex kn-list-no-border-right" :options="reverseTransformations()" option-label="type"
                    ><template #option="slotProps">
                        <div class="p-text-uppercase kn-list-item transformationSidebarElement">
                            <div v-if="slotProps.option.type != 'calculatedField'">{{ slotProps.option.type }} - {{ slotProps.option.parameters[0].columns[0] }}</div>
                            <div v-else>{{ slotProps.option.type }} - {{ slotProps.option.parameters[0].colName }}</div>
                            <div>
                                <Button v-if="slotProps.option.type != 'trim' && slotProps.option.type != 'drop'" v-tooltip="$t('common.preview')" icon="fas fa-eye" :class="descriptor.css.buttonClassHeader" @click="openTransformationDetail(slotProps.option)" />
                                <Button v-if="slotProps.index == 0" v-tooltip="$t('common.delete')" icon="p-jc-end pi pi-trash" :class="descriptor.css.buttonClassHeader" :disabled="loading > 0" @click="deleteTransformation()" />
                            </div>
                        </div> </template
                ></Listbox>
            </Sidebar>
            <DataTable
                ref="dt"
                :value="datasetData"
                class="p-datatable-sm kn-table data-prep-table"
                data-key="id"
                :paginator="datasetData.length > 20"
                :rows="20"
                paginator-template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                breakpoint="960px"
                :current-page-report-template="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                :loading="loading > 0"
                :resizable-columns="true"
                column-resize-mode="expand"
                show-gridlines
                responsive-layout="scroll"
                :scrollable="true"
                scroll-direction="both"
            >
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>

                <Column v-for="(col, colIndex) in columns" :key="colIndex" :field="col.header" :style="{ width: '200px' }">
                    <template #header>
                        <Button v-if="col.fieldType" :class="descriptor.css.buttonClassHeader" @click="toggle($event, 'opType-' + colIndex)">
                            <span v-if="descriptor.roles.filter((x) => x.code === col.fieldType)[0].icon.class" :class="descriptor.roles.filter((x) => x.code === col.fieldType)[0].icon.class">{{ descriptor.roles.filter((x) => x.code === col.fieldType)[0].icon.name }}</span>
                            <i v-else :class="descriptor.roles.filter((x) => x.code === col.fieldType)[0].icon"></i>
                        </Button>
                        <OverlayPanel :ref="'opType-' + colIndex" :popup="true">
                            <span class="p-float-label">
                                <Dropdown v-model="col.fieldType" :options="translateRoles()" option-label="label" option-value="code" class="kn-material-input" />
                            </span>
                        </OverlayPanel>
                        <div class="aliasAndType p-ml-2">
                            <input v-if="col.editing" v-model="col.fieldAlias" class="kn-input-text-sm" type="text" @blur="changeAlias(col)" @keydown.enter="changeAlias(col)" />
                            <span v-else class="kn-clickable" @click="changeAlias(col)">{{ col.fieldAlias }}</span>
                            <span class="kn-list-item-text-secondary kn-truncated roleType">{{ $t(removePrefixFromType(col.Type)) }}</span>
                        </div>
                        <Button icon="pi pi-ellipsis-v" :class="descriptor.css.buttonClassHeader" @click="toggle($event, 'trOpType-' + colIndex)" />
                        <Menu :ref="'trOpType-' + colIndex" :model="getTransformationsMenu(col)" :popup="true">
                            <template #item="{ item }">
                                <span :class="['p-menuitem-link', 'toolbarCustomConfig', descriptor.css.buttonClassHeader]" @click="callFunction(item, col)">
                                    <span v-if="item.icon.class" :class="item.icon.class" class="menu-icon">{{ item.icon.name }}</span>
                                    <i v-else :class="item.icon"></i> <span class="p-ml-2"> {{ $t(item.label) }}</span>
                                </span>
                            </template>
                        </Menu> </template
                    ><template #body="{ data }">
                        <span v-if="col.Type.toLowerCase().includes('time')"> {{ getFormattedDate(data[col.header], { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' }) }}</span>
                        <span v-else-if="col.Type.toLowerCase().includes('date')"> {{ getFormattedDate(data[col.header], { year: 'numeric', month: '2-digit', day: '2-digit' }) }}</span>
                        <span v-else> {{ data[col.header] }}</span>
                    </template></Column
                >
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'

import { AxiosResponse } from 'axios'
import Badge from 'primevue/badge'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import DataPreparationDescriptor from './DataPreparationDescriptor.json'
import Divider from 'primevue/divider'
import Dropdown from 'primevue/dropdown'
import Sidebar from 'primevue/sidebar'
import OverlayPanel from 'primevue/overlaypanel'
import Listbox from 'primevue/listbox'

import Menu from 'primevue/menu'

import DataPreparationDialog from '@/modules/workspace/dataPreparation/DataPreparationDialog.vue'
import DataPreparationSaveDialog from '@/modules/workspace/dataPreparation/DataPreparationSaveDialog.vue'
import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'
import KnCalculatedField from '@/components/functionalities/KnCalculatedField/KnCalculatedField.vue'
import DataPreparationSimpleDescriptor from '@/modules/workspace/dataPreparation/DataPreparationSimple/DataPreparationSimpleDescriptor.json'
import DataPreparationSplitDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationSplitDescriptor.json'
import calculatedFieldDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCalculatedFieldDescriptor.json'

import { Client } from '@stomp/stompjs'
import { formatDateWithLocale } from '@/helpers/commons/localeHelper'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'data-preparation-detail',
    components: { Listbox, KnCalculatedField, Badge, Column, DataPreparationDialog, DataPreparationSaveDialog, DataTable, Divider, Dropdown, OverlayPanel, Sidebar, Menu },
    props: {
        id: Number,
        transformations: Array as PropType<any[]>,
        existingProcessId: String,
        existingInstanceId: String,
        existingDataset: String
    },
    setup() {
        const store = mainStore()
        return { store }
    },

    data() {
        return {
            descriptor: DataPreparationDescriptor,
            loading: 0,
            datasetData: Array<any>(),
            displayDataPreparationDialog: false as boolean,
            selectedProduct: null,
            visibleRight: false as boolean,
            visibility: false as boolean,
            selectedTransformation: null,
            showSaveDialog: false as boolean,
            showCFDialog: false as boolean,
            columns: [] as IDataPreparationColumn[],
            col: null,
            descriptorTransformations: Array<any>(),
            dataset: {} as any,
            simpleDescriptor: DataPreparationSimpleDescriptor,
            splitDescriptor: DataPreparationSplitDescriptor,
            client: {} as any,
            cfDescriptor: calculatedFieldDescriptor,
            instanceId: '' as string,
            processId: '' as string,
            readOnly: false as boolean,
            preparedDsMeta: {},
            progressMode: 'indeterminate',
            cfType: ''
        }
    },
    async created() {
        this.loading++
        this.descriptorTransformations = Object.assign([], this.descriptor.transformations)

        await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/datasets/dataset/id/' + this.id).then((response: AxiosResponse<any>) => {
            this.dataset = response.data[0]
        })
        if (this.dataset) {
            await this.initDsMetadata()
            this.initTransformations()

            const url = new URL(window.location.origin)
            url.protocol = url.protocol.replace('http', 'ws')
            const uri = url + 'knowage-data-preparation/ws?' + import.meta.env.VITE_DEFAULT_AUTH_HEADER + '=' + localStorage.getItem('token')
            this.client = new Client({
                brokerURL: uri,
                connectHeaders: {},
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000
            })

            this.client.onConnect = () => {
                this.client.subscribe(
                    '/user/queue/preview',
                    (message) => {
                        // called when the client receives a STOMP message from the server
                        if (message.body) {
                            this.updateTable(message.body)
                        } else {
                            console.log('got empty message')
                        }
                        this.loading--
                    },
                    {
                        dsId: this.dataset.id
                    }
                )

                this.client.subscribe('/user/queue/error', (error) => {
                    // called when the client receives a STOMP message from the server
                    if (error.body) {
                        const message = JSON.parse(error.body)
                        this.store.setError({ title: 'Error', msg: message.message })
                    } else {
                        this.store.setError({ title: 'Error' })
                    }
                    if (this.dataset.config && this.dataset.config.transformations?.length > 0) this.dataset.config.transformations.splice(-1)
                    this.loading--
                })

                this.client.subscribe(
                    '/user/queue/prepare',
                    (message) => {
                        // called when the client receives a STOMP message from the server
                        if (message.body) {
                            const avroJobResponse = JSON.parse(message.body)
                            if (avroJobResponse.statusOk) this.store.setInfo({ title: 'Dataset prepared successfully' })
                            else this.store.setError({ title: 'Cannot prepare dataset', msg: avroJobResponse.errorMessage })
                            //TODO: refresh data?
                        } else {
                            this.store.setError({ title: 'Websocket error', msg: 'got empty message' })
                        }
                    },
                    {
                        dsId: this.dataset.id
                    }
                )

                if (this.transformations) {
                    this.client.publish({ destination: '/app/preview', headers: { dsId: this.dataset.id }, body: JSON.stringify(this.dataset.config.transformations) })
                }
            }

            this.client.activate()
        }
    },
    unmounted() {
        if (Object.keys(this.client).length > 0) {
            this.client.deactivate()
            this.client = {}
        }
    },

    methods: {
        getFormattedDate(date: any, format: any) {
            return formatDateWithLocale(date, format, true)
        },
        getProgressValue() {
            if (this.dataset.config && this.dataset.config.transformations && this.dataset.config.transformations.length && this.dataset.config.transformations.length > 1) {
                this.progressMode = ''
                const tot = this.dataset.config.transformations.length

                return (100 * (tot - this.loading)) / tot
            }

            this.progressMode = 'indeterminate'

            return 0
        },
        reverseTransformations() {
            if (this.dataset.config && this.dataset.config.transformations) {
                const transformations = [...this.dataset.config.transformations]
                return transformations.reverse()
            }

            return []
        },
        cancelCFDialog(): void {
            this.selectedTransformation = null
            this.showCFDialog = false
            this.cfType = ''
        },
        saveCFDialog(t): void {
            const convertedTransformation = this.convertCFTransformation(t)
            this.handleTransformation(convertedTransformation)
            this.showCFDialog = false
        },
        convertCFTransformation(t) {
            const transformation = { parameters: [] as Array<any>, type: 'calculatedField' }
            const par = { columns: [] as Array<any> }
            Object.keys(t).forEach((key) => {
                if (key === 'column') par.columns.push(t[key].header)
                else par[key] = t[key]
            })
            if (this.cfDescriptor.availableTypes) {
                par['type'] = this.cfType
            }
            transformation.parameters.push(par)
            return transformation
        },
        calculateDisabledProperty(menu): boolean {
            if (this.loading > 0) return true
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
            this.$router.go(-1)
        },
        refreshOriginalDataset(): void {
            // launch avro export job
            this.$http
                .post(
                    import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/data-preparation/prepare/${this.dataset.id}`,
                    {},
                    {
                        headers: {
                            Accept: 'application/json, text/plain, */*',
                            'Content-Type': 'application/json;charset=UTF-8'
                        }
                    }
                )
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('workspace.myData.isPreparing')
                    })
                })
                .catch(() => {})

            // listen on websocket for avro export job to be finished
            this.client.publish({ destination: '/app/prepare', body: this.dataset.id })
        },
        openTransformationDetail(t) {
            this.readOnly = true
            const selectedTransformation = this.descriptorTransformations.filter((x) => x.name == t.type)[0]
            selectedTransformation['parameters'] = []
            const param = t.parameters[0]
            Object.keys(param).forEach((key) => {
                const obj = {}
                obj['name'] = key
                if (key == 'columns') {
                    const value = [] as Array<any>
                    for (let i = 0; i < param[key].length; i++) {
                        const col = this.columns.filter((x) => x.fieldAlias.toUpperCase() === param[key][i].toUpperCase())[0]
                        value.push(col)
                    }
                    obj['value'] = value
                } else {
                    obj['value'] = param[key]
                }

                if (t.type === 'calculatedField' && this.cfDescriptor.availableTypes) {
                    this.cfType = t.parameters[0].type
                }

                selectedTransformation['parameters'].push(obj)
            })

            if (t.type == 'filter' || t.type == 'split') {
                const col = this.columns.filter((x) => x.fieldAlias.toUpperCase() === t.parameters[0].columns[0].toUpperCase())[0]
                this.callFunction(selectedTransformation, col)
            } else this.callFunction(selectedTransformation, undefined)
        },
        getTransformationsMenu(col: IDataPreparationColumn): Array<any> {
            return this.descriptorTransformations
                .filter((x) => x.editColumn && !x.hidden)
                .filter((x) => {
                    if (x.incompatibleDataTypes) return !x.incompatibleDataTypes?.includes(col.Type)
                    return true
                })
        },
        initTransformations(): void {
            if (this.transformations) {
                if (!this.dataset.config) this.dataset.config = {}
                this.dataset.config.transformations = this.transformations
                this.loading++
            }
        },
        async initDsMetadata() {
            if (this.existingProcessId) this.processId = this.existingProcessId
            if (this.existingInstanceId) this.instanceId = this.existingInstanceId
            if (this.existingDataset) {
                const dsMeta = JSON.parse(this.existingDataset)
                const tmp = {}
                tmp['label'] = dsMeta.label
                tmp['name'] = dsMeta.name
                tmp['description'] = dsMeta.description
                tmp['id'] = dsMeta.id
                await this.$http.get(import.meta.env.VITE_DATA_PREPARATION_PATH + '1.0/process/by-destination-data-set/' + dsMeta.id).then((response: AxiosResponse<any>) => {
                    const instance = response.data.instance
                    if (instance.config) {
                        tmp['config'] = instance.config
                    }
                })

                this.preparedDsMeta = tmp
            }
        },
        getColHeader(metadata: Array<any>, idx: number): string {
            const columnMapping = 'column_' + idx
            const toReturn = metadata.filter((x) => x.mappedTo == columnMapping)[0].alias
            return toReturn
        },
        callFunction(transformation: any, col): void {
            if (transformation.name === 'changeType' || transformation.name === 'split') {
                const parsArray = transformation.name === 'changeType' ? this.simpleDescriptor[transformation.name].parameters : this.splitDescriptor.parameters
                for (let i = 0; i < parsArray.length; i++) {
                    const element = parsArray[i]
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
                        const par = this.simpleDescriptor[transformation.name].parameters[0]
                        par.value = col.header
                        transformation.parameters = []
                        transformation.parameters.push(par)
                        const toReturn = { parameters: [] as Array<any>, type: 'drop' }
                        const obj = { columns: [] as Array<any> }
                        obj.columns.push(col.header)

                        toReturn.parameters.push(obj)

                        this.handleTransformation(toReturn)
                    }
                })
            } else {
                this.selectedTransformation = transformation
                if (col) this.col = col.header
                if (transformation.name === 'calculatedField') this.showCFDialog = true
            }
        },
        handleTransformation(t: any): void {
            if (!this.dataset.config) this.dataset.config = {}
            if (!this.dataset.config.transformations) this.dataset.config.transformations = []
            this.dataset.config.transformations.push(t)
            this.loading++
            this.client.publish({ destination: '/app/preview', headers: { dsId: this.dataset.id }, body: JSON.stringify(this.dataset.config.transformations) })
        },
        toggleSidebarVisibility() {
            this.visibleRight = true
        },
        deleteTransformation(index: number): void {
            if (index) this.dataset.config.transformations.splice(index, 1)
            else this.dataset.config.transformations.splice(-1) // remove last element
            this.loading++
            this.client.publish({ destination: '/app/preview', headers: { dsId: this.dataset.id }, body: JSON.stringify(this.dataset.config.transformations) })
        },
        getCompatibilityType(col: IDataPreparationColumn): void {
            return this.descriptor.compatibilityMap[col.Type].values
        },
        toggle(event: Event, trOp: string): void {
            // eslint-disable-next-line
            // @ts-ignore
            const temp = this.$refs[trOp] as any

            temp[0].toggle(event)
        },
        getMenuForToolbar(): Array<any> {
            const tmp = this.descriptorTransformations
                .filter((x) => x.toolbar && !x.hidden)
                .sort(function (a, b) {
                    if (a.position > b.position) return 1
                    if (a.position < b.position) return -1
                    return 0
                })

            const menu = [] as Array<any>
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
        removePrefixFromType(type: string): string {
            const splitted = type.split('.', -1)

            return splitted.length > 0 ? splitted[splitted.length - 1] : splitted[0]
        },
        saveDataset(): void {
            this.showSaveDialog = true
        },
        translateRoles() {
            const translatedRoles = this.descriptor.roles
            translatedRoles.forEach((x) => (x.label = this.$t(x.label)))
            return translatedRoles
        },
        switchEditMode(col) {
            col.edit = !col.edit
        },
        updateReadOnly(state): void {
            this.readOnly = state
        },
        updateInstanceId(iid): void {
            this.instanceId = iid
        },
        updateprocessId(pid): void {
            this.processId = pid
        },
        updateTable(message) {
            const response = JSON.parse(message)
            // set headers
            const metadata = response.metadata.columns
            this.columns = []
            for (let i = 0; i < metadata.length; i++) {
                const obj = {} as IDataPreparationColumn
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
                const obj = {}
                for (let i = 0; i < row.length; i++) {
                    const colHeader = this.getColHeader(metadata, i)
                    obj[colHeader] = row[i]
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
        .p-button {
            padding-left: 20px;
        }
        .arrow-badge {
            position: absolute;
            top: 0;
            left: 25px;
        }
    }

    .managerDetail {
        width: calc(100vw - var(--kn-mainmenu-width));
    }

    .p-datatable.p-datatable-sm.data-prep-table {
        width: 100%;
        .p-datatable-thead {
            tr {
                th {
                    background-color: var(--kn-table-header-background-color);
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
        border: 1px dashed var(--kn-color-borders);
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
.p-column-header-content {
    .p-button {
        min-width: 0;
    }
}

.toolbarCustomConfig {
    background-color: white !important;

    .kn-datapreparation-button {
        min-width: 0;

        span {
            width: 16px;
            height: 16px;
            font-size: 16px;
        }
        i {
            width: 16px;
            height: 16px;
            font-size: 16px;
        }
    }

    &.kn-datapreparation-button {
        min-width: 0;

        .menu-icon {
            width: 16px;
            height: 16px;
            font-size: 16px;
        }
        i {
            width: 16px;
            height: 16px;
            font-size: 16px;
        }
    }
}
.dividerCustomConfig {
    border: 1px solid;
    border-color: var(--kn-color-borders);
}
.p-overlaypanel-content {
    padding: 0px !important;
}
.transformationDescription {
    color: var(--kn-list-item-text-secondary-color);
    font-size: var(--kn-list-item-text-secondary-font-size);
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
    font-size: 0.75em;
    justify-content: space-between !important;
    padding: 0 !important;
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

.titleContainer {
    display: flex;
    justify-content: center;
    width: 100%;
}

.aliasAndType {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
