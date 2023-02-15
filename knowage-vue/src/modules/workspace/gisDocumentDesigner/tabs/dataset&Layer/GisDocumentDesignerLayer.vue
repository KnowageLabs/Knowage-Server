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
            <DataTable v-model:filters="mainFilters" class="georef-step1-table p-datatable-sm kn-table" :value="documentData.selectedLayer" data-key="layerId" responsive-layout="scroll" breakpoint="600px" :global-filter-fields="globalFilterFields">
                <template #empty>
                    {{ $t('workspace.gis.dnl.layersTableEmpty') }}
                </template>
                <template #header>
                    <span v-if="!isDatasetChosen" id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText v-model="mainFilters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                    </span>
                </template>
                <Column v-for="col of columns" :key="col.field" :field="col.field" :header="$t(col.header)" :sortable="true">
                    <template #body="{data}">
                        <span v-tooltip.top="data[col.field]" class="kn-truncated">{{ data[col.field] }}</span>
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

    <Dialog v-if="layerDialogVisible" class="p-fluid kn-dialog--toolbar--primary" :style="styleDescriptor.style.dialogSize" :visible="layerDialogVisible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>{{ $t('workspace.gis.dnl.layerList') }}</template>
            </Toolbar>
        </template>
        <DataTable v-if="isDatasetChosen" v-model:selection="selectedLayer" v-model:filters="singleFilters" class="p-datatable-sm kn-table" :value="documentData.allLayers" data-key="layerId" responsive-layout="scroll" :global-filter-fields="globalFilterFields" selection-mode="single">
            <template #empty>
                {{ $t('workspace.gis.dnl.layersDialogEmpty') }}
            </template>
            <template #header>
                <span id="search-container" class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText v-model="singleFilters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                </span>
            </template>
            <Column v-for="col of columns" :key="col.field" :field="col.field" :header="$t(col.header)">
                <template #body="{data}">
                    <span v-tooltip.top="data[col.field]" class="kn-truncated">{{ data[col.field] }}</span>
                </template>
            </Column>
        </DataTable>
        <DataTable v-else v-model:selection="selectedLayersList" v-model:filters="multipleFilters" class="p-datatable-sm kn-table" :value="documentData.allLayers" data-key="layerId" responsive-layout="scroll" :global-filter-fields="globalFilterFields">
            <template #empty>
                {{ $t('workspace.gis.dnl.layersDialogEmpty') }}
            </template>
            <template #header>
                <span id="search-container" class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText v-model="multipleFilters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                </span>
            </template>
            <Column selection-mode="multiple" />
            <Column v-for="col of columns" :key="col.field" :field="col.field" :header="$t(col.header)">
                <template #body="{data}">
                    <span v-tooltip.top="data[col.field]" class="kn-truncated">{{ data[col.field] }}</span>
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
    props: { documentDataProp: { type: Object as any, required: false }, isDatasetChosen: Boolean },
    emits: ['layerChanged'],
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
    computed: {},
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
