<template>
    <Dialog id="olap-wizard-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.outputWizard.title') }}
                </template>
            </Toolbar>
        </template>

        <form class="p-fluid p-formgrid p-grid p-m-1">
            <InlineMessage class="p-m-1" severity="info" closable="false">{{ $t('documentExecution.olap.outputWizard.infoMsg') }}</InlineMessage>
            <div id="type-container" class="p-field p-d-flex p-ai-center p-m-2">
                <span>{{ $t('managers.workspaceManagement.dataPreparation.transformations.outputType') }}: </span>
                <div class="p-mx-2">
                    <RadioButton id="fileType" name="file" value="file" v-model="selectedType" />
                    <label for="fileType" class="p-ml-1">{{ $t('common.file') }}</label>
                </div>
                <div>
                    <RadioButton id="tableType" name="table" value="table" v-model="selectedType" />
                    <label for="tableType" class="p-ml-1">{{ $t('common.table.table') }}</label>
                </div>
            </div>

            <div class="p-field p-float-label p-col-12 p-mt-2">
                <Dropdown id="version" class="kn-material-input" v-model="selectedVersion" :options="olapVersionsProp" optionLabel="name" optionValue="name" />
                <label for="version" class="kn-material-input-label"> {{ $t('documentExecution.olap.outputWizard.version') }} </label>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--secondary"> {{ $t('common.back') }}</Button>
            <Button class="kn-button kn-button--secondary"> {{ $t('common.next') }}</Button>
            <Button class="kn-button kn-button--primary"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import descriptor from './OlapOutputWizardDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InlineMessage from 'primevue/inlinemessage'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog, Dropdown, InlineMessage, RadioButton },
    props: { olapVersionsProp: { type: Boolean, required: true } },
    emits: ['close'],
    data() {
        return {
            descriptor,
            selectedVersion: null as any,
            selectedType: 'file' as any
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
