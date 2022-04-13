<template>
    <Dialog id="olap-wizard-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.outputWizard.title') }}
                </template>
            </Toolbar>
        </template>

        {{ olapVersionsProp }}

        <form class="p-fluid p-formgrid p-grid p-m-1">
            <span class="p-field p-float-label p-col-12 p-lg-6" v-bind:class="{ 'p-lg-12': !isDataSetVisible }">
                <Dropdown id="version" class="kn-material-input" v-model="selectedVersion" :options="olapVersionsProp" optionLabel="name" optionValue="name" />
                <label for="version" class="kn-material-input-label"> {{ $t('documentExecution.olap.outputWizard.version') }} </label>
            </span>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import descriptor from './OlapOutputWizardDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog, Dropdown },
    props: { olapVersionsProp: { type: Boolean, required: true } },
    emits: ['close'],
    data() {
        return {
            descriptor,
            selectedVersion: {} as any
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
</style>
