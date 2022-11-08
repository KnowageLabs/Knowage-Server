<template>
    <span class="p-float-label">
        <InputText class="kn-material-input" v-model="i18n" @change="onColumnChanged" />
        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.editorTags.i18n') }}</label>
    </span>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'widget-editor-repeater',
    emits: ['insertChanged'],
    data() {
        return {
            i18n: ''
        }
    },
    methods: {
        onColumnChanged() {
            const forInsert = `[kn-i18n='${this.i18n}']`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
