<template>
    <AssociationsList
        :dashboardAssociationsProp="dashboardAssociationsProp"
        :selectedAssociationProp="selectedAssociation"
        :associationInvalid="associationInvalid"
        @createNewAssociation="$emit('createNewAssociation')"
        @associationDeleted="$emit('associationDeleted', $event)"
        @associationSelected="associationSelected"
    />
    <AssociationsDetail v-if="selectedAssociation" :dashboardAssociationsProp="dashboardAssociationsProp" :selectedDatasetsProp="selectedDatasetsProp" :selectedAssociationProp="selectedAssociation" @fieldSelected="manageAssociationField" @fieldUnselected="unselectAssociationField" />
    <KnHint v-else class="p-as-center" :title="'documentExecution.dossier.title'" :hint="'documentExecution.dossier.hint'"></KnHint>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IAssociation, IAssociationField } from '../../Dashboard'
import KnHint from '@/components/UI/KnHint.vue'
import AssociationsList from './DatasetEditorAssociationsList/DatasetEditorAssociationsList.vue'
import AssociationsDetail from './DatasetEditorAssociationsDetail/DatasetEditorAssociationsDetail.vue'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'dataset-editor-data-tab',
    components: { AssociationsList, AssociationsDetail, KnHint },
    props: { selectedDatasetsProp: { required: true, type: Array }, dashboardAssociationsProp: { required: true, type: Array as PropType<IAssociation[]> }, selectedAssociationProp: { required: true, type: Object as PropType<IAssociation> } },
    emits: ['createNewAssociation', 'associationDeleted'],
    data() {
        return {
            selectedAssociation: null as any
        }
    },
    watch: {
        selectedAssociationProp() {
            this.selectedAssociation = this.selectedAssociationProp as IAssociation
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    methods: {
        associationSelected(associationToSelect) {
            this.selectedAssociation = associationToSelect as IAssociation
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
