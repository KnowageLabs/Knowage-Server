import { defineStore } from 'pinia'
import { deleteWidgetHelper, emitter, updateWidgetHelper } from './DashboardHelpers'
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
            allDatasets: [] as IDataset[],
            internationalization: {}
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
        getDashboardDocument(dashboardId: string) {
            return this.dashboards[dashboardId].document
        },
        setDashboardDocument(dashboardId: string, document: any) {
            this.dashboards[dashboardId].document = document
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
        deleteWidget(dashboardId: string, widget: IWidget) {
            deleteWidgetHelper(dashboardId, widget, this.dashboards)
        },
        setSelectedSheetIndex(index: number) {
            this.selectedSheetIndex = index
        },
        getDashboardSelectedDatasets(dashboardId: string) {
            const temp = this.dashboards[dashboardId]?.configuration?.datasets
            return temp ?? []
        },
        getCrossNavigations(dashboardId: string) {
            return this.dashboards[dashboardId].crossNavigations
        },
        setCrosssNavigations(dashboardId: string, crossNavigations: any[]) {
            this.dashboards[dashboardId].crossNavigations = crossNavigations
        },
        getOutputParameters(dashboardId: string) {
            return this.dashboards[dashboardId].outputParameters
        },
        setOutputParameters(dashboardId: string, outputParameters: any) {
            this.dashboards[dashboardId].outputParameters = outputParameters
        },
        getSelections(dashboardId: string) {

            return this.dashboards[dashboardId].selections
        },
        setInternationalization(internationalization) {
            this.internationalization = internationalization
        },
        setSelections(dashboardId: string, selections: ISelection[], $http: any) {
            this.dashboards[dashboardId].selections = selections
            if (selections.length > 0 && selectionsUseDatasetWithAssociation(selections, this.dashboards[dashboardId].configuration.associations)) {
                loadAssociativeSelections(this.dashboards[dashboardId], this.allDatasets, selections, $http)
            } else {
                emitter.emit('selectionsChanged', { dashboardId: dashboardId, selections: this.dashboards[dashboardId].selections })
            }
        },
        removeSelection(payload: { datasetId: number; columnName: string }, dashboardId: string) {
            const index = this.dashboards[dashboardId].selections?.findIndex((selection: ISelection) => selection.datasetId === payload.datasetId && selection.columnName === payload.columnName)
            if (index !== -1) {
                const tempSelection = deepcopy(this.dashboards[dashboardId].selections[index])
                this.dashboards[dashboardId].selections.splice(index, 1)

                emitter.emit('selectionsDeleted', [tempSelection])
                emitter.emit('selectionsChanged', { dashboardId: dashboardId, selections: this.dashboards[dashboardId].selections })
            }
        },
        removeSelections(selectionsToRemove: ISelection[], dashboardId: string) {
            const removedSelections = [] as ISelection[]
            selectionsToRemove?.forEach((selection: ISelection) => {
                const index = this.dashboards[dashboardId].selections.findIndex((activeSelection: ISelection) => activeSelection.datasetId === selection.datasetId && activeSelection.columnName === selection.columnName)
                if (index !== -1) {
                    this.dashboards[dashboardId].selections.splice(index, 1)
                    removedSelections.push(selection)
                }
            })
            if (removedSelections.length > 0) emitter.emit('selectionsDeleted', removedSelections)
        },
        getAllDatasets() {
            return this.allDatasets
        },
        getInternationalization() {
            return this.internationalization
        },
        setAllDatasets(datasets: IDataset[]) {
            this.allDatasets = datasets
        }
    }
})

export default store
