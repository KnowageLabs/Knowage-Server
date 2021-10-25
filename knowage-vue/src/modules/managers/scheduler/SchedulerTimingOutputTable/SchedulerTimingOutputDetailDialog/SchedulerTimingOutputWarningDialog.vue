<template>
    <Dialog :style="schedulerTimingOutputDetailDialogDescriptor.warningDialog.style" :header="warningTitle" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-m-2">
            {{ warningMessage }}
        </div>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import schedulerTimingOutputDetailDialogDescriptor from './SchedulerTimingOutputDetailDialogDescriptor.json'

export default defineComponent({
    name: 'scheduler-timing-output-warning-dialog',
    components: { Dialog },
    props: {
        visible: { type: Boolean },
        warningTitle: { type: String },
        warningMessage: { type: String }
    },
    emits: ['close'],
    data() {
        return {
            schedulerTimingOutputDetailDialogDescriptor
        }
    },
    created() {},
    methods: {}
})
</script>
