<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary widget-tags-dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t(`dashboard.widgetEditor.editorTags.${mode}`) }}
                </template>
            </Toolbar>
        </template>

        <div class="tags-dialog-content p-mx-2">
            <p class="kn-material-input-label">{{ $t(`dashboard.widgetEditor.editorTags.hint.${mode}`) }}</p>
            {{ widgetType }}

            <WidgetEditorParameters v-if="mode === 'parameters'" :drivers="drivers" @insertChanged="onInsertChanged"></WidgetEditorParameters>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="!forInsert" @click="addInsert"> {{ $t('common.add') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dialog from 'primevue/dialog'
import WidgetEditorParameters from './options/WidgetEditorParameters.vue'
import descriptor from './WidgetTagsDialogDescriptor.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog, WidgetEditorParameters },
    props: { visible: Boolean, mode: { type: String, required: true }, widgetType: String, drivers: { type: Array as PropType<any[]>, required: true } },
    emited: ['close', 'insert'],
    computed: {},
    data() {
        return {
            descriptor,
            forInsert: '' as string
        }
    },
    setup() {},
    created() {},
    watch: {},
    methods: {
        onInsertChanged(value: string) {
            console.log('>>> ON INSERT CHANGED: ', value)
            this.forInsert = value
        },
        addInsert() {
            this.$emit('insert', this.forInsert)
        },
        closeDialog() {
            this.forInsert = ''
            this.$emit('close')
        }
    }
})
</script>
<style lang="scss">
.widget-tags-dialog .p-dialog-content {
    padding: 0;
}
</style>
