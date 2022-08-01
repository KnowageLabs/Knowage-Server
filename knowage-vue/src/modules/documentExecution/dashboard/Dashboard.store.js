import { defineStore } from 'pinia'

const store = defineStore('dashboardStore', {
    state() {
        return {
            dashboards: {},
            selectedSheetIndex: 0
        }
    },
    actions: {
        removeDashboard(state, dashboard) {
            delete state.dashboards[dashboard.id]
        },
        setDashboard(dashboard) {
            console.log('DASHBOARD: ', dashboard)
            this.dashboards[dashboard.id] = dashboard
        },

        setDashboardSheet(dashboard) {
            this.dashboardModel[dashboard.id].sheet = dashboard.sheet
        },
        updateWidget(widget) {
            console.log('UPDATE: ', widget)
            console.log('DASHBOARDS: ', this.dashboards)
        },
        setSelectedSheetIndex(index) {
            this.selectedSheetIndex = index
        }
    }
})

export default store
