<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary widget-tags-dialog" :style="descriptor.dialogStyle" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t(`dashboard.widgetEditor.editorTags.${mode}`) }}
                </template>
            </Toolbar>
        </template>

        <div class="tags-dialog-content p-mx-2">
            <Message severity="info" :closable="false" :style="descriptor.hintStyle">
                {{ $t(`dashboard.widgetEditor.editorTags.hint.${mode}`) }}
            </Message>

            <WidgetEditorParameters v-if="mode === 'parameters'" :drivers="drivers" @insertChanged="onInsertChanged"></WidgetEditorParameters>
            <WidgetEditorActiveSelections v-else-if="mode === 'activesel'" :widgetModel="widgetModel" @insertChanged="onInsertChanged"></WidgetEditorActiveSelections>
            <WidgetEditorRepeater v-else-if="mode === 'repeater'" :widgetModel="widgetModel" @insertChanged="onInsertChanged"></WidgetEditorRepeater>
            <WidgetEditorCalculator v-else-if="mode === 'calculator'" :widgetModel="widgetModel" @insertChanged="onInsertChanged"></WidgetEditorCalculator>
            <WidgetEditorRepeatIndex v-else-if="mode === 'repeatIndex'" :widgetModel="widgetModel"></WidgetEditorRepeatIndex>
            <WidgetEditorVariables v-else-if="mode === 'variables'" :variables="variables" @insertChanged="onInsertChanged"></WidgetEditorVariables>
            <WidgetEditorConditionalContainer v-else-if="mode === 'conditional'" @insertChanged="onInsertChanged"></WidgetEditorConditionalContainer>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="!forInsert" @click="addInsert"> {{ $t('common.add') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVariable, IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Dialog from 'primevue/dialog'
import descriptor from './WidgetTagsDialogDescriptor.json'
import Message from 'primevue/message'
import WidgetEditorParameters from './options/WidgetEditorParameters.vue'
import WidgetEditorActiveSelections from './options/WidgetEditorActiveSelections.vue'
import WidgetEditorVariables from './options/WidgetEditorVariables.vue'
import WidgetEditorRepeater from './options/WidgetEditorRepeater.vue'
import WidgetEditorRepeatIndex from './options/WidgetEditorRepeatIndex.vue'
import WidgetEditorConditionalContainer from './options/WidgetEditorConditionalContainer.vue'
import WidgetEditorCalculator from './options/WidgetEditorCalculator.vue'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog, Message, WidgetEditorParameters, WidgetEditorActiveSelections, WidgetEditorVariables, WidgetEditorRepeater, WidgetEditorRepeatIndex, WidgetEditorConditionalContainer, WidgetEditorCalculator },
    props: { visible: Boolean, widgetModel: { type: Object as PropType<IWidget>, required: true }, mode: { type: String, required: true }, widgetType: String, drivers: { type: Array as PropType<any[]>, required: true }, variables: { type: Array as PropType<IVariable[]>, required: true } },
    emited: ['close', 'insert'],
    computed: {},
    data() {
        return {
            descriptor,
            forInsert: '' as string
        }
    },
    watch: {
        mode() {
            this.setInitialInsertValue()
        }
    },
    created() {
        this.setInitialInsertValue()
    },

    methods: {
        setInitialInsertValue() {
            switch (this.mode) {
                case 'crossnav':
                    this.forInsert = '<div kn-cross></div>'
                    break
                case 'repeatIndex':
                    if (this.widgetModel.dataset) this.forInsert = '[kn-repeat-index]'
                    break
                default:
                    this.forInsert = ''
            }
        },
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
