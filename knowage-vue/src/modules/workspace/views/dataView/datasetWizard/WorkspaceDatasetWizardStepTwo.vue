<template>
    <Card class="p-mt-4">
        <template #content>
            <DataTable v-if="dataset.meta" class="p-datatable-sm kn-table" :scrollable="true" scroll-height="40vh" :value="fieldsMetadata" striped-rows row-hover>
                <Column field="fieldAlias" :header="$t('managers.datasetManagement.fieldAlias')" :sortable="true">
                    <template #body="{ data }"> {{ data.fieldAlias }} </template>
                </Column>
                <Column field="Type" :header="$t('importExport.catalogFunction.column.type')" :sortable="true">
                    <template #body="{ data }">
                        <Dropdown v-model="data.Type" class="kn-material-input" :style="wizardDescriptor.style.maxwidth" :options="valueTypes" option-label="value" option-value="name" :disabled="true" @change="saveFieldsMetadata" />
                    </template>
                </Column>
                <Column field="fieldType" :header="$t('managers.datasetManagement.fieldType')" :sortable="true">
                    <template #body="{ data }">
                        <Dropdown v-model="data.fieldType" class="kn-material-input" :style="wizardDescriptor.style.maxwidth" :options="fieldMetadataTypes" option-label="value" option-value="value" @change="saveFieldsMetadata" />
                    </template>
                </Column>
            </DataTable>
            <div v-else>
                <Message severity="info" :closable="false">{{ $t('managers.datasetManagement.metadataInfo') }}</Message>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import wizardDescriptor from './WorkspaceDatasetWizardDescriptor.json'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Message from 'primevue/message'
import Dropdown from 'primevue/dropdown'
import mainStore from '../../../../../App.store'

export default defineComponent({
    components: { Card, Column, DataTable, Message, Dropdown },
    props: {
        selectedDataset: { type: Object as any }
    },
    emits: ['touched'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            wizardDescriptor,
            fieldMetadataTypes: wizardDescriptor.fieldsMetadataTypes,
            valueTypes: wizardDescriptor.valueTypes,
            dataset: {} as any,
            fieldsMetadata: [] as any
        }
    },
    computed: {},
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.dataset.meta ? this.exctractFieldsMetadata(this.dataset.meta.columns) : ''
        }
    },
    created() {
        this.dataset = this.selectedDataset
        this.dataset.meta ? this.exctractFieldsMetadata(this.dataset.meta.columns) : ''
    },
    methods: {
        exctractFieldsMetadata(array) {
            const object = {}

            for (const item in array) {
                let element = object[array[item].column]
                if (!element) {
                    element = {}
                    object[array[item].column] = element
                    element['column'] = array[item].column
                }
                element[array[item].pname] = array[item].pvalue
            }

            const fieldsMetadata = []

            for (const item in object) {
                fieldsMetadata.push(object[item])
            }

            this.fieldsMetadata = fieldsMetadata
        },
        saveFieldsMetadata() {
            let numberOfSpatialAttribute = 0
            for (let i = 0; i < this.fieldsMetadata.length; i++) {
                if (this.fieldsMetadata[i].fieldType == 'SPATIAL_ATTRIBUTE') {
                    numberOfSpatialAttribute++
                    if (numberOfSpatialAttribute > 1) {
                        this.store.setError({ title: this.$t('common.error.saving'), msg: this.$t('managers.datasetManagement.duplicateSpatialAttribute') })
                        return
                    }
                }
            }
            for (let i = 0; i < this.fieldsMetadata.length; i++) {
                for (let j = 0; j < this.dataset.meta.columns.length; j++) {
                    if (this.fieldsMetadata[i].column == this.dataset.meta.columns[j].column && this.dataset.meta.columns[j].pname == 'fieldType') {
                        this.dataset.meta.columns[j].pvalue = this.fieldsMetadata[i].fieldType
                    }
                }
            }
            for (let i = 0; i < this.fieldsMetadata.length; i++) {
                for (let j = 0; j < this.dataset.meta.columns.length; j++) {
                    if (this.fieldsMetadata[i].column == this.dataset.meta.columns[j].column && this.dataset.meta.columns[j].pname == 'Type') {
                        this.dataset.meta.columns[j].pvalue = this.fieldsMetadata[i].Type
                    }
                }
            }
        }
    }
})
</script>
