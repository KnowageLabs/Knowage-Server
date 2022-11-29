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
            <WidgetEditorVariables v-else-if="mode === 'variables'" :variables="variables" @insertChanged="onInsertChanged"></WidgetEditorVariables>
            <WidgetEditorInternationalization v-else-if="mode === 'internationalization'" @insertChanged="onInsertChanged"></WidgetEditorInternationalization>
            <WidgetEditorRepeater v-else-if="mode === 'repeater'" :widgetModel="widgetModel" @insertChanged="onInsertChanged"></WidgetEditorRepeater>
            <WidgetEditorRepeatIndex v-else-if="mode === 'repeatIndex'" :widgetModel="widgetModel"></WidgetEditorRepeatIndex>
            <WidgetEditorCalculator v-else-if="mode === 'calculator'" @insertChanged="onInsertChanged"></WidgetEditorCalculator>
            <WidgetEditorPreview v-else-if="mode === 'preview'" :widgetModel="widgetModel" :selectedDatasets="selectedDatasets" @insertChanged="onInsertChanged"></WidgetEditorPreview>
            <WidgetEditorConditionalContainer v-else-if="mode === 'conditional'" @insertChanged="onInsertChanged"></WidgetEditorConditionalContainer>
            <WidgetEditorActiveSelections v-else-if="mode === 'activesel'" :widgetModel="widgetModel" @insertChanged="onInsertChanged"></WidgetEditorActiveSelections>
            <WidgetEditorSelection v-else-if="mode === 'selection'" :widgetModel="widgetModel" @insertChanged="onInsertChanged"></WidgetEditorSelection>
            <WidgetEditorColumnData v-else-if="mode === 'columnsData'" :widgetModel="widgetModel" @insertChanged="onInsertChanged"></WidgetEditorColumnData>
            <WidgetEditorCrossNavigation v-else-if="mode === 'crossnav'" :widgetModel="widgetModel" @insertChanged="onInsertChanged"></WidgetEditorCrossNavigation>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="!forInsert" @click="addInsert"> {{ $t('common.add') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDriver, IDataset, IVariable, IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
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
import WidgetEditorInternationalization from './options/WidgetEditorInternationalization.vue'
import WidgetEditorPreview from './options/WidgetEditorPreview.vue'
import WidgetEditorSelection from './options/WidgetEditorSelection.vue'
import WidgetEditorColumnData from './options/WidgetEditorColumnData.vue'
import WidgetEditorCrossNavigation from './options/WidgetEditorCrossNavigation.vue'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: {
        Dialog,
        Message,
        WidgetEditorParameters,
        WidgetEditorActiveSelections,
        WidgetEditorVariables,
        WidgetEditorRepeater,
        WidgetEditorRepeatIndex,
        WidgetEditorConditionalContainer,
        WidgetEditorCalculator,
        WidgetEditorInternationalization,
        WidgetEditorPreview,
        WidgetEditorSelection,
        WidgetEditorColumnData,
        WidgetEditorCrossNavigation
    },
    props: {
        visible: Boolean,
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        mode: { type: String, required: true },
        widgetType: String,
        drivers: { type: Array as PropType<IDashboardDriver[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        selectedDatasets: { type: Array as PropType<IDataset[]> }
    },
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
                case 'repeatIndex':
                    if (this.widgetModel.dataset) this.forInsert = '[kn-repeat-index]'
                    break
                default:
                    this.forInsert = ''
            }
        },
        onInsertChanged(value: string) {
            this.forInsert = value
        },
        addInsert() {
            this.$emit('insert', this.forInsert, this.mode)
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
