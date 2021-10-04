<template>
    <Dialog :header="title" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-m-2">
            <p class="p-m-0" v-for="(field, index) in missingFields" :key="index">{{ field }}</p>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'functions-catalog-warning-dialog',
    components: { Dialog },
    emits: ['close'],
    props: {
        visible: { type: Boolean },
        title: { type: String },
        missingFields: { type: Array }
    }
})
</script>
