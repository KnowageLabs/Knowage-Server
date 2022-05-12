<template>
    <Dialog :header="title" :visible="visible" :modal="true" :contentStyle="functionsCatalogDetailDescriptor.warningDialog.style" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-m-2">
            <Message class="p-m-2" severity="warn" :closable="false" :style="functionsCatalogDetailDescriptor.styles.message">
                <p class="p-m-2" v-for="(field, index) in missingFields" :key="index">{{ field }}</p>
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
    emits: ['close'],
    props: {
        visible: { type: Boolean },
        title: { type: String },
        missingFields: { type: Array }
    },
    data() {
        return {
            functionsCatalogDetailDescriptor
        }
    }
})
</script>
