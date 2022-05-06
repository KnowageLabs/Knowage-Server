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
                    <Checkbox id="showRightConfigMenu" class="p-mr-2" v-model="visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showRightConfigMenu">{{ $t('workspace.gis.menu.showRightConfigMenu') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showLegendButton" class="p-mr-2" v-model="visibilityControls.showLegendButton" :binary="true" />
                    <label for="showLegendButton">{{ $t('workspace.gis.menu.showLegendButton') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showDistanceCalculator" class="p-mr-2" v-model="visibilityControls.showDistanceCalculator" :binary="true" />
                    <label for="showDistanceCalculator">{{ $t('workspace.gis.menu.showDistanceCalculator') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showDownloadButton" class="p-mr-2" v-model="visibilityControls.showDownloadButton" :binary="true" />
                    <label for="showDownloadButton">{{ $t('workspace.gis.menu.showDownloadButton') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showSelectMode" class="p-mr-2" v-model="visibilityControls.showSelectMode" :disabled="!visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showSelectMode">{{ $t('workspace.gis.menu.showSelectMode') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showLayer" class="p-mr-2" v-model="visibilityControls.showLayer" :disabled="!visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showLayer">{{ $t('workspace.gis.menu.showLayer') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showBaseLayer" class="p-mr-2" v-model="visibilityControls.showBaseLayer" :disabled="!visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showBaseLayer">{{ $t('workspace.gis.menu.showBaseLayer') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="showMapConfig" class="p-mr-2" v-model="visibilityControls.showMapConfig" :disabled="!visibilityControls.showRightConfigMenu" :binary="true" />
                    <label for="showMapConfig">{{ $t('workspace.gis.menu.showMapConfig') }}</label>
                </div>
                <div class="p-col-12 p-md-6 p-lg-3">
                    <Checkbox id="crossNavigationMultiselect" class="p-mr-2" v-model="crossNavigationMultiselect" :disabled="crossDisabled" :binary="true" />
                    <label for="crossNavigationMultiselect">{{ $t('workspace.gis.menu.crossNavigationMultiselect') }}</label>
                </div>
            </div>
        </template>
    </Card>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12" v-if="documentData.selectedDataset.length > 0">
        <template #start>{{ $t('common.filters') }}</template>
        <template #end> <Button class="p-button-link" :label="$t('workspace.gis.dsj.addButton')" @click="addFilterRow" /> </template>
    </Toolbar>
    <div id="informations-content" class="kn-flex kn-relative kn-height-full" v-if="documentData.selectedDataset.length > 0">
        <div :style="styleDescriptor.style.absoluteScroll">
            <Card>
                <template #content>
                    <DataTable class="p-datatable-sm kn-table georef-step1-table" :value="documentDataProp.filters" dataKey="id" responsiveLayout="scroll" breakpoint="600px">
                        <template #empty>
                            {{ $t('workspace.gis.dnl.emptyInfo') }}
                        </template>
                        <Column field="name" :header="$t('workspace.gis.dsj.dsJoinCol')" :sortable="true">
                            <template #body="slotProps">
                                <Dropdown id="dsJoinCol" class="kn-material-input kn-width-full" v-model="slotProps.data.name" :options="documentDataProp.datasetJoinColumns" optionLabel="id" optionValue="id" :class="{ 'p-invalid': slotProps.data.name == null }" />
                                <small for="dsJoinCol" v-if="slotProps.data.name == null" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
                            </template>
                        </Column>
                        <Column field="label" :header="$t('workspace.gis.dsj.lyrJoinCol')" :sortable="true">
                            <template #body="slotProps">
                                <InputText id="label" class="kn-material-input kn-width-full" v-model="slotProps.data.label" :class="{ 'p-invalid': slotProps.data.label == null || slotProps.data.label == '' }" />
                                <small for="label" v-if="slotProps.data.label == null || slotProps.data.label == ''" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
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
    emits: ['filtersValidationChanged'],
    props: {
        documentDataProp: { type: Object as any, required: false }
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
    data() {
        return {
            styleDescriptor,
            documentData: {} as any,
            visibilityControls: {} as any,
            crossNavigationMultiselect: null
        }
    },
    created() {
        this.documentData = this.documentDataProp
        this.visibilityControls = this.documentDataProp.visibilityData.visibilityControls
        this.crossNavigationMultiselect = this.documentDataProp.visibilityData.crossNavigationMultiselect
        this.$emit('filtersValidationChanged', 'filtersInvalid', this.filtersInvalid)
    },
    watch: {
        documentDataProp() {
            this.documentData = this.documentDataProp
        },
        filtersInvalid() {
            this.$emit('filtersValidationChanged', 'filtersInvalid', this.filtersInvalid)
        }
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
