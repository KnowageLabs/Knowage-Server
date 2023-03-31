import { luxonFormatDate } from '@/helpers/commons/localeHelper';
import { AxiosResponse } from 'axios'
import { iParameter } from './KnParameterSidebar'

export function setDataDependency(loadedParameters: { filterStatus: iParameter[]; isReadyForExecution: boolean }, parameter: iParameter) {
    if (parameter.dependencies?.data.length !== 0) {
        parameter.dependencies?.data.forEach((dependency: any) => {
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

export async function updateDataDependency(loadedParameters: { filterStatus: iParameter[]; isReadyForExecution: boolean }, parameter: iParameter, loading: boolean, document: any, sessionRole: string | null, $http: any, mode: string, resetValue: boolean, userDateFormat: string) {
    if (parameter && parameter.dataDependentParameters) {
        for (let i = 0; i < parameter.dataDependentParameters.length; i++) {
            await dataDependencyCheck(loadedParameters, parameter.dataDependentParameters[i], loading, document, sessionRole, $http, mode, resetValue, userDateFormat)

        }
    }
}

export async function dataDependencyCheck(loadedParameters: { filterStatus: iParameter[]; isReadyForExecution: boolean }, parameter: iParameter, loading: boolean, document: any, sessionRole: string | null, $http: any, mode: string, resetValue: boolean, userDateFormat: string) {
    loading = true

    const postData = { label: document?.label, parameters: getFormattedParameters(loadedParameters, userDateFormat), paramId: parameter.urlName, role: sessionRole }
    let url = '2.0/documentExeParameters/admissibleValues'

    if (mode !== 'execution' && document) {
        url = document.type === 'businessModel' ? `1.0/businessmodel/${document.name}/admissibleValues` : `/3.0/datasets/${document.label}/admissibleValues`
    }

    await $http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData).then((response: AxiosResponse<any>) => {
        parameter.data = response.data.result.data
        parameter.metadata = response.data.result.metadata
        formatParameterAfterDataDependencyCheck(parameter, resetValue)
    })
    loading = false
}

export function formatParameterAfterDataDependencyCheck(parameter: any, resetValue: boolean) {
    if (resetValue || !checkIfParameterDataContainsNewValue(parameter)) {
        parameter.parameterValue = parameter.multivalue ? [] : [{ value: '', description: '' }]
    }

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

export function formatParameterDataOptions(parameter: iParameter, data: any) {
    const valueAndDescriptionIndex = getValueAndDescriptionIndex(parameter)
    return { value: valueAndDescriptionIndex.valueIndex ? data[valueAndDescriptionIndex.valueIndex] : '', description: valueAndDescriptionIndex.descriptionIndex ? data[valueAndDescriptionIndex.descriptionIndex] : '' }
}

const getValueAndDescriptionIndex = (parameter: iParameter) => {
    const valueColumn = parameter.metadata.valueColumn
    const descriptionColumn = parameter.metadata.descriptionColumn
    const valueIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === valueColumn)
    const descriptionIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === descriptionColumn)


    return { valueIndex: valueIndex ?? '', descriptionIndex: descriptionIndex ?? '' }
}

export function getFormattedParameters(loadedParameters: { filterStatus: iParameter[]; isReadyForExecution: boolean }, userDateFormat: string) {
    const parameters = [] as any[]

    Object.keys(loadedParameters.filterStatus).forEach((key: any) => {
        const parameter = loadedParameters.filterStatus[key]


        if (parameter.type === 'DATE') {
            const dateValue = getFormattedDateParameterValue(parameter, userDateFormat)
            parameters.push({ label: parameter.label, value: dateValue, description: dateValue })
        }
        else if (!parameter.multivalue) {
            parameters.push({ label: parameter.label, value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description })
        } else {
            parameters.push({ label: parameter.label, value: parameter.parameterValue?.map((el: any) => el.value), description: parameter.parameterDescription ?? '' })
        }
    })

    return parameters
}

function getFormattedDateParameterValue(parameter: iParameter, userDateFormat: string) {
    return parameter.parameterValue[0] && parameter.parameterValue[0].value ? luxonFormatDate(parameter.parameterValue[0].value, undefined, userDateFormat) : null
}

function checkIfParameterDataContainsNewValue(parameter: iParameter) {
    const valueColumn = parameter.metadata.valueColumn
    const descriptionColumn = parameter.metadata.descriptionColumn
    let valueIndex = null as any
    if (parameter.metadata.colsMap) {
        valueIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === valueColumn)
    }
    let descriptionIndex = null as any
    if (parameter.metadata.colsMap) {
        descriptionIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === descriptionColumn)
    }

    const index = parameter.data.findIndex((option: any) => {
        if (option.value || option.description) {
            return parameter.parameterValue[0] && parameter.parameterValue[0].value === option.value && parameter.parameterValue[0].description === option.description
        } else {
            return parameter.parameterValue[0] && parameter.parameterValue[0].value === option[valueIndex] && parameter.parameterValue[0].description === option[descriptionIndex]
        }
    })
    return index !== -1
}

export function addDefaultValueForSelectionTypeParameters(parameter: iParameter) {
    if (!parameter.driverDefaultValue || !parameter.selectionType) return
    const valueAndDescriptionIndex = getValueAndDescriptionIndex(parameter)
    if (parameter.multivalue && parameter.parameterValue.length === 0) {
        addMultiDriverDefaultValue(parameter, valueAndDescriptionIndex)
    } else if (!parameter.multivalue && (!parameter.parameterValue[0] || parameter.parameterValue[0].value === '' || parameter.parameterValue[0].value === null)) {
        addSingleDriverDefaultValue(parameter, valueAndDescriptionIndex)
    }
}

function addSingleDriverDefaultValue(parameter: iParameter, valueAndDescriptionIndex: { valueIndex: string, descriptionIndex: string }) {
    if (!parameter.driverDefaultValue[0]) return
    parameter.parameterValue = [{ value: parameter.driverDefaultValue[0][valueAndDescriptionIndex.valueIndex], description: parameter.driverDefaultValue[0][valueAndDescriptionIndex.descriptionIndex] }]
    removeNonCompatibleParameterValues(parameter)
}

function addMultiDriverDefaultValue(parameter: iParameter, valueAndDescriptionIndex: { valueIndex: string, descriptionIndex: string }) {
    parameter.parameterValue = parameter.driverDefaultValue.map((defaultValue: any) => { return { value: defaultValue[valueAndDescriptionIndex.valueIndex], description: defaultValue[valueAndDescriptionIndex.descriptionIndex] } })
    removeNonCompatibleParameterValues(parameter)
}

function removeNonCompatibleParameterValues(parameter: iParameter) {
    for (let i = parameter.parameterValue.length - 1; i >= 0; i--) {
        const index = parameter.data?.findIndex((parameterValue: any) => parameterValue.value === parameter.parameterValue[i].value && parameterValue.description === parameter.parameterValue[i].description)
        if (index === undefined || index === -1) parameter.parameterValue.splice(i, 1)
    }
    if (!parameter.multivalue && parameter.parameterValue.length === 0) parameter.parameterValue = [{ value: '', description: '' }]
}