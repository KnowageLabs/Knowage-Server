import { IDashboardCrossNavigation } from "../dashboard/Dashboard"
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { IDocumentNavigationParameter, ICrossNavigationParameter } from "./DocumentExecution"

let documentCrossNavigations = [] as IDashboardCrossNavigation[]

export const executeCrossNavigation = (payload: any, document: any) => {
    console.log('------ doc exe onExecuteCrossNavigation: ', payload)
    documentCrossNavigations = payload.crossNavigations
    console.log('------ doc exe documentCrossNavigations: ', documentCrossNavigations)

    // TODO - add for multiple cross navs
    const formattedCrossNavigationParameters = getFormattedCrossNavigationParameters(documentCrossNavigations[0])
    console.log('-------- formattedCrossNavigationParameters: ', formattedCrossNavigationParameters)
    document.navigationParams = createDocumentNavigationParametersForFilterService(formattedCrossNavigationParameters)
    document.navigationFromDashboard = true
    console.log(">>>>>>>>>>>     document.formattedCrossNavigationParameters: ", document.navigationParams)

}

const getFormattedCrossNavigationParameters = (documentCrossNavigation: IDashboardCrossNavigation) => {
    const formattedCrossNavigationParameters = [] as ICrossNavigationParameter[]
    const documentCrossNavigationParameters = getFormattedDocumentCrossNavigationParameters(documentCrossNavigation.navigationParams)
    documentCrossNavigationParameters.forEach((documentCrossNavigationParameter: IDocumentNavigationParameter) => {
        if (documentCrossNavigationParameter.fixed) {
            addFixedDocumentCrossNavigationParameter(documentCrossNavigationParameter, formattedCrossNavigationParameters)
        }
    })
    console.log('-------- documentCrossNavigationParameters: ', documentCrossNavigationParameters)
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
    console.log("-------- DOCUMENT: ", document)
    console.log("-------- FILTERS DATA: ", filtersData)
}