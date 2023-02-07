<template>
    <AssociationsList
        :dashboardAssociationsProp="dashboardAssociationsProp"
        :selectedAssociationProp="selectedAssociation"
        :associationInvalid="associationInvalid"
        @createNewAssociation="createNewAssociation"
        @associationDeleted="deleteAssociation"
        @associationSelected="associationSelected"
        @addIndexesOnAssociations="addIndexesOnAssociations"
    />
    <AssociationsDetail v-if="selectedAssociation" :dashboardAssociationsProp="dashboardAssociationsProp" :selectedDatasetsProp="selectedDatasets" :selectedAssociationProp="selectedAssociation" @fieldSelected="manageAssociationField" @fieldUnselected="unselectAssociationField" />
    <KnHint v-else class="p-as-center" :title="'dashboard.datasetEditor.associationsDetailTitle'" :hint="'dashboard.datasetEditor.associationsDetailMsg'"></KnHint>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IAssociation, IAssociationField } from '../../Dashboard'
import KnHint from '@/components/UI/KnHint.vue'
import AssociationsList from './DatasetEditorAssociationsList/DatasetEditorAssociationsList.vue'
import AssociationsDetail from './DatasetEditorAssociationsDetail/DatasetEditorAssociationsDetail.vue'
import mainStore from '../../../../../App.store'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'dataset-editor-data-tab',
    components: { AssociationsList, AssociationsDetail, KnHint },
    props: { selectedDatasetsProp: { required: true, type: Array as any }, dashboardAssociationsProp: { required: true, type: Array as PropType<IAssociation[]> }, selectedAssociationProp: { required: true, type: Object as PropType<IAssociation> } },
    emits: ['createNewAssociation', 'associationDeleted', 'addIndexesOnAssociations', 'associationSelected'],
    data() {
        return {
            selectedAssociation: null as any,
            selectedDatasets: [] as any[]
        }
    },
    watch: {
        selectedAssociationProp: {
            handler() {
                this.selectedAssociation = this.selectedAssociationProp as IAssociation
            },
            deep: true
        },
        selectedDatasetsProp() {
            this.loadSelectedDatasets()
        }
    },
    created() {
        this.loadSelectedDatasets()
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    methods: {
        loadSelectedDatasets() {
            this.selectedDatasets = this.selectedDatasetsProp
        },
        createNewAssociation() {
            this.selectedAssociation = { fields: [], id: cryptoRandomString({ length: 16, type: 'base64' }) } as IAssociation
            this.dashboardAssociationsProp.push(this.selectedAssociation)
            this.$emit('associationSelected', this.selectedAssociation)
        },
        associationSelected(associationToSelect) {
            this.selectedAssociation = associationToSelect as IAssociation
            this.$emit('associationSelected', associationToSelect)
        },
        deleteAssociation(associationId) {
            let index = this.dashboardAssociationsProp.findIndex((association) => association.id === associationId)
            if (index !== -1) this.dashboardAssociationsProp.splice(index, 1)
            this.selectedAssociation = null as any
        },
        addIndexesOnAssociations() {
            let selectedFields = {}
            this.dashboardAssociationsProp.forEach((association) => {
                association.fields.reduce((obj, item) => {
                    obj[item.dataset] = obj[item.dataset] || []
                    obj[item.dataset].push(item.column)
                    return obj
                }, selectedFields)
            })

            this.selectedDatasetsProp.forEach((dataset) => {
                dataset.modelIndexes ? '' : (dataset.modelIndexes = [])
                dataset.modelIndexes.push(...selectedFields[dataset.id.dsId].filter((item) => dataset.modelIndexes.indexOf(item) == -1))
            })
        },
        manageAssociationField(selectedField: IAssociationField) {
            let fieldToEdit = this.selectedAssociation.fields.find((field) => selectedField.dataset === field.dataset) as IAssociationField

            if (fieldToEdit && selectedField.column !== fieldToEdit.column) {
                fieldToEdit.column = selectedField.column
            } else {
                this.selectedAssociation.fields.push(selectedField)
            }
        },
        unselectAssociationField(unselectedFieldDatasetId: IAssociationField) {
            let fieldToUnselectIndex = this.selectedAssociation.fields.findIndex((field) => unselectedFieldDatasetId === field.dataset) as IAssociationField
            this.selectedAssociation.fields.splice(fieldToUnselectIndex, 1)
        }
    }
})
</script>
