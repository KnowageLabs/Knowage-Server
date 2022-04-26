<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
        <template #start>{{ $t('workspace.gis.datasetJoinTitle') }}</template>
        <template #end> <Button class="p-button-link" :label="$t('workspace.gis.dsj.addButton')" @click="addJoinRow" /> </template>
    </Toolbar>
    <div id="informations-content" class="kn-flex kn-relative kn-height-full">
        <div :style="styleDescriptor.style.absoluteScroll">
            <Card>
                <template #content>
                    <DataTable class="p-datatable-sm kn-table georef-step1-table" :value="documentDataProp.dsJoins" dataKey="id" responsiveLayout="scroll" breakpoint="600px">
                        <template #empty>
                            {{ $t('workspace.gis.dnl.emptyInfo') }}
                        </template>
                        <Column field="datasetColumn" :header="$t('workspace.gis.dsj.dsJoinCol')" :sortable="true">
                            <template #body="slotProps">
                                <Dropdown id="dsJoinCol" class="kn-material-input kn-width-full" v-model="slotProps.data.datasetColumn" :options="documentDataProp.datasetJoinColumns" optionLabel="id" optionValue="id" :class="{ 'p-invalid': slotProps.data.datasetColumn == null }" />
                                <small for="dsJoinCol" v-if="slotProps.data.datasetColumn == null" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
                            </template>
                        </Column>
                        <Column field="layerColumn" :header="$t('workspace.gis.dsj.lyrJoinCol')" :sortable="true">
                            <template #body="slotProps">
                                <Dropdown class="kn-material-input kn-width-full" v-model="slotProps.data.layerColumn" :options="documentDataProp.layerJoinColumns" optionLabel="property" optionValue="property" :class="{ 'p-invalid': slotProps.data.layerColumn == null }" />
                                <small for="dsJoinCol" v-if="slotProps.data.layerColumn == null" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
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
    emits: ['joinsValidationChanged'],
    props: { documentDataProp: { type: Object as any, required: false } },
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
    data() {
        return {
            styleDescriptor,
            documentData: {} as any
        }
    },
    created() {
        this.documentData = this.documentDataProp
        this.$emit('joinsValidationChanged', 'joinsInvalid', this.joinsInvalid)
    },
    watch: {
        documentDataProp() {
            this.documentData = this.documentDataProp
        },
        joinsInvalid() {
            this.$emit('joinsValidationChanged', 'joinsInvalid', this.joinsInvalid)
        }
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
