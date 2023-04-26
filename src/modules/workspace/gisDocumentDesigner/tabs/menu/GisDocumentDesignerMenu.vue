<template>
    <Card class="p-mb-3">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #start>{{ $t('common.menu') }}</template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-grid p-m-2">
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showRightConfigMenu" v-model="visibilityControls.showRightConfigMenu" class="p-mr-2" :binary="true" />
                    <label for="showRightConfigMenu">{{ $t('workspace.gis.menu.showRightConfigMenu') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showLegendButton" v-model="visibilityControls.showLegendButton" class="p-mr-2" :binary="true" />
                    <label for="showLegendButton">{{ $t('workspace.gis.menu.showLegendButton') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showDistanceCalculator" v-model="visibilityControls.showDistanceCalculator" class="p-mr-2" :binary="true" />
                    <label for="showDistanceCalculator">{{ $t('workspace.gis.menu.showDistanceCalculator') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showDownloadButton" v-model="visibilityControls.showDownloadButton" class="p-mr-2" :binary="true" />
                    <label for="showDownloadButton">{{ $t('workspace.gis.menu.showDownloadButton') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showSelectMode" v-model="visibilityControls.showSelectMode" class="p-mr-2" :disabled="!visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showSelectMode">{{ $t('workspace.gis.menu.showSelectMode') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showLayer" v-model="visibilityControls.showLayer" class="p-mr-2" :disabled="!visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showLayer">{{ $t('workspace.gis.menu.showLayer') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showBaseLayer" v-model="visibilityControls.showBaseLayer" class="p-mr-2" :disabled="!visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showBaseLayer">{{ $t('workspace.gis.menu.showBaseLayer') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showMapConfig" v-model="visibilityControls.showMapConfig" class="p-mr-2" :disabled="!visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showMapConfig">{{ $t('workspace.gis.menu.showMapConfig') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="crossNavigationMultiselect" v-model="crossNavigationMultiselect" class="p-mr-2" :disabled="crossDisabled" :binary="true" />
                    <label for="crossNavigationMultiselect">{{ $t('workspace.gis.menu.crossNavigationMultiselect') }}</label>
                </div>
            </div>
        </template>
    </Card>
    <Toolbar v-if="documentData.selectedDataset.length > 0" class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
        <template #start>{{ $t('common.filters') }}</template>
        <template #end> <Button class="p-button-link" :label="$t('workspace.gis.dsj.addFilter')" @click="addFilterRow" /> </template>
    </Toolbar>
    <div v-if="documentData.selectedDataset.length > 0" id="informations-content" class="kn-flex kn-relative kn-height-full">
        <div :style="styleDescriptor.style.absoluteScroll">
            <Card>
                <template #content>
                    <DataTable class="p-datatable-sm kn-table georef-step1-table" :value="documentDataProp.filters" data-key="id" responsive-layout="scroll" breakpoint="600px">
                        <template #empty>
                            {{ $t('workspace.gis.dnl.emptyInfo') }}
                        </template>
                        <Column field="name" :header="$t('workspace.gis.dsj.dsJoinCol')" :sortable="true">
                            <template #body="slotProps">
                                <Dropdown id="dsJoinCol" v-model="slotProps.data.name" class="kn-material-input kn-width-full" :options="documentDataProp.datasetJoinColumns" option-label="id" option-value="id" :class="{ 'p-invalid': slotProps.data.name == null }" />
                                <small v-if="slotProps.data.name == null" for="dsJoinCol" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
                            </template>
                        </Column>
                        <Column field="label" :header="$t('workspace.gis.dsj.lyrJoinCol')" :sortable="true">
                            <template #body="slotProps">
                                <InputText id="label" v-model="slotProps.data.label" class="kn-material-input kn-width-full" :class="{ 'p-invalid': slotProps.data.label == null || slotProps.data.label == '' }" />
                                <small v-if="slotProps.data.label == null || slotProps.data.label == ''" for="label" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
                            </template>
                        </Column>
                        <Column :style="styleDescriptor.style.trashColumn">
                            <template #body="slotProps">
                                <Button icon="pi pi-trash" class="p-button-link" @click="deleteFilterRow(slotProps)" />
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </Card>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import styleDescriptor from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesignerDescriptor.json'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    components: { Checkbox, Column, DataTable, Dropdown },
    props: {
        documentDataProp: { type: Object as any, required: false }
    },
    emits: ['filtersValidationChanged'],
    data() {
        return {
            styleDescriptor,
            documentData: {} as any,
            visibilityControls: {} as any,
            crossNavigationMultiselect: null
        }
    },
    computed: {
        crossDisabled(): any {
            return !this.visibilityControls.showRightConfigMenu || !this.documentDataProp.visibilityData.crossNavigation
        },
        filtersInvalid() {
            if (this.filtersContainEmptyFields) {
                return true
            } else return false
        },
        filtersContainEmptyFields() {
            let value = false
            this.documentDataProp.filters.forEach((field) => {
                if (!field.name || !field.label || field.label === '') {
                    value = true
                } else value = false
            })
            return value
        }
    },
    watch: {
        documentDataProp() {
            this.documentData = this.documentDataProp
        },
        filtersInvalid() {
            this.$emit('filtersValidationChanged', 'filtersInvalid', this.filtersInvalid)
        }
    },
    created() {
        this.documentData = this.documentDataProp
        this.visibilityControls = this.documentDataProp.visibilityData.visibilityControls
        this.crossNavigationMultiselect = this.documentDataProp.visibilityData.crossNavigationMultiselect
        this.$emit('filtersValidationChanged', 'filtersInvalid', this.filtersInvalid)
    },
    methods: {
        addFilterRow() {
            this.documentData.filters.push({ name: null, label: null })
        },
        deleteFilterRow(eventData) {
            this.documentData.filters.splice(eventData.index, 1)
        }
    }
})
</script>
