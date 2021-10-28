<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left> {{ $t('workspace.federationDefinition.title') }}</template>
        <template #right>
            <Button class="kn-button p-button-text p-button-rounded" @click="closeFederationDefinition"> {{ $t('common.close') }}</Button></template
        >
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

    <div>
        <div v-if="step === 0" class="p-d-flex p-flex-row">
            <WorkspaceFederationDatasetList class="kn-flex p-m-2" :mode="'available'" :propDatasets="availableDatasets" @showInfo="showDatasetInfo" @datasetSelected="moveDataset"></WorkspaceFederationDatasetList>
            <WorkspaceFederationDatasetList class="kn-flex p-m-2" :mode="'selected'" :propDatasets="selectedDatasets" @datasetSelected="moveDataset"></WorkspaceFederationDatasetList>
        </div>

        <div class="p-d-flex p-flex-row p-jc-end p-m-4">
            <Button class="kn-button kn-button--secondary" @click="changeSteps"> {{ step === 0 ? $t('common.next') : $t('common.back') }}</Button>
            <Button v-if="step === 1" class="kn-button kn-button--secondary p-mx-2" @click="saveFederation"> {{ $t('workspace.federationDefinition.saveFederation') }}</Button>
        </div>

        <WorskpaceFederationDatasetDialog :visible="infoDialogVisible" :dataset="selectedDataset" @close="closeInfoDialog"></WorskpaceFederationDatasetDialog>
        <WorkspaceFederationWarningDialog :visible="warningDialogVisbile" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceFederationWarningDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IFederatedDataset } from '../Workspace'
import WorskpaceFederationDatasetDialog from './dialogs/WorskpaceFederationDatasetDialog.vue'
import WorkspaceFederationWarningDialog from './dialogs/WorkspaceFederationWarningDialog.vue'
import WorkspaceFederationDatasetList from './WorkspaceFederationDatasetList.vue'

export default defineComponent({
    name: 'workspace-federation-definition',
    components: { WorkspaceFederationDatasetList, WorskpaceFederationDatasetDialog, WorkspaceFederationWarningDialog },
    props: { id: { type: String } },
    data() {
        return {
            federatedDataset: null as IFederatedDataset | null,
            datasets: [] as any[],
            availableDatasets: [] as any[],
            selectedDatasets: [] as any[],
            sourceDatasetUsedInRelations: [] as any[],
            multiRelationships: [] as any[],
            selectedDataset: null as any,
            infoDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: '' as string,
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
    async created() {
        this.user = (this.$store.state as any).user
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
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `federateddataset/${this.id}/`).then((response) => (this.federatedDataset = { ...response.data, relationships: JSON.parse(response.data.relationships) }))

            // console.log('LOADED FEDERATED DATASET: ', this.federatedDataset)
        },
        async loadDatasets() {
            this.datasets = []
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/?includeDerived=no`).then((response) => {
                response.data.forEach((el: any) => {
                    if (el.pars.length === 0) {
                        this.datasets.push(el)
                    }
                })
            })

            // console.log('LOADED DATASETS: ', this.datasets)
        },
        formatRelationship() {
            // console.log('DATASET RELATIONSHIP: ', this.federatedDataset?.relationships)
            this.federatedDataset?.relationships.forEach((relationship: any) => {
                relationship.forEach((el: any) => {
                    this.sourceDatasetUsedInRelations.push(el.sourceTable.name)
                    this.sourceDatasetUsedInRelations.push(el.destinationTable.name)
                    this.multiRelationships.push(el.sourceTable.name.toUpperCase() + '.' + el.sourceColumns[0] + ' -> ' + el.destinationTable.name.toUpperCase() + '.' + el.destinationColumns[0])
                })
            })

            // console.log('SOURCE DATASET USED IN RELATIONS: ', this.sourceDatasetUsedInRelations)
            // console.log('MULTIRELATIONSHIPS: ', this.multiRelationships)
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

            console.log('SELECTED DATASETS: ', this.selectedDatasets)
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
        saveFederation() {
            console.log('SAVE FEDERATION CLICKED!')
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
            console.log('MOVE DATASET PAYLOAD: ', payload)
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
            console.log('DATASET CAN BE UNSELECTED: ', dataset)
            console.log('SOURCE DATASET USED IN...: ', this.sourceDatasetUsedInRelations)
            const index = this.sourceDatasetUsedInRelations.findIndex((el: any) => {
                console.log(el + ' ' + dataset.label)
                return el === dataset.label
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
        }
    }
})
</script>
