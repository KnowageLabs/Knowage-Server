import { AxiosResponse } from 'axios'
import { iParameter } from './KnParameterSidebar'
import { formatParameterDataOptions, getFormattedParameters, addDefaultValueForSelectionTypeParameters, resetParameterValueToEmptyValues } from './KnParameterSidebarDataDependency'

export function setLovsDependency(loadedParameters: { filterStatus: iParameter[]; isReadyForExecution: boolean }, parameter: iParameter) {
    if (parameter.dependencies.lov.length !== 0) {
        parameter.dependencies.lov.forEach((dependency: any) => {
            const index = loadedParameters.filterStatus.findIndex((param: any) => {
                return param.urlName === dependency
            })
            if (index !== -1) {
                const tempParameter = loadedParameters.filterStatus[index]
                parameter.lovDependsOnParameters ? parameter.lovDependsOnParameters.push(tempParameter) : (parameter.lovDependsOnParameters = [tempParameter])
                tempParameter.lovDependentParameters ? tempParameter.lovDependentParameters.push(parameter) : (tempParameter.lovDependentParameters = [parameter])
            }
        })
    }
}

export async function updateLovDependency(loadedParameters: { filterStatus: iParameter[]; isReadyForExecution: boolean }, parameter: iParameter, loading: boolean, document: any, sessionRole: string | null, $http: any, mode: string, resetValue: boolean, userDateFormat: string) {
    if (parameter && parameter.lovDependentParameters) {
        for (let i = 0; i < parameter.lovDependentParameters.length; i++) {
            await lovDependencyCheck(loadedParameters, parameter.lovDependentParameters[i], loading, document, sessionRole, $http, mode, resetValue, userDateFormat)
        }
    }
}

export async function lovDependencyCheck(loadedParameters: { filterStatus: iParameter[]; isReadyForExecution: boolean }, parameter: iParameter, loading: boolean, document: any, sessionRole: string | null, $http: any, mode: string, resetValue: boolean, userDateFormat: string) {
    loading = true

    resetParameterValueToEmptyValues(parameter)
    if (resetValue) return

    const postData = { label: document?.label, parameters: getFormattedParameters(loadedParameters, userDateFormat), paramId: parameter.urlName, role: sessionRole }
    let url = '2.0/documentExeParameters/admissibleValues'

    if (mode !== 'execution' && document) {
        url = document.type === 'businessModel' ? `1.0/businessmodel/${document.name}/admissibleValues` : `/3.0/datasets/${document.label}/admissibleValues`
    }

    await $http
        .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url, postData)
        .then((response: AxiosResponse<any>) => {
            parameter.data = response.data.result.data
            parameter.metadata = response.data.result.metadata
            formatParameterAfterDataDependencyCheck(parameter)
        })
        .catch(() => {})
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
        parameter.visible = false
    }

    if (parameter.parameterValue[0] && !parameter.parameterValue[0].description) {
        parameter.parameterValue[0].description = ''
    }

    addDefaultValueForSelectionTypeParameters(parameter)
}
