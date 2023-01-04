<template>
    <div v-if="titleStyleModel" class="p-grid p-ai-center kn-flex p-p-4">
        <div class="p-col-8 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.text') }}</label>
            <InputText class="kn-material-input p-inputtext-sm kn-flex" v-model="(titleStyleModel as IWidgetTitle).text" :disabled="titleStyleDisabled" @change="titleStyleChanged" />
        </div>
        <div id="height-input-container" class="p-col-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.height') }}</label>
            <InputNumber class="kn-material-input p-inputtext-sm" v-model="titleStyleModel.height" :disabled="titleStyleDisabled" @blur="titleStyleChanged" />
        </div>

        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="toolbarStyleSettings" :propModel="titleStyleModel.properties" :disabled="titleStyleDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel, IWidgetTitle } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'widget-title-style',
    components: { InputNumber, InputSwitch, WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        toolbarStyleSettings: { type: Array, required: true }
    },
    data() {
        return {
            titleStyleModel: null as IWidgetTitle | null,
            widgetType: '' as string
        }
    },
    computed: {
        titleStyleDisabled() {
            return !this.titleStyleModel || !this.titleStyleModel.enabled
        }
    },
    created() {
        this.loadTitleStyleModel()
    },
    methods: {
        loadTitleStyleModel() {
            if (!this.widgetModel) return
            this.widgetType = this.widgetModel.type
            if (this.widgetModel.settings?.style?.title) this.titleStyleModel = this.widgetModel.settings.style.title
        },
        titleStyleChanged() {
            emitter.emit('titleStyleChanged', this.titleStyleModel)
            switch (this.widgetType) {
                case 'table':
                    emitter.emit('refreshTable', this.widgetModel.id)
                    break
                case 'selector':
                    emitter.emit('refreshSelector', this.widgetModel.id)
                    break
                case 'selection':
                    emitter.emit('refreshSelection', this.widgetModel.id)
            }
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
            this.titleStyleChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
#height-input-container {
    max-width: 200px;
}
</style>
