<template>
    <Dialog :header="warningTitle" :visible="visible" :modal="true" :content-style="functionsCatalogPreviewDialogDescriptor.warningDialog.style" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-m-2">
            <Message class="p-m-2" severity="warn" :closable="false" :style="functionsCatalogPreviewDialogDescriptor.styles.message">
                {{ warningMessage }}
            </Message>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import functionsCatalogPreviewDialogDescriptor from './FunctionsCatalogPreviewDialogDescriptor.json'

export default defineComponent({
    name: 'functions-catalog-preview-warning-dialog',
    components: { Dialog, Message },
    props: {
        visible: { type: Boolean },
        warningTitle: { type: String },
        warningMessage: { type: String }
    },
    emits: ['close'],
    data() {
        return { functionsCatalogPreviewDialogDescriptor }
    },
    created() {},
    methods: {}
})
</script>
