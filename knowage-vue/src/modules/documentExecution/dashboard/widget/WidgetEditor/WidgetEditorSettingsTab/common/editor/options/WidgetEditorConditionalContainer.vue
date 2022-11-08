<template>
    <span class="p-float-label">
        <Textarea class="kn-material-input kn-width-full" rows="4" :autoResize="true" v-model="condition" maxlength="150" @change="onColumnChanged" />
        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.editorTags.limit') }}</label>
    </span>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'widget-editor-repeater',
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
