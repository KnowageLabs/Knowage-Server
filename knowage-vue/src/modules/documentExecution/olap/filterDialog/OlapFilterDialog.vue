<template>
    <Dialog id="olap-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapFilterDialogDescriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.outputWizard.title') }}
                </template>
            </Toolbar>
        </template>

        {{ propFilter }}

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import olapFilterDialogDescriptor from './OlapFilterDialogDescriptor.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog },
    props: { olapVersionsProp: { type: Boolean, required: true }, propFilter: { type: Object } },
    emits: ['close'],
    data() {
        return {
            olapFilterDialogDescriptor
        }
    },
    watch: {},
    created() {},
    methods: {
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#olap-filter-dialog .p-dialog-header,
#olap-filter-dialog .p-dialog-content {
    padding: 0;
}
#olap-filter-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
