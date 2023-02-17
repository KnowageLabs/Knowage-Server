<template>
    <Dialog :header="title" :visible="visible" :modal="true" :content-style="functionsCatalogDetailDescriptor.warningDialog.style" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-m-2">
            <Message class="p-m-2" severity="warn" :closable="false" :style="functionsCatalogDetailDescriptor.styles.message">
                <p v-for="(field, index) in missingFields" :key="index" class="p-m-2">{{ field }}</p>
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
import functionsCatalogDetailDescriptor from './FunctionsCatalogDetailDescriptor.json'

export default defineComponent({
    name: 'functions-catalog-warning-dialog',
    components: { Dialog, Message },
    props: {
        visible: { type: Boolean },
        title: { type: String },
        missingFields: { type: Array }
    },
    emits: ['close'],
    data() {
        return {
            functionsCatalogDetailDescriptor
        }
    }
})
</script>
