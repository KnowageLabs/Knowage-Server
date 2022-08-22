<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #start>{{ $t('managers.layersManagement.layerTitle') }}</template>
                <template #end>
                    <Button v-if="documentData.selectedLayer.length === 0" class="p-button-link" :label="$t('workspace.gis.dnl.addLayer')" @click="showLayerDialogConfirm" />
                    <Button v-else class="p-button-link" :label="$t('workspace.gis.dnl.changeLayer')" @click="showLayerDialogConfirm" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable class="georef-step1-table p-datatable-sm kn-table" :value="documentData.selectedLayer" dataKey="layerId" responsiveLayout="scroll" breakpoint="600px" v-model:filters="mainFilters" :globalFilterFields="globalFilterFields">
                <template #empty>
                    {{ $t('workspace.gis.dnl.layersTableEmpty') }}
                </template>
                <template #header>
                    <span v-if="!isDatasetChosen" id="search-container" class="p-input-icon-left p-mr-3">
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
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteSelectedLayer(slotProps)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>

    <Dialog class="p-fluid kn-dialog--toolbar--primary" :style="styleDescriptor.style.dialogSize" v-if="layerDialogVisible" :visible="layerDialogVisible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>{{ $t('workspace.gis.dnl.layerList') }}</template>
            </Toolbar>
        </template>
        <DataTable v-if="isDatasetChosen" class="p-datatable-sm kn-table" :value="documentData.allLayers" v-model:selection="selectedLayer" dataKey="layerId" responsiveLayout="scroll" v-model:filters="singleFilters" :globalFilterFields="globalFilterFields" selectionMode="single">
            <template #empty>
                {{ $t('workspace.gis.dnl.layersDialogEmpty') }}
            </template>
            <template #header>
                <span id="search-container" class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" v-model="singleFilters['global'].value" :placeholder="$t('common.search')" />
                </span>
            </template>
            <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field">
                <template #body="{data}">
                    <span class="kn-truncated" v-tooltip.top="data[col.field]">{{ data[col.field] }}</span>
                </template>
            </Column>
        </DataTable>
        <DataTable v-else class="p-datatable-sm kn-table" :value="documentData.allLayers" v-model:selection="selectedLayersList" dataKey="layerId" responsiveLayout="scroll" v-model:filters="multipleFilters" :globalFilterFields="globalFilterFields">
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
import { iLayer } from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesigner'
import { filterDefault } from '@/helpers/commons/filterHelper'
import descriptor from './GisDocumentDesignerDataset&LayerDescriptor.json'
import styleDescriptor from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesignerDescriptor.json'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'

import deepcopy from 'deepcopy'

export default defineComponent({
    components: {
        DataTable,
        Column,
        Dialog
    },
    emits: ['layerChanged'],
    props: { documentDataProp: { type: Object as any, required: false }, isDatasetChosen: Boolean },
    computed: {},
    data() {
        return {
            descriptor,
            styleDescriptor,
            columns: descriptor.layerColumns,
            globalFilterFields: descriptor.layerFilterFields,
            mainFilters: { global: [filterDefault] } as Object,
            singleFilters: { global: [filterDefault] } as Object,
            multipleFilters: { global: [filterDefault] } as Object,
            layerDialogVisible: false,
            selectedLayer: {} as iLayer,
            selectedLayersList: [] as iLayer[],
            documentData: {} as any
        }
    },
    created() {
        this.documentData = this.documentDataProp
    },

    methods: {
        showLayerDialogConfirm() {
            if (this.isDatasetChosen && this.documentData.dsJoins.length > 0) {
                this.$confirm.require({
                    header: this.$t('common.toast.warning'),
                    message: this.$t('workspace.gis.dnl.changeLayerMessage'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.showLayerDialog()
                    }
                })
            } else {
                this.showLayerDialog()
            }
        },
        showLayerDialog() {
            this.isDatasetChosen ? (this.selectedLayer = deepcopy(this.documentData.selectedLayer[0])) : (this.selectedLayer = deepcopy(this.documentData.selectedLayer))
            this.layerDialogVisible = true
        },
        closeDialog() {
            this.selectedLayer = {} as iLayer
            this.selectedLayersList = [] as iLayer[]
            this.layerDialogVisible = false
        },
        saveLayerSelection() {
            this.isDatasetChosen ? (this.documentData.selectedLayer = deepcopy([this.selectedLayer])) : (this.documentData.selectedLayer = deepcopy(this.selectedLayersList))
            this.documentData.dsJoins = []
            this.$emit('layerChanged', this.isDatasetChosen ? [this.selectedLayer] : this.selectedLayersList)
            this.layerDialogVisible = false
        },
        deleteSelectedLayer(eventData) {
            this.documentData.selectedLayer.splice(eventData.index, 1)
        }
    }
})
</script>
