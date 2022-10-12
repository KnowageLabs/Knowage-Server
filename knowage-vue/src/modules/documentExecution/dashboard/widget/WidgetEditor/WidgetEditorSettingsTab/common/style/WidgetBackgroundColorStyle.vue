<template>
    <div class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12">
            <WidgetEditorColorPicker :initialValue="backgroundColor" :label="$t('dashboard.widgetEditor.iconTooltips.backgroundColor')" @change="onBackroundColorChanged"></WidgetEditorColorPicker>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../../WidgetEditorSettingsTabDescriptor.json'
import WidgetEditorColorPicker from '../WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'widget-background-color-style',
    components: { WidgetEditorColorPicker },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            descriptor,
            backgroundColor: '' as string,
            widgetType: '' as string,
            getTranslatedLabel
        }
    },
    created() {
        this.loadBackgroundColor()
    },
    methods: {
        loadBackgroundColor() {
            if (!this.widgetModel) return
            this.widgetType = this.widgetModel.type
            if (this.widgetModel.settings?.style) this.backgroundColor = this.widgetModel.settings.style['background-color'] ?? ''
        },
        backgroundColorStyleChanged() {
            emitter.emit('backgroundColorStyleChanged', this.backgroundColor)
            switch (this.widgetType) {
                case 'selector':
                    emitter.emit('refreshSelector', this.widgetModel.id)
            }
        },
        onBackroundColorChanged(event: string | null) {
            if (event && this.widgetModel?.settings?.style) {
                this.widgetModel.settings.style['background-color'] = event
            }
        }
    }
})
</script>
