<template>
    <div v-if="labelStyleModel" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12">
            {{ labelStyleModel }}
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { ISelectorWidgetLabelStyle } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectorWidget'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../SelectorWidgetSettingsDescriptor.json'

export default defineComponent({
    name: 'selector-widget-label-style',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            labelStyleModel: null as ISelectorWidgetLabelStyle | null
        }
    },
    created() {
        this.loadLabelStyleModel()
    },
    methods: {
        loadLabelStyleModel() {
            if (this.widgetModel.settings?.style?.label) this.labelStyleModel = this.widgetModel.settings.configuration.label
        },
        labelStyleChanged() {
            emitter.emit('labelStyleChanged', this.labelStyleModel)
            emitter.emit('refreshSelector', this.widgetModel.id)
        }
    }
})
</script>
