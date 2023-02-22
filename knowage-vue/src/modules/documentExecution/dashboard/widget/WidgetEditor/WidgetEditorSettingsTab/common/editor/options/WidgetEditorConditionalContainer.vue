<template>
    <span class="p-float-label">
        <Textarea v-model="condition" class="kn-material-input kn-width-full" rows="4" :auto-resize="true" maxlength="150" @change="onColumnChanged" />
        <label class="kn-material-input-label">{{ $t('common.condition') }}</label>
    </span>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'widget-editor-conditional-container',
    components: { Textarea },
    emits: ['insertChanged'],
    data() {
        return {
            condition: ''
        }
    },
    methods: {
        onColumnChanged() {
            const forInsert = `<div kn-if="${this.condition}"></div>`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
