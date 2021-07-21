<template>
    <Card></Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Card from 'primevue/card'

export default defineComponent({
    name: 'filters-card',
    components: { Card },
    props: {
        selectedSchedule: { type: Object },
        allKpiList: { type: Array, required: true }
    },
    data() {
        return {
            scheduler: {} as any,
            kpiIds: [] as any[],
            filters: [] as any[]
        }
    },
    watch: {
        async selectedSchedule() {
            this.loadScheduler()
            await this.loadFilters()
            this.addMissingPlaceholder()
        }
    },
    async created() {
        this.loadScheduler()
        await this.loadFilters()
        this.addMissingPlaceholder()
        console.log('SCHEDULER ', this.scheduler)
    },
    methods: {
        loadScheduler() {
            this.scheduler = this.selectedSchedule
            console.log('SELECTED SCHEDULER FILTERS CARD', this.scheduler)
            this.kpiIds = []
            this.scheduler?.kpis.forEach((kpi: any) => this.kpiIds.push({ id: kpi.id, version: kpi.version }))
            console.log('LOADED KPI IDS', this.kpiIds)
        },
        async loadFilters() {
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listPlaceholderByKpi', this.kpiIds).then((response) => (this.filters = response.data))
            console.log('FILTERS', this.filters)
        },
        addMissingPlaceholder() {
            const keys = Object.keys(this.filters)
            for (let i = 0; i < keys.length; i++) {
                if (this.scheduler.filters) {
                    const tempPlaceholders = [] as any[]
                    console.log('SCHEDULER FILTERS', this.scheduler.filters)
                    for (let id in this.scheduler.filters) {
                        // console.log(this.scheduler.filters[id].kpiName + ' == ' + keys[i])
                        if (this.scheduler.filters[id].kpiName == keys[i]) {
                            // console.log('FOUND!!!!')
                            tempPlaceholders.push(this.scheduler.filters[id])
                        }

                        const array = JSON.parse(this.filters[keys[i]])
                        console.log('ARRRAY', array)
                        let temp = null as any
                        for (let tempPLaceholder in tempPlaceholders) {
                            for (let i = 0; i < array.length; i++) {
                                // console.log(Object.keys(array[i])[0] + ' === ' + tempPlaceholders[tempPLaceholder].placeholderName)
                                if (Object.keys(array[i])[0] == tempPlaceholders[tempPLaceholder].placeholderName) {
                                    temp = tempPLaceholder
                                    break
                                }
                            }
                            // console.log('TEMP', temp)
                            if (temp == null) this.scheduler.filters.splice(this.indexInList(tempPlaceholders[tempPLaceholder].placeholderName, this.scheduler.filters, 'placeholderName'), 1)
                        }

                        for (let i = 0; i < array.length; i++) {
                            temp = null
                            for (let tempPLaceholder in tempPlaceholders) {
                                if (Object.keys(array[i])[0] == tempPlaceholders[tempPLaceholder].placeholderName) {
                                    temp = tempPLaceholder
                                    break
                                }
                            }

                            console.log('TEMP BEFORE PUSH', temp)
                            if (temp == null) {
                                const objType = { valueCd: 'FIXED_VALUE', valueId: 355 }

                                const obj = {} as any
                                obj.kpiName = keys[i]
                                obj.placeholderName = Object.keys(array[i])[0]
                                obj.value = array[i][obj.placeholderName]
                                obj.type = objType
                                const index2 = this.indexInList(keys[i], this.allKpiList, 'name')
                                obj.kpiId = (this.allKpiList[index2] as any).id
                                obj.kpiVersion = (this.allKpiList[index2] as any).version

                                this.scheduler.filters.push(obj)
                            }
                        }
                    }
                } else {
                    this.scheduler['filters'] = []
                    const objType = { valueCd: 'FIXED_VALUE', valueId: 355 }
                    const array = JSON.parse(this.filters[keys[i]])
                    for (let i = 0; i < array.length; i++) {
                        const obj = {} as any
                        obj.kpiName = keys[i]
                        obj.placeholderName = Object.keys(array[i])[0]
                        obj.value = array[i][obj.placeholderName]
                        obj.type = objType
                        const index2 = this.indexInList(keys[i], this.allKpiList, 'name')
                        obj.kpiId = (this.allKpiList[index2] as any).id
                        obj.kpiVersion = (this.allKpiList[index2] as any).version

                        this.scheduler.filters.push(obj)
                    }
                }
            }
        },
        indexInList(item, list, param) {
            for (let i = 0; i < list.length; i++) {
                const object = list[i]
                if (object[param] == item) {
                    return i
                }
            }
            return -1
        }
    }
})
</script>
