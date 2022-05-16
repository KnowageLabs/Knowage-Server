<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionCNContainerDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ data?.documentLabel }}
                </template>
                <template #end>
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip="$t('common.close')" @click="closeDialog" />
                </template>
            </Toolbar>
        </template>

        <iframe v-if="data" ref="documentFrame" :name="data.iFrameName" class="iframe"></iframe>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import documentExecutionCNContainerDialogDescriptor from './DocumentExecutionCNContainerDialogDescriptor.json'

export default defineComponent({
    name: 'document-execution-cn-container-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, data: { type: Object } },
    emits: ['close', 'selected'],
    data() {
        return {
            documentExecutionCNContainerDialogDescriptor
        }
    },
    methods: {
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss" scoped>
.iframe {
    border: 0;
    height: 95%;
    width: 100%;
}
</style>
