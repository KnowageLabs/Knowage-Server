import { IDataset, ISelection, IWidget } from "../../Dashboard"

interface ISelectionValue { columnName: string, value: string, columnType: string }

export const getFormattedClickedValueForCrossNavigation = (cellEvent: any, dataFields: any) => {
    if (['T', 'GT'].includes(cellEvent.cell.type)) return null
    const value = cellEvent.area === 'data' ? cellEvent.cell.value : cellEvent.cell.text
    const type = cellEvent.area === 'data' ? getDataCellType(cellEvent, dataFields) : 'string'
    return { value: value, type: type }
}

const getDataCellType = (cellEvent: any, dataFields: any) => {
    // TODO - see about date
    const dataField = dataFields[cellEvent.cell.dataIndex]
    return dataField ? dataField.dataType : 'string'
}

export const createPivotTableSelection = (cellEvent: any, widgetModel: IWidget, datasets: IDataset[]) => {
    if (!cellEvent || cellEvent.area !== 'data') return null
    const selectionValues = [] as ISelectionValue[]
    addSelectionValues(cellEvent.cell.columnPath, cellEvent.columnFields, selectionValues)
    addSelectionValues(cellEvent.cell.rowPath, cellEvent.rowFields, selectionValues)
    return selectionValues.length > 0 ? createSelectionsFromSelectedValues(selectionValues, widgetModel, datasets) : null
}

const addSelectionValues = (path: string[], columnFields: any[], selectionValues: ISelectionValue[]) => {
    path.forEach((pathValue: string, index) => {
        if (columnFields[index]) {
            const column = columnFields[index]
            selectionValues.push({ columnName: column.caption, value: pathValue, columnType: column.dataType })
        }
    })
}

const createSelectionsFromSelectedValues = (selectionValues: ISelectionValue[], widgetModel: IWidget, datasets: IDataset[]) => {
    const selections = [] as ISelection[]
    selectionValues.forEach((selectionValue: ISelectionValue) => selections.push(createSelection([selectionValue.value], selectionValue.columnName, widgetModel, datasets)))
    return selections
}

const createSelection = (value: (string | number)[], columnName: string, widget: IWidget, datasets: IDataset[]) => {
    return { datasetId: widget.dataset as number, datasetLabel: getDatasetLabel(widget.dataset as number, datasets) as string, columnName: columnName, value: value, aggregated: false, timestamp: new Date().getTime() }
}

const getDatasetLabel = (datasetId: number, datasets: IDataset[]) => {
    const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
    return index !== -1 ? datasets[index].label : ''
}