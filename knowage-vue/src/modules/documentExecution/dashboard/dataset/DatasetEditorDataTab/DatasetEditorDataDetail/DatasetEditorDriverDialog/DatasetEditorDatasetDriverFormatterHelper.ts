import { IDashboardDatasetDriver } from "@/modules/documentExecution/dashboard/Dashboard"
import { setDataDependency } from "./DatasetEditorDriverDependencyHelper"
import moment from "moment"

export const getFormattedDatasetDrivers = (dataset: any) => {
    if (!dataset.drivers || dataset.drivers.length === 0) return []
    console.log("---------------------- DATASET DRIVERS: ", dataset.drivers)
    const formattedDrivers = [] as IDashboardDatasetDriver[]
    dataset.drivers.forEach((driver: any) => formattedDrivers.push(getFormattedDatasetDriver(driver)))

    console.log("---------------------- FORMATTED DRIVERS: ", formattedDrivers)
    formattedDrivers.forEach((formattedDriver: IDashboardDatasetDriver) => setDataDependency(formattedDriver, formattedDrivers))
    return formattedDrivers
}


const getFormattedDatasetDriver = (driver: any) => {
    // console.log(">>>>>>> DRIVER: ", driver)
    const formattedDriver = { urlName: driver.urlName, type: driver.type, typeCode: driver.typeCode, selectionType: driver.selectionType, label: driver.label, multivalue: driver.multivalue } as IDashboardDatasetDriver
    getFormattedDriverProperties(driver, formattedDriver)
    if (driver.dataDependencies) formattedDriver.dataDependencies = driver.dataDependencies
    // console.log(">>>>>>>>> FORMATTED DRIVER: ", formattedDriver)
    return formattedDriver
}

const getFormattedDriverProperties = (driver: any, formattedDriver: IDashboardDatasetDriver) => {

    if (driver.typeCode === 'MAN_IN' && (driver.type === 'NUM' || driver.type === 'STRING')) {
        getFormattedManualStringDriver(driver, formattedDriver)
    } else if (driver.type === 'DATE') {
        getFormattedDateDriver(driver, formattedDriver)
    } else if (driver.selectionType === 'LIST') {
        getFormattedListDriver(driver, formattedDriver)
    } else if (driver.selectionType === 'COMBOBOX') {
        getFormattedDropdownDriver(driver, formattedDriver)
    } else if (driver.selectionType === 'LOOKUP') {
        getFormattedPopupAndTreeDriver(driver, formattedDriver)
    } else if (driver.selectionType === 'TREE') {
        formattedDriver.allowInternalNodeSelection = driver.allowInternalNodeSelection
        getFormattedPopupAndTreeDriver(driver, formattedDriver)
    }


    getFormattedDriverDefaultValue(driver, formattedDriver)
}


const getFormattedManualStringDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
}

const getFormattedDateDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    console.log(" >>>>>>>>>> DATE DRIVER: ", driver)
    const dateValue = driver.parameterValue ? moment(driver.parameterValue).toDate() : ''
    formattedDriver.parameterValue = [{ value: dateValue, description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
    console.log(" >>>>>>>>>> DATE DRIVER formattedDriver: ", formattedDriver)
}

const getFormattedListDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    formattedDriver.options = driver.defaultValues ? driver.defaultValues.map((option: any) => { return { value: option.value, description: option.description } }) : []
    formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
}

const getFormattedDropdownDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    formattedDriver.options = driver.defaultValues ? driver.defaultValues.map((option: any) => { return { value: option.value, description: option.description } }) : []
    if (driver.multivalue) {
        formattedDriver.parameterValue = []
        if (driver.parameterValue && Array.isArray(driver.parameterValue)) {
            driver.parameterValue.forEach((value: string) => {
                const option = formattedDriver.options?.find((option: { value: string, description: string }) => option.value === value)
                if (option) formattedDriver.parameterValue.push({ value: value, description: option.description })
            })
        } else {
            formattedDriver.parameterValue.push({ value: driver.parameterValue, description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' })
        }

    } else {
        formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
    }
}

const getFormattedPopupAndTreeDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    if (driver.multivalue) {
        formattedDriver.parameterValue = []
        if (driver.parameterValue && Array.isArray(driver.parameterValue)) {
            driver.parameterValue.forEach((value: string) => {
                const option = formattedDriver.options?.find((option: { value: string, description: string }) => option.value === value)
                if (option) formattedDriver.parameterValue.push({ value: value, description: option.description })
            })
        } else {
            formattedDriver.parameterValue.push({ value: driver.parameterValue, description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' })
        }
    } else {
        formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
    }
}

const getFormattedDriverDefaultValue = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    const formattedDefaultValues = [] as { value: string, description: string }[]
    driver.driverDefaultValue?.forEach((defaultValue: string) => {
        const defaultValueString = defaultValue.substring(defaultValue.indexOf('[') + 1, defaultValue.lastIndexOf(']'))
        if (defaultValueString) {
            const value = defaultValueString.substring(defaultValueString.indexOf('=') + 1, defaultValueString.indexOf(','))
            const description = defaultValueString.substring(defaultValueString.lastIndexOf('=') + 1)
            formattedDefaultValues.push({ value: value?.trim() ?? '', description: description?.trim() ?? '' })

        }
    })
    formattedDriver.defaultValue = formattedDefaultValues
}


