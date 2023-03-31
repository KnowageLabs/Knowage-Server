import { AxiosResponse } from 'axios'
import deepcopy from 'deepcopy'
import moment from 'moment'

export const executeAngularCrossNavigation = async (vueComponent: any, event: any, $http: any) => {
    vueComponent.angularData = event.data
    await loadCrossNavigationByDocument(vueComponent, event.data, $http)
}

const loadCrossNavigationByDocument = async (vueComponent: any, angularData: any, $http: any) => {
    if (!vueComponent.document) return

    let temp = {} as any

    vueComponent.loadingCrossNavigationDocument = true
    await $http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/crossNavigation/${vueComponent.document.label}/loadCrossNavigationByDocument`).then((response: AxiosResponse<any>) => (temp = response.data))
    vueComponent.loadingCrossNavigationDocument = false

    if (temp.length === 0) return

    const crossTarget = findCrossTargetByCrossName(angularData, temp)

    if (!crossTarget && temp.length > 1) {
        vueComponent.crossNavigationDocuments = temp
        vueComponent.destinationSelectDialogVisible = true
    } else {
        loadCrossNavigation(vueComponent, crossTarget ?? temp[0], angularData)
    }
}

export const loadCrossNavigation = async (vueComponent: any, crossNavigationDocument: any, angularData: any) => {
    formatAngularOutputParameters(vueComponent, angularData.otherOutputParameters)
    const navigationParams = formatNavigationParams(vueComponent, angularData.otherOutputParameters, crossNavigationDocument ? crossNavigationDocument.navigationParams : [])
    addDocumentOtherParametersToNavigationParamas(vueComponent, navigationParams, angularData, crossNavigationDocument)

    const popupOptions = crossNavigationDocument?.popupOptions ? JSON.parse(crossNavigationDocument.popupOptions) : null

    checkIfParameterHasFixedValue(navigationParams, crossNavigationDocument)

    if (crossNavigationDocument?.crossType !== 2) {
        vueComponent.document = {
            ...crossNavigationDocument?.document,
            navigationParams: navigationParams
        }
    }

    if (crossNavigationDocument?.crossType === 2) {
        openCrossNavigationInNewWindow(vueComponent, popupOptions, crossNavigationDocument, navigationParams)
    } else if (crossNavigationDocument?.crossType === 1) {
        const documentLabel = crossNavigationDocument?.document.label
        vueComponent.crossNavigationContainerData = {
            documentLabel: documentLabel,
            iFrameName: documentLabel
        }
        vueComponent.crossNavigationContainerVisible = true
        await vueComponent.loadPage(false, documentLabel, true)
    } else {
        const index = vueComponent.breadcrumbs.findIndex((el: any) => el.label === vueComponent.document.name)
        if (index !== -1) {
            vueComponent.breadcrumbs[index].document = vueComponent.document
        } else {
            vueComponent.breadcrumbs.push({
                label: vueComponent.document.name,
                document: vueComponent.document,
                crossBreadcrumb: getCrossBeadcrumb(crossNavigationDocument, angularData, document)
            })
        }

        await vueComponent.loadPage()
    }
    vueComponent.documentMode = 'VIEW'
}

const formatAngularOutputParameters = (vueComponent: any, otherOutputParameters: any[]) => {
    const startDocumentInputParameters = deepcopy(vueComponent.document.drivers)
    const keys = [] as any[]
    otherOutputParameters.forEach((parameter: any) => keys.push(Object.keys(parameter)[0]))
    for (let i = 0; i < startDocumentInputParameters.length; i++) {
        if (!keys.includes(startDocumentInputParameters[i].label)) {
            const tempObject = {} as any
            tempObject[startDocumentInputParameters[i].label] = getParameterValueForCrossNavigation(vueComponent, startDocumentInputParameters[i].label)
            otherOutputParameters.push(tempObject)
        }
    }
}

const getParameterValueForCrossNavigation = (vueComponent: any, parameterLabel: string) => {
    if (!parameterLabel) return
    const index = vueComponent.filtersData.filterStatus?.findIndex((param: any) => param.label === parameterLabel)
    return index !== -1 ? vueComponent.filtersData.filterStatus[index].parameterValue[0].value : ''
}

const formatNavigationParams = (vueComponent: any, otherOutputParameters: any[], navigationParams: any) => {
    const formatedParams = {} as any

    otherOutputParameters.forEach((el: any) => {
        let found = false
        let label = ''

        for (let i = 0; i < Object.keys(navigationParams).length; i++) {
            if (navigationParams[Object.keys(navigationParams)[i]].value.label === Object.keys(el)[0]) {
                found = true
                label = Object.keys(navigationParams)[i]
                break
            }
        }

        if (found) {
            formatedParams[label] = el[Object.keys(el)[0]]
            formatedParams[label + '_field_visible_description'] = el[Object.keys(el)[0]]
        }
    })

    setNavigationParametersFromCurrentFilters(vueComponent, formatedParams, navigationParams)

    return formatedParams
}

const setNavigationParametersFromCurrentFilters = (vueComponent: any, formatedParams: any, navigationParams: any) => {
    const navigationParamsKeys = navigationParams ? Object.keys(navigationParams) : []
    const formattedParameters = vueComponent.getFormattedParameters()
    const formattedParametersKeys = formattedParameters ? Object.keys(formattedParameters) : []
    if (navigationParamsKeys.length > 0 && formattedParametersKeys.length > 0) {
        for (let i = 0; i < navigationParamsKeys.length; i++) {
            const index = formattedParametersKeys.findIndex((key: string) => key === navigationParams[navigationParamsKeys[i]].value.label && navigationParams[navigationParamsKeys[i]].value.isInput)
            if (index !== -1) {
                formatedParams[navigationParamsKeys[i]] = formattedParameters[formattedParametersKeys[index]]
                formatedParams[navigationParamsKeys[i] + '_field_visible_description'] = formattedParameters[formattedParametersKeys[index] + '_field_visible_description'] ? formattedParameters[formattedParametersKeys[index] + '_field_visible_description'] : ''
            }
        }
    }
}


const addDocumentOtherParametersToNavigationParamas = (vueComponent: any, navigationParams: any[], angularData: any, crossNavigationDocument: any) => {
    if (!angularData.outputParameters || angularData.outputParameters.length === 0 || !crossNavigationDocument?.navigationParams) return
    const keys = Object.keys(angularData.outputParameters)
    const documentNavigationParamsKeys = Object.keys(crossNavigationDocument.navigationParams)
    for (let i = 0; i < keys.length; i++) {
        const tempKey = keys[i]
        let newKey = ''
        for (let j = 0; j < documentNavigationParamsKeys.length; j++) {
            if (crossNavigationDocument.navigationParams[documentNavigationParamsKeys[j]].value?.label === tempKey) {
                newKey = documentNavigationParamsKeys[j]
            }
        }
        if (newKey) navigationParams[newKey] = angularData.outputParameters[tempKey]
    }

    addSourceDocumentParameterValuesFromDocumentNavigationParameters(vueComponent, navigationParams, crossNavigationDocument)
}

const addSourceDocumentParameterValuesFromDocumentNavigationParameters = (vueComponent: any, navigationParams: any[], crossNavigationDocument: any) => {
    const documentNavigationParamsKeys = Object.keys(crossNavigationDocument.navigationParams)
    documentNavigationParamsKeys.forEach((key: string) => {
        if (!navigationParams[key]) {
            const sourceParameter = vueComponent.filtersData.filterStatus.find((parameter: any) => {
                return parameter.urlName === crossNavigationDocument.navigationParams[key].value.label
            })
            if (sourceParameter) {
                navigationParams[key] = sourceParameter.parameterValue[0].value ?? ''
                navigationParams[key + '_field_visible_description'] = sourceParameter.parameterValue[0].description ?? ''
            }
        }
    })
}


const checkIfParameterHasFixedValue = (navigationParams: any, crossNavigationDocument: any) => {
    if (!crossNavigationDocument || !crossNavigationDocument.navigationParams) return
    Object.keys(crossNavigationDocument.navigationParams).forEach((key: string) => {
        const tempParam = crossNavigationDocument.navigationParams[key]
        if (tempParam.fixed) {
            navigationParams[key] = tempParam.value
            navigationParams[key + '_field_visible_description'] = tempParam.value
        }
    })
}

const openCrossNavigationInNewWindow = (vueComponent: any, popupOptions: any, crossNavigationDocument: any, navigationParams: any) => {
    if (!crossNavigationDocument || !crossNavigationDocument.document) return
    const parameters = encodeURI(JSON.stringify(navigationParams))
    const url =
        import.meta.env.VITE_HOST_URL +
        `/knowage/restful-services/publish?PUBLISHER=documentExecutionNg&OBJECT_ID=${crossNavigationDocument.document.id}&OBJECT_LABEL=${crossNavigationDocument.document.label}&SELECTED_ROLE=${vueComponent.sessionRole}&SBI_EXECUTION_ID=null&OBJECT_NAME=${crossNavigationDocument.document.name}&CROSS_PARAMETER=${parameters}`
    window.open(url, '_blank', `toolbar=0,status=0,menubar=0,width=${popupOptions.width || '800'},height=${popupOptions.height || '600'}`)
}

function findCrossTargetByCrossName(angularData: any, temp: any[]) {
    if (!angularData || !temp) return
    const targetCross = typeof angularData.targetCrossNavigation === 'string' ? angularData.targetCrossNavigation : angularData.targetCrossNavigation.crossName
    const index = temp.findIndex((el: any) => el.crossName === targetCross)
    return index !== -1 ? temp[index] : null
}

export function loadNavigationParamsInitialValue(vueComponent: any) {
    Object.keys(vueComponent.document.navigationParams).forEach((key: string) => {
        for (let i = 0; i < vueComponent.filtersData.filterStatus.length; i++) {
            const tempParam = vueComponent.filtersData.filterStatus[i]
            if (key === tempParam.urlName || key === tempParam.label) {
                if (tempParam.multivalue && Array.isArray(vueComponent.document.navigationParams[key])) {
                    tempParam.parameterValue = vueComponent.document.navigationParams[key].map((value: string) => {
                        return { value: value, description: '' }
                    })
                } else {
                    const crossNavigationValue = Array.isArray(vueComponent.document.navigationParams[key]) && vueComponent.document.navigationParams[key][0] ? vueComponent.document.navigationParams[key][0] : vueComponent.document.navigationParams[key]
                    let parameterDescription = vueComponent.document.navigationParams[key + '_field_visible_description']
                    if (!parameterDescription) parameterDescription = tempParam.parameterValue[0] ? tempParam.parameterValue[0].description : ''
                    if (tempParam.parameterValue[0] && tempParam.parameterValue[0].value === '') tempParam.parameterValue = []
                    if (!checkIfMultivalueDriverContainsCrossNavigationValue(tempParam, crossNavigationValue) || parameterDescription === 'NOT ADMISSIBLE') return
                    if (crossNavigationValue) tempParam.parameterValue[0] = { value: crossNavigationValue, description: parameterDescription }
                    if (tempParam.type === 'DATE' && tempParam.parameterValue[0] && tempParam.parameterValue[0].value) {
                        tempParam.parameterValue[0].value = getValidDate(tempParam.parameterValue[0].value)
                    }
                }
                if (tempParam.selectionType === 'COMBOBOX') formatCrossNavigationComboParameterDescription(tempParam)
            }
        }
    })
}

function checkIfMultivalueDriverContainsCrossNavigationValue(tempParam: any, crossNavigationValue: any) {
    if (!['LIST', 'COMBOBOX'].includes(tempParam.selectionType)) return true
    const index = tempParam.data.findIndex((option: { value: string; description: string }) => option.value == crossNavigationValue)
    return index !== -1
}

function getValidDate(value: string) {
    let momentDate = moment(deepcopy(value))
    if (momentDate.isValid()) return momentDate.toDate()
    const validFormats = ['DD/MM/YYYY', 'DD/MM/YYYY HH:mm:ss.SSS']
    for (let i = 0; i < validFormats.length; i++) {
        momentDate = moment(deepcopy(value), validFormats[i])
        if (momentDate.isValid()) return momentDate.toDate()
    }
    return ''
}

function formatCrossNavigationComboParameterDescription(tempParam: any) {
    for (let i = tempParam.parameterValue.length - 1; i >= 0; i--) {
        if (tempParam.parameterValue[i].value) {
            const index = tempParam.data.findIndex((option: any) => option.value == tempParam.parameterValue[i].value)
            if (index !== -1) {
                tempParam.parameterValue[i] = { value: tempParam.data[index].value, description: tempParam.data[index].description }
            } else tempParam.parameterValue.splice(i, 1)
        }
    }
}

const getCrossBeadcrumb = (crossNavigationDocument: any, angularData: any, document: any) => {
    let tempCrossBreadcrumb = crossNavigationDocument?.crossBreadcrumb
    if (tempCrossBreadcrumb?.includes('$P{')) {
        tempCrossBreadcrumb = updateCrossBreadCrumbWithParameterValues(tempCrossBreadcrumb, angularData)
    }
    return tempCrossBreadcrumb ?? document.name
}

const updateCrossBreadCrumbWithParameterValues = (tempCrossBreadcrumb: string, angularData: any) => {
    const parameterPlaceholders = tempCrossBreadcrumb.match(/{[\w\d]+}/g)
    if (!parameterPlaceholders) return ''
    const parameters = angularData.outputParameters
    for (let i = 0; i < angularData.otherOutputParameters.length; i++) {
        const key = Object.keys(angularData.otherOutputParameters[i])[0]
        parameters[key] = angularData.otherOutputParameters[i][key]
    }
    const temp = [] as any[]
    for (let i = 0; i < parameterPlaceholders.length; i++) {
        const tempParameterName = parameterPlaceholders[i].substring(1, parameterPlaceholders[i].length - 1)
        temp.push({
            parameterPlaceholder: parameterPlaceholders[i],
            value: parameters[tempParameterName]
        })
    }
    let finalString = tempCrossBreadcrumb
    for (let i = 0; i < temp.length; i++) {
        finalString = finalString.replaceAll('$P' + temp[i].parameterPlaceholder, temp[i].value)
    }
    return finalString
}