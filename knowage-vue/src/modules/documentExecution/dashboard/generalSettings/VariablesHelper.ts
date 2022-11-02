import { IDataset, IVariable } from "../Dashboard";
import { getVariableData } from '../DataProxyHelper'

export const setVariableValueFromDataset = async (variable: IVariable, datasets: IDataset[], $http: any) => {
    const variableData = await getVariableData(variable, datasets, $http)
    if (!variableData) return variable.value = ''
    variable.column ? setVariableValueFromColumn(variable, variableData) : setVariablePivotedValues(variable, variableData)
}

const setVariableValueFromColumn = (variable: IVariable, variableData: any) => {
    const index = variableData.metaData?.fields.findIndex((field: any) => field.header === variable.column)
    if (index === -1) return variable.value = ''
    const columnName = variableData.metaData.fields[index].name
    variable.value = variableData.rows[0] ? variableData.rows[0][columnName] : ''
}

export const setVariablePivotedValues = async (variable: IVariable, variableData: any) => {
    variable.pivotedValues = getPivotedDataset(variableData)
}

const getPivotedDataset = (variableData: any) => {
    const pivotedDataset = {}
    variableData.rows?.forEach((row: any) => pivotedDataset[row.column_1] = row.column_2)
    return pivotedDataset
}