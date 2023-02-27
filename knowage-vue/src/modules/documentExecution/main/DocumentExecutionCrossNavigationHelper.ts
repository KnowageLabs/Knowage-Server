import { IDashboardCrossNavigation } from "../dashboard/Dashboard"
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { IDocumentNavigationParameter, ICrossNavigationParameter, ICrossNavigationBreadcrumb, IDocumentNavigationParameterValue } from "./DocumentExecution"
import { getDateStringFromJSDate } from "@/helpers/commons/localeHelper"
import moment from "moment"

let documentCrossNavigations = [] as IDashboardCrossNavigation[]


/* 
    Creates target document with formatted crossNavigationParameters (for loading initial values for Target Document drivers) and navigationParams for filter service
*/
export const getDocumentForCrossNavigation = (payload: { documentCrossNavigationOutputParameters: ICrossNavigationParameter[]; crossNavigationName: string | undefined; crossNavigations: IDashboardCrossNavigation[] }, sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }) => {
    documentCrossNavigations = payload.crossNavigations
    const selectedCrossNavigation = getSelectedCrossNavigation(payload.crossNavigationName, payload.crossNavigations)
    // TODO - add for multiple cross navs
    if (!selectedCrossNavigation) return null
    const formattedCrossNavigationParameters = getFormattedCrossNavigationParameters(selectedCrossNavigation, sourceFiltersData, payload.documentCrossNavigationOutputParameters)
    const crossNavigationDocument = documentCrossNavigations[0].document
    crossNavigationDocument.formattedCrossNavigationParameters = formattedCrossNavigationParameters
    crossNavigationDocument.navigationParams = createDocumentNavigationParametersForFilterService(formattedCrossNavigationParameters)
    crossNavigationDocument.navigationFromDashboard = true
    // console.log(">>>>>>>>>>>     document.formattedCrossNavigationParameters: ", crossNavigationDocument.navigationParams)
    return crossNavigationDocument
}

/* 
    If the widget has selected cross navigation get it from the list of cross navigations that we got from the BE service
*/
const getSelectedCrossNavigation = (crossNavigationName: string | undefined, crossNavigations: IDashboardCrossNavigation[]) => {
    const index = crossNavigations.findIndex((crossNavigation: IDashboardCrossNavigation) => crossNavigation.crossName === crossNavigationName)
    return index !== -1 ? crossNavigations[index] : null
}


/* 
    Get formatted cross navigation parameters that will be used for loading initial values for the Target Document Drivers
*/
const getFormattedCrossNavigationParameters = (documentCrossNavigation: IDashboardCrossNavigation, sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }, documentCrossNavigationOutputParameters: ICrossNavigationParameter[]) => {
    const formattedCrossNavigationParameters = [] as ICrossNavigationParameter[]
    const documentCrossNavigationParameters = getFormattedDocumentCrossNavigationParameters(documentCrossNavigation.navigationParams)
    documentCrossNavigationParameters.forEach((documentCrossNavigationParameter: IDocumentNavigationParameter) => {
        if (documentCrossNavigationParameter.fixed) {
            addFixedDocumentCrossNavigationParameter(documentCrossNavigationParameter, formattedCrossNavigationParameters)
        } else if (documentCrossNavigationParameter.value && (documentCrossNavigationParameter.value as IDocumentNavigationParameterValue).isInput) {
            addSourceDocumentCrossNavigationParameterFromInputDriver(documentCrossNavigationParameter, formattedCrossNavigationParameters, sourceFiltersData)
        } else {
            addSourceDocumentCrossNavigationParameterFromOutputParameter(documentCrossNavigationParameter, documentCrossNavigationOutputParameters, formattedCrossNavigationParameters)
        }
    })
    return formattedCrossNavigationParameters
}


/* 
    Get document cross navigation parameters from BE service in proper format, id is used for target driver url
*/
const getFormattedDocumentCrossNavigationParameters = (navigationParams: any) => {
    const formattedCrossNavigationParameters = [] as IDocumentNavigationParameter[]
    if (!navigationParams) return formattedCrossNavigationParameters
    Object.keys(navigationParams).forEach((key: string) => {
        const tempNavigationParameter = { ...navigationParams[key] }
        tempNavigationParameter.id = key
        if (!navigationParams[key].fixed) tempNavigationParameter.sourceDriverUrlName = navigationParams[key].value.label
        formattedCrossNavigationParameters.push(tempNavigationParameter)
    })
    return formattedCrossNavigationParameters
}

/* 
    Add fixed cross navigation parameters (created in Cross Navigation Management) to the final formatted cross nav params list
*/
const addFixedDocumentCrossNavigationParameter = (documentCrossNavigationParameter: IDocumentNavigationParameter, formattedCrossNavigationParameters: ICrossNavigationParameter[]) => {
    const formattedCrossNavigationParameter = { targetDriverUrlName: documentCrossNavigationParameter.id, parameterValue: [{ value: documentCrossNavigationParameter.value, description: documentCrossNavigationParameter.value }], multivalue: false, type: 'fixed' } as ICrossNavigationParameter
    formattedCrossNavigationParameters.push(formattedCrossNavigationParameter)
}


/* 
    Add source cross navigation parameters (created in Cross Navigation Management) to the final formatted cross nav params list
*/
const addSourceDocumentCrossNavigationParameterFromInputDriver = (documentCrossNavigationParameter: IDocumentNavigationParameter, formattedCrossNavigationParameters: ICrossNavigationParameter[], sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }) => {
    const sourceDocumentDriver = getSourceDocumentDriver(documentCrossNavigationParameter, sourceFiltersData)
    const formattedCrossNavigationParameter = createCrossNavigationParameterFromSourceDocumentDriver(sourceDocumentDriver, documentCrossNavigationParameter.id)
    if (formattedCrossNavigationParameter) formattedCrossNavigationParameters.push(formattedCrossNavigationParameter)
}

const getSourceDocumentDriver = (documentCrossNavigationParameter: IDocumentNavigationParameter, sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }) => {
    const index = sourceFiltersData.filterStatus.findIndex((parameter: iParameter) => parameter.urlName === documentCrossNavigationParameter.sourceDriverUrlName)
    return index !== -1 ? sourceFiltersData.filterStatus[index] : null
}

const createCrossNavigationParameterFromSourceDocumentDriver = (sourceDocumentDriver: iParameter | null, targetDriverUrlName: string) => {
    if (!sourceDocumentDriver || !targetDriverUrlName) return
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

//#region ===== FILTER SERVICE ======

/* 
    Creating parameters for filter service, for fixed drivers only value is sent, for output parameters the description is same as the value, description doesn't seem to be doing anything anyway
*/
const createDocumentNavigationParametersForFilterService = (formattedCrossNavigationParameters: ICrossNavigationParameter[]) => {
    const documentNavigationParamsForFilterService = {}
    formattedCrossNavigationParameters.forEach((formattedCrossNavigationParameter: ICrossNavigationParameter) => {
        if (formattedCrossNavigationParameter.type === 'fixed') {
            documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName] = formattedCrossNavigationParameter.parameterValue[0].value
        } else if (formattedCrossNavigationParameter.type === 'fromSourceDocumentDriver') {
            const valueAndDescription = getValueAndDescriptionForFilterServiceFromSourceDocumentDriverCrossNavigationParameter(formattedCrossNavigationParameter) as { value: string | number | (string | number)[] | null, description: string | string[] | null }
            documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName] = valueAndDescription.value
            if (valueAndDescription.description) documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName + '_field_visible_description'] = valueAndDescription.description
        } else if (formattedCrossNavigationParameter.type === 'fromSourceDocumentOutputParameter') {
            const value = getValueForFilterServiceFromSourceDocumentOutputParameter(formattedCrossNavigationParameter)
            documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName] = value
            documentNavigationParamsForFilterService[formattedCrossNavigationParameter.targetDriverUrlName + '_field_visible_description'] = value
        }
    })
    return documentNavigationParamsForFilterService
}

/* 
    Creating parameter value and description for filter service from source document drivers, getting the value and description in proper format
*/
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

/* 
    Creating parameter value for filter service from source document output parameters
*/
const getValueForFilterServiceFromSourceDocumentOutputParameter = (formattedCrossNavigationParameter: ICrossNavigationParameter) => {
    const values = [] as (string | number)[]
    for (let i = 0; i < formattedCrossNavigationParameter.parameterValue.length; i++) {
        const tempValue = formattedCrossNavigationParameter.parameterValue[i].value;
        const formattedParameterValue = formattedCrossNavigationParameter.parameterType === 'DATE' ? getDateStringFromMilliseconds(tempValue) : tempValue
        values.push(formattedParameterValue)
    }
    return values.length === 1 ? values[0] : values
}
//#endregion ===== FILTER SERVICE ======


const getDateStringFromMilliseconds = (miliseconds: any) => {
    if (!miliseconds || isNaN(miliseconds)) return ''
    const date = new Date(+miliseconds)
    const dateAsString = getDateStringFromJSDate(new Date(date), 'MM/dd/y')
    return dateAsString ?? ''
}


const addSourceDocumentCrossNavigationParameterFromOutputParameter = (documentCrossNavigationParameter: IDocumentNavigationParameter, documentCrossNavigationOutputParameters: ICrossNavigationParameter[], formattedCrossNavigationParameters: ICrossNavigationParameter[]) => {
    const selectedDocumentCrossNavigationParameter = documentCrossNavigationOutputParameters.find((tempParameter: ICrossNavigationParameter) => tempParameter.outputDriverName === documentCrossNavigationParameter.sourceDriverUrlName)
    if (selectedDocumentCrossNavigationParameter) formattedCrossNavigationParameters.push({ ...selectedDocumentCrossNavigationParameter, targetDriverUrlName: documentCrossNavigationParameter.id })
}


//#region ===== INITIAL VALUES ======
export const loadNavigationInitialValuesFromDashboard = (document: any, filtersData: { filterStatus: iParameter[], isReadyForExecution: boolean }, dateFormat: string) => {
    document.formattedCrossNavigationParameters.forEach((crossNavigationParameter: ICrossNavigationParameter) => {
        const index = filtersData.filterStatus.findIndex((parameter: iParameter) => parameter.urlName === crossNavigationParameter.targetDriverUrlName)
        if (index !== -1) loadDriverInitialValue(filtersData.filterStatus[index], crossNavigationParameter, dateFormat)
    })
}

const loadDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter, dateFormat: string) => {
    if (!crossNavigationParameter.parameterValue || !crossNavigationParameter.parameterValue[0]) return
    if (!parameter.selectionType && parameter.valueSelection === 'man_in') {
        if (parameter.type === 'STRING') loadManualStringDriverInitialValue(parameter, crossNavigationParameter)
        else if (parameter.type === 'NUM') loadManualNumberDriverInitialValue(parameter, crossNavigationParameter)
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
    parameter.parameterValue[0] = { value: "" + crossNavigationParameter.parameterValue[0].value, description: "" + crossNavigationParameter.parameterValue[0].description }
}

const loadManualNumberDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    parameter.parameterValue[0] = { value: getFormattedNumberValue(crossNavigationParameter.parameterValue[0].value), description: "" + crossNavigationParameter.parameterValue[0].description }
}

const getFormattedNumberValue = (value: string | number) => {
    if (typeof value === 'number') return value
    const parsedValue = parseFloat(value)
    return isNaN(parsedValue) ? null : parsedValue
}

const loadDateDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter, dateFormat: string) => {
    console.log('---------- loadDateDriverInitialValue - parameter: ', parameter)
    console.log('---------- loadDateDriverInitialValue - crossNavigationParameter: ', crossNavigationParameter)
    if (crossNavigationParameter.type === 'fixed') {
        const date = moment(crossNavigationParameter.parameterValue[0].value, 'MM/DD/YYYY')
        if (date.isValid()) parameter.parameterValue[0].value = date.toDate()
    }
    else if (typeof crossNavigationParameter.parameterValue[0].value !== 'number') {
        if (parameter.parameterValue[0].value instanceof Date) parameter.parameterValue[0].value = null
        return
    } else {
        const dateValue = new Date(crossNavigationParameter.parameterValue[0].value)
        const dateDescription = getDateStringFromJSDate(dateValue, dateFormat)
        parameter.parameterValue[0] = { value: dateValue, description: dateDescription ?? '' }
    }
}

const loadListDropdownDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    if (parameter.multivalue) {
        loadListComboboxMultiInitialValue(parameter, crossNavigationParameter)
    } else {
        loadListComboboxSingleInitialValue(parameter, crossNavigationParameter)
    }
}

const loadListComboboxSingleInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
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
    // TODO - see for NON ADMISSABLE
    if (parameter.multivalue) {
        parameter.parameterValue = crossNavigationParameter.parameterValue
    } else {
        parameter.parameterValue = crossNavigationParameter.parameterValue
    }
}

const loadTreeDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // TODO - see for NON ADMISSABLE
    if (parameter.multivalue) {
        parameter.parameterValue = crossNavigationParameter.parameterValue
    } else {
        parameter.parameterValue = crossNavigationParameter.parameterValue
    }
}

//#endregion ===== INITIAL VALUES ======

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