<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="schedulerTimingOutputInfoDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.scheduler.schedulationInformations') }}
                </template>
            </Toolbar>
        </template>

        <TabView v-if="info">
            <TabPanel v-for="(document, index) in info.documents" :key="index">
                <template #header>
                    <span class="document-label">{{ document.label }}</span>
                </template>

                <div>
                    <h4>{{ $t('managers.scheduler.mailTo') }}:</h4>
                    <p>{{ document.mailtos }}</p>
                </div>
                <div>
                    <h4>{{ $t('managers.scheduler.attachedZip') }}:</h4>
                    <p>{{ document.zipMailName }}</p>
                </div>
                <div>
                    <h4>{{ $t('managers.scheduler.mailSubject') }}:</h4>
                    <p>{{ document.mailsubj }}</p>
                </div>
                <div>
                    <h4>{{ $t('managers.scheduler.containedFileName') }}:</h4>
                    <p>{{ document.containedFileName }}</p>
                </div>
                <div>
                    <h4>{{ $t('managers.scheduler.mailText') }}</h4>
                    <p>{{ document.mailtxt }}</p>
                </div>
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
import schedulerTimingOutputInfoDialogDescriptor from './SchedulerTimingOutputInfoDialogDescriptor.json'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'scheduler-timing-output-info-dialog',
    components: { Dialog, TabView, TabPanel },
    props: { visible: { type: Boolean }, triggerInfo: { type: Object } },
    emits: ['close'],
    data() {
        return {
            schedulerTimingOutputInfoDialogDescriptor,
            info: null as any
        }
    },
    watch: {
        triggerInfo() {
            this.loadTriggerInfo()
        }
    },
    created() {
        this.loadTriggerInfo()
    },
    methods: {
        loadTriggerInfo() {
            this.info = this.triggerInfo
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss" scoped>
.document-label {
    text-transform: uppercase;
}
</style>
