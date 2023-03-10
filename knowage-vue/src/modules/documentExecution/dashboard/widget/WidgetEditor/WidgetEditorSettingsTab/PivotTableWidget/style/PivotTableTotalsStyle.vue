<template>
    <div v-if="titleStyleModel" class="p-grid p-ai-center kn-flex p-p-4">
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="toolbarStyleSettings" :prop-model="titleStyleModel.properties" :disabled="titleStyleDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import * as pivotTableDefaultValues from '../../../helpers/pivotTableWidget/PivotTableDefaultValues'
import { IPivotTotal } from '@/modules/documentExecution/dashboard/interfaces/pivotTable/DashboardPivotTableWidget'

export default defineComponent({
    name: 'widget-title-style',
    components: { WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        toolbarStyleSettings: { type: Array, required: true },
        totalType: { type: String, required: true }
    },
    data() {
        return {
            titleStyleModel: null as IPivotTotal | null
        }
    },
    computed: {
        titleStyleDisabled() {
            return !this.titleStyleModel || !this.titleStyleModel.enabled
        }
    },
    created() {
        this.loadStyle()
    },
    methods: {
        loadStyle() {
            if (!this.widgetModel) return
            if (this.totalType == 'Totals' && this.widgetModel.settings?.style?.totals) this.titleStyleModel = this.widgetModel.settings.style.totals
            else if (this.totalType == 'SubTotals' && this.widgetModel.settings?.style?.totals) this.titleStyleModel = this.widgetModel.settings.style.subTotals
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.titleStyleModel) return
            const defaultTotalsStyle = pivotTableDefaultValues.getDefaultTotals()
            this.titleStyleModel.properties = {
                'background-color': model['background-color'] ?? defaultTotalsStyle.properties['background-color'],
                color: model.color ?? defaultTotalsStyle.properties.color,
                'text-align': model['text-align'] ?? defaultTotalsStyle.properties['text-align'],
                'font-size': model['font-size'] ?? defaultTotalsStyle.properties['font-size'],
                'font-family': model['font-family'] ?? defaultTotalsStyle.properties['font-family'],
                'font-style': model['font-style'] ?? defaultTotalsStyle.properties['font-style'],
                'font-weight': model['font-weight'] ?? defaultTotalsStyle.properties['font-weight']
            }
        }
    }
})
</script>
