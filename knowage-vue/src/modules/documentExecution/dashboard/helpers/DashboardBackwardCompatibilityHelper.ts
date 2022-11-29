import { formatTableWidget } from './tableWidget/TableWidgetCompatibilityHelper'
import { formatSelectorWidget } from '@/modules/documentExecution/dashboard/helpers/selectorWidget/SelectorWidgetCompatibilityHelper'
import { IAssociation, IDashboard, IDashboardConfiguration, IDashboardDatasetDriver, IDataset, IDatasetParameter, ISelection, IVariable, IWidget, IWidgetColumn, IWidgetColumnFilter, IWidgetEditorDataset } from '../Dashboard'
import { formatSelectionWidget } from './selectionWidget/SelectionsWidgetCompatibilityHelper'
import { setVariableValueFromDataset } from '../generalSettings/VariablesHelper'
import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'
import { formatHTMLWidget } from './htmlWidget/HTMLWidgetCompatibilityHelper'
import { formatTextWidget } from './textWidget/TextWidgetCompatibilityHelper'
import moment from 'moment'

const datasetIdLabelMap = {}

export const formatModel = async (model: any, document: any, datasets: IDataset[], drivers: any[], profileAttributes: { name: string, value: string }[], $http: any) => {
    if (!model.sheets) return

    console.log(">>>>>>>> LOADED MODEL: ", model)

    loadDatasetIdNameMap(datasets)
    const formattedModel = {
        id: cryptoRandomString({ length: 16, type: 'base64' }),
        widgets: [],
        version: model.knowageVersion,
        configuration: await getFormattedModelConfiguration(model, document, drivers, profileAttributes, datasets, $http),
        sheets: []
    } as any
    for (let i = 0; i < model.sheets.length; i++) {
        const formattedSheet = formatSheet(model.sheets[i], formattedModel)
        formattedModel.sheets.push(formattedSheet)
    }

    console.log(">>>>>>>> LOADED FORMATTED MODEL: ", formattedModel)
    return formattedModel
}

const loadDatasetIdNameMap = (datasets: IDataset[]) => {
    if (!datasets) return
    datasets.forEach((dataset: IDataset) => {
        datasetIdLabelMap[dataset.label] = dataset.id.dsId
    })
}

const getDatasetId = (datasetLabel: string) => {
    return datasetIdLabelMap[datasetLabel]
}

const getFormattedModelConfiguration = async (model: any, document: any, drivers: any[], profileAttributes: { name: string, value: string }[], datasets: IDataset[], $http: any) => {
    const formattedConfiguration = { id: document.id, name: document.name, label: document.label, description: document.description, associations: getFormattedAssociations(model), datasets: getFormattedDatasets(model), variables: await getFormattedVariables(model, drivers, profileAttributes, datasets, $http), selections: getFormattedSelections(model), themes: {} } as IDashboardConfiguration

    return formattedConfiguration
}

const getFormattedAssociations = (model: any) => {
    if (!model.configuration || !model.configuration.associations) return []
    const formattedAssociations = [] as IAssociation[]
    for (let i = 0; i < model.configuration.associations.length; i++) {
        formattedAssociations.push(getFormattedAssociation(model.configuration.associations[i]))
    }
    return formattedAssociations
}

const getFormattedAssociation = (association: any) => {
    const formattedAssociation = { id: association.id, fields: [] } as IAssociation
    association.fields?.forEach((field: { column: string, store: string, type: string }) => formattedAssociation.fields.push({ column: field.column, dataset: getDatasetId(field.store) }))
    return formattedAssociation
}

const getFormattedDatasets = (model: any) => {
    if (!model.configuration || !model.configuration.datasets) return
    const formattedDatasets = [] as IWidgetEditorDataset[]
    for (let i = 0; i < model.configuration.datasets.length; i++) {
        formattedDatasets.push(getFormattedDataset(model.configuration.datasets[i]))
    }

    return formattedDatasets
}

const getFormattedDataset = (dataset: any) => {
    const formattedDataset = { id: dataset.dsId, dsLabel: dataset.dsLabel, cache: dataset.useCache } as IWidgetEditorDataset
    if (dataset.indexes) formattedDataset.indexes = dataset.indexes
    if (dataset.parameters) formattedDataset.parameters = getFormattedDatasetParameters(dataset)
    if (dataset.drivers) formattedDataset.drivers = getFormattedDatasetDrivers(dataset)

    return formattedDataset
}

const getFormattedDatasetDrivers = (dataset: any) => {
    if (!dataset.drivers || dataset.drivers.length === 0) return []
    console.log("---------------------- DATASET DRIVERS: ", dataset.drivers)
    const formattedDrivers = [] as IDashboardDatasetDriver[]
    dataset.drivers.forEach((driver: any) => formattedDrivers.push(getFormattedDatasetDriver(driver)))

    console.log("---------------------- FORMATTED DRIVERS: ", formattedDrivers)
    return formattedDrivers
}

const getFormattedDatasetDriver = (driver: any) => {
    //  console.log(">>>>>>> DRIVER: ", driver)
    const formattedDriver = { urlName: driver.urlName, type: driver.type, typeCode: driver.typeCode, selectionType: driver.selectionType, label: driver.label, multivalue: driver.multivalue } as IDashboardDatasetDriver
    getFormattedDriverProperties(driver, formattedDriver)
    // console.log(">>>>>>>>> FORMATTED DRIVER: ", formattedDriver)
    return formattedDriver
}

const getFormattedDriverProperties = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    if (driver.typeCode === 'MAN_IN' && (driver.type === 'NUM' || driver.type === 'STRING')) {
        driver.type === 'NUM' ? getFormattedManualNumberDriver(driver, formattedDriver) : getFormattedManualStringDriver(driver, formattedDriver)
    } else if (driver.type === 'DATE') {
        getFormattedDateDriver(driver, formattedDriver)
    } else if (driver.selectionType === 'LIST') {
        getFormattedListDriver(driver, formattedDriver)
    }
    else if (driver.selectionType === 'COMBOBOX') {
        getFormattedDropdownDriver(driver, formattedDriver)
    } else if (driver.selectionType === 'LOOKUP') {
        getFormattedPopupDriver(driver, formattedDriver)
    }
    // TODO - Tree

    getFormattedDriverDefaultValue(driver, formattedDriver)
}


const getFormattedManualStringDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
}


const getFormattedManualNumberDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
}

const getFormattedDateDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    console.log(">>>>>>> DRIVER: ", driver)
    formattedDriver.parameterValue = [{ value: driver.parameterValue, description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
    console.log(">>>>>>> FORMATTED DRIVER: ", formattedDriver)
}

const getFormattedListDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    formattedDriver.options = driver.defaultValues ? driver.defaultValues.map((option: any) => { return { value: option.value, description: option.description } }) : []
    formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
}

const getFormattedDropdownDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    formattedDriver.options = driver.defaultValues ? driver.defaultValues.map((option: any) => { return { value: option.value, description: option.description } }) : []
    if (driver.multivalue) {
        formattedDriver.parameterValue = []
        driver.parameterValue?.forEach((value: string) => {
            const option = formattedDriver.options?.find((option: { value: string, description: string }) => option.value === value)
            if (option) formattedDriver.parameterValue.push({ value: value, description: option.description })
        })
    } else {
        formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
    }
}

const getFormattedPopupDriver = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    if (driver.multivalue) {
        formattedDriver.parameterValue = []
        driver.parameterValue?.forEach((value: string) => {
            formattedDriver.parameterValue.push({ value: value, description: driver.parameterDescription[value] })
        })
    } else {
        formattedDriver.parameterValue = [{ value: driver.parameterValue ?? '', description: driver.parameterDescription && Array.isArray(driver.parameterDescription) ? driver.parameterDescription[0] : '' }]
    }
}


const getFormattedDriverDefaultValue = (driver: any, formattedDriver: IDashboardDatasetDriver) => {
    const formattedDefaultValues = [] as { value: string, description: string }[]
    driver.driverDefaultValue?.forEach((defaultValue: string) => {
        console.log(">>>>>>>>>>> defaultValue: ", defaultValue)
        const defaultValueString = defaultValue.substring(defaultValue.indexOf('[') + 1, defaultValue.lastIndexOf(']'))
        console.log(">>>>>>>>> defaultValueString: ", defaultValueString)
        if (defaultValueString) {
            const value = defaultValueString.substring(defaultValueString.indexOf('=') + 1, defaultValueString.indexOf(','))
            const description = defaultValueString.substring(defaultValueString.lastIndexOf('=') + 1)
            console.log(">>>>>>>>> value: ", value)
            console.log(">>>>>>>>> description: ", description)
            formattedDefaultValues.push({ value: value?.trim() ?? '', description: description?.trim() ?? '' })

        }
    })
    formattedDriver.defaultValue = formattedDefaultValues
}





const getFormattedDatasetParameters = (dataset: any) => {
    const parameters = [] as IDatasetParameter[]
    Object.keys(dataset.parameters).forEach((key: string) => parameters.push({
        name: key,
        type: "static",
        value: dataset.parameters[key]
    }))
    return parameters
}

const getFormattedVariables = async (model: any, drivers: any[], profileAttributes: { name: string, value: string }[], datasets: IDataset[], $http: any) => {
    const formattedVariables = [] as IVariable[]
    if (!model.configuration || !model.configuration.variables) return formattedVariables
    for (let i = 0; i < model.configuration.variables.length; i++) {
        const tempVariable = model.configuration.variables[i]
        const formattedVariable = { name: tempVariable.name, type: tempVariable.type, value: '' } as IVariable
        switch (formattedVariable.type) {
            case 'static':
                formattedVariable.value = tempVariable.value;
                break
            case 'dataset':
                formattedVariable.dataset = tempVariable.dataset;
                formattedVariable.column = tempVariable.column;
                await setVariableValueFromDataset(formattedVariable, datasets, $http)
                break
            case 'driver':
                formattedVariable.driver = tempVariable.driver;
                formattedVariable.value = getDriverValue(tempVariable.driver, drivers);
                break
            case 'profile':
                formattedVariable.attribute = tempVariable.attribute
                formattedVariable.value = getProfileAttributeValue(tempVariable.attribute, profileAttributes);
                break
        }
        formattedVariables.push(formattedVariable)
    }
    return formattedVariables
}

const getDriverValue = (driverUrlName: string, drivers: any[]) => {
    if (!drivers) return ''
    const index = drivers.findIndex((driver: any) => driver.urlName === driverUrlName)
    return index !== -1 ? drivers[index].value : ''
}

const getProfileAttributeValue = (profileAttributeName: string, profileAttributes: { name: string, value: string }[]) => {
    if (!profileAttributes) return ''
    const index = profileAttributes.findIndex((profileAttribute: { name: string, value: string }) => profileAttribute.name === profileAttributeName)
    return index !== -1 ? profileAttributes[index].value : ''
}

const getFormattedSelections = (model: any) => {
    if (!model.configuration || !model.selections) return []
    const formattedSelections = [] as ISelection[]
    model.selections.forEach((selection: { ds: string, columnName: string, value: string | (string | number)[], aggregated: boolean }) => {
        formattedSelections.push({ datasetId: getDatasetId(selection.ds), datasetLabel: selection.ds, columnName: selection.columnName, value: Array.isArray(selection.value) ? selection.value : [selection.value], aggregated: selection.aggregated, timestamp: new Date().getTime() })
    })
    return formattedSelections
}

const formatSheet = (sheet: any, formattedModel: any) => {
    if (!sheet.widgets) return

    const formattedSheet = deepcopy(sheet)
    formattedSheet.widgets = { lg: [] }

    for (let i = 0; i < sheet.widgets.length; i++) {
        const tempWidget = sheet.widgets[i]
        formattedSheet.widgets.lg.push({ id: tempWidget.id, h: tempWidget.sizeY, w: tempWidget.sizeX, x: tempWidget.col, y: tempWidget.row, i: cryptoRandomString({ length: 16, type: 'base64' }), moved: false })
        addWidgetToModel(tempWidget, formattedModel)
    }

    return formattedSheet
}

const addWidgetToModel = (widget: any, formattedModel: any) => {
    if (checkIfWidgetInModel(widget, formattedModel)) return
    formattedModel.widgets.push(formatWidget(widget, formattedModel))
}

const checkIfWidgetInModel = (widget: any, formattedModel: any) => {
    let found = false
    if (!formattedModel.widgets) return found
    for (let i = 0; i < formattedModel.widgets.length; i++) {
        if (formattedModel.widgets[i].id === widget.id) {
            found = true
            break
        }
    }

    return found
}

export const formatWidget = (widget: any, formattedModel: IDashboard) => {
    let formattedWidget = {} as any

    switch (widget.type) {
        case 'table':
            formattedWidget = formatTableWidget(widget, formattedModel)
            break;
        case 'selector':
            formattedWidget = formatSelectorWidget(widget)
            break;
        case 'selection':
            formattedWidget = formatSelectionWidget(widget)
            break
        case 'html':
            formattedWidget = formatHTMLWidget(widget)
            break
        case 'text':
            formattedWidget = formatTextWidget(widget)
    }

    return formattedWidget
}

export const getFiltersForColumns = (formattedWidget: IWidget, oldWidget: any) => {
    if (!oldWidget.filters || oldWidget.filters.length === 0) return
    for (let i = 0; i < oldWidget.filters.length; i++) {
        const tempFilter = oldWidget.filters[i]
        const index = formattedWidget.columns?.findIndex((column: IWidgetColumn) => column.columnName === tempFilter.colName)
        if (index !== -1) {
            formattedWidget.columns[index].filter = { enabled: true, operator: tempFilter.filterOperator, value: tempFilter.filterVal1 }
            if (tempFilter.filterVal2 && formattedWidget.columns[index].filter) (formattedWidget.columns[index].filter as IWidgetColumnFilter).value2 = tempFilter.filterVal2
        }
    }
}
