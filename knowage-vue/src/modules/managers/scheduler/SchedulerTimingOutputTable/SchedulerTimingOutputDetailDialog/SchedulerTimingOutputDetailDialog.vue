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
                <SchedulerTimingOutputTimingTab :propTrigger="trigger"></SchedulerTimingOutputTimingTab>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>OUTPUT</span>
                </template>
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
import Dialog from 'primevue/dialog'
import schedulerTimingOutputDetailDialogDescriptor from './SchedulerTimingOutputDetailDialogDescriptor.json'
import SchedulerTimingOutputTimingTab from './tabs/SchedulerTimingOutputTimingTab/SchedulerTimingOutputTimingTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'scheduler-timing-output-detail-dialog',
    components: { Dialog, SchedulerTimingOutputTimingTab, TabView, TabPanel },
    props: { visible: { type: Boolean }, propTrigger: { type: Object } },
    emits: ['close'],
    data() {
        return {
            schedulerTimingOutputDetailDialogDescriptor,
            trigger: null as any,
            info: null as any
        }
    },
    watch: {
        propTrigger() {
            this.loadTrigger()
        }
    },
    created() {
        this.loadTrigger()
    },
    methods: {
        loadTrigger() {
            this.trigger = this.propTrigger ? { ...this.propTrigger } : {}
            console.log('LOADED TRIGGER IN MAIN DIALOG: ', this.trigger)
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
