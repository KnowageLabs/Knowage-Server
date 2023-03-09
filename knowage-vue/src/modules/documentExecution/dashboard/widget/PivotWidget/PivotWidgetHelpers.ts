import { IDataset, IWidget } from "../../Dashboard"

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
    if (!cellEvent || !cellEvent.area || !cellEvent.cell) return null
    if (cellEvent.area === 'column' && cellEvent.cell.dataIndex === undefined) {
        return createSelectionFromHeader(cellEvent, widgetModel, datasets, 'columnFields')
    } else if (cellEvent.area === 'row' && cellEvent.cell.dataIndex === undefined && !['T', 'GT'].includes(cellEvent.cell.type)) {
        return createSelectionFromHeader(cellEvent, widgetModel, datasets, 'rowFields')
    }

    return null
}

const createSelectionFromHeader = (cellEvent: any, widgetModel: IWidget, datasets: IDataset[], property: 'columnFields' | 'rowFields') => {
    const value = cellEvent.cell.text
    const index = cellEvent.cell.path.findIndex((pathValue: string | number) => value == pathValue)
    const columnName = index !== -1 && cellEvent[property] && cellEvent[property][index] ? cellEvent[property][index].caption : ''
    return createSelection([value], columnName, widgetModel, datasets)
}

const createSelection = (value: (string | number)[], columnName: string, widget: IWidget, datasets: IDataset[]) => {
    return { datasetId: widget.dataset as number, datasetLabel: getDatasetLabel(widget.dataset as number, datasets) as string, columnName: columnName, value: value, aggregated: false, timestamp: new Date().getTime() }
}

const getDatasetLabel = (datasetId: number, datasets: IDataset[]) => {
    const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
    return index !== -1 ? datasets[index].label : ''
}