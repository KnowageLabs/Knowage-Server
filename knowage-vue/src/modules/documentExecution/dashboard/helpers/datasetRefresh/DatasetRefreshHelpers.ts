import { IDataset, IDashboardDataset } from "@/modules/documentExecution/dashboard/Dashboard";
import { emitter } from '../../DashboardHelpers'

let dataSetIntervals = {}

export const setDatasetIntervals = (modelDatasets: IDashboardDataset[], datasets: IDataset[]) => {
    if (!modelDatasets || !datasets) return

    for (let i = 0; i < modelDatasets.length; i++) {
        const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId === modelDatasets[i].id)
        // TODO - check condition and remove hardcoded interval when realtime dataset example is ready
        if (index !== -1 && datasets[i].isRealtime) {
            setDatasetInterval(modelDatasets[i].id, 10000)
        }
    }

    removeUnusedDatasetIntervals(modelDatasets)
}

export const setDatasetInterval = (modelDatasetId: number, interval: number) => {
    if (dataSetIntervals[modelDatasetId]) clearInterval(dataSetIntervals[modelDatasetId])
    dataSetIntervals[modelDatasetId] = setInterval(() => emittDatasetRefresh(modelDatasetId), interval)

}

const removeUnusedDatasetIntervals = (modelDatasets: IDashboardDataset[]) => {
    const keysToRemove = [] as string[]
    Object.keys(dataSetIntervals).forEach((key: string) => {
        const index = modelDatasets.findIndex((dataset: IDashboardDataset) => '' + dataset.id === key)
        if (index === -1) keysToRemove.push(key)
    })
    keysToRemove.forEach((key: string) => clearDatasetInterval(+key))
}

const emittDatasetRefresh = (modelDatasetId: number) => { emitter.emit('datasetRefreshed', modelDatasetId) }


export const clearDatasetInterval = (modelDatasetId: number) => {
    clearInterval(dataSetIntervals[modelDatasetId])
    delete dataSetIntervals[modelDatasetId]
}

export const clearAllDatasetIntervals = () => {
    Object.keys(dataSetIntervals).forEach((key: string) => clearDatasetInterval(+key))
}