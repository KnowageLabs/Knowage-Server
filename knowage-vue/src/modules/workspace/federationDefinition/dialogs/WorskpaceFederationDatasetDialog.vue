<template>
    <div>
        <Dialog :header="$t('workspace.federationDefinition.datasetDetail')" :style="worskpaceFederationDatasetDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
            <div>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <h3 class="p-m-2">{{ $t('common.name') }}</h3>
                    <span class="dataset-info-span">{{ dataset.name }}</span>
                </div>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <h3 class="p-m-2">{{ $t('common.label') }}</h3>
                    <span class="dataset-info-span">{{ dataset.label }}</span>
                </div>
                <div v-if="dataset.description" class="p-d-flex p-flex-row p-ai-center">
                    <h3 class="p-m-2">{{ $t('common.description') }}</h3>
                    <span class="dataset-info-span">{{ dataset.description }}</span>
                </div>
            </div>
            <template #footer>
                <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import worskpaceFederationDatasetDialogDescriptor from './WorskpaceFederationDatasetDialogDescriptor.json'

export default defineComponent({
    name: 'workspace-federation-dataset-dialog',
    components: { Dialog },
    emits: ['close'],
    props: { visible: { type: Boolean }, dataset: { type: Object } },
    data() {
        return {
            worskpaceFederationDatasetDialogDescriptor
        }
    }
})
</script>

<style lang="scss" scoped>
.dataset-info-span {
    margin-left: auto;
}
</style>
