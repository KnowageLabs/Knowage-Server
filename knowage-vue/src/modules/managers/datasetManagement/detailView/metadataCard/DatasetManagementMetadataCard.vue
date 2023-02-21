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
            <DataTable v-if="dataset.meta && (dataset.meta.ccolumns || dataset.meta.dataset)" class="p-datatable-sm kn-table kn-table-small-input" :auto-layout="true" :value="fieldsMetadata" responsive-layout="stack" breakpoint="960px">
                <Column field="fieldAlias" :header="$t('managers.datasetManagement.fieldAlias')" :sortable="true">
                    <template #body="{ data }"> {{ data.fieldAlias }} </template>
                </Column>
                <Column field="Type" :header="$t('importExport.catalogFunction.column.type')" :sortable="true">
                    <template #body="{ data }">
                        <Dropdown v-model="data.Type" class="kn-material-input" :style="linkTabDescriptor.style.maxwidth" :options="valueTypes" option-disabled="disabled" option-label="value" option-value="name" :disabled="true" @change="saveFieldsMetadata" />
                    </template>
                </Column>
                <Column field="fieldType" :header="$t('managers.datasetManagement.fieldType')" :sortable="true">
                    <template #body="{ data }">
                        <Dropdown v-model="data.fieldType" class="kn-material-input" :style="linkTabDescriptor.style.maxwidth" :options="fieldMetadataTypes" option-label="value" option-value="value" @change="saveFieldsMetadata('fieldType')" />
                    </template>
                </Column>
                <Column field="personal" :header="$t('managers.datasetManagement.personal')" :sortable="true">
                    <template #body="{ data }">
                        <Checkbox id="personal" v-model="data.personal" :binary="true" @change="saveFieldsMetadata('personal')" />
                    </template>
                </Column>
                <Column field="decrypt" :header="$t('managers.datasetManagement.decrypt')" :sortable="true">
                    <template #body="{ data }">
                        <Checkbox id="decrypt" v-model="data.decrypt" :binary="true" @change="saveFieldsMetadata('decrypt')" />
                    </template>
                </Column>
                <Column field="subjectId" :header="$t('managers.datasetManagement.subjectId')" :sortable="true">
                    <template #body="{ data }">
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
import mainStore from '../../../../../App.store'
import { mapActions } from 'pinia'

export default defineComponent({
    components: { Card, Column, DataTable, Message, Dropdown, Checkbox },
    props: {
        selectedDataset: { type: Object as any }
    },
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
        ...mapActions(mainStore, ['setInfo', 'setError']),
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
            let numberOfSpatialAttribute = 0
            for (let i = 0; i < this.fieldsMetadata.length; i++) {
                if (this.fieldsMetadata[i].fieldType == 'SPATIAL_ATTRIBUTE') {
                    numberOfSpatialAttribute++
                    if (numberOfSpatialAttribute > 1) {
                        this.setError({ title: this.$t('common.error.saving'), msg: this.$t('managers.datasetManagement.duplicateSpatialAttribute') })
                        return
                    }
                }
            }
        }
    }
})
</script>
