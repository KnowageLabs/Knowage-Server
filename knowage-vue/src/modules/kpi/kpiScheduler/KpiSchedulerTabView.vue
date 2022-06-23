<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #start>{{ this.clone || this.id ? this.selectedSchedule.name : this.$t('kpi.kpiScheduler.newScheduler') }} </template>
        <template #end>
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

                <KpiSchedulerKpiCard :expired="selectedSchedule.jobStatus === 'EXPIRED'" :kpis="selectedSchedule.kpis" :allKpiList="kpiList" @touched="setTouched" @kpiAdded="onKpiAdded($event)" @kpiDeleted="onKpiDeleted"></KpiSchedulerKpiCard>
            </TabPanel>

            <TabPanel :disabled="Object.keys(this.formatedFilters).length === 0">
                <template #header>
                    <span>{{ $t('kpi.kpiScheduler.filters') }}</span>
                </template>

                <KpiSchedulerFiltersCard :formatedFilters="formatedFilters" :placeholderType="domainsKpiPlaceholderType" :temporalType="domainsKpiPlaceholderFunction" :lovs="lovs" @touched="setTouched"></KpiSchedulerFiltersCard>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.kpiScheduler.frequency') }}</span>
                </template>

                <KnCron :frequency="selectedSchedule.frequency" @touched="setTouched" @cronValid="setCronValid($event)"></KnCron>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.kpiScheduler.execute') }}</span>
                </template>

                <KpiSchedulerExecuteCard :selectedSchedule="selectedSchedule" @touched="setTouched"></KpiSchedulerExecuteCard>
            </TabPanel>
        </TabView>
    </div>

    <KpiSchedulerSaveDialog v-if="saveDialogVisible" :schedulerName="selectedSchedule.name" @save="saveScheduler($event)" @close="saveDialogVisible = false"></KpiSchedulerSaveDialog>

    <Dialog :style="kpiSchedulerTabViewDescriptor.errorDialog.style" :modal="true" :visible="errorDialogVisible" :header="$t('common.toast.' + this.operation + 'Title')" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary error-dialog" :closable="false" data-test="save-dialog">
        <p>{{ errorMessage }}</p>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="errorMessage = null"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFilter, iKpiSchedule, iKpi, iLov } from './KpiScheduler'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import KpiSchedulerExecuteCard from './card/KpiSchedulerExecuteCard/KpiSchedulerExecuteCard.vue'
import KpiSchedulerFiltersCard from './card/KpiSchedulerFiltersCard/KpiSchedulerFiltersCard.vue'
import KnCron from '@/components/UI/KnCron/KnCron.vue'
import KpiSchedulerKpiCard from './card/KpiSchedulerKpiCard/KpiSchedulerKpiCard.vue'
import KpiSchedulerSaveDialog from './KpiSchedulerSaveDialog.vue'
import kpiSchedulerTabViewDescriptor from './KpiSchedulerTabViewDescriptor.json'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'kpi-scheduler-tab-view',
    components: { Dialog, KpiSchedulerExecuteCard, KpiSchedulerFiltersCard, KnCron, KpiSchedulerKpiCard, KpiSchedulerSaveDialog, TabView, TabPanel },
    props: {
        id: { type: String },
        clone: { type: String }
    },
    emits: ['touched', 'inserted', 'closed'],
    data() {
        return {
            kpiSchedulerTabViewDescriptor,
            selectedSchedule: {} as iKpiSchedule,
            domainsKpiPlaceholderType: [] as any[],
            domainsKpiPlaceholderFunction: [] as any[],
            lovs: [] as iLov[],
            kpiList: [] as iKpi[],
            kpiIds: [] as any[],
            filters: [] as iFilter[],
            formatedFilters: {} as any,
            loading: false,
            touched: false,
            saveDialogVisible: false,
            errorMessage: null as string | null,
            operation: 'create',
            validCron: true
        }
    },
    computed: {
        buttonDisabled(): Boolean {
            return !this.selectedSchedule.kpis || this.selectedSchedule.kpis.length == 0 || this.validCron == false || this.loading || this.emptyFilters()
        },
        errorDialogVisible(): Boolean {
            return this.errorMessage ? true : false
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        },
        async clone() {
            await this.loadPage()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
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
                this.selectedSchedule = {
                    kpis: [],
                    name: '',
                    delta: true,
                    filters: [],
                    frequency: {
                        cron: { type: 'minute', parameter: { numRepetition: '1' } },
                        startDate: new Date().valueOf(),
                        endDate: null,
                        startTime: new Date().valueOf(),
                        endTime: ''
                    }
                }
            }
            if (this.clone === 'true') {
                delete this.selectedSchedule.id
                this.selectedSchedule.name = 'Copy of ' + this.selectedSchedule.name
            }
            await this.loadDomainsData()
            await this.loadLovs()
            await this.loadKpiList()
            this.loadKpiIds()
            await this.loadFilters()
            this.addMissingPlaceholder()
            this.loading = false
        },
        async loadSchedule() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/loadSchedulerKPI`).then((response: AxiosResponse<any>) => (this.selectedSchedule = response.data))
            if (this.selectedSchedule.frequency.cron) {
                this.selectedSchedule.frequency.cron = JSON.parse(this.selectedSchedule.frequency.cron)
                this.selectedSchedule.frequency.endTime = ''
            }
        },
        async loadDomainsData() {
            await this.loadDomainsByCode('KPI_PLACEHOLDER_TYPE').then((response: AxiosResponse<any>) => (this.domainsKpiPlaceholderType = response.data))
            await this.loadDomainsByCode('KPI_PLACEHOLDER_FUNC').then((response: AxiosResponse<any>) => (this.domainsKpiPlaceholderFunction = response.data))
        },
        loadDomainsByCode(code: string) {
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/${code}`)
        },
        async loadLovs() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all/').then((response: AxiosResponse<any>) => (this.lovs = response.data))
        },
        async loadKpiList() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/kpi/listKpi').then((response: AxiosResponse<any>) => (this.kpiList = response.data))
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
                        this.$emit('closed')
                        this.$router.push(path)
                    }
                })
            }
        },
        async onKpiAdded(kpiList: []) {
            this.touched = true
            this.selectedSchedule = { ...this.selectedSchedule, kpis: kpiList }
            await this.loadFormatedFilters()
        },
        async onKpiDeleted(event: any) {
            this.selectedSchedule.filters = this.selectedSchedule.filters.filter((filter: iFilter) => {
                return filter.kpiId != event.id
            })
            this.touched = true
            await this.loadFormatedFilters()
        },
        async loadFormatedFilters() {
            this.loadKpiIds()
            await this.loadFilters()
            this.addMissingPlaceholder()
        },
        loadKpiIds() {
            this.kpiIds = []
            if (this.selectedSchedule.kpis) {
                this.selectedSchedule?.kpis.forEach((kpi: iKpi) => this.kpiIds.push({ id: kpi.id, version: kpi.version }))
            }
        },
        async loadFilters() {
            await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/kpi/listPlaceholderByKpi', this.kpiIds).then((response: AxiosResponse<any>) => (this.filters = response.data))
        },
        addMissingPlaceholder() {
            this.formatedFilters = []
            const keys = Object.keys(this.filters)
            for (let i = 0; i < keys.length; i++) {
                if (this.selectedSchedule.filters.length > 0) {
                    const tempPlaceholders = [] as iFilter[]

                    this.getPlaceholders(tempPlaceholders, keys, i)

                    const array = JSON.parse(this.filters[keys[i]])

                    this.removeUnusedPlaceholders(tempPlaceholders, array)

                    this.pushPlaceholderToFormatedFilters(tempPlaceholders, array, keys, i)
                } else {
                    this.selectedSchedule['filters'] = []
                    const array = JSON.parse(this.filters[keys[i]])
                    for (let k = 0; k < array.length; k++) {
                        const filter = this.createNewFilter(keys[i], array[k])

                        this.selectedSchedule.filters.push(filter)
                        this.addToFormatedFilters(filter)
                    }
                }
            }
        },
        getPlaceholders(tempPlaceholders: iFilter[], keys: string[], index: number) {
            for (let id in this.selectedSchedule.filters) {
                if ((this.selectedSchedule.filters[id] as iFilter).kpiName == keys[index]) {
                    tempPlaceholders.push(this.selectedSchedule.filters[id])
                }
            }
        },
        pushPlaceholderToFormatedFilters(tempPlaceholders: iFilter[], array: any, keys: string[], index: number) {
            let temp = null as iFilter | null
            for (let j = 0; j < array.length; j++) {
                temp = null
                for (let tempPLaceholder in tempPlaceholders) {
                    if (Object.keys(array[j])[0] == tempPlaceholders[tempPLaceholder].placeholderName) {
                        temp = tempPlaceholders[tempPLaceholder]
                        break
                    }
                }

                if (temp == null) {
                    const filter = this.createNewFilter(keys[index], array[j])

                    this.selectedSchedule.filters.push(filter)
                    this.addToFormatedFilters(filter)
                } else {
                    this.addToFormatedFilters(temp)
                }
            }
        },
        removeUnusedPlaceholders(tempPlaceholders: iFilter[], array: any) {
            let temp = null
            for (let tempPLaceholder in tempPlaceholders) {
                for (let i = 0; i < array.length; i++) {
                    if (Object.keys(array[i])[0] == tempPlaceholders[tempPLaceholder].placeholderName) {
                        temp = tempPLaceholder as any
                        break
                    }
                }
                if (temp == null) this.selectedSchedule.filters.splice(this.indexInList(tempPlaceholders[tempPLaceholder].placeholderName, this.selectedSchedule.filters, 'placeholderName'), 1)
            }
        },
        createNewFilter(kpiName: string, placeholder: iFilter) {
            const filter = { kpiName: kpiName, placeholderName: Object.keys(placeholder)[0], type: { valueCd: 'FIXED_VALUE', valueId: 242 } } as iFilter
            filter.value = placeholder[filter.placeholderName]

            const index = this.indexInList(kpiName, this.kpiList, 'name')
            filter.kpiId = (this.kpiList[index] as iKpi).id
            filter.kpiVersion = (this.kpiList[index] as iKpi).version

            return filter
        },
        addToFormatedFilters(filter: iFilter) {
            if (this.formatedFilters[filter.kpiName]) {
                this.formatedFilters[filter.kpiName].push(filter)
            } else {
                this.formatedFilters[filter.kpiName] = [filter]
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
        },
        async saveScheduler(schedulerName: string) {
            this.saveDialogVisible = false
            this.loading = true

            this.selectedSchedule.name = schedulerName
            this.selectedSchedule.frequency.cron = JSON.stringify(this.selectedSchedule.frequency.cron)

            this.selectedSchedule.filters.forEach((filter: iFilter) => {
                if (filter.type.valueCd === 'LOV') {
                    filter.value = this.getLovValue(filter.value as string)
                }
            })

            if (this.schedulerInvalid()) {
                return
            }

            if (this.selectedSchedule.id) {
                this.operation = 'update'
            }

            if (this.clone) {
                this.selectedSchedule.filters.forEach((filter: iFilter) => delete filter.executionId)
            }

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/kpi/saveSchedulerKPI', this.selectedSchedule)
                .then((response: AxiosResponse<any>) => {
                    this.store.setInfo({
                        title: this.$t('common.toast.' + this.operation + 'Title'),
                        msg: this.$t('common.toast.success')
                    })
                    this.$emit('inserted')

                    this.$router.push(`/kpi-scheduler/edit-kpi-schedule?id=${response.data.id}&clone=false`)
                })
                .catch((error) => {
                    this.errorMessage = error
                })
                .finally(() => (this.selectedSchedule.frequency.cron = JSON.parse(this.selectedSchedule.frequency.cron)))

            this.loading = false
        },
        getLovValue(value: string) {
            const tempLov = this.lovs.find((lov: iLov) => lov.name === value)
            return tempLov ? tempLov.label : ''
        },
        setCronValid(value: boolean) {
            this.validCron = value
        },
        schedulerInvalid() {
            if (!('delta' in this.selectedSchedule)) {
                this.errorMessage = this.$t('kpi.kpiScheduler.missingExecuteValue')
                return true
            }
            if (!this.selectedSchedule.kpis || this.selectedSchedule.kpis.length == 0) {
                this.errorMessage = this.$t('kpi.kpiScheduler.missingKpiList')
                return true
            }

            if (this.validCron == false) {
                this.errorMessage = this.$t('kpi.kpiScheduler.dateError')
                return true
            }

            return false
        },
        emptyFilters() {
            let invalid = false

            this.selectedSchedule.filters.forEach((filter) => {
                if (!filter.value) {
                    invalid = true
                }
            })

            return invalid
        }
    }
})
</script>
