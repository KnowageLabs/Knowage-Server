<template>
    <Dialog :style="kpiSchedulerTabViewDescriptor.dialog.style" :content-style="kpiSchedulerTabViewDescriptor.dialog.contentStyle" :header="$t('kpi.kpiScheduler.saveScheduler')" :visible="true" :modal="true" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-field p-m-4">
            <span class="p-float-label">
                <InputText
                    v-model.trim="name"
                    class="kn-material-input"
                    type="text"
                    max="40"
                    :class="{
                        'p-invalid': name && name.length == 0 && nameDirty
                    }"
                    @blur="nameDirty = true"
                />
                <label class="kn-material-input-label"> {{ $t('common.name') }} *</label>
                <div id="name-help">
                    <small id="name-help">{{ nameHelp }}</small>
                </div>
            </span>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="$emit('close')"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="saveSchedulerButtonDisabled" @click="$emit('save', name)"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import kpiSchedulerTabViewDescriptor from './KpiSchedulerTabViewDescriptor.json'

export default defineComponent({
    name: 'kpi-scheduler-save-dialog',
    components: { Dialog },
    props: { schedulerName: { type: String } },
    emits: ['close'],
    data() {
        return {
            kpiSchedulerTabViewDescriptor,
            name: '' as string,
            nameDirty: false
        }
    },
    computed: {
        nameHelp(): string {
            return (this.name?.length ?? '0') + ' / 40'
        },
        saveSchedulerButtonDisabled(): boolean {
            return !this.name
        }
    },
    watch: {
        currentRule() {
            this.loadSchedulerName()
        }
    },
    async created() {
        this.loadSchedulerName()
    },
    methods: {
        loadSchedulerName() {
            this.name = this.schedulerName as string
        }
    }
})
</script>

<style lang="scss" scoped>
#name-help {
    display: flex;
    justify-content: flex-end;
}
</style>
