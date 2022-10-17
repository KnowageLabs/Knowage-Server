import { defineStore } from 'pinia'
import { emitter } from './DashboardHelpers'
import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'
import { ISelection, IWidget } from './Dashboard'

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
            for (let i = 0; i < this.dashboards[dashboardId].widgets.length; i++) {
                console.log(widget.id + ' === ' + this.dashboards[dashboardId].widgets[i].id)
                if (widget.id === this.dashboards[dashboardId].widgets[i].id) {
                    this.dashboards[dashboardId].widgets[i] = deepcopy(widget)
                }
            }
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
            console.log(" ----- STORE - GET SEELCTIONS: ", dashboardId)
            return this.selections[dashboardId]
        },
        setSelections(dashboardId: string, selections: ISelection[]) {
            console.log(" ---- STORE - SET SELECTIONS: ", selections)
            this.selections[dashboardId] = selections
            emitter.emit('selectionsChanged', { dashboardId: dashboardId, selections: this.selections[dashboardId] })
        }
    }
})

export default store
