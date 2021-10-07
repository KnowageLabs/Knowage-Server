<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="schedulerTimingOutputDetailDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.scheduler.timingAndOutput') }}
                </template>
            </Toolbar>
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
                    <span>{{ $t('managers.scheduler.output') }}</span>
                </template>

                <SchedulerTimingOutputOutputTab :propDocuments="documents"></SchedulerTimingOutputOutputTab>
            </TabPanel>
        </TabView>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Dialog from 'primevue/dialog'
import schedulerTimingOutputDetailDialogDescriptor from './SchedulerTimingOutputDetailDialogDescriptor.json'
import SchedulerTimingOutputTimingTab from './tabs/SchedulerTimingOutputTimingTab/SchedulerTimingOutputTimingTab.vue'
import SchedulerTimingOutputOutputTab from './tabs/SchedulerTimingOutputOutputTab/SchedulerTimingOutputOutputTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'scheduler-timing-output-detail-dialog',
    components: { Dialog, SchedulerTimingOutputTimingTab, SchedulerTimingOutputOutputTab, TabView, TabPanel },
    props: { visible: { type: Boolean }, propTrigger: { type: Object }, propDocuments: { type: Array } },
    emits: ['close'],
    data() {
        return {
            schedulerTimingOutputDetailDialogDescriptor,
            trigger: null as any,
            info: null as any,
            validCron: false,
            datasets: [] as any[],
            documents: [] as any[]
        }
    },
    watch: {
        propTrigger() {
            this.loadTrigger()
        },
        propDocuments() {
            this.loadDocuments()
        }
    },
    async created() {
        this.loadTrigger()
        this.loadDocuments()
        await this.loadDatasets()
    },
    methods: {
        loadTrigger() {
            this.trigger = this.propTrigger ? { ...this.propTrigger } : {}
            console.log('LOADED TRIGGER IN MAIN DIALOG: ', this.trigger)
        },
        loadDocuments() {
            this.documents = this.propDocuments ? [...this.propDocuments] : []
            console.log('LOADED DOCUMENTS IN MAIN DIALOG: ', this.documents)
        },
        async loadDatasets() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/?asPagedList=true`).then((response) => (this.datasets = response.data.item))
            console.log('LOADED DATASETS: ', this.datasets)
        },
        setCronValid(value: boolean) {
            this.validCron = value
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#timing-tabs .p-tabview-panels {
    padding: 0;
}
</style>
