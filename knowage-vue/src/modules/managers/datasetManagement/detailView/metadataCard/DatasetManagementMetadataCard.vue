<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('managers.datasetManagement.fieldsMetadata') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable v-if="dataset.meta && (dataset.meta.ccolumns || dataset.meta.dataset)" class="p-datatable-sm kn-table kn-table-small-input" :scrollable="true" scrollHeight="750px" :value="fieldsMetadata" responsiveLayout="stack" breakpoint="960px">
                <Column field="fieldAlias" :header="$t('managers.datasetManagement.fieldAlias')" :sortable="true">
                    <template #body="{data}"> {{ data.fieldAlias }} </template>
                </Column>
                <Column field="Type" :header="$t('importExport.catalogFunction.column.type')" :sortable="true">
                    <template #body="{data}">
                        <Dropdown class="kn-material-input" :style="linkTabDescriptor.style.maxwidth" v-model="data.Type" :options="valueTypes" optionDisabled="disabled" optionLabel="value" optionValue="name" @change="saveFieldsMetadata" :disabled="true" />
                    </template>
                </Column>
                <Column field="fieldType" :header="$t('managers.datasetManagement.fieldType')" :sortable="true">
                    <template #body="{data}">
                        <Dropdown class="kn-material-input" :style="linkTabDescriptor.style.maxwidth" v-model="data.fieldType" :options="fieldMetadataTypes" optionLabel="value" optionValue="value" @change="saveFieldsMetadata('fieldType')" />
                    </template>
                </Column>
                <Column field="personal" :header="$t('managers.datasetManagement.personal')" :sortable="true">
                    <template #body="{data}">
                        <Checkbox id="personal" v-model="data.personal" :binary="true" @change="saveFieldsMetadata('personal')" />
                    </template>
                </Column>
                <Column field="decript" :header="$t('managers.datasetManagement.decript')" :sortable="true">
                    <template #body="{data}">
                        <Checkbox id="decript" v-model="data.decript" :binary="true" @change="saveFieldsMetadata('decript')" />
                    </template>
                </Column>
                <Column field="subjectId" :header="$t('managers.datasetManagement.subjectId')" :sortable="true">
                    <template #body="{data}">
                        <Checkbox id="subjectId" v-model="data.subjectId" :binary="true" @change="saveFieldsMetadata('subjectId')" />
                    </template>
                </Column>
            </DataTable>
            <div v-if="!dataset.meta || dataset.meta.length == 0">
                <Message severity="info" :closable="false">{{ $t('managers.datasetManagement.metadataInfo') }}</Message>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import linkTabDescriptor from './DatasetManagementMetadataCardDescriptor.json'
    import Card from 'primevue/card'
    import DataTable from 'primevue/datatable'
    import Column from 'primevue/column'
    import Message from 'primevue/message'
    import Dropdown from 'primevue/dropdown'
    import Checkbox from 'primevue/checkbox'

    export default defineComponent({
        components: { Card, Column, DataTable, Message, Dropdown, Checkbox },
        props: {
            selectedDataset: { type: Object as any }
        },
        computed: {},
        emits: ['touched'],
        data() {
            return {
                linkTabDescriptor,
                fieldMetadataTypes: linkTabDescriptor.fieldsMetadataTypes,
                valueTypes: linkTabDescriptor.valueTypes,
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
            saveFieldsMetadata(fieldName) {
                this.warnForDuplicateSpatialFields()
                this.applyMetadataChangesToFields(fieldName)
            },
            applyMetadataChangesToFields(fieldName) {
                for (let i = 0; i < this.fieldsMetadata.length; i++) {
                    for (let j = 0; j < this.dataset.meta.columns.length; j++) {
                        if (this.fieldsMetadata[i].column == this.dataset.meta.columns[j].column && this.dataset.meta.columns[j].pname == fieldName) {
                            this.dataset.meta.columns[j].pvalue = this.fieldsMetadata[i][fieldName]
                        }
                    }
                }
            },
            warnForDuplicateSpatialFields() {
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
            }
        }
    })
</script>
