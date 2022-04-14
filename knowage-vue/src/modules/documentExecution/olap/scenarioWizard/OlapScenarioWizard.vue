<template>
    <Dialog id="olap-wizard-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.scenarioWizard.title') }}
                </template>
            </Toolbar>
        </template>

        <template #footer>
            <Button class="kn-button"> {{ $t('documentExecution.olap.scenarioWizard.clearData') }}</Button>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import descriptor from './OlapScenarioWizardDescriptor.json'

export default defineComponent({
    name: 'olap-scenario-wizard',
    components: { Dialog },
    props: { olapVersionsProp: { type: Boolean, required: true }, sbiExecutionId: { type: String } },
    emits: ['close'],
    computed: {},
    data() {
        return {
            descriptor
        }
    },
    watch: {},
    created() {},
    methods: {}
})
</script>

<style lang="scss">
#olap-wizard-dialog .p-dialog-header,
#olap-wizard-dialog .p-dialog-content {
    padding: 0;
}
#olap-wizard-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
.wizard-overlay-spinner .p-progress-spinner-svg {
    width: 125px;
}
</style>
