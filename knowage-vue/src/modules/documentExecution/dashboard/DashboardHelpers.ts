import { IDashboard } from './Dashboard'
import mitt from 'mitt'
export const emitter = mitt()
import cryptoRandomString from 'crypto-random-string'

export const createNewDashboardModel = () => {
    const dashboardModel = {
        sheets: [],
        widgets: [],
        configuration: {
            id: cryptoRandomString({ length: 16, type: 'base64' }),
            name: '',
            label: '',
            description: '',
            associations: [],
            datasets: [],
            variables: [],
            themes: {}
        },
        version: "8.2.0"
    } as IDashboard

    return dashboardModel
}