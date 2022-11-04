<template>
    <div>
        <Dialog :header="title" :style="descriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
            <Message class="p-m-4" severity="info" :closable="false" :style="descriptor.styles.message">
                {{ infoMessage }}
            </Message>
            <Timeline :value="events" align="alternate">
                <template #marker="slotProps">
                    <span class="custom-marker shadow-2">
                        <span v-if="slotProps.item.id < 4">
                            <i v-if="events && slotProps.item.id === events.length - 1" class="fa-solid fa-spinner fa-spin"></i>
                            <i v-else class="fa-regular fa-circle-check"></i>
                        </span>
                        <i v-if="slotProps.item.id === 4">
                            <i v-if="slotProps.item.status && slotProps.item.status === 'error'" class="fa-regular fa-circle-xmark"></i>
                            <i v-else class="fa-regular fa-circle-check"></i>
                        </i>
                    </span>
                </template>
                <template #content="slotProps">
                    {{ slotProps.item.message }}
                </template>
            </Timeline>
            <template #footer>
                <Button class="kn-button kn-button--primary" @click="closeDialog" :label="computeLabel()"> </Button>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import descriptor from './DataPreparationAvroHandlingDialogDescriptor.json'
import Timeline from 'primevue/timeline'
export default defineComponent({
    name: 'data-preparation-avro-handling-dialog',
    components: { Dialog, Message, Timeline },
    emits: ['close'],
    props: { visible: { type: Boolean }, title: { type: String }, infoMessage: { type: String }, events: { type: Array } },
    data() {
        return {
            descriptor,
            sec: 0
        }
    },
    mounted() {
        this.sec = 5
    },
    methods: {
        computeLabel() {
            if (this.events?.length === 5) {
                let lastEvent = this.events[this.events.length - 1] as any
                if (lastEvent?.status === 'error') {
                    return this.$t('common.close')
                } else {
                    return this.$t('common.continue') + ' (' + this.sec + ')'
                }
            } else {
                return this.$t('common.continue')
            }
        },
        closeDialog() {
            this.$emit('close')
        },
        handleTimeout() {
            let self = this
            var x = setInterval(function () {
                if (self.sec > 0) {
                    self.sec--
                } else if (self.sec == 0) {
                    clearInterval(x)
                }
            }, 1000)
            setTimeout(() => {
                this.closeDialog()
            }, 5000)
        }
    },
    watch: {
        events: {
            handler(newValue) {
                if (newValue.length === 5) {
                    let lastEvent = newValue[newValue.length - 1] as any
                    if (lastEvent?.status !== 'error') {
                        this.handleTimeout()
                    }
                }
            },
            deep: true
        }
    }
})
</script>

<style lang="scss">
.p-timeline-event {
    min-height: 40px;
    max-height: 40px;
}
</style>
