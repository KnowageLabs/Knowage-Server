<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ this.clone || this.id ? this.selectedSchedule.name : this.$t('kpi.kpiScheduler.newScheduler') }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="saveDialogVisible = true" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" data-test="close-button" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card" v-else>
        <TabView class="tabview-custom">
            <TabPanel>
                <template #header>
                    <span>{{ $t('common.kpi') }}</span>
                </template>

                <KpiCard :kpis="selectedSchedule.kpis" :allKpiList="kpiList" @touched="setTouched" @kpiAdded="onKpiAdded($event)"></KpiCard>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.kpiScheduler.filters') }}</span>
                </template>

                <FiltersCard :formatedFilters="formatedFilters" :placeholderType="domainsKpiPlaceholderType" :temporalType="domainsKpiPlaceholderFunction" :lovs="lovs"></FiltersCard>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.kpiScheduler.frequency') }}</span>
                </template>

                <FrequencyCard :frequency="selectedSchedule.frequency"></FrequencyCard>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.kpiScheduler.execute') }}</span>
                </template>

                <ExecuteCard :selectedSchedule="selectedSchedule"></ExecuteCard>
            </TabPanel>
        </TabView>
    </div>

    <KpiSchedulerSaveDialog v-if="saveDialogVisible" :schedulerName="selectedSchedule.name" @save="saveScheduler($event)" @close="saveDialogVisible = false"></KpiSchedulerSaveDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import ExecuteCard from './card/ExecuteCard/ExecuteCard.vue'
import FiltersCard from './card/FiltersCard/FiltersCard.vue'
import FrequencyCard from './card/FrequencyCard/FrequencyCard.vue'
import KpiCard from './card/KpiCard/KpiCard.vue'
import KpiSchedulerSaveDialog from './KpiSchedulerSaveDialog.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'kpi-scheduler-tab-view',
    components: { ExecuteCard, FiltersCard, FrequencyCard, KpiCard, KpiSchedulerSaveDialog, TabView, TabPanel },
    props: {
        id: { type: String },
        clone: { type: String }
    },
    emits: ['touched'],
    data() {
        return {
            selectedSchedule: {} as any,
            domainsKpiPlaceholderType: [] as any[],
            domainsKpiPlaceholderFunction: [] as any[],
            lovs: [] as any[],
            kpiList: [] as any[],
            kpiIds: [] as any[],
            filters: [] as any[],
            formatedFilters: {} as any,
            loading: false,
            touched: false,
            saveDialogVisible: false,
            operation: 'create'
        }
    },
    computed: {
        buttonDisabled(): Boolean {
            return false
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    async created() {
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            if (this.id) {
                await this.loadSchedule()
            } else {
                this.selectedSchedule = {}
            }
            if (this.clone === 'true') {
                delete this.selectedSchedule.id
            }
            await this.loadDomainsData()
            await this.loadLovs()
            await this.loadKpiList()
            this.loadKpiIds()
            await this.loadFilters()
            this.addMissingPlaceholder()
            this.loading = false

            // console.log('KPI_PLACEHOLDER_TYPE', this.domainsKpiPlaceholderType)
            // console.log('KPI_PLACEHOLDER_FUNC', this.domainsKpiPlaceholderFunction)
            // console.log('LOVS', this.lovs)
            // console.log('ALL KPI LIST', this.kpiList)
        },
        async loadSchedule() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/loadSchedulerKPI`).then((response) => {
                this.selectedSchedule = response.data
                if (this.selectedSchedule.frequency.cron) {
                    this.selectedSchedule.frequency.cron = JSON.parse(this.selectedSchedule.frequency.cron)
                }
            })
            console.log('SELECTED SCHEDULE AFTER LOAD', this.selectedSchedule)
        },
        async loadDomainsData() {
            await this.loadDomainsByCode('KPI_PLACEHOLDER_TYPE').then((response) => (this.domainsKpiPlaceholderType = response.data))
            await this.loadDomainsByCode('KPI_PLACEHOLDER_FUNC').then((response) => (this.domainsKpiPlaceholderFunction = response.data))
        },
        loadDomainsByCode(code: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/${code}`)
        },
        async loadLovs() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all/').then((response) => (this.lovs = response.data))
        },
        async loadKpiList() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listKpi').then((response) => (this.kpiList = response.data))
        },
        setTouched() {
            this.touched = true
            this.$emit('touched')
        },
        closeTemplate() {
            const path = '/kpi-scheduler'
            if (!this.touched) {
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
                    }
                })
            }
        },
        async onKpiAdded(kpiList: []) {
            this.selectedSchedule = { ...this.selectedSchedule, kpis: kpiList }
            this.loadKpiIds()
            await this.loadFilters()
            this.addMissingPlaceholder()
            // console.log('TEST ON KPI ADDED', this.selectedSchedule.kpis)
        },
        loadKpiIds() {
            this.kpiIds = []
            this.selectedSchedule?.kpis.forEach((kpi: any) => this.kpiIds.push({ id: kpi.id, version: kpi.version }))
            console.log('LOADED KPI IDS', this.kpiIds)
        },
        async loadFilters() {
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listPlaceholderByKpi', this.kpiIds).then((response) => (this.filters = response.data))
            console.log('FILTERS', this.filters)
        },
        addMissingPlaceholder() {
            // console.log('CALLLED addMissingPlaceholder')
            this.formatedFilters = []
            const keys = Object.keys(this.filters)
            for (let i = 0; i < keys.length; i++) {
                if (this.selectedSchedule.filters.length > 0) {
                    const tempPlaceholders = [] as any[]
                    // console.log('SCHEDULER FILTERS', this.selectedSchedule.filters)
                    // Check if returned filter is in selected schedule filters
                    for (let id in this.selectedSchedule.filters) {
                        // console.log(this.scheduler.filters[id].kpiName + ' == ' + keys[i])

                        // If found push to temp placeholders
                        if (this.selectedSchedule.filters[id].kpiName == keys[i]) {
                            // console.log('FOUND!!!!')
                            tempPlaceholders.push(this.selectedSchedule.filters[id])
                        }
                    }

                    // Parse from filter
                    const array = JSON.parse(this.filters[keys[i]])
                    // console.log('DDD - ARRRAY', array)
                    // console.log('DDD - TEMP PLACEHOLDERS', tempPlaceholders)

                    let temp = null as any

                    // Remove filters from scheduler that were not returned from filters API
                    for (let tempPLaceholder in tempPlaceholders) {
                        for (let i = 0; i < array.length; i++) {
                            // console.log('MMM -' + Object.keys(array[i])[0] + '==' + tempPlaceholders[tempPLaceholder].placeholderName + '=')
                            if (Object.keys(array[i])[0] == tempPlaceholders[tempPLaceholder].placeholderName) {
                                // console.log('MMM - FOUND')
                                temp = tempPLaceholder
                                break
                            }
                        }
                        // console.log('TEMP', temp)
                        if (temp == null) this.selectedSchedule.filters.splice(this.indexInList(tempPlaceholders[tempPLaceholder].placeholderName, this.selectedSchedule.filters, 'placeholderName'), 1)
                    }

                    // Check if filters exist in scheduler
                    console.log('MMM - START')
                    for (let j = 0; j < array.length; j++) {
                        temp = null
                        for (let tempPLaceholder in tempPlaceholders) {
                            // console.log('ARRAY ELEMENT KEYS: ', Object.keys(array[j]))
                            // console.log('TEMP PLACEHOLDER: ', tempPLaceholder)
                            // console.log(Object.keys(array[j])[0] + ' === ' + tempPlaceholders[tempPLaceholder].placeholderName)
                            if (Object.keys(array[j])[0] == tempPlaceholders[tempPLaceholder].placeholderName) {
                                // console.log('MMM - FOUND!!!')
                                temp = tempPlaceholders[tempPLaceholder]
                                break
                            }
                        }

                        // console.log('MMM - TEMP BEFORE PUSH', temp)
                        // Add new filter to scheduler
                        if (temp == null) {
                            const objType = { valueCd: 'FIXED_VALUE', valueId: 355 }

                            const obj = {} as any
                            obj.kpiName = keys[i]
                            obj.placeholderName = Object.keys(array[j])[0]
                            obj.value = array[j][obj.placeholderName]
                            obj.type = objType
                            const index2 = this.indexInList(keys[i], this.kpiList, 'name')
                            obj.kpiId = (this.kpiList[index2] as any).id
                            obj.kpiVersion = (this.kpiList[index2] as any).version

                            this.selectedSchedule.filters.push(obj)

                            // TODO izdvoji u metodu
                            if (this.formatedFilters[obj.kpiName]) {
                                this.formatedFilters[obj.kpiName].push(obj)
                            } else {
                                this.formatedFilters[obj.kpiName] = [obj]
                            }
                        } else {
                            // console.log('BBB - TEMP', temp)
                            if (this.formatedFilters[temp.kpiName]) {
                                this.formatedFilters[temp.kpiName].push(temp)
                            } else {
                                this.formatedFilters[temp.kpiName] = [temp]
                            }
                        }
                    }
                } else {
                    // Scheduler doesn't have filters, add new ones
                    // console.log('UUU - Start')
                    this.selectedSchedule['filters'] = []
                    const objType = { valueCd: 'FIXED_VALUE', valueId: 355 }
                    const array = JSON.parse(this.filters[keys[i]])
                    //  console.log('UUU - Array', array)
                    for (let k = 0; k < array.length; k++) {
                        const obj = {} as any
                        obj.kpiName = keys[i]
                        obj.placeholderName = Object.keys(array[k])[0]
                        obj.value = array[k][obj.placeholderName]
                        obj.type = objType
                        const index2 = this.indexInList(keys[i], this.kpiList, 'name')
                        obj.kpiId = (this.kpiList[index2] as any).id
                        obj.kpiVersion = (this.kpiList[index2] as any).version

                        this.selectedSchedule.filters.push(obj)

                        if (this.formatedFilters[obj.kpiName]) {
                            this.formatedFilters[obj.kpiName].push(obj)
                        } else {
                            this.formatedFilters[obj.kpiName] = [obj]
                        }
                    }
                }
            }
            console.log('SCHEDULE AFTER FILTERS MAPPING', this.selectedSchedule)
            console.log('FORMATED FILTERS FILTERS MAPPING', this.formatedFilters)
        },
        indexInList(item, list, param) {
            for (let i = 0; i < list.length; i++) {
                const object = list[i]
                if (object[param] == item) {
                    return i
                }
            }
            return -1
        },
        async saveScheduler(schedulerName: string) {
            this.saveDialogVisible = false
            this.loading = true
            this.selectedSchedule.name = schedulerName
            // console.log('JSON STRINGIGY', JSON.stringify(this.selectedSchedule.frequency.cron))
            this.selectedSchedule.frequency.cron = JSON.stringify(this.selectedSchedule.frequency.cron)

            this.selectedSchedule.filters.forEach((filter: any) => {
                console.log('MAIN filter', filter)
                if (filter.type.valueCd === 'LOV') {
                    filter.value = this.getLovValue(filter.value)
                }
            })

            if (this.selectedSchedule.id) {
                this.operation = 'update'
            }

            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/saveSchedulerKPI', this.selectedSchedule).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.' + this.operation + 'Title'),
                    msg: this.$t('common.toast.success')
                })
            })

            this.loading = false
        },
        getLovValue(value: string) {
            // console.log('FC - Value ', value)
            const tempLov = this.lovs.find((lov: any) => lov.name === value) as any
            return tempLov ? tempLov.label : ''
        }
    }
})
</script>
