<template>
    <div class="p-d-flex p-flex-row p-ai-center">
        <InputSwitch v-if="model" class="p-mr-2" v-model="model.enabled" @click.stop="() => {}"></InputSwitch>
        <label class="kn-material-input-label">{{ title ? $t(title) : '' }}</label>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { ITableWidgetColumnGroups, ITableWidgetHeaders, ITableWidgetSummaryRows, IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-settings-accordion-header',
    components: { Checkbox, Dropdown, InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, title: { type: String }, type: { type: String, required: true } },
    data() {
        return {
            model: null as ITableWidgetSummaryRows | ITableWidgetHeaders | ITableWidgetColumnGroups | null
        }
    },
    computed: {},
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            console.log('>>>>>>>>>> TYPE: ', this.type)
            if (!this.widgetModel || !this.widgetModel.settings) return
            switch (this.type) {
                case 'SummaryRows':
                    this.model = this.widgetModel.settings.configuration.summaryRows
                    break
                case 'Header':
                    this.model = this.widgetModel.settings.configuration.headers
                    break
                case 'ColumnGroups':
                    this.model = this.widgetModel.settings.configuration.columnGroups
                    break
                default:
                    this.model = null
            }
        }
    }
})
</script>
