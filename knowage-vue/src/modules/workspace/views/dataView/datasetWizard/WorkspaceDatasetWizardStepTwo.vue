<template>
    <Card class="p-mt-4">
        <template #content>
            <DataTable v-if="dataset.meta" class="p-datatable-sm kn-table" :scrollable="true" scrollHeight="40vh" :value="fieldsMetadata" stripedRows rowHover>
                <Column field="fieldAlias" :header="$t('managers.datasetManagement.fieldAlias')" :sortable="true">
                    <template #body="{data}"> {{ data.fieldAlias }} </template>
                </Column>
                <Column field="Type" :header="$t('importExport.catalogFunction.column.type')" :sortable="true">
                    <template #body="{data}">
                        <Dropdown class="kn-material-input" :style="wizardDescriptor.style.maxwidth" v-model="data.Type" :options="valueTypes" optionLabel="value" optionValue="name" @change="saveFieldsMetadata" :disabled="true" />
                    </template>
                </Column>
                <Column field="fieldType" :header="$t('managers.datasetManagement.fieldType')" :sortable="true">
                    <template #body="{data}">
                        <Dropdown class="kn-material-input" :style="wizardDescriptor.style.maxwidth" v-model="data.fieldType" :options="fieldMetadataTypes" optionLabel="value" optionValue="value" @change="saveFieldsMetadata" />
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

    export default defineComponent({
        components: { Card, Column, DataTable, Message, Dropdown },
        props: {
            selectedDataset: { type: Object as any }
        },
        computed: {},
        emits: ['touched'],
        data() {
            return {
                wizardDescriptor,
                fieldMetadataTypes: wizardDescriptor.fieldsMetadataTypes,
                valueTypes: wizardDescriptor.valueTypes,
                dataset: {} as any,
                fieldsMetadata: [] as any
            }
        },
        created() {
            this.dataset = this.selectedDataset
            this.dataset.meta ? this.exctractFieldsMetadata(this.dataset.meta.columns) : ''
        },
        watch: {
            selectedDataset() {
                this.dataset = this.selectedDataset
                this.dataset.meta ? this.exctractFieldsMetadata(this.dataset.meta.columns) : ''
            }
        },

        methods: {
            exctractFieldsMetadata(array) {
                var object = {}

                for (var item in array) {
                    var element = object[array[item].column]
                    if (!element) {
                        element = {}
                        object[array[item].column] = element
                        element['column'] = array[item].column
                    }
                    element[array[item].pname] = array[item].pvalue
                }

                var fieldsMetadata = new Array()

                for (item in object) {
                    fieldsMetadata.push(object[item])
                }

                this.fieldsMetadata = fieldsMetadata
            },
            saveFieldsMetadata() {
                var numberOfSpatialAttribute = 0
                for (let i = 0; i < this.fieldsMetadata.length; i++) {
                    if (this.fieldsMetadata[i].fieldType == 'SPATIAL_ATTRIBUTE') {
                        numberOfSpatialAttribute++
                        if (numberOfSpatialAttribute > 1) {
                            this.$store.commit('setError', { title: this.$t('common.error.saving'), msg: this.$t('managers.datasetManagement.duplicateSpatialAttribute') })
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
