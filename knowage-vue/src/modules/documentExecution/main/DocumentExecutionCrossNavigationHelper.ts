import { IDashboardCrossNavigation } from "../dashboard/Dashboard"
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { IDocumentNavigationParameter, ICrossNavigationParameter, ICrossNavigationBreadcrumb, IDocumentNavigationParameterValue } from "./DocumentExecution"
import { getDateStringFromJSDate } from "@/helpers/commons/localeHelper"
import moment from "moment"




/* 
    If the widget has selected cross navigation get it from the list of cross navigations that we got from the BE service
*/
export const getSelectedCrossNavigation = (crossNavigationName: string | undefined, crossNavigations: IDashboardCrossNavigation[]) => {
    const index = crossNavigations.findIndex((crossNavigation: IDashboardCrossNavigation) => crossNavigation.crossName === crossNavigationName)
    return index !== -1 ? crossNavigations[index] : null
}


/* 
    Creates target document with formatted crossNavigationParameters (for loading initial values for Target Document drivers) and navigationParams for filter service
*/
export const getDocumentForCrossNavigation = (documentCrossNavigationOutputParameters: ICrossNavigationParameter[], sourceFiltersData: { filterStatus: iParameter[], isReadyForExecution: boolean }, selectedCrossNavigation: IDashboardCrossNavigation | null) => {
    console.log('------  !!! selectedCrossNavigation: ', selectedCrossNavigation)
    if (!selectedCrossNavigation) return null
    const formattedCrossNavigationParameters = getFormattedCrossNavigationParameters(selectedCrossNavigation, sourceFiltersData, documentCrossNavigationOutputParameters)
    const crossNavigationDocument = selectedCrossNavigation.document
    crossNavigationDocument.formattedCrossNavigationParameters = formattedCrossNavigationParameters
    crossNavigationDocument.navigationParams = createDocumentNavigationParametersForFilterService(formattedCrossNavigationParameters)
    crossNavigationDocument.navigationFromDashboard = true

    formatDocumentBreadcrumbLabel(selectedCrossNavigation, crossNavigationDocument)
    // console.log(">>>>>>>>>>>     document.formattedCrossNavigationParameters: ", crossNavigationDocument.navigationParams)
    return crossNavigationDocument
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
    const parameterValue = getFormattedSourceDocumentParameterValue(sourceDocumentDriver)

    const crossNavigationParameter = {
        sourceDriverName: sourceDocumentDriver.label,
        targetDriverUrlName: targetDriverUrlName,
        parameterValue: parameterValue,
        multivalue: sourceDocumentDriver.multivalue,
        type: 'fromSourceDocumentDriver',
        selectionType: sourceDocumentDriver.selectionType ?? null,
        parameterType: sourceDocumentDriver.type,
    } as ICrossNavigationParameter
    return crossNavigationParameter
}

const getFormattedSourceDocumentParameterValue = (sourceDocumentDriver: iParameter) => {
    if (sourceDocumentDriver.type === 'DATE') {
        return getDateValueFromSourceDocumentDriverAsMilliseconds(sourceDocumentDriver.parameterValue)
    } else if (!sourceDocumentDriver.multivalue && sourceDocumentDriver.parameterValue.length > 1) {
        return [sourceDocumentDriver.parameterValue[0]]
    } else {
        return sourceDocumentDriver.parameterValue
    }
}

const getDateValueFromSourceDocumentDriverAsMilliseconds = (parameterValue: { value: string | number | Date | null; description: string }[]) => {
    if (!parameterValue[0] || !parameterValue[0].value) return [{ value: '', description: '' }]
    if (parameterValue[0].value instanceof Date) {
        return [{ value: parameterValue[0].value.valueOf(), descripton: '' }]
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
    let valueAndDescription = { value: '', description: null } as { value: string | number | (string | number)[] | null, description: string | string[] | null }
    if (!formattedCrossNavigationParameter.selectionType && parameterValue[0]) {
        if (formattedCrossNavigationParameter.parameterType === 'STRING') valueAndDescription = { value: parameterValue[0].value, description: '' + parameterValue[0].value }
        else if (formattedCrossNavigationParameter.parameterType === 'NUM' && parameterValue[0].value) valueAndDescription = { value: +parameterValue[0].value, description: "" + parameterValue[0].value }
        else if (formattedCrossNavigationParameter.parameterType === 'DATE') {
            const dateValue = getDateStringFromJSDate(new Date(parameterValue[0].value), 'dd/MM/y')
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
    } else if (formattedCrossNavigationParameter.selectionType === 'LOOKUP' || formattedCrossNavigationParameter.selectionType === 'TREE') {
        valueAndDescription.value = parameterValue.map((tempValue: { value: string | number; description: string }) => tempValue.value)
        valueAndDescription.description = parameterValue.map((tempValue: { value: string | number; description: string }) => tempValue.description)
    }
    return valueAndDescription

}

/* 
    Creating parameter value for filter service from source document output parameters
*/
const getValueForFilterServiceFromSourceDocumentOutputParameter = (formattedCrossNavigationParameter: ICrossNavigationParameter) => {
    // console.log('-------- formattedCrossNavigationParameter: ', formattedCrossNavigationParameter)
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
    const dateAsString = getDateStringFromJSDate(new Date(date), 'dd/MM/y')
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
    if (parameter.type !== 'DATE' && crossNavigationParameter.parameterType === 'DATE') formattDateCrossNavigatonParameterForNonDateDriver(crossNavigationParameter, dateFormat)
    if (!parameter.selectionType && parameter.valueSelection === 'man_in') {
        if (parameter.type === 'STRING') loadManualStringDriverInitialValue(parameter, crossNavigationParameter)
        else if (parameter.type === 'NUM') loadManualNumberDriverInitialValue(parameter, crossNavigationParameter)
        else if (parameter.type === 'DATE') loadDateDriverInitialValue(parameter, crossNavigationParameter, dateFormat)
    } else if (parameter.selectionType === 'LIST' || parameter.selectionType === 'COMBOBOX') {
        loadListDropdownDriverInitialValue(parameter, crossNavigationParameter)
    } else if (parameter.selectionType === 'LOOKUP' || parameter.selectionType === 'TREE') {
        loadPopupAndTreeDriverInitialValue(parameter, crossNavigationParameter)
    }
}

const formattDateCrossNavigatonParameterForNonDateDriver = (crossNavigationParameter: ICrossNavigationParameter, dateFormat: string) => {
    crossNavigationParameter.parameterValue.forEach((parameterValue: { value: string | number, description: string }) => {
        const dateValue = new Date(parameterValue.value)
        const dateDescription = getDateStringFromJSDate(dateValue, dateFormat) ?? '' + parameterValue.value
        parameterValue.value = dateDescription
        parameterValue.description = dateDescription
    })
}

const loadManualStringDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log('---------- loadManualStringDriverInitialValue - parameter: ', parameter)
    // console.log('---------- loadManualStringDriverInitialValue - crossNavigationParameter: ', crossNavigationParameter)
    const value = getCrossNavigationParameterValuesAsString(crossNavigationParameter)
    parameter.parameterValue[0] = { value: value, description: value }
}

const getCrossNavigationParameterValuesAsString = (crossNavigationParameter: ICrossNavigationParameter) => {
    let value = ''
    crossNavigationParameter.parameterValue.forEach((parameterValue: { value: string | number, description: string }, index) => value += index === crossNavigationParameter.parameterValue.length - 1 ? parameterValue.value : parameterValue.value + ',')
    return value
}

const loadManualNumberDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log('---------- loadManualNumberDriverInitialValue - parameter: ', parameter)
    // console.log('---------- loadManualNumberDriverInitialValue - crossNavigationParameter: ', crossNavigationParameter)
    parameter.parameterValue[0] = { value: getFormattedNumberValue(crossNavigationParameter.parameterValue[0].value), description: "" + crossNavigationParameter.parameterValue[0].description }
}

const getFormattedNumberValue = (value: string | number) => {
    if (typeof value === 'number') return value
    const parsedValue = parseFloat(value)
    return isNaN(parsedValue) ? null : parsedValue
}

const loadDateDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter, dateFormat: string) => {
    console.log('---------- loadDateDriverInitialValue - parameter: ', parameter)
    console.log('---------- loadDriverInitialValue - crossNavigationParameter: ', crossNavigationParameter)
    if (crossNavigationParameter.type === 'fixed') {
        const date = moment(crossNavigationParameter.parameterValue[0].value, 'MM/DD/YYYY', true)
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
    // console.log('---------- loadListComboboxSingleInitialValue - parameter: ', parameter)
    // console.log('---------- loadListComboboxSingleInitialValue - crossNavigationParameter: ', crossNavigationParameter)
    const parameterValue = getListComboboxCrossNavigationValue(parameter, crossNavigationParameter.parameterValue[0].value)
    if (parameterValue) parameter.parameterValue = [parameterValue]
}

const loadListComboboxMultiInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    //  console.log('---------- loadListComboboxMultiInitialValue - parameter: ', parameter)
    // console.log('---------- loadListComboboxMultiInitialValue - crossNavigationParameter: ', crossNavigationParameter)
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

const loadPopupAndTreeDriverInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // TODO - see for NON ADMISSABLE
    //console.log('---------- loadPopupAndTreeDriverInitialValue - parameter: ', parameter)
    //  console.log('---------- loadPopupAndTreeDriverInitialValue - crossNavigationParameter: ', crossNavigationParameter)
    addMissingDescriptionForPopupAndTreeDriver(crossNavigationParameter)
    if (parameter.multivalue) {
        loadPopupAndTreeDriverMultiInitialValue(parameter, crossNavigationParameter)
    } else {
        loadPopupAndTreeDriverSingleInitialValue(parameter, crossNavigationParameter)
    }
}

const loadPopupAndTreeDriverSingleInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log('---------- loadPopupAndTreeDriverInitialValue - parameter: ', deepcopy(parameter))
    // console.log('---------- loadPopupAndTreeDriverInitialValue - crossNavigationParameter: ', deepcopy(crossNavigationParameter))
    if (parameter.parameterValue[0]?.description === 'NOT ADMISSIBLE') {
        parameter.parameterValue = [{ value: '', description: '' }]
        return
    }
    parameter.parameterValue = crossNavigationParameter.parameterValue[0] ? [crossNavigationParameter.parameterValue[0]] : []
}

const loadPopupAndTreeDriverMultiInitialValue = (parameter: iParameter, crossNavigationParameter: ICrossNavigationParameter) => {
    // console.log('---------- loadPopupAndTreeDriverInitialValue - parameter: ', deepcopy(parameter))
    // console.log('---------- loadPopupAndTreeDriverInitialValue - crossNavigationParameter: ', deepcopy(crossNavigationParameter))
    const parameterValues = [] as { value: string | number, description: string }[]
    crossNavigationParameter.parameterValue.forEach((parameterValue: { value: string | number, description: string }, index) => {
        if (parameter.parameterDescription[index] !== 'NOT ADMISSIBLE') parameterValues.push(parameterValue)
    })
    parameter.parameterValue = parameterValues
}


const addMissingDescriptionForPopupAndTreeDriver = (crossNavigationParameter: ICrossNavigationParameter) => {
    crossNavigationParameter.parameterValue.forEach((parameterValue: { value: string | number, description: string }) => {
        if (parameterValue.description === '' || parameterValue.description === null) parameterValue.description = '' + parameterValue.value
    })
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
            crossBreadcrumb: document.crossBreadCrumb ?? document.name
        })
    }
}

const formatDocumentBreadcrumbLabel = (crossNavigation: IDashboardCrossNavigation, crossNavigationDocument: any) => {
    if (!crossNavigation.crossBreadcrumb) return
    crossNavigationDocument.crossBreadCrumb = crossNavigation.crossBreadcrumb.includes('$P{') ? updateCrossBreadCrumbWithParameterValues(crossNavigation.crossBreadcrumb, crossNavigationDocument) : crossNavigation.crossBreadcrumb
}

const updateCrossBreadCrumbWithParameterValues = (crossBreadCrumb: string, crossNavigationDocument: any) => {
    const crossNavigationParameters = crossNavigationDocument.formattedCrossNavigationParameters as ICrossNavigationParameter[]
    if (!crossNavigationParameters || crossNavigationParameters.length === 0) return crossBreadCrumb
    const parameterPlaceholders = crossBreadCrumb.match(/(?<=\$P\{).*?(?=\})/g)
    const parameterPlaceholderMapWithValues = {}
    getvaluesforparameterplaceholders(parameterPlaceholders, crossNavigationParameters, parameterPlaceholderMapWithValues)
    return replacePlaceholdersWithRealValues(crossBreadCrumb, parameterPlaceholderMapWithValues)
}

const getvaluesforparameterplaceholders = (parameterPlaceholders: string[] | null, crossNavigationParameters: ICrossNavigationParameter[], parameterPlaceholderMapWithValues: any) => {
    parameterPlaceholders?.forEach((parameterPlaceholder: string) => {
        const driverName = parameterPlaceholder
        const index = crossNavigationParameters.findIndex((crossNavigationParameter: ICrossNavigationParameter) => crossNavigationParameter.sourceDriverName === driverName || crossNavigationParameter.outputDriverName === driverName)
        let value = ''
        if (index !== -1) value = getCrossNavigationParameterValuesAsString(crossNavigationParameters[index])

        parameterPlaceholderMapWithValues[parameterPlaceholder] = value
    })
}

const replacePlaceholdersWithRealValues = (crossBreadCrumb: string, parameterPlaceholderMapWithValues: any) => {
    let finalString = crossBreadCrumb
    Object.keys(parameterPlaceholderMapWithValues).forEach((key: string) => {
        finalString = finalString.replaceAll('$P{' + key + '}', parameterPlaceholderMapWithValues[key])
    })
    return finalString
}
