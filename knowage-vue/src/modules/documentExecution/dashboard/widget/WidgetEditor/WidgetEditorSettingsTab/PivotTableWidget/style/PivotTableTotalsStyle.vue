<template>
    <div v-if="titleStyleModel" class="p-grid p-ai-center kn-flex p-p-4">
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="toolbarStyleSettings" :prop-model="titleStyleModel.properties" :disabled="titleStyleDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel, IWidgetTitle } from '@/modules/documentExecution/Dashboard/Dashboard'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

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
            titleStyleModel: null as IWidgetTitle | null
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
            this.titleStyleModel.properties = {
                'background-color': model['background-color'] ?? 'rgb(137, 158, 175)',
                color: model.color ?? 'rgb(255, 255, 255)',
                'justify-content': model['justify-content'] ?? 'center',
                'font-size': model['font-size'] ?? '14px',
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? 'normal',
                'font-weight': model['font-weight'] ?? ''
            }
        }
    }
})
</script>

<style lang="scss" scoped>
#height-input-container {
    max-width: 200px;
}
</style>
