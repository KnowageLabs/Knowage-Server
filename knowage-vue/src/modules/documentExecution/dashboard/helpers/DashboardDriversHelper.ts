import { iParameter } from "@/components/UI/KnParameterSidebar/KnParameterSidebar";
import { IDashboard, IDashboardDatasetDriver, IDashboardDriver, IDashboardDataset } from "../Dashboard";

export const loadDrivers = (filtersData: { filterStatus: iParameter[]; isReadyForExecution: boolean }, dashboardModel: IDashboard) => {
    console.log("dashboardModel: ", dashboardModel)
    const drivers = [] as IDashboardDriver[]
    const dataset = datasetWithDriversExists(dashboardModel)
    if (dataset && dataset.drivers) {
        updateDatasetDrivers(dataset, filtersData)
        getFormattedDashboardDrivers(dataset.drivers, drivers)
    }
    else if (filtersData?.filterStatus) {
        getFormattedDashboardDrivers(filtersData.filterStatus, drivers)
    }
    return drivers
}


const datasetWithDriversExists = (dashboardModel: IDashboard) => {
    if (!dashboardModel || !dashboardModel.configuration.datasets) return null
    const dataset = dashboardModel.configuration.datasets.find((dataset: IDashboardDataset) => dataset.drivers && dataset.drivers.length > 0)
    return dataset

}

const updateDatasetDrivers = (dataset: IDashboardDataset, filtersData: { filterStatus: iParameter[]; isReadyForExecution: boolean }) => {
    dataset.drivers?.forEach((datasetDriver: IDashboardDatasetDriver) => {
        const index = filtersData.filterStatus.findIndex((driver: iParameter) => driver.urlName === datasetDriver.urlName)
        if (index !== -1) formatDatasetDriver(datasetDriver, filtersData.filterStatus[index])
    })
}

const formatDatasetDriver = (datasetDriver: IDashboardDatasetDriver, driver: iParameter) => {
    datasetDriver.parameterValue = driver.parameterValue
    if (driver.driverDefaultValue) updateDatasetDefaultValue(datasetDriver, driver)
    if (driver.data && datasetDriver.options) datasetDriver.options = driver.data
}

const updateDatasetDefaultValue = (datasetDriver: IDashboardDatasetDriver, driver: iParameter) => {
    datasetDriver.defaultValue = driver.driverDefaultValue.map((defaultValue: { value: string | number; desc: string }) => {
        return { value: "" + defaultValue.value, description: defaultValue.desc }
    })
}

const getFormattedDashboardDrivers = (dashboardDrivers: (iParameter | IDashboardDatasetDriver)[], drivers: IDashboardDriver[]) => {
    dashboardDrivers?.forEach((driver: iParameter | IDashboardDatasetDriver) => {
        const formattedDriver = {
            name: driver.label,
            type: driver.type,
            multivalue: driver.multivalue,
            value: getFormattedDriverValue(driver),
            urlName: driver.urlName
        } as IDashboardDriver
        drivers.push(formattedDriver)
    })
}


const getFormattedDriverValue = (filter: iParameter | IDashboardDatasetDriver) => {
    if (!filter || !filter.parameterValue) return ''
    let value = ''
    for (let i = 0; i < filter.parameterValue.length; i++) {
        value += filter.parameterValue[i].value
        value += i === filter.parameterValue.length ? '  ' : '; '
    }
    return value.substring(0, value.length - 2)
}
