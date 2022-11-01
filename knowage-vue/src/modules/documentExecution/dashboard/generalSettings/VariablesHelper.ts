import { IDataset, IVariable } from "../Dashboard";
import { getVariableData } from '../DataProxyHelper'

export const setVariableValueFromDatasetColumn = async (variable: IVariable, datasets: IDataset[], $http) => {
    console.log(">>>>>>>>>> VARIABLE: ", variable)
    const variableData = await getVariableData(variable, datasets, $http)
    console.log(">>>>>>>>>> VARIABLE DATA: ", variableData)
    if (!variableData) return ''
    const index = variableData.metaData?.fields.findIndex((field: any) => field.header === variable.column)
    if (index === -1) return ''
    const columnName = variableData.metaData.fields[index].name

    console.log("VARIABLE VALUE: ", variableData.rows[0] ? variableData.rows[0][columnName] : '')
    variable.value = variableData.rows[0] ? variableData.rows[0][columnName] : ''
}