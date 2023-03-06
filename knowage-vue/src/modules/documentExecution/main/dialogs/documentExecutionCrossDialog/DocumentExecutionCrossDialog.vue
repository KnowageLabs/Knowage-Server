<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :content-style="documentExecutionCrossDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ crossNavigationDocument?.name }}
                </template>
                <template #end>
                    <Button v-tooltip="$t('common.close')" icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeDialog" />
                </template>
            </Toolbar>
        </template>

        <div>
            <DocumentExecution v-if="crossNavigationDocument" :id="crossNavigationDocument.label" :prop-mode="'document-execution'" :parameter-values-map="parameterValuesMap" :tab-key="tabKey" @parametersChanged="$emit('parametersChanged', $event)" @close="$emit('close')"></DocumentExecution>
        </div>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import documentExecutionCrossDialogDescriptor from './DocumentExecutionCrossDialogDescriptor.json'
// import DocumentExecution from '../../DocumentExecution.vue'

export default defineComponent({
    name: 'document-execution-cross-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, document: { type: Object, required: true }, parameterValuesMap: { type: Object }, tabKey: { type: String } },
    emits: ['parametersChanged', 'close'],
    data() {
        return {
            documentExecutionCrossDialogDescriptor,
            crossNavigationDocument: null as any
        }
    },
    watch: {
        document() {
            this.loadCrossNavigationDocument()
        }
    },
    created() {
        this.loadCrossNavigationDocument()
    },
    methods: {
        loadCrossNavigationDocument() {
            this.crossNavigationDocument = this.document
            console.log('------------- LOADED DOCUMENT: ', this.document)
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss" scoped></style>
