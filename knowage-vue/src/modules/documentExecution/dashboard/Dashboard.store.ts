import { defineStore } from 'pinia'
import { emitter, updateWidgetHelper } from './DashboardHelpers'
import { ISelection, IWidget } from './Dashboard'
import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'

const store = defineStore('dashboardStore', {
    state() {
        return {
            dashboards: {},
            selectedSheetIndex: 0,
            crossNavigations: [] as any,
            outputParameters: [] as any,
            selections: {}
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
            updateWidgetHelper(dashboardId, widget, this.dashboards, this.removeSelection)
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
        setSelections(dashboardId: string, selections: ISelection[]) {
            console.log(" ---- STORE - SET SELECTIONS: ", selections)
            this.selections[dashboardId] = selections
            emitter.emit('selectionsChanged', { dashboardId: dashboardId, selections: this.selections[dashboardId] })
        },
        removeSelection(payload: { datasetId: number, columnName: string }, dashboardId: string) {
            const index = this.selections[dashboardId]?.findIndex((selection: ISelection) => selection.datasetId === payload.datasetId && selection.columnName === payload.columnName)
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
        }
    }
})

export default store
