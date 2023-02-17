import { defineStore } from 'pinia'

const workspaceStore = defineStore('workspaceStore', {
    state() {
        return {
            dataPreparation: {
                loadedAvros: [],
                loadingAvros: [],
                avroDatasets: []
            }
        }
    },
    actions: {
        addToLoadedAvros(dsId) {
            const stringId = dsId.toString()
            const idx = this.dataPreparation.loadedAvros.indexOf(stringId)
            if (idx == -1) this.dataPreparation.loadedAvros.push(stringId)
        },
        addToAvroDatasets(dsId) {
            const stringId = dsId.toString()
            const idx = this.dataPreparation.avroDatasets.indexOf(stringId)
            if (idx == -1) this.dataPreparation.avroDatasets.push(stringId)
        },
        addToLoadingAvros(dsId) {
            const stringId = dsId.toString()
            const idx = this.dataPreparation.loadingAvros.indexOf(stringId)
            if (idx == -1) this.dataPreparation.loadingAvros.push(stringId)
        },
        removeFromLoadedAvros(dsId) {
            const stringId = dsId.toString()
            const idx = this.dataPreparation.loadedAvros.indexOf(stringId)
            if (idx >= 0) this.dataPreparation.loadedAvros.splice(idx, 1)
        },
        removeFromLoadingAvros(dsId) {
            const stringId = dsId.toString()
            const idx = this.dataPreparation.loadingAvros.indexOf(stringId)
            if (idx >= 0) this.dataPreparation.loadingAvros.splice(idx, 1)
        },
        setAvroDatasets(data) {
            this.dataPreparation.avroDatasets = data
        },
        setLoadedAvros(data) {
            this.dataPreparation.loadedAvros = data
        },
        setLoadingAvros(data) {
            this.dataPreparation.loadingAvros = data
        }
    },
    getters: {
        isAvroLoaded(state) {
            return (id) => state.dataPreparation.loadedAvros.indexOf(id.toString()) >= 0
        },
        isAvroLoading(state) {
            return (id) => state.dataPreparation.loadingAvros.indexOf(id.toString()) >= 0
        },
        isAvroReady(state) {
            return (dsId) => {
                if ((dsId && state.dataPreparation.avroDatasets.indexOf(dsId.toString()) >= 0) || (dsId && state.dataPreparation.avroDatasets.indexOf(dsId.toString())) >= 0) return true
                else return false
            }
        }
    }
})

export default workspaceStore
