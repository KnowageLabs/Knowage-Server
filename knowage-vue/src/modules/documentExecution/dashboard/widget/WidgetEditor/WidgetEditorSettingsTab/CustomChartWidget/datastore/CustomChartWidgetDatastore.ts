import deepcopy from 'deepcopy'

export class CustomChartDatastore {
    data: any = {}

    constructor(data) {
        this.data = data
    }

    setData(data) {
        this.data = this.transformDataStore(data)
    }

    getRecords() {
        return deepcopy(this.data.rows)
    }

    transformDataStore(data) {
        var newDataStore = {} as any
        newDataStore.metaData = data.metaData
        newDataStore.results = data.results
        newDataStore.rows = []

        for (var i = 0; i < data.rows.length; i++) {
            var obj = {}
            for (var j = 1; j < data.metaData.fields.length; j++) {
                if (data.rows[i][data.metaData.fields[j].name] != undefined) {
                    obj[data.metaData.fields[j].header] = data.rows[i][data.metaData.fields[j].name]
                }
            }
            newDataStore.rows.push(obj)
        }
        return newDataStore
    }
}
