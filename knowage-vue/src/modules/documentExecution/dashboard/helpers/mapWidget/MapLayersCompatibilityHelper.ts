import { IDashboard, IDashboardDriver, IVariable, IWidget } from '../../Dashboard'
import { IMapWidgetConditionalStyle } from '../../interfaces/mapWidget/DashboardMapWidget'

export const getFormattedSettingsFromLayers = (widget: any, formattedWidget: IWidget, formattedDashboardModel: IDashboard, drivers: IDashboardDriver[]) => {
    const layers = widget.content.layers
    layers?.forEach((layer: any) => {
        layer?.content?.columnSelectedOfDataset?.forEach((column: any) => {
            addLayerColumnTooltipOptions(column, formattedWidget, layer.name)
            addLayerColumnConditionalStyleSettings(column, formattedWidget, layer.name, formattedDashboardModel, drivers)
        })
        addLayerVisualizationTypeSettings(layer, formattedWidget)
    })
}

const addLayerColumnTooltipOptions = (oldColumn: any, formattedWidget: IWidget, layerName: string) => {
    if (oldColumn?.properties?.showTooltip) {
        formattedWidget.settings.tooltips.layers.push({ name: layerName, columns: [oldColumn.name] })
    }
}

const addLayerVisualizationTypeSettings = (layer: any, formattedWidget: IWidget) => {
    formattedWidget.settings.visualization.types.push({
        target: [layer.name],
        type: layer.visualizationType,
        markerConf: layer.markerConf,
        clusterConf: layer.clusterConf,
        heatmapConf: layer.heatmapConf,
        analysisConf: layer.analysisConf
    })
}

const addLayerColumnConditionalStyleSettings = (oldColumn: any, formattedWidget: IWidget, layerName: string, formattedDashboardModel: IDashboard, drivers: IDashboardDriver[]) => {
    if (oldColumn && oldColumn.fieldType === 'MEASURE' && oldColumn.ranges) {
        oldColumn.ranges.forEach((range: any) => {
            const tempConditionalStyle = createConditionalStyleFromRange(oldColumn, range, layerName)
            addNonstaticConditionalStyles(tempConditionalStyle, range, formattedDashboardModel, drivers)
            formattedWidget.settings.conditionalStyles.enabled = true
            formattedWidget.settings.conditionalStyles.conditions.push(tempConditionalStyle)
        })
    }
}

const createConditionalStyleFromRange = (oldColumn: any, range: any, layerName: string) => {
    return {
        targetLayer: layerName,
        targetColumn: oldColumn.name,
        condition: { type: 'static', operator: range.operator, value: range.value },
        properties: { 'background-color': range['background-color'] ?? '' }
    } as IMapWidgetConditionalStyle
}

const addNonstaticConditionalStyles = (tempConditionalStyle: IMapWidgetConditionalStyle, range: any, formattedDashboardModel: IDashboard, drivers: IDashboardDriver[]) => {
    if (range.compareValueType === 'variable') {
        tempConditionalStyle.condition.type = 'variable'
        tempConditionalStyle.condition.variable = range.value
        updateConditionalStyleFromVariable(tempConditionalStyle, range, formattedDashboardModel)
    } else if (range.compareValueType === 'parameter') {
        tempConditionalStyle.condition.type = 'parameter'
        tempConditionalStyle.condition.parameter = range.value
        tempConditionalStyle.condition.value = getValueFromDriver(range.value, drivers)
    }
}

const updateConditionalStyleFromVariable = (conditionStyle: IMapWidgetConditionalStyle, range: any, formattedDashboardModel: IDashboard) => {
    const modelVariable = formattedDashboardModel.configuration.variables?.find((variable: IVariable) => variable.name === range.value)
    setConditionalStyleValueFromVariable(conditionStyle, modelVariable, range)
}

const setConditionalStyleValueFromVariable = (conditionStyle: IMapWidgetConditionalStyle, modelVariable: IVariable | undefined, rowThreshold: any) => {
    if (!modelVariable) return
    switch (modelVariable.type) {
        case 'static':
        case 'profile':
        case 'driver':
            conditionStyle.condition.value = modelVariable.value
            break
        case 'dataset':
            if (modelVariable.column) {
                conditionStyle.condition.value = modelVariable.value
            } else {
                conditionStyle.condition.variableKey = rowThreshold.compareValueKey
                conditionStyle.condition.variablePivotDatasetOptions = modelVariable.pivotedValues
                conditionStyle.condition.value = conditionStyle.condition.variableKey ? conditionStyle.condition.variablePivotDatasetOptions[conditionStyle.condition.variableKey] : ''
            }
    }
}

const getValueFromDriver = (driverUrl: string, drivers: IDashboardDriver[]) => {
    const index = drivers.findIndex((driver: IDashboardDriver) => driver.urlName === driverUrl)
    return index !== -1 ? drivers[index].value : ''
}
