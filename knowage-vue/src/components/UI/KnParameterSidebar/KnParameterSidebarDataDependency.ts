import { AxiosResponse } from 'axios'
import { iParameter, } from './KnParameterSidebar'

export function setDataDependency(loadedParameters: { filterStatus: iParameter[], isReadyForExecution: boolean }, parameter: iParameter) {
    if (parameter.dependencies.data.length !== 0) {
        parameter.dependencies.data.forEach((dependency: any) => {
            const index = loadedParameters.filterStatus.findIndex((param: any) => {
                return param.urlName === dependency.parFatherUrlName
            })
            if (index !== -1) {
                const tempParameter = loadedParameters.filterStatus[index]
                parameter.dataDependsOnParameters ? parameter.dataDependsOnParameters.push(tempParameter) : (parameter.dataDependsOnParameters = [tempParameter])
                tempParameter.dataDependentParameters ? tempParameter.dataDependentParameters.push(parameter) : (tempParameter.dataDependentParameters = [parameter])
            }
        })
    }
}

export async function updateDataDependency(loadedParameters: { filterStatus: iParameter[], isReadyForExecution: boolean }, parameter: iParameter, loading: boolean, document: any, sessionRole: string, $http: any) {
    if (parameter && parameter.dataDependentParameters) {
        for (let i = 0; i < parameter.dataDependentParameters.length; i++) {
            await dataDependencyCheck(loadedParameters, parameter.dataDependentParameters[i], loading, document, sessionRole, $http)
        }
    }
}

export async function dataDependencyCheck(loadedParameters: { filterStatus: iParameter[], isReadyForExecution: boolean }, parameter: iParameter, loading: boolean, document: any, sessionRole: string, $http: any) {
    loading = true
    if (parameter.parameterValue[0]) {
        parameter.parameterValue[0] = { value: '', description: '' }
    } else {
        parameter.parameterValue = [{ value: '', description: '' }]
    }

    const postData = { label: document?.label, parameters: getFormattedParameters(loadedParameters), paramId: parameter.urlName, role: sessionRole }
    await $http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentExeParameters/admissibleValues`, postData).then((response: AxiosResponse<any>) => {
        parameter.data = response.data.result.data
        parameter.metadata = response.data.result.metadata
        formatParameterAfterDataDependencyCheck(parameter)
    })
    loading = false
}

export function formatParameterAfterDataDependencyCheck(parameter: any) {
    parameter.parameterValue = parameter.multivalue ? [] : [{ value: '', description: '' }]
    if (parameter.data) {
        parameter.data = parameter.data.map((data: any) => {
            return formatParameterDataOptions(parameter, data)
        })

        if (parameter.data.length === 1) {
            parameter.parameterValue = [...parameter.data]
        }
    }

    if ((parameter.selectionType === 'COMBOBOX' || parameter.selectionType === 'LIST') && parameter.multivalue && parameter.mandatory && parameter.data.length === 1) {
        parameter.showOnPanel = 'false'
    }

    if (parameter.parameterValue[0] && !parameter.parameterValue[0].description) {
        parameter.parameterValue[0].description = ''
    }
}

export function formatParameterDataOptions(parameter: iParameter, data: any) {
    const valueColumn = parameter.metadata.valueColumn
    const descriptionColumn = parameter.metadata.descriptionColumn
    const valueIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === valueColumn)
    const descriptionIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === descriptionColumn)

    return { value: valueIndex ? data[valueIndex] : '', description: descriptionIndex ? data[descriptionIndex] : '' }
}

export function getFormattedParameters(loadedParameters: { filterStatus: iParameter[], isReadyForExecution: boolean }) {
    let parameters = [] as any[]

    Object.keys(loadedParameters.filterStatus).forEach((key: any) => {
        const parameter = loadedParameters.filterStatus[key]

        if (!parameter.multivalue) {
            parameters.push({ label: parameter.label, value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description })
        } else {
            parameters.push({ label: parameter.label, value: parameter.parameterValue?.map((el: any) => el.value), description: parameter.parameterDescription })
        }
    })

    return parameters
}
