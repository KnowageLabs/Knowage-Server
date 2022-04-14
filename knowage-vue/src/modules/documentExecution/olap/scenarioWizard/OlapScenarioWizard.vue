<template>
    <Dialog id="olap-scenario-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.scenarioWizard.title') }}
                </template>
                <template #end>
                    <Button icon="fas fa-info-circle" class="p-button-text p-button-rounded p-button-plain" @click="showInfo = !showInfo" />
                </template>
            </Toolbar>
        </template>

        <form class="p-fluid p-formgrid p-grid p-m-1">
            <InlineMessage v-if="showInfo" class="p-m-1" severity="info">{{ $t('documentExecution.olap.scenarioWizard.longInfo') }}</InlineMessage>

            <!-- <div class="p-field p-float-label p-col-12 p-mt-2">
                <Dropdown id="version" class="kn-material-input" v-model="selectedVersion" :options="olapVersionsProp" optionLabel="name" />
                <label for="version" class="kn-material-input-label"> {{ $t('documentExecution.olap.outputWizard.version') }} </label>
            </div> -->
        </form>

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
import InlineMessage from 'primevue/inlinemessage'
// import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'olap-scenario-wizard',
    components: { Dialog, InlineMessage },
    props: { olapVersionsProp: { type: Boolean, required: true }, sbiExecutionId: { type: String } },
    emits: ['close'],
    computed: {},
    data() {
        return {
            descriptor,
            showInfo: false,
            loading: false
        }
    },
    watch: {},
    created() {},
    methods: {}
})
</script>

<style lang="scss">
#olap-scenario-dialog .p-dialog-header,
#olap-scenario-dialog .p-dialog-content {
    padding: 0;
}
#olap-scenario-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
