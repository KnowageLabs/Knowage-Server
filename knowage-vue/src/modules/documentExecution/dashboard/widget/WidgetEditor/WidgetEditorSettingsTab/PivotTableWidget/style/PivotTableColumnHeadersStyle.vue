<template>
    <div v-if="headersStyleModel" class="p-grid p-ai-center kn-flex p-p-4">
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="toolbarStyleSettings" :prop-model="headersStyleModel.properties" :disabled="headersStyleDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import { IPivotTableColumnHeadersStyle } from '@/modules/documentExecution/dashboard/interfaces/pivotTable/DashboardPivotTableWidget'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import * as pivotTableDefaultValues from '../../../helpers/pivotTableWidget/PivotTableDefaultValues'

export default defineComponent({
    name: 'pivot-table-column-headers-style',
    components: { WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        toolbarStyleSettings: { type: Array, required: true },
        type: { type: String, required: true }
    },
    data() {
        return {
            headersStyleModel: null as IPivotTableColumnHeadersStyle | null
        }
    },
    computed: {
        headersStyleDisabled() {
            return !this.headersStyleModel || !this.headersStyleModel.enabled
        }
    },
    created() {
        this.loadHeadersStyleModel()
    },
    methods: {
        loadHeadersStyleModel() {
            if (!this.widgetModel || !this.widgetModel.settings || !this.widgetModel.settings.style) return
            this.headersStyleModel = this.type === 'columns' ? this.widgetModel.settings.style.columnHeaders : this.widgetModel.settings.style.rowHeaders
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.headersStyleModel) return
            const defaultColumnHeadersStyle = this.type === 'columns' ? pivotTableDefaultValues.getDefaultColumnHeadersStyle() : pivotTableDefaultValues.getDefaultRowsHeadersStyle()
            this.headersStyleModel.properties = {
                'background-color': model['background-color'] ?? defaultColumnHeadersStyle.properties['background-color'],
                color: model.color ?? defaultColumnHeadersStyle.properties.color,
                'text-align': model['text-align'] ?? defaultColumnHeadersStyle.properties['text-align'],
                'font-size': model['font-size'] ?? defaultColumnHeadersStyle.properties['font-size'],
                'font-family': model['font-family'] ?? defaultColumnHeadersStyle.properties['font-family'],
                'font-style': model['font-style'] ?? defaultColumnHeadersStyle.properties['font-style'],
                'font-weight': model['font-weight'] ?? defaultColumnHeadersStyle.properties['font-weight']
            }
        }
    }
})
</script>
