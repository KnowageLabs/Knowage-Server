import { IDashboardCrossNavigation } from "../dashboard/Dashboard"
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { IDocumentNavigationParameter, ICrossNavigationParameter } from "./DocumentExecution"

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

export const loadNavigationInitialValuesFromDashboard = (document: any, filtersData: { filterStatus: iParameter[], isReadyForExecution: boolean }) => {
    //console.log("-------- DOCUMENT: ", document)
    // console.log("-------- FILTERS DATA: ", filtersData)
    document.formattedCrossNavigationParameters.forEach((crossNavigationParameter: ICrossNavigationParameter) => {
        const index = filtersData.filterStatus.findIndex((parameter: iParameter) => parameter.urlName === crossNavigationParameter.targetDriverUrlName)
        if (index !== -1) loadDriverInitialValue(filtersData.filterStatus[index], crossNavigationParameter)
    })
}

const loadDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {

    console.log("$$$$$$$$$ parameter: ", parameter)
    console.log("$$$$$$$$ crossNavigationParameter: ", crossNavigationParameter)
    if (parameter.typeCode === 'MAN_IN' && (parameter.type === 'NUM' || parameter.type === 'STRING')) {
        loadManualStringDriverInitialValue(parameter, crossNavigationParameter)
    } else if (parameter.type === 'DATE') {
        loadDateDriverInitialValue(parameter, crossNavigationParameter)
    } else if (parameter.selectionType === 'LIST') {
        loadListDriverInitialValue(parameter, crossNavigationParameter)
    } else if (parameter.selectionType === 'COMBOBOX') {
        loadDropdownDriverInitialValue(parameter, crossNavigationParameter)
    } else if (parameter.selectionType === 'LOOKUP') {
        loadPopupDriverInitialValue(parameter, crossNavigationParameter)
    } else if (parameter.selectionType === 'TREE') {
        loadTreeDriverInitialValue(parameter, crossNavigationParameter)
    }

}

const loadManualStringDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    console.log("--------manual parameter: ", parameter)
    console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
}

const loadDateDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log("-------- date parameter: ", parameter)
    // console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
}

const loadListDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    //   console.log("-------- list parameter: ", parameter)
    //console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
}

const loadDropdownDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    //  console.log("-------- dropdown parameter: ", parameter)
    // console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
}

const loadPopupDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log("-------- popup parameter: ", parameter)
    // console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
}

const loadTreeDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    //  console.log("-------- tree parameter: ", parameter)
    //console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
}


