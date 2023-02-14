import { IDashboardDatasetDriver } from "@/modules/documentExecution/dashboard/Dashboard"
import { AxiosResponse } from "axios"
import { getFormattedDrivers, getUserRole } from "./DatasetEditorDriverHelper"

export const setDataDependency = (formattedDriver: IDashboardDatasetDriver, formattedDrivers: IDashboardDatasetDriver[]) => {
    if (formattedDriver.dataDependencies && formattedDriver.dataDependencies.length !== 0) {
        formattedDriver.dataDependencies.forEach((dependency: any) => {
            const index = formattedDrivers.findIndex((tempformattedDriver: IDashboardDatasetDriver) => {
                return tempformattedDriver.urlName === dependency.parFatherUrlName
            })
            if (index !== -1) {
                const tempParameter = formattedDrivers[index]
                formattedDriver.dataDependsOnParameters ? formattedDriver.dataDependsOnParameters.push(tempParameter) : (formattedDriver.dataDependsOnParameters = [tempParameter])
                tempParameter.dataDependentParameters ? tempParameter.dataDependentParameters.push(formattedDriver) : (tempParameter.dataDependentParameters = [formattedDriver])
            }
        })
    }
}

export const updateDataDependency = async (drivers: IDashboardDatasetDriver[], driver: IDashboardDatasetDriver, document: any, user: any, $http: any) => {
    if (driver && driver.dataDependentParameters) {
        for (let i = 0; i < driver.dataDependentParameters.length; i++) {
            await dataDependencyCheck(drivers, driver.dataDependentParameters[i], document, user, $http)
        }
    }
}

export const dataDependencyCheck = async (drivers: IDashboardDatasetDriver[], driver: IDashboardDatasetDriver, document: any, user: any, $http: any) => {
    const postData = {
        OBJECT_LABEL: document?.label,
        ROLE: getUserRole(user),
        PARAMETER_ID: driver.urlName,
        MODE: "simple",
        PARAMETERS: getFormattedDrivers(drivers),
    }

    await $http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/documentExeParameters/getParameters', postData).then((response: AxiosResponse<any>) => {
        if (response.data.status === 'OK' && response.data.root && Array.isArray(response.data.root)) {
            driver.options = response.data.root.map((tempOption: { value: string, label: string, description: string }) => {
                return {
                    value: tempOption.value, description: tempOption.description
                }
            })
        }
        formatParameterAfterDataDependencyCheck(driver)
    })
}

export const formatParameterAfterDataDependencyCheck = (driver: IDashboardDatasetDriver) => {
    if (!checkIfDriverOptionsContainsNewValue(driver)) {
        driver.parameterValue = driver.multivalue ? [] : [{ value: '', description: '' }]
    }
}

const checkIfDriverOptionsContainsNewValue = (driver: IDashboardDatasetDriver) => {
    const index = driver.options?.findIndex((option: { value: string, description: string }) => {
        return driver.parameterValue[0].value === option.value && driver.parameterValue[0].description === option.description
    })
    return index !== -1
}
