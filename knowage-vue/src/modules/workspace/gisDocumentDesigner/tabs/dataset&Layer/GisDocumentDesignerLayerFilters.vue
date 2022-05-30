<template>
    <Card class="p-mt-3">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #start>{{ $t('workspace.gis.dnl.layerFilters') }}</template>
                <template #end>
                    <Button class="p-button-link" :label="$t('workspace.gis.dnl.addFilters')" @click="driverDialogVisible = true" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable class="georef-step1-table p-datatable-sm kn-table" :value="documentData.selectedDriver" dataKey="id" responsiveLayout="scroll" breakpoint="600px" v-model:filters="mainFilters" :globalFilterFields="globalFilterFields">
                <template #empty>
                    {{ $t('workspace.gis.dnl.driversTableEmpty') }}
                </template>
                <template #header>
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="mainFilters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </template>
                <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true">
                    <template #body="{data}">
                        <span class="kn-truncated" v-tooltip.top="data[col.field]">{{ data[col.field] }}</span>
                    </template>
                </Column>
                <Column v-if="!isDatasetChosen" :style="styleDescriptor.style.trashColumn" @rowClick="false">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteselectedDriver(slotProps)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>

    <Dialog class="p-fluid kn-dialog--toolbar--primary" :style="styleDescriptor.style.dialogSize" v-if="driverDialogVisible" :visible="driverDialogVisible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>{{ $t('workspace.gis.dnl.layerList') }}</template>
            </Toolbar>
        </template>
        <DataTable class="p-datatable-sm kn-table" :value="documentData.allDrivers" v-model:selection="selectedDriversList" dataKey="id" responsiveLayout="scroll" v-model:filters="multipleFilters" :globalFilterFields="globalFilterFields">
            <template #empty>
                {{ $t('workspace.gis.dnl.layersDialogEmpty') }}
            </template>
            <template #header>
                <span id="search-container" class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" v-model="multipleFilters['global'].value" :placeholder="$t('common.search')" />
                </span>
            </template>
            <Column selectionMode="multiple" />
            <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field">
                <template #body="{data}">
                    <span class="kn-truncated" v-tooltip.top="data[col.field]">{{ data[col.field] }}</span>
                </template>
            </Column>
        </DataTable>
        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--secondary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="saveLayerSelection"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDriver } from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesigner'
import { filterDefault } from '@/helpers/commons/filterHelper'
import descriptor from './GisDocumentDesignerDataset&LayerDescriptor.json'
import styleDescriptor from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesignerDescriptor.json'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'

const deepcopy = require('deepcopy')

export default defineComponent({
    components: {
        DataTable,
        Column,
        Dialog
    },
    emits: ['driverChanged'],
    props: { documentDataProp: { type: Object as any, required: false }, isDatasetChosen: Boolean },
    computed: {},
    data() {
        return {
            descriptor,
            styleDescriptor,
            columns: descriptor.driverColumns,
            globalFilterFields: descriptor.layerFilterFields,
            mainFilters: { global: [filterDefault] } as Object,
            singleFilters: { global: [filterDefault] } as Object,
            multipleFilters: { global: [filterDefault] } as Object,
            driverDialogVisible: false,
            selectedDriver: {} as iDriver,
            selectedDriversList: [] as iDriver[],
            documentData: {} as any
        }
    },
    created() {
        this.documentData = this.documentDataProp
        this.selectedDriversList = deepcopy(this.documentDataProp.selectedDriver)
    },

    methods: {
        closeDialog() {
            this.selectedDriver = {} as iDriver
            this.selectedDriversList = deepcopy(this.documentDataProp.selectedDriver)
            this.driverDialogVisible = false
        },
        saveLayerSelection() {
            this.$emit('driverChanged', this.selectedDriversList)
            this.driverDialogVisible = false
        },
        deleteselectedDriver(eventData) {
            this.documentData.selectedDriver.splice(eventData.index, 1)
        }
    }
})
</script>
