<template>
    <Dialog class="kn-dialog--toolbar--primary importExportDialog" :style="dataViewDescriptor.style.dialog" v-bind:visible="visible" footer="footer" :header="$t('datasetWizard')" :closable="false" modal>
        <span v-if="wizardStep === 1">
            <Step1 :selectedDataset="dataset" />
        </span>
        <span v-if="wizardStep === 2">Step 2</span>
        <span v-if="wizardStep === 3">Step 3</span>
        <span v-if="wizardStep === 4">Step 4</span>

        <template #footer>
            <div>
                <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="$emit('closeDialog')" />
                <Button v-if="wizardStep > 1" class="kn-button kn-button--secondary" :label="$t('common.back')" @click="wizardStep--" />
                <Button class="kn-button kn-button--primary" :label="$t('common.next')" @click="wizardStep++" />
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import dataViewDescriptor from './WorkspaceDatasetWizardDescriptor.json'
import Step1 from './WorkspaceDatasetWizardStep1.vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    components: { Dialog, Step1 },
    props: { selectedDataset: { type: Object as any }, visible: { type: Boolean as any } },
    emits: ['touched', 'fileUploaded', 'closeDialog'],
    data() {
        return {
            dataViewDescriptor,
            dataset: {} as any,
            wizardStep: 1
        }
    },
    created() {
        this.dataset = this.selectedDataset
        this.dataset.id ? this.getSelectedDataset() : ''
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.dataset.id ? this.getSelectedDataset() : ''
        }
    },
    methods: {
        async getSelectedDataset() {
            this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.selectedDataset.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.dataset = response.data[0] ? { ...response.data[0] } : {}
                })
                .catch()
        }
    }
})
</script>
