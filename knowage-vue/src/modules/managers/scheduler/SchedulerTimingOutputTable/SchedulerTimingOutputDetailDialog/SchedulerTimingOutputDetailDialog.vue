<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="schedulerTimingOutputDetailDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.scheduler.timingAndOutput') }}
                </template>
            </Toolbar>
            <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />
        </template>

        <TabView id="timing-tabs">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.scheduler.timing') }}</span>
                </template>
                <SchedulerTimingOutputTimingTab :propTrigger="trigger" :datasets="datasets" @cronValid="setCronValid($event)"></SchedulerTimingOutputTimingTab>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>{{ $t('common.output') }}</span>
                </template>

                <SchedulerTimingOutputOutputTab :propDocuments="trigger.documents" :functionalities="functionalities" :datasets="datasets" :jobInfo="jobInfo"></SchedulerTimingOutputOutputTab>
            </TabPanel>
        </TabView>

        <SchedulerTimingOutputWarningDialog :visible="warningVisible" :warningTitle="warningTitle" :warningMessage="warningMessage" @close="warningVisible = false"></SchedulerTimingOutputWarningDialog>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" :disabled="saveDisabled" @click="saveTrigger">{{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import schedulerTimingOutputDetailDialogDescriptor from './SchedulerTimingOutputDetailDialogDescriptor.json'
import SchedulerTimingOutputTimingTab from './tabs/SchedulerTimingOutputTimingTab/SchedulerTimingOutputTimingTab.vue'
import SchedulerTimingOutputOutputTab from './tabs/SchedulerTimingOutputOutputTab/SchedulerTimingOutputOutputTab.vue'
import SchedulerTimingOutputWarningDialog from './SchedulerTimingOutputWarningDialog.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'scheduler-timing-output-detail-dialog',
    components: { Dialog, SchedulerTimingOutputTimingTab, SchedulerTimingOutputOutputTab, SchedulerTimingOutputWarningDialog, TabView, TabPanel },
    props: { visible: { type: Boolean }, propTrigger: { type: Object } },
    emits: ['close', 'saved'],
    data() {
        return {
            schedulerTimingOutputDetailDialogDescriptor,
            trigger: null as any,
            info: null as any,
            validCron: true,
            datasets: [] as any[],
            jobInfo: null as any,
            functionalities: [],
            warningVisible: false,
            warningTitle: null as string | null,
            warningMessage: null as string | null,
            operation: 'create',
            loading: false
        }
    },
    computed: {
        saveDisabled(): any {
            let disabled = false

            if (!this.trigger.triggerDescription || this.trigger.triggerDescription.length === 0 || !this.validCron) {
                return true
            }

            for (let i = 0; i < this.trigger.documents?.length; i++) {
                if (
                    (this.trigger.documents[i].invalid &&
                        (this.trigger.documents[i].invalid.invalidSnapshot || this.trigger.documents[i].invalid.invalidFile || this.trigger.documents[i].invalid.invalidJavaClass || this.trigger.documents[i].invalid.invalidMail || this.trigger.documents[i].invalid.invalidDocument)) ||
                    (!this.trigger.documents[i].saveassnapshot && !this.trigger.documents[i].saveasfile && !this.trigger.documents[i].saveasdocument && !this.trigger.documents[i].sendtojavaclass && !this.trigger.documents[i].sendmail)
                ) {
                    disabled = true
                    break
                }
            }
            return disabled
        }
    },
    watch: {
        async propTrigger() {
            this.loadTrigger()
            await this.loadJobInfo()
        }
    },
    async created() {
        this.loadTrigger()
        await this.loadDatasets()
        await this.loadJobInfo()
    },
    methods: {
        loadTrigger() {
            this.trigger = this.propTrigger ? { ...this.propTrigger } : {}
        },
        async loadDatasets() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/?asPagedList=true`).then((response: AxiosResponse<any>) => (this.datasets = response.data.item))
        },
        async loadJobInfo() {
            if (this.trigger.jobName) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/getJob?jobName=${this.trigger.jobName}&jobGroup=${this.trigger.jobGroup}&triggerName=${this.trigger.triggerName}&triggerGroup=${this.trigger.triggerGroup}`).then((response: AxiosResponse<any>) => {
                    this.jobInfo = response.data.job
                    this.functionalities = response.data.functionality
                })
            }
            if (!this.trigger.documents) {
                this.trigger = { ...this.trigger, documents: this.jobInfo?.documents }
            }
        },
        setCronValid(value: boolean) {
            this.validCron = value
        },
        closeDialog() {
            this.trigger = null
            this.jobInfo = null
            this.functionalities = []
            this.$emit('close')
        },
        async saveTrigger() {
            this.loading = true

            const formattedTrigger = this.formatTrigger()

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/saveTrigger`, formattedTrigger, { headers: { 'X-Disable-Errors': 'true' } })
                .then((response: AxiosResponse<any>) => {
                    if (response.data.Errors) {
                        this.setWarningMessage(response.data.Errors[0] ?? 'default error')
                    } else {
                        this.store.setInfo({
                            title: this.$t('common.toast.' + this.operation + 'Title'),
                            msg: this.$t('common.toast.success')
                        })
                        this.$emit('saved')
                    }
                })
                .catch((response) => {
                    this.setWarningMessage(response)
                })
            this.loading = false
        },
        setWarningMessage(response: string) {
            this.warningTitle = this.$t('common.toast.' + this.operation + 'Title')
            this.warningVisible = true
            this.warningMessage = this.getErrorMessage(response)
        },
        getErrorMessage(message: string) {
            switch (message) {
                case 'errors.trigger.missingDataSet':
                    return this.$t('managers.scheduler.missingDataSet')
                case 'errors.trigger.missingDataSetParameter':
                    return this.$t('managers.scheduler.missingDataSetParameter')
                case 'Empty name':
                    return this.$t('managers.scheduler.emptyName')
                case 'sbi.scheduler.schedulation.error.alreadyPresent':
                    return this.$t('managers.scheduler.triggerAlreadyPresent')
                case 'Error in setting java class ':
                    return this.$t('managers.scheduler.javaClassError')
                default:
                    return this.$t('managers.scheduler.savingTriggerGenericError')
            }
        },
        formatTrigger() {
            const formattedTrigger = {
                ...this.trigger,
                documents: this.trigger.documents.map((el: any) => {
                    return { ...el }
                })
            }

            if (this.trigger.frequency) {
                formattedTrigger.frequency = { ...this.trigger.frequency, cron: { ...this.trigger.frequency.cron, parameter: { type: this.trigger.frequency.cron.type, parameter: { ...this.trigger.frequency.cron.parameter } } } }
            }

            if (!formattedTrigger.triggerGroup) formattedTrigger.triggerGroup = ''
            formattedTrigger._endTime = new Date().getHours() + ':' + new Date().getMinutes()

            if (formattedTrigger.chrono.type === 'single') {
                delete formattedTrigger.chrono.parameter
            } else if (formattedTrigger.chrono.type !== 'event') {
                this.formatCron(formattedTrigger)
            }

            if (!formattedTrigger.endDateTiming || !formattedTrigger.zonedEndTime) {
                delete formattedTrigger.zonedEndTime
            }

            this.deleteTriggerProps(formattedTrigger)
            this.formatTriggerDocuments(formattedTrigger)

            return formattedTrigger
        },
        deleteTriggerProps(formattedTrigger: any) {
            const props = ['startDateTiming', 'startTimeTiming', 'endDateTiming', 'endTimeTiming', 'startDate', 'startTime', 'startDateRFC3339', 'endDate', 'endTime']
            props.forEach((property: string) => delete formattedTrigger[property])
        },
        formatCron(formattedTrigger: any) {
            formattedTrigger.chrono = this.trigger.frequency.cron

            formattedTrigger.zonedStartTime = new Date(this.trigger.frequency.startDate)

            if (formattedTrigger.frequency.endDate) {
                formattedTrigger.zonedEndTime = new Date(this.trigger.frequency.endDate)
                formattedTrigger.endDateTiming = formattedTrigger.zonedEndTime
            } else {
                formattedTrigger.zonedEndtime = null
                formattedTrigger.endDateTiming = null
            }
        },
        formatTriggerDocuments(formattedTrigger: any) {
            formattedTrigger.documents.forEach((el: any, index: number) => {
                el.label = el.name
                el.labelId = el.labelId ?? el.id + '__' + (index + 1)
            })
        }
    }
})
</script>

<style lang="scss">
#timing-tabs .p-tabview-panels {
    padding: 0;
}
</style>
