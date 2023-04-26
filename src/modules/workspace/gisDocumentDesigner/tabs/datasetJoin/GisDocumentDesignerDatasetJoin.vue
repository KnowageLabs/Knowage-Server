<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
        <template #start>{{ $t('workspace.gis.datasetJoinTitle') }}</template>
        <template #end> <Button class="p-button-link" :label="$t('workspace.gis.dsj.addButton')" @click="addJoinRow" /> </template>
    </Toolbar>
    <div id="informations-content" class="kn-flex kn-relative kn-height-full">
        <div :style="styleDescriptor.style.absoluteScroll">
            <Card>
                <template #content>
                    <DataTable class="p-datatable-sm kn-table georef-step1-table" :value="documentDataProp.dsJoins" data-key="id" responsive-layout="scroll" breakpoint="600px">
                        <template #empty>
                            {{ $t('workspace.gis.dnl.emptyInfo') }}
                        </template>
                        <Column field="datasetColumn" :header="$t('workspace.gis.dsj.dsJoinCol')" :sortable="true">
                            <template #body="slotProps">
                                <Dropdown id="dsJoinCol" v-model="slotProps.data.datasetColumn" class="kn-material-input kn-width-full" :options="documentDataProp.datasetJoinColumns" option-label="id" option-value="id" :class="{ 'p-invalid': slotProps.data.datasetColumn == null }" />
                                <small v-if="slotProps.data.datasetColumn == null" for="dsJoinCol" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
                            </template>
                        </Column>
                        <Column field="layerColumn" :header="$t('workspace.gis.dsj.lyrJoinCol')" :sortable="true">
                            <template #body="slotProps">
                                <Dropdown v-model="slotProps.data.layerColumn" class="kn-material-input kn-width-full" :options="documentDataProp.layerJoinColumns" option-label="property" option-value="property" :class="{ 'p-invalid': slotProps.data.layerColumn == null }" />
                                <small v-if="slotProps.data.layerColumn == null" for="dsJoinCol" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
                            </template>
                        </Column>
                        <Column :style="styleDescriptor.style.trashColumn">
                            <template #body="slotProps">
                                <Button icon="pi pi-trash" class="p-button-link" @click="deleteJoinRow(slotProps)" />
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
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    components: { DataTable, Column, Dropdown },
    props: { documentDataProp: { type: Object as any, required: false } },
    emits: ['joinsValidationChanged'],
    data() {
        return {
            styleDescriptor,
            documentData: {} as any
        }
    },
    computed: {
        joinsInvalid() {
            if ((this.documentDataProp.dsJoins.length == 0 && this.documentDataProp.datasetLabel != '') || this.joinsContainEmptyFields) {
                return true
            } else return false
        },
        joinsContainEmptyFields() {
            let value = false
            this.documentDataProp.dsJoins.forEach((field) => {
                if (!field.datasetColumn || !field.layerColumn) {
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
        joinsInvalid() {
            this.$emit('joinsValidationChanged', 'joinsInvalid', this.joinsInvalid)
        }
    },
    created() {
        this.documentData = this.documentDataProp
        this.$emit('joinsValidationChanged', 'joinsInvalid', this.joinsInvalid)
    },
    methods: {
        addJoinRow() {
            this.documentData.dsJoins.push({ datasetColumn: null, layerColumn: null })
        },
        deleteJoinRow(eventData) {
            this.documentData.dsJoins.splice(eventData.index, 1)
        }
    }
})
</script>
