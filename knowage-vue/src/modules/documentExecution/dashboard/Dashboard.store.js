import { defineStore } from 'pinia'

const store = defineStore('dashboardStore', {
    state() {
        return {
            dashboards: {}
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
        }
    }
})

export default store
