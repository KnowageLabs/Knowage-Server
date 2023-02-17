<template>
    <div class="p-d-flex p-flex-row p-ai-center">
        <InputSwitch v-if="model" v-model="model[property]" class="p-mr-3" @click.stop="onModelChange"></InputSwitch>
        <label class="kn-material-input-label">{{ title ? $t(title) : '' }}</label>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'highcharts-widget-settings-accordion-header',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, title: { type: String }, type: { type: String, required: true } },
    data() {
        return {
            model: null as any,
            property: 'enabled' as string
        }
    },
    computed: {},
    watch: {
        type() {
            this.model = this.loadModel()
        }
    },
    created() {
        this.model = this.loadModel()
    },
    methods: {
        loadModel() {
            if (!this.widgetModel || !this.widgetModel.settings || !this.widgetModel.settings.chartModel) return null
            switch (this.type) {
                case 'ConfigurationOf3D':
                    return this.widgetModel.settings.chartModel.model?.chart?.options3d
                case 'AccessibilitySettings':
                    return this.widgetModel.settings.chartModel.model?.accessibility
                case 'Legend':
                    return this.widgetModel.settings.chartModel.model?.legend
                case 'Tooltip':
                case 'ActivityGaugeTooltip':
                    return this.widgetModel.settings.chartModel.model?.tooltip
                case 'Title':
                    return this.widgetModel.settings.style.title
                case 'ColumnStyle':
                    return this.widgetModel.settings.style.columns
                case 'ColumnGroupsStyle':
                    return this.widgetModel.settings.style.columnGroups
                case 'BackgroundColorStyle':
                    return this.widgetModel.settings.style.background
                case 'BordersStyle':
                    return this.widgetModel.settings.style.borders
                case 'PaddingStyle':
                    return this.widgetModel.settings.style.padding
                case 'ShadowsStyle':
                    return this.widgetModel.settings.style.shadows
                case 'Drilldown':
                    return this.widgetModel.settings.interactions.drilldown
                case 'Selection':
                    return this.widgetModel.settings.interactions.selection
                case 'CrossNavigation':
                    return this.widgetModel.settings.interactions.crossNavigation
                case 'Link':
                    return this.widgetModel.settings.interactions.link
                case 'Preview':
                    return this.widgetModel.settings.interactions.preview
                default:
                    return null
            }
        },
        onModelChange() {
            switch (this.type) {
                case 'ConfigurationOf3D':
                case 'AccessibilitySettings':
                case 'Legend':
                case 'Tooltip':
                case 'ActivityGaugeTooltip':
                    setTimeout(() => emitter.emit('refreshChart', this.widgetModel.id), 250)
            }
        }
    }
})
</script>
