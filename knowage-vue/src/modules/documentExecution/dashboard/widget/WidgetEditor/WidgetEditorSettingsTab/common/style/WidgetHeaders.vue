<template>
    <div v-if="headersStyleModel" class="p-grid p-ai-center kn-flex p-p-4">
        <div v-if="widgetType !== 'table'" class="p-col-6 p-sm-12 p-md-6">
            <InputSwitch v-model="(headersStyleModel as IWidgetTitle).enabled" @change="headersStyleChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.titles.enableTitle') }}</label>
        </div>
        <div v-if="widgetType !== 'table'" class="p-col-8 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.text') }}</label>
            <InputText class="kn-material-input p-inputtext-sm kn-flex" v-model="(headersStyleModel as IWidgetTitle).text" :disabled="headersDisabled" @change="headersStyleChanged" />
        </div>
        <div id="height-input-container" class="p-col-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.height') }}</label>
            <InputNumber class="kn-material-input p-inputtext-sm" v-model="headersStyleModel.height" :disabled="headersDisabled" @blur="headersStyleChanged" />
        </div>

        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="toolbarStyleSettings" :propModel="headersStyleModel.properties" :disabled="headersDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITawbleWidgetHeadersStyle, IWidgetStyleToolbarModel, IWidgetTitle } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-headers',
    components: { InputNumber, InputSwitch, WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        toolbarStyleSettings: { type: Array, required: true }
    },
    data() {
        return {
            headersStyleModel: null as ITawbleWidgetHeadersStyle | IWidgetTitle | null,
            widgetType: '' as string
        }
    },
    computed: {
        headersDisabled() {
            return !this.widgetModel || (this.widgetType !== 'table' && !(this.headersStyleModel as IWidgetTitle).enabled)
        }
    },
    created() {
        this.loadHeaderModel()
    },
    methods: {
        loadHeaderModel() {
            if (!this.widgetModel) return
            this.widgetType = this.widgetModel.type
            switch (this.widgetType) {
                case 'table':
                    if (this.widgetModel.settings?.style?.headers) this.headersStyleModel = this.widgetModel.settings.style.headers
                    break
                case 'selector':
                    if (this.widgetModel.settings?.style?.title) this.headersStyleModel = this.widgetModel.settings.style.title
            }
        },
        headersStyleChanged() {
            setTimeout(() => {
                switch (this.widgetType) {
                    case 'table':
                        emitter.emit('headersStyleChanged', this.headersStyleModel)
                        emitter.emit('refreshTable', this.widgetModel.id)
                        break
                    case 'selector':
                        emitter.emit('titleStyleChanged', this.headersStyleModel)
                        emitter.emit('refreshSelector', this.widgetModel.id)
                        break
                }
            }, 0)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.headersStyleModel) return
            this.headersStyleModel.properties = {
                'background-color': model['background-color'] ?? 'rgb(137, 158, 175)',
                color: model.color ?? 'rgb(255, 255, 255)',
                'justify-content': model['justify-content'] ?? 'center',
                'font-size': model['font-size'] ?? '14px',
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? 'normal',
                'font-weight': model['font-weight'] ?? ''
            }
            this.headersStyleChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
#height-input-container {
    max-width: 200px;
}
</style>
