import { defineStore } from 'pinia'
import { emitter, updateWidgetHelper } from './DashboardHelpers'
import { IDataset, ISelection, IWidget } from './Dashboard'
import { selectionsUseDatasetWithAssociation } from './widget/interactionsHelpers/DatasetAssociationsHelper'
import { loadAssociativeSelections } from './widget/interactionsHelpers/InteractionHelper'
import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'

const store = defineStore('dashboardStore', {
    state() {
        return {
            dashboards: {},
            selectedSheetIndex: 0,
            crossNavigations: [] as any,
            outputParameters: [] as any,
            selections: {},
            allDatasets: [] as IDataset[]
        }
    },
    actions: {
        removeDashboard(dashboard: any) {
            delete this.dashboards[dashboard.id]
        },
        getDashboard(dashboardId: string) {
            return this.dashboards[dashboardId]
        },
        setDashboard(id: string, dashboard: any) {
            this.dashboards[id] = dashboard
        },
        setDashboardSheet(dashboard: any) {
            this.dashboards[dashboard.id].sheet = dashboard.sheet
        },
        createNewWidget(dashboardId: string, widget: IWidget) {
            this.dashboards[dashboardId].widgets.push(widget)
            if (this.dashboards[dashboardId].sheets[this.selectedSheetIndex]) {
                this.dashboards[dashboardId].sheets[this.selectedSheetIndex].widgets.lg.push({ id: widget.id, h: 10, i: cryptoRandomString({ length: 16, type: 'base64' }), w: 10, x: 0, y: 0, moved: false })
            } else {
                this.dashboards[dashboardId].sheets[this.selectedSheetIndex] = { widgets: { lg: [{ id: widget.id, h: 10, i: cryptoRandomString({ length: 16, type: 'base64' }), w: 10, x: 0, y: 0, moved: false }] } }
            }
        },
        updateWidget(dashboardId: string, widget: IWidget) {
            updateWidgetHelper(dashboardId, widget, this.dashboards)
        },
        setSelectedSheetIndex(index: number) {
            this.selectedSheetIndex = index
        },
        getDashboardSelectedDatasets(dashboardId: string) {
            const temp = this.dashboards[dashboardId]?.configuration?.datasets
            return temp ?? []
        },
        getCrossNavigations() {
            return this.crossNavigations
        },
        setCrosssNavigations(crossNavigations: any[]) {
            this.crossNavigations = crossNavigations
        },
        getOutputParameters() {
            return this.outputParameters
        },
        setOutputParameters(outputParameters) {
            this.outputParameters = outputParameters
        },
        getSelections(dashboardId: string) {
            return this.selections[dashboardId]
        },
        setSelections(dashboardId: string, selections: ISelection[], $http: any) {
            console.log(" ---- STORE - SET SELECTIONS: ", selections)
            console.log(" ---- STORE - SET SELECTIONS A: ", $http)
            this.selections[dashboardId] = selections
            console.log('----- STORE- --- TEST', selectionsUseDatasetWithAssociation(selections, this.dashboards[dashboardId].configuration.associations))
            if (selectionsUseDatasetWithAssociation(selections, this.dashboards[dashboardId].configuration.associations)) {
                loadAssociativeSelections(this.dashboards[dashboardId], this.allDatasets, selections, $http)
            } else {
                emitter.emit('selectionsChanged', { dashboardId: dashboardId, selections: this.selections[dashboardId] })
            }
        },
        removeSelection(payload: { datasetId: number, columnName: string }, dashboardId: string) {
            const index = this.selections[dashboardId]?.findIndex((selection: ISelection) => selection.datasetId === payload.datasetId && selection.columnName === payload.columnName)
            console.log("----- STORE - REMOVE SELECTION: ", index)
            if (index !== -1) {
                const tempSelection = deepcopy(this.selections[dashboardId][index])
                this.selections[dashboardId].splice(index, 1)

                emitter.emit('selectionsDeleted', [tempSelection])
                emitter.emit('selectionsChanged', { dashboardId: dashboardId, selections: this.selections[dashboardId] })

            }
        },
        removeSelections(selectionsToRemove: ISelection[], dashboardId: string) {
            const removedSelections = [] as ISelection[]
            selectionsToRemove?.forEach((selection: ISelection) => {
                const index = this.selections[dashboardId].findIndex((activeSelection: ISelection) => activeSelection.datasetId === selection.datasetId && activeSelection.columnName === selection.columnName)
                if (index !== -1) {
                    this.selections[dashboardId].splice(index, 1)
                    removedSelections.push(selection)
                }
            })
            if (removedSelections.length > 0) emitter.emit('selectionsDeleted', removedSelections)
        },
        getAllDatasets() {
            return this.allDatasets
        },
        setAllDatasets(datasets: IDataset[]) {
            this.allDatasets = datasets
        }
    }
})

export default store
