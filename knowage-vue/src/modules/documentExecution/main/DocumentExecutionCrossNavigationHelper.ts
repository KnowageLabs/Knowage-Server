import deepcopy from 'deepcopy'
import moment from 'moment'

export function findCrossTargetByCrossName(angularData: any, temp: any[]) {
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
                        tempParam.parameterValue[0].value = getValidDate(tempParam.parameterValue[0].value, vueComponent.dateFormat)
                    }
                }
                if (tempParam.selectionType === 'COMBOBOX') formatCrossNavigationComboParameterDescription(tempParam)
                else if (['TREE', 'LOOKUP'].includes(tempParam.selectionType) && tempParam.parameterValue[0]) tempParam.parameterValue[0].description = tempParam.parameterValue[0].value
            }
        }
    })
}

function checkIfMultivalueDriverContainsCrossNavigationValue(tempParam: any, crossNavigationValue: any) {
    if (!['LIST', 'COMBOBOX'].includes(tempParam.selectionType)) return true
    const index = tempParam.data.findIndex((option: { value: string; description: string }) => option.value == crossNavigationValue)
    return index !== -1
}

export function getValidDate(value: string, serverDateFormat: string) {
    const extractedDateValue = extractDatePart(value)
    let momentDate = moment(deepcopy(extractedDateValue))
    const tempServerDateFormat = serverDateFormat.replaceAll('y', 'Y')
    const validFormats = [tempServerDateFormat, 'DD/MM/YYYY', 'DD/MM/YYYY HH:mm:ss.SSS']
    let tempDateFormatFromTheDateValue = extractDateFormatPart(value)
    if (tempDateFormatFromTheDateValue) {
        tempDateFormatFromTheDateValue = tempDateFormatFromTheDateValue === 'yyyy-MM-dd' ? 'YYYY-MM-DD' : tempDateFormatFromTheDateValue
        validFormats.unshift(tempDateFormatFromTheDateValue)
    }
    for (let i = 0; i < validFormats.length; i++) {
        momentDate = moment(deepcopy(extractedDateValue), validFormats[i])
        if (momentDate.isValid()) return momentDate.toDate()
    }
    return ''
}

function extractDatePart(dateString: string) {
    if (dateString.includes('#')) {
        return dateString.split('#')[0]
    }
    return dateString
}

function extractDateFormatPart(dateString: string) {
    if (dateString.includes('#')) {
        return dateString.split('#')[1]
    }
    return dateString
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
