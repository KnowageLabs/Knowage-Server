import moment from 'moment'

export function formatDrivers(filtersData: any) {
    filtersData?.filterStatus?.forEach((el: any) => {
        el.parameterValue = el.multivalue ? [] : [{ value: '', description: '' }]
        if (el.driverDefaultValue?.length > 0) {
            let valueIndex = '_col0'
            let descriptionIndex = 'col1'
            if (el.metadata?.colsMap) {
                valueIndex = Object.keys(el.metadata?.colsMap).find((key: string) => el.metadata.colsMap[key] === el.metadata.valueColumn) as any
                descriptionIndex = Object.keys(el.metadata?.colsMap).find((key: string) => el.metadata.colsMap[key] === el.metadata.descriptionColumn) as any
            }

            el.parameterValue = el.driverDefaultValue.map((defaultValue: any) => {
                return { value: defaultValue.value ?? defaultValue[valueIndex], description: defaultValue.desc ?? defaultValue[descriptionIndex] }
            })

            if (el.type === 'DATE' && !el.selectionType && el.valueSelection === 'man_in' && el.showOnPanel === 'true') {
                el.parameterValue[0].value = moment(el.parameterValue[0].description?.split('#')[0]).toDate() as any
            }
        }
        if (el.data) {
            el.data = el.data.map((data: any) => {
                return formatParameterDataOptions(el, data)
            })

            if (el.data.length === 1) {
                el.parameterValue = [...el.data]
            }
        }
        if ((el.selectionType === 'COMBOBOX' || el.selectionType === 'LIST') && el.multivalue && el.mandatory && el.data.length === 1) {
            el.showOnPanel = 'false'
        }

        if (!el.parameterValue) {
            el.parameterValue = [{ value: '', description: '' }]
        }

        if (el.parameterValue[0] && !el.parameterValue[0].description) {
            el.parameterValue[0].description = el.parameterDescription ? el.parameterDescription[0] : ''
        }
    })
}

export function formatParameterDataOptions(parameter: any, data: any) {
    const valueColumn = parameter.metadata.valueColumn
    const descriptionColumn = parameter.metadata.descriptionColumn
    const valueIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === valueColumn)
    const descriptionIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === descriptionColumn)

    return { value: valueIndex ? data[valueIndex] : '', description: descriptionIndex ? data[descriptionIndex] : '' }
}