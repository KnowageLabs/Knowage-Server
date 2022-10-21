import { IDataset, IModelDataset } from "@/modules/documentExecution/dashboard/Dashboard";
import { emitter } from '../../DashboardHelpers'

let dataSetIntervals = {}

export const setDatasetIntervals = (modelDatasets: IModelDataset[], datasets: IDataset[]) => {
    if (!modelDatasets || !datasets) return

    for (let i = 0; i < modelDatasets.length; i++) {
        const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId === modelDatasets[i].id)
        // TODO - add condition and remove hardcoded interval
        if (index !== -1) {
            setDatasetInterval(modelDatasets[i].id, 10000)
        }
    }

    removeUnusedDatasetIntervals(modelDatasets)
}

export const setDatasetInterval = (modelDatasetId: number, interval: number) => {
    // TODO - dataset interval
    if (dataSetIntervals[modelDatasetId]) clearInterval(dataSetIntervals[modelDatasetId])
    // dataSetIntervals[modelDatasetId] = setInterval(() => emittDatasetRefresh(modelDatasetId), 30000)

}

const removeUnusedDatasetIntervals = (modelDatasets: IModelDataset[]) => {
    const keysToRemove = [] as string[]
    Object.keys(dataSetIntervals).forEach((key: string) => {
        const index = modelDatasets.findIndex((dataset: IModelDataset) => '' + dataset.id === key)
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