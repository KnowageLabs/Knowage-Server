<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary" :style="mainDescriptor.style.maxWidth">
        <template #start> {{ $t('workspace.federationDefinition.title') }}</template>
        <template #end>
            <Button class="kn-button p-button-text p-button-rounded federation-button" @click="changeSteps"> {{ step === 0 ? $t('common.next') : $t('common.back') }}</Button>
            <Button v-if="step === 1" class="kn-button p-button-text p-button-rounded federation-button p-mr-2" @click="saveFederation"> {{ $t('workspace.federationDefinition.saveFederation') }}</Button>
            <Button class="kn-button p-button-text p-button-rounded" @click="closeFederationDefinition"> {{ $t('common.close') }}</Button></template
        >
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

    <div class="kn-overflow-y kn-flex p-d-flex p-flex-column">
        <div v-if="step === 0" class="p-d-flex p-flex-row p-flex-wrap kn-flex">
            <WorkspaceFederationDatasetList class="kn-flex p-m-2" :mode="'available'" :propDatasets="availableDatasets" @showInfo="showDatasetInfo" @datasetSelected="moveDataset"></WorkspaceFederationDatasetList>
            <WorkspaceFederationDatasetList class="kn-flex p-m-2" :mode="'selected'" :propDatasets="selectedDatasets" @datasetSelected="moveDataset"></WorkspaceFederationDatasetList>
        </div>
        <div v-else class="kn-flex">
            <WorkspaceFederationDefinitionAssociationsEditor class="p-m-2" :selectedDatasets="selectedDatasets" :selectedMetafields="selectedMetafields" :resetSelectedMetafield="resetSelectedMetafield"></WorkspaceFederationDefinitionAssociationsEditor>
            <WorkspaceFederationDefinitionAssociationsList class="p-m-2" :propAssociations="multirelationships" @createAssociationClick="createAssociation()"></WorkspaceFederationDefinitionAssociationsList>
        </div>

        <WorskpaceFederationDatasetDialog :visible="infoDialogVisible" :dataset="selectedDataset" @close="closeInfoDialog"></WorskpaceFederationDatasetDialog>
        <WorkspaceFederationSaveDialog :visible="saveDialogVisible" :federatedDataset="federatedDataset" @close="closeSaveDialog" @save="handleSaveFederation"></WorkspaceFederationSaveDialog>
        <WorkspaceWarningDialog :visible="warningDialogVisbile" :title="$t('workspace.federationDefinition.title')" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceWarningDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IFederatedDataset } from '../Workspace'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import WorskpaceFederationDatasetDialog from './dialogs/WorskpaceFederationDatasetDialog.vue'
import WorkspaceWarningDialog from '../genericComponents/WorkspaceWarningDialog.vue'
import WorkspaceFederationSaveDialog from './dialogs/WorkspaceFederationSaveDialog.vue'
import WorkspaceFederationDatasetList from './WorkspaceFederationDatasetList.vue'
import WorkspaceFederationDefinitionAssociationsEditor from './WorkspaceFederationDefinitionAssociationsEditor.vue'
import WorkspaceFederationDefinitionAssociationsList from './WorkspaceFederationDefinitionAssociationsList.vue'
import { AxiosResponse } from 'axios'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'workspace-federation-definition',
    components: { WorkspaceFederationDatasetList, WorskpaceFederationDatasetDialog, WorkspaceWarningDialog, WorkspaceFederationDefinitionAssociationsEditor, WorkspaceFederationDefinitionAssociationsList, WorkspaceFederationSaveDialog },
    props: { id: { type: String } },
    data() {
        return {
            mainDescriptor,
            federatedDataset: null as IFederatedDataset | null,
            datasets: [] as any[],
            availableDatasets: [] as any[],
            selectedDatasets: [] as any[],
            sourceDatasetUsedInRelations: [] as any[],
            multirelationships: [] as any[],
            selectedDataset: null as any,
            selectedMetafields: [] as any[],
            resetSelectedMetafield: false,
            infoDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: '' as string,
            saveDialogVisible: false,
            operation: 'create',
            user: null as any,
            step: 0,
            loading: false
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    async created() {
        this.user = (this.store.$state as any).user
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadDatasets()
            if (this.id) {
                await this.loadFederatedDataset()
                this.formatRelationship()
            } else {
                this.federatedDataset = { name: '', label: '', description: '', relationships: [], degenerated: false, owner: this.user.userId }
            }
            this.setSelectedDatasets()
            this.loading = false
        },
        async loadFederatedDataset() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `federateddataset/${this.id}/`).then((response: AxiosResponse<any>) => (this.federatedDataset = { ...response.data, relationships: JSON.parse(response.data.relationships) }))
        },
        async loadDatasets() {
            this.datasets = []
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/?includeDerived=no`).then((response: AxiosResponse<any>) => {
                response.data.forEach((el: any) => {
                    if (el.pars.length === 0) {
                        this.formatDatasetMetaFields(el)
                        this.datasets.push(el)
                    }
                })
            })
        },
        formatDatasetMetaFields(dataset: any) {
            if (!dataset.metadata) {
                dataset.metadata = {}
            }

            if (!dataset.metadata.fieldsMeta) {
                dataset.metadata.fieldsMeta = []
                if (dataset.meta && dataset.meta.columns) {
                    let columnsJson = {}
                    for (let c in dataset.meta.columns) {
                        if (dataset.meta.columns[c].pname === 'fieldAlias') {
                            columnsJson[dataset.meta.columns[c].column] = dataset.meta.columns[c].pvalue
                        }
                    }

                    for (let column in columnsJson) {
                        dataset.metadata.fieldsMeta.push({ name: column, alias: columnsJson[column] })
                    }
                }
            }
        },
        formatRelationship() {
            this.federatedDataset?.relationships.forEach((relationship: any) => {
                relationship.forEach((el: any) => {
                    this.sourceDatasetUsedInRelations.push(el.sourceTable.name)
                    this.sourceDatasetUsedInRelations.push(el.destinationTable.name)
                    this.multirelationships.push({
                        relationship: el.sourceTable?.name.toUpperCase() + '.' + el.sourceColumns[0] + ' -> ' + el.destinationTable?.name.toUpperCase() + '.' + el.destinationColumns[0],
                        datasets: [
                            { ...el.sourceTable, label: el.sourceTable?.name },
                            { ...el.destinationTable, label: el.destinationTable?.name }
                        ]
                    })
                })
            })
        },
        setSelectedDatasets() {
            this.availableDatasets = [...this.datasets]
            this.sourceDatasetUsedInRelations.forEach((el: any) => {
                const index = this.availableDatasets.findIndex((dataset: any) => {
                    return dataset.label === el
                })

                if (index !== -1) {
                    this.selectedDatasets.push(this.availableDatasets[index])
                    this.availableDatasets.splice(index, 1)
                }
            })
        },
        changeSteps() {
            if (this.step === 0 && this.canMoveToNextStep()) {
                this.step = 1
            } else {
                this.step = 0
            }
        },
        canMoveToNextStep() {
            if (this.selectedDatasets.length === 0 || this.selectedDatasets.length === 1) {
                this.warningMessage = this.selectedDatasets.length === 0 ? this.$t('workspace.federationDefinition.noDatasetsSelectedError') : this.$t('workspace.federationDefinition.onlyOneSelectedDatasetError')
                this.warningDialogVisbile = true
                return false
            }

            return true
        },

        showDatasetInfo(dataset: any) {
            this.selectedDataset = dataset
            this.infoDialogVisible = true
        },
        closeInfoDialog() {
            this.selectedDataset = null
            this.infoDialogVisible = false
        },
        moveDataset(payload: any) {
            const fromArray = payload.mode === 'available' ? this.availableDatasets : this.selectedDatasets
            const toArray = payload.mode === 'available' ? this.selectedDatasets : this.availableDatasets

            if (payload.mode === 'selected' && !this.datasetCanBeUnselected(payload.dataset)) {
                this.warningMessage = this.$t('workspace.federationDefinition.removeSelectedDatasetError')
                this.warningDialogVisbile = true
                return
            }

            const index = fromArray.findIndex((el: any) => el.name === payload.dataset.name)
            if (index !== -1) {
                toArray.push(fromArray[index])
                fromArray.splice(index, 1)
            }
        },
        datasetCanBeUnselected(dataset: any) {
            const index = this.multirelationships.findIndex((el: any) => {
                return el.datasets[0].name === dataset.name || el.datasets[1].name === dataset.name
            })
            return index === -1
        },
        closeWarningDialog() {
            this.warningMessage = ''
            this.warningDialogVisbile = false
        },
        closeFederationDefinition() {
            this.federatedDataset = null
            this.$router.push('/workspace/models')
        },
        createAssociation() {
            if (this.selectedMetafields.length === 2) {
                const association = {
                    relationship: this.selectedMetafields[0].dataset.label + '.' + this.selectedMetafields[0].metafield.name + ' -> ' + this.selectedMetafields[1].dataset.label + '.' + this.selectedMetafields[1].metafield.name,
                    datasets: [this.selectedMetafields[0].dataset, this.selectedMetafields[1].dataset]
                }
                if (!this.checkIfAssociationAlreadyPresent(association)) {
                    this.multirelationships.push(association)
                    this.selectedMetafields = []
                    this.resetSelectedMetafield = !this.resetSelectedMetafield
                } else {
                    this.warningMessage = this.$t('workspace.federationDefinition.relationshipAlreadyPresentError')
                    this.warningDialogVisbile = true
                }
            }
        },
        checkIfAssociationAlreadyPresent(association: any) {
            const index = this.multirelationships.findIndex((el: any) => el.relationship === association.relationship)
            return index !== -1
        },
        checkIfAllSelectedDatasetArePresentInRelationships() {
            let present = true

            for (let i = 0; i < this.selectedDatasets.length; i++) {
                const index = this.multirelationships.findIndex((el: any) => {
                    return el.datasets[0].label === this.selectedDatasets[i].label || el.datasets[1].label === this.selectedDatasets[i].label
                })

                if (index === -1) {
                    present = false
                    break
                }
            }

            return present
        },
        saveFederation() {
            if (!this.checkIfAllSelectedDatasetArePresentInRelationships()) {
                this.warningMessage = this.$t('workspace.federationDefinition.datasetNotInRelationshipError')
                this.warningDialogVisbile = true
            } else {
                this.saveDialogVisible = true
            }
        },
        closeSaveDialog() {
            this.saveDialogVisible = false
        },
        async handleSaveFederation(federationDataset: IFederatedDataset) {
            federationDataset.relationships = [this.getFormattedRelationshipsForSave()]
            await this.saveFederationDataset(federationDataset)
        },
        getFormattedRelationshipsForSave() {
            const formattedRelationships = [] as any[]
            this.multirelationships.forEach((el: any) => {
                const sourceAndDestination = el.relationship?.split('->')
                const source = sourceAndDestination ? sourceAndDestination[0]?.trim().split('.') : []
                const destination = sourceAndDestination ? sourceAndDestination[1]?.trim().split('.') : []

                const tempRelationship = {
                    bidirectional: true,
                    cardinality: 'many-to-one',
                    sourceTable: {
                        name: source[0],
                        className: source[0]
                    },
                    sourceColumns: [source[1]],
                    destinationTable: {
                        name: destination[0],
                        className: destination[0]
                    },
                    destinationColumns: [destination[1]]
                }
                formattedRelationships.push(tempRelationship)
            })

            return formattedRelationships
        },
        async saveFederationDataset(federatedDataset: IFederatedDataset) {
            let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'federateddataset/post'
            const tempDataset = { ...federatedDataset }

            if (tempDataset.federation_id) {
                this.operation = 'update'
                url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + `federateddataset/${federatedDataset.federation_id}`
                delete tempDataset.federation_id
            }

            delete tempDataset.owner
            delete tempDataset.degenerated

            await this.sendRequest(url, tempDataset)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.' + this.operation + 'Title'),
                        msg: this.$t('common.toast.success')
                    })
                    this.saveDialogVisible = false
                    this.$router.push('/workspace/models')
                })
                .catch((response: any) => {
                    this.warningMessage = response.message
                    this.warningDialogVisbile = true
                })
        },
        sendRequest(url: string, federatedDataset: IFederatedDataset) {
            if (this.operation === 'create') {
                return this.$http.post(url, federatedDataset, { headers: { 'X-Disable-Errors': 'true' } })
            } else {
                return this.$http.put(url, federatedDataset, { headers: { 'X-Disable-Errors': 'true' } })
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.federation-button .p-button.p-button-text:enabled:active {
    background: none;
    color: inherit;
    border-color: none;
}
</style>
