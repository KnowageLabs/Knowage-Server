import { IDashboardCrossNavigation } from "../dashboard/Dashboard"
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { IDocumentNavigationParameter, ICrossNavigationParameter, ICrossNavigationBreadcrumb, IDocumentNavigationParameterValue } from "./DocumentExecution"
import { getDateStringFromJSDate } from "@/helpers/commons/localeHelper"
import deepcopy from "deepcopy"

let documentCrossNavigations = [] as IDashboardCrossNavigation[]

export const getDocumentForCrossNavigation = (payload: any, sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }) => {
    // console.log('------ doc exe onExecuteCrossNavigation: ', payload)
    documentCrossNavigations = payload.crossNavigations
    console.log('------ doc exe documentCrossNavigations: ', documentCrossNavigations)

    // TODO - add for multiple cross navs
    const formattedCrossNavigationParameters = getFormattedCrossNavigationParameters(documentCrossNavigations[0], sourceFiltersData)
    const crossNavigationDocument = documentCrossNavigations[0].document
    // console.log('-------- formattedCrossNavigationParameters: ', formattedCrossNavigationParameters)
    crossNavigationDocument.formattedCrossNavigationParameters = formattedCrossNavigationParameters
    crossNavigationDocument.navigationParams = createDocumentNavigationParametersForFilterService(formattedCrossNavigationParameters)
    crossNavigationDocument.navigationFromDashboard = true
    // console.log(">>>>>>>>>>>     document.formattedCrossNavigationParameters: ", crossNavigationDocument.navigationParams)
    return crossNavigationDocument
}

const getFormattedCrossNavigationParameters = (documentCrossNavigation: IDashboardCrossNavigation, sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }) => {
    const formattedCrossNavigationParameters = [] as ICrossNavigationParameter[]
    const documentCrossNavigationParameters = getFormattedDocumentCrossNavigationParameters(documentCrossNavigation.navigationParams)
    // console.log("-------------- documentCrossNavigationParameters: ", documentCrossNavigationParameters)
    documentCrossNavigationParameters.forEach((documentCrossNavigationParameter: IDocumentNavigationParameter) => {
        if (documentCrossNavigationParameter.fixed) {
            addFixedDocumentCrossNavigationParameter(documentCrossNavigationParameter, formattedCrossNavigationParameters)
        } else if (documentCrossNavigationParameter.value && (documentCrossNavigationParameter.value as IDocumentNavigationParameterValue).isInput) {
            addSourceDocumentCrossNavigationParameter(documentCrossNavigationParameter, formattedCrossNavigationParameters, sourceFiltersData)
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
        //  tempNavigationParameter.id = navigationParams[key].fixed ? key : navigationParams[key].value.label // TODO - Not sure what is the urlName for drivers in the loadCrossNavigationService
        tempNavigationParameter.id = key
        if (!navigationParams[key].fixed) tempNavigationParameter.sourceDriverUrlName = navigationParams[key].value.label
        formattedCrossNavigationParameters.push(tempNavigationParameter)
    })
    return formattedCrossNavigationParameters
}

const addFixedDocumentCrossNavigationParameter = (documentCrossNavigationParameter: IDocumentNavigationParameter, formattedCrossNavigationParameters: ICrossNavigationParameter[]) => {
    const formattedCrossNavigationParameter = { targetDriverUrlName: documentCrossNavigationParameter.id, parameterValue: [{ value: documentCrossNavigationParameter.value, description: documentCrossNavigationParameter.value }], multivalue: false, type: 'fixed' } as ICrossNavigationParameter
    formattedCrossNavigationParameters.push(formattedCrossNavigationParameter)
}

const addSourceDocumentCrossNavigationParameter = (documentCrossNavigationParameter: IDocumentNavigationParameter, formattedCrossNavigationParameters: ICrossNavigationParameter[], sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }) => {
    // console.log("-------------- documentCrossNavigationParameter: ", documentCrossNavigationParameter)
    // console.log("-------------- formattedCrossNavigationParameters: ", formattedCrossNavigationParameters)
    // console.log("-------------- sourceFiltersData: ", sourceFiltersData)
    const sourceDocumentDriver = getSourceDocumentDriver(documentCrossNavigationParameter, sourceFiltersData)
    const formattedCrossNavigationParameter = createCrossNavigationParameterFromSourceDocumentDriver(sourceDocumentDriver, documentCrossNavigationParameter.id)
    // console.log('- 2 - formattedCrossNavigationParameter: ', formattedCrossNavigationParameter)
    if (formattedCrossNavigationParameter) formattedCrossNavigationParameters.push(formattedCrossNavigationParameter)
}

const getSourceDocumentDriver = (documentCrossNavigationParameter: IDocumentNavigationParameter, sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }) => {
    const index = sourceFiltersData.filterStatus.findIndex((parameter: iParameter) => parameter.urlName === documentCrossNavigationParameter.sourceDriverUrlName)
    return index !== -1 ? sourceFiltersData.filterStatus[index] : null
}

const createDocumentNavigationParametersForFilterService = (formattedCrossNavigationParameters: ICrossNavigationParameter[]) => {
    const documentNavigationParamsForFilterService = {}
    formattedCrossNavigationParameters.forEach((formattedCrossNavigationParameter: ICrossNavigationParameter) => {
        if (formattedCrossNavigationParameter.type === 'fixed') {
            documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName] = formattedCrossNavigationParameter.parameterValue[0].value
        } else if (formattedCrossNavigationParameter.type === 'fromSourceDocumentDriver') {
            const valueAndDescription = getValueAndDescriptionForFilterServiceFromSourceDocumentDriverCrossNavigationParameter(formattedCrossNavigationParameter) as { value: string | number | (string | number)[] | null, description: string | string[] | null }
            documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName] = valueAndDescription.value
            if (valueAndDescription.description) documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName + '_field_visible_description'] = valueAndDescription.description
        }
    })
    return documentNavigationParamsForFilterService
}

const getValueAndDescriptionForFilterServiceFromSourceDocumentDriverCrossNavigationParameter = (formattedCrossNavigationParameter: ICrossNavigationParameter) => {
    const parameterValue = formattedCrossNavigationParameter.parameterValue
    const valueAndDescription = { value: '', description: null } as { value: string | number | (string | number)[] | null, description: string | string[] | null }
    if (!formattedCrossNavigationParameter.selectionType) {
        if (formattedCrossNavigationParameter.parameterType === 'STRING') valueAndDescription.value = parameterValue[0] ? parameterValue[0].value : ''
        else if (formattedCrossNavigationParameter.parameterType === 'NUM') valueAndDescription.value = parameterValue[0] && parameterValue[0].value ? +parameterValue[0].value : null
        else if (formattedCrossNavigationParameter.parameterType === 'DATE') {
            const dateValue = getDateStringFromJSDate(new Date(parameterValue[0].value), 'MM/dd/y')
            valueAndDescription.value = dateValue ?? ''
            valueAndDescription.description = dateValue ?? ''
        }
    } else if (formattedCrossNavigationParameter.selectionType === 'LIST' || formattedCrossNavigationParameter.selectionType === 'COMBOBOX') {
        if (formattedCrossNavigationParameter.multivalue) {
            valueAndDescription.value = parameterValue.map((tempValue: { value: string | number; description: string }) => tempValue.value)
            valueAndDescription.description = parameterValue.map((tempValue: { value: string | number; description: string }) => tempValue.description)
        } else {
            valueAndDescription.value = parameterValue[0] ? parameterValue[0].value : ''
            valueAndDescription.description = parameterValue[0] ? parameterValue[0].description : ''
        }
    }
    else if (formattedCrossNavigationParameter.selectionType === 'LOOKUP' || formattedCrossNavigationParameter.selectionType === 'TREE') {
        valueAndDescription.value = parameterValue.map((tempValue: { value: string | number; description: string }) => tempValue.value)
        valueAndDescription.description = parameterValue.map((tempValue: { value: string | number; description: string }) => tempValue.description)
    }
    return valueAndDescription

}

const createCrossNavigationParameterFromSourceDocumentDriver = (sourceDocumentDriver: iParameter | null, targetDriverUrlName: string) => {
    if (!sourceDocumentDriver || !targetDriverUrlName) return
    // console.log("- 1 - sourceDocumentDriver: ", sourceDocumentDriver)
    const crossNavigationParameter = {
        targetDriverUrlName: targetDriverUrlName,
        parameterValue: sourceDocumentDriver.type === 'DATE' ? getDateValueFromSourceDocumentDriverAsMilliseconds(sourceDocumentDriver.parameterValue) : sourceDocumentDriver.parameterValue,
        multivalue: sourceDocumentDriver.multivalue,
        type: 'fromSourceDocumentDriver',
        selectionType: sourceDocumentDriver.selectionType ?? null,
        parameterType: sourceDocumentDriver.type,
    } as ICrossNavigationParameter
    return crossNavigationParameter
}

const getDateValueFromSourceDocumentDriverAsMilliseconds = (parameterValue: { value: string | number | Date | null; description: string }[]) => {
    if (!parameterValue[0] || !parameterValue[0].value) return [{ value: '', description: '' }]
    if (parameterValue[0].value instanceof Date) {
        return [{ value: parameterValue[0].value.valueOf(), descripton: '' }]  // TODO - add date as string for description
    }
    else return [{ value: parameterValue[0].value, description: '' + parameterValue[0].value }] as { value: string | number, description: string }[]
}

export const loadNavigationInitialValuesFromDashboard = (document: any, filtersData: { filterStatus: iParameter[], isReadyForExecution: boolean }, dateFormat: string) => {
    //console.log("-------- DOCUMENT: ", document)
    console.log("-------- FILTERS DATA: ", filtersData)
    document.formattedCrossNavigationParameters.forEach((crossNavigationParameter: ICrossNavigationParameter) => {
        const index = filtersData.filterStatus.findIndex((parameter: iParameter) => parameter.urlName === crossNavigationParameter.targetDriverUrlName)
        if (index !== -1) loadDriverInitialValue(filtersData.filterStatus[index], crossNavigationParameter, dateFormat)
    })
}

const loadDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter, dateFormat: string) => {
    if (!crossNavigationParameter.parameterValue || !crossNavigationParameter.parameterValue[0]) return

    // console.log("$$$$$$$$$ parameter: ", deepcopy(parameter))
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
    // console.log("-------- date parameter: ", parameter)
    //console.log("-------- crossNavigationParameter: ", crossNavigationParameter)
    if (typeof crossNavigationParameter.parameterValue[0].value !== 'number') return
    const dateValue = new Date(crossNavigationParameter.parameterValue[0].value)
    const dateDescription = getDateStringFromJSDate(dateValue, dateFormat)
    parameter.parameterValue[0] = { value: dateValue, description: dateDescription ?? '' }
}

const loadListDropdownDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log("-------- list/combo parameter 1: ", deepcopy(parameter))
    // console.log("-------- crossNavigationParameter 1: ", crossNavigationParameter)
    if (parameter.multivalue) {
        loadListComboboxMultiInitialValue(parameter, crossNavigationParameter)
    } else {
        loadListComboboxSingleInitialValue(parameter, crossNavigationParameter)
    }
}

const loadListComboboxSingleInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    //console.log("-------- list/combo parameter 2: ", deepcopy(parameter))
    // console.log("-------- crossNavigationParameter 2: ", crossNavigationParameter)
    const parameterValue = getListComboboxCrossNavigationValue(parameter, crossNavigationParameter.parameterValue[0].value)
    if (parameterValue) parameter.parameterValue = [parameterValue]
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