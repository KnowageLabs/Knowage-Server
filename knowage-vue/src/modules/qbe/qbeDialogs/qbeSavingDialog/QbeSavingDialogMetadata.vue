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
            <DataTable class="p-datatable-sm kn-table kn-table-small-input" :autoLayout="true" :value="fieldsMetadata" responsiveLayout="stack" breakpoint="960px">
                <Column field="fieldAlias" :header="$t('managers.datasetManagement.fieldAlias')" :sortable="true">
                    <template #body="{ data }"> {{ data.fieldAlias }} </template>
                </Column>
                <Column field="Type" :header="$t('importExport.catalogFunction.column.type')" :sortable="true">
                    <template #body="{ data }">
                        <Dropdown class="kn-material-input" :style="linkTabDescriptor.style.maxwidth" v-model="data.Type" :options="valueTypes" optionDisabled="disabled" optionLabel="value" optionValue="name" @change="warnForDuplicateSpatialFields" :disabled="true" />
                    </template>
                </Column>
                <Column field="fieldType" :header="$t('managers.datasetManagement.fieldType')" :sortable="true">
                    <template #body="{ data }">
                        <Dropdown class="kn-material-input" :style="linkTabDescriptor.style.maxwidth" v-model="data.fieldType" :options="fieldMetadataTypes" optionLabel="value" optionValue="value" @change="warnForDuplicateSpatialFields('fieldType')" />
                    </template>
                </Column>
                <Column hidden="true" field="personal" :header="$t('managers.datasetManagement.personal')" :sortable="true">
                    <template #body="{ data }">
                        <Checkbox id="personal" v-model="data.personal" :binary="true" @change="warnForDuplicateSpatialFields('personal')" />
                    </template>
                </Column>
                <Column hidden="true" field="decrypt" :header="$t('managers.datasetManagement.decrypt')" :sortable="true">
                    <template #body="{ data }">
                        <Checkbox id="decrypt" v-model="data.decrypt" :binary="true" @change="warnForDuplicateSpatialFields('decrypt')" />
                    </template>
                </Column>
                <Column hidden="true" field="subjectId" :header="$t('managers.datasetManagement.subjectId')" :sortable="true">
                    <template #body="{ data }">
                        <Checkbox id="subjectId" v-model="data.subjectId" :binary="true" @change="warnForDuplicateSpatialFields('subjectId')" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import linkTabDescriptor from '@/modules/managers/datasetManagement/detailView/metadataCard/DatasetManagementMetadataCardDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import Card from 'primevue/card'
import mainStore from '../../../../App.store'

export default defineComponent({
    components: { Card, Column, DataTable, Dropdown, Checkbox },
    props: { propMetadata: { type: Array as any } },
    emits: ['touched'],
    data() {
        return {
            linkTabDescriptor,
            fieldMetadataTypes: linkTabDescriptor.fieldsMetadataTypes,
            valueTypes: linkTabDescriptor.valueTypes,
            fieldsMetadata: [] as any
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.fieldsMetadata = this.propMetadata
    },
    watch: {
        propMetadata: {
            handler() {
                this.fieldsMetadata = this.propMetadata
            },
            deep: true
        }
    },

    methods: {
        warnForDuplicateSpatialFields() {
            var numberOfSpatialAttribute = 0
            for (let i = 0; i < this.fieldsMetadata.length; i++) {
                if (this.fieldsMetadata[i].fieldType == 'SPATIAL_ATTRIBUTE') {
                    numberOfSpatialAttribute++
                    if (numberOfSpatialAttribute > 1) {
                        this.store.setError({ title: this.$t('common.error.saving'), msg: this.$t('managers.datasetManagement.duplicateSpatialAttribute') })
                        return
                    }
                }
            }
        }
    }
})
</script>
