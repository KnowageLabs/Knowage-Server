import { IDashboardCrossNavigation } from "../dashboard/Dashboard"
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { IDocumentNavigationParameter, ICrossNavigationParameter, ICrossNavigationBreadcrumb } from "./DocumentExecution"
import { getDateStringFromJSDate } from "@/helpers/commons/localeHelper"

let documentCrossNavigations = [] as IDashboardCrossNavigation[]

export const getDocumentForCrossNavigation = (payload: any, document: any) => {
    // console.log('------ doc exe onExecuteCrossNavigation: ', payload)
    documentCrossNavigations = payload.crossNavigations
    console.log('------ doc exe documentCrossNavigations: ', documentCrossNavigations)

    // TODO - add for multiple cross navs
    const formattedCrossNavigationParameters = getFormattedCrossNavigationParameters(documentCrossNavigations[0])
    const crossNavigationDocument = documentCrossNavigations[0].document
    // console.log('-------- formattedCrossNavigationParameters: ', formattedCrossNavigationParameters)
    crossNavigationDocument.formattedCrossNavigationParameters = formattedCrossNavigationParameters
    crossNavigationDocument.navigationParams = createDocumentNavigationParametersForFilterService(formattedCrossNavigationParameters)
    crossNavigationDocument.navigationFromDashboard = true
    // console.log(">>>>>>>>>>>     document.formattedCrossNavigationParameters: ", crossNavigationDocument.navigationParams)
    return crossNavigationDocument
}

const getFormattedCrossNavigationParameters = (documentCrossNavigation: IDashboardCrossNavigation) => {
    const formattedCrossNavigationParameters = [] as ICrossNavigationParameter[]
    const documentCrossNavigationParameters = getFormattedDocumentCrossNavigationParameters(documentCrossNavigation.navigationParams)
    documentCrossNavigationParameters.forEach((documentCrossNavigationParameter: IDocumentNavigationParameter) => {
        if (documentCrossNavigationParameter.fixed) {
            addFixedDocumentCrossNavigationParameter(documentCrossNavigationParameter, formattedCrossNavigationParameters)
        }
    })
    // console.log('-------- documentCrossNavigationParameters: ', documentCrossNavigationParameters)
    return formattedCrossNavigationParameters
}

const getFormattedDocumentCrossNavigationParameters = (navigationParams: any) => {
    const formattedCrossNavigationParameters = [] as IDocumentNavigationParameter[]
    if (!navigationParams) return formattedCrossNavigationParameters
    Object.keys(navigationParams).forEach((key: string) => {
        const tempNavigationParameter = { ...navigationParams[key] }
        tempNavigationParameter.id = key
        formattedCrossNavigationParameters.push(tempNavigationParameter)
    })
    return formattedCrossNavigationParameters
}

const addFixedDocumentCrossNavigationParameter = (documentCrossNavigationParameter: IDocumentNavigationParameter, formattedCrossNavigationParameters: ICrossNavigationParameter[]) => {
    const formattedCrossNavigationParameter = { targetDriverUrlName: documentCrossNavigationParameter.id, parameterValue: [{ value: documentCrossNavigationParameter.value, description: documentCrossNavigationParameter.value }], multivalue: false, type: 'fixed' } as ICrossNavigationParameter
    formattedCrossNavigationParameters.push(formattedCrossNavigationParameter)
}

const createDocumentNavigationParametersForFilterService = (formattedCrossNavigationParameters: ICrossNavigationParameter[]) => {
    const documentNavigationParamsForFilterService = {}
    formattedCrossNavigationParameters.forEach((formattedCrossNavigationParameter: ICrossNavigationParameter) => {
        if (formattedCrossNavigationParameter.type === 'fixed') {
            documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName] = formattedCrossNavigationParameter.parameterValue[0].value
        }
    })
    return documentNavigationParamsForFilterService
}

export const loadNavigationInitialValuesFromDashboard = (document: any, filtersData: { filterStatus: iParameter[], isReadyForExecution: boolean }, dateFormat: string) => {
    //console.log("-------- DOCUMENT: ", document)
    // console.log("-------- FILTERS DATA: ", filtersData)
    document.formattedCrossNavigationParameters.forEach((crossNavigationParameter: ICrossNavigationParameter) => {
        const index = filtersData.filterStatus.findIndex((parameter: iParameter) => parameter.urlName === crossNavigationParameter.targetDriverUrlName)
        if (index !== -1) loadDriverInitialValue(filtersData.filterStatus[index], crossNavigationParameter, dateFormat)
    })
}

const loadDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter, dateFormat: string) => {
    if (!crossNavigationParameter.parameterValue || !crossNavigationParameter.parameterValue[0]) return

    //console.log("$$$$$$$$$ parameter: ", parameter)
    //console.log("$$$$$$$$ crossNavigationParameter: ", crossNavigationParameter)
    if (!parameter.selectionType && parameter.valueSelection === 'man_in') {
        if (parameter.type === 'STRING') loadManualStringDriverInitialValue(parameter, crossNavigationParameter)
        else if (parameter.type === 'NUM') loadManualNumbergDriverInitialValue(parameter, crossNavigationParameter)
        else if (parameter.type === 'DATE') loadDateDriverInitialValue(parameter, crossNavigationParameter, dateFormat)
    } else if (parameter.selectionType === 'LIST' || parameter.selectionType === 'COMBOBOX') {
        loadListDropdownDriverInitialValue(parameter, crossNavigationParameter)
    } else if (parameter.selectionType === 'LOOKUP') {
        loadPopupDriverInitialValue(parameter, crossNavigationParameter)
    } else if (parameter.selectionType === 'TREE') {
        loadTreeDriverInitialValue(parameter, crossNavigationParameter)
    }

}

const loadManualStringDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log("--------manual parameter: ", parameter)
    // console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
    parameter.parameterValue[0] = { value: "" + crossNavigationParameter.parameterValue[0].value, description: "" + crossNavigationParameter.parameterValue[0].description }
}

const loadManualNumbergDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    parameter.parameterValue[0] = { value: getFormattedNumberValue(crossNavigationParameter.parameterValue[0].value), description: "" + crossNavigationParameter.parameterValue[0].description }
}

const getFormattedNumberValue = (value: string | number) => {
    if (typeof value === 'number') return value
    const parsedValue = parseFloat(value)
    return isNaN(parsedValue) ? null : parsedValue
}

const loadDateDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter, dateFormat: string) => {
    console.log("-------- date parameter: ", parameter)
    console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
    if (typeof crossNavigationParameter.parameterValue[0].value !== 'number') return
    const dateValue = new Date(crossNavigationParameter.parameterValue[0].value)
    const dateDescription = getDateStringFromJSDate(dateValue, dateFormat)
    parameter.parameterValue[0] = { value: dateValue, description: dateDescription ?? '' }
}

const loadListDropdownDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log("-------- list/combo parameter: ", parameter)
    //console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
    if (parameter.multivalue) {
        loadListComboboxMultiInitialValue(parameter, crossNavigationParameter)
    } else {
        loadListComboboxSingleInitialValue(parameter, crossNavigationParameter)
    }
}

const loadListComboboxSingleInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    const parameterValue = getListComboboxCrossNavigationValue(parameter, crossNavigationParameter.parameterValue[0].value)
    if (parameterValue) parameter.parameterValue = parameterValue
}

const loadListComboboxMultiInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    const parameterValues = [] as { value: string | number | Date | null; description: string }[]
    crossNavigationParameter.parameterValue.forEach((tempParameterValue: { value: string | number | Date | null; description: string }) => {
        const parameterValue = getListComboboxCrossNavigationValue(parameter, tempParameterValue.value)
        if (parameterValue) parameterValues.push(parameterValue)
    })
    if (parameterValues.length > 0) parameter.parameterValue = parameterValues
}


const getListComboboxCrossNavigationValue = (parameter: iParameter, crossNavigationValue: any) => {
    const index = parameter.data.findIndex((option: { value: string; description: string }) => option.value == crossNavigationValue)
    return index !== -1 ? parameter.data[index] : null
}

const loadPopupDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    //  console.log("-------- popup parameter: ", parameter)
    // console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
    // TODO - see for NON ADMISSABLE
    if (parameter.multivalue) {
        parameter.parameterValue = crossNavigationParameter.parameterValue
    } else {
        parameter.parameterValue = crossNavigationParameter.parameterValue
    }
}

const loadTreeDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    //  console.log("-------- tree parameter: ", parameter)
    //console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
    // TODO - see for NON ADMISSABLE
    if (parameter.multivalue) {
        parameter.parameterValue = crossNavigationParameter.parameterValue
    } else {
        parameter.parameterValue = crossNavigationParameter.parameterValue
    }
}


export const updateBreadcrumbForCrossNavigation = (breadcrumbs: ICrossNavigationBreadcrumb[], document: any) => {

    const index = breadcrumbs.findIndex((el: any) => el.label === document.name)
    if (index !== -1) {
        breadcrumbs[index].document = document
    } else {
        breadcrumbs.push({
            label: document.name,
            document: document,
            crossBreadcrumb: document.name // TODO - add cross breadcrumb logic
        })
    }
}