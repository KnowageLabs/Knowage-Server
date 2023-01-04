<template>
    <div class="p-d-flex p-flex-row p-ai-center">
        <InputSwitch v-if="model" class="p-mr-3" v-model="model.enabled" @click.stop="() => {}"></InputSwitch>
        <label class="kn-material-input-label">{{ title ? $t(title) : '' }}</label>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { ITableWidgetColumnGroups, ITableWidgetHeaders, ITableWidgetSummaryRows, ITableWidgetVisibilityConditions, ITableWidgetVisualizationTypes, IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-settings-accordion-header',
    components: { Checkbox, Dropdown, InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, title: { type: String }, type: { type: String, required: true } },
    data() {
        return {
            model: null as any
        }
    },
    computed: {},
    created() {
        this.model = this.loadModel()
    },
    methods: {
        loadModel() {
            console.log('>>>>>>>>>> TYPE: ', this.type)
            if (!this.widgetModel || !this.widgetModel.settings) return null
            switch (this.type) {
                case 'SummaryRows':
                    return this.widgetModel.settings.configuration.summaryRows
                case 'Header':
                    return this.widgetModel.settings.configuration.headers
                case 'ColumnGroups':
                    return this.widgetModel.settings.configuration.columnGroups
                case 'VisualizationType':
                    return this.widgetModel.settings.visualization.visualizationTypes
                case 'VisibilityConditions':
                    return this.widgetModel.settings.visualization.visibilityConditions
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
                    return this.widgetModel.settings.visualization.visibilityConditions
                case 'ShadowsStyle':
                    return this.widgetModel.settings.visualization.visibilityConditions
                default:
                    return null
            }
        }
    }
})
</script>
