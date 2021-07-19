<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ this.clone || this.id ? this.selectedSchedule.name : this.$t('kpi.kpiScheduler.newScheduler') }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" data-test="submit-button" />
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

                <KpiCard :kpis="selectedSchedule.kpis" @touched="setTouched"></KpiCard>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>FILTERS</span>
                </template>

                {{ selectedSchedule }}
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>FREQUENCY</span>
                </template>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>EXECUTE</span>
                </template>
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import KpiCard from './card/KpiCard.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'name',
    components: { KpiCard, TabView, TabPanel },
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
            loading: false,
            touched: false
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
            if (this.clone) {
                delete this.selectedSchedule.id
            }
            await this.loadDomainsData()
            await this.loadLovs()
            this.loading = false

            // console.log('KPI_PLACEHOLDER_TYPE', this.domainsKpiPlaceholderType)
            // console.log('KPI_PLACEHOLDER_FUNC', this.domainsKpiPlaceholderFunction)
            // console.log('LOVS', this.lovs)
        },
        async loadSchedule() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/loadSchedulerKPI`).then((response) => (this.selectedSchedule = response.data))
        },
        async loadDomainsData() {
            await this.loadDomainsByCode('KPI_PLACEHOLDER_TYPE').then((response) => (this.domainsKpiPlaceholderType = response.data))
            await this.loadDomainsByCode('KPI_PLACEHOLDER_FUNC').then((response) => (this.domainsKpiPlaceholderFunction = response.data))
        },
        loadDomainsByCode(code: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/${code}`)
        },
        async loadLovs() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all/').then((response) => (this.lovs = response.data))
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
        }
    }
})
</script>
