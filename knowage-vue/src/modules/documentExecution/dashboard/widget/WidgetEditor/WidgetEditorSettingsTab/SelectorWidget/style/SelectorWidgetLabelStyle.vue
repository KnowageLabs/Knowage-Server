<template>
    <div v-if="labelStyleModel" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12">
            <InputSwitch v-model="labelStyleModel.enabled" @change="labelStyleChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('common.enable') }}</label>
        </div>
        <div class="p-col-6 p-mt-1">
            <InputSwitch v-model="labelStyleModel.wrapText" :disabled="labelStyleDisabled" @change="labelStyleChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.valuesManagement.wrapText') }}</label>
        </div>
        <div class="p-col-6"></div>
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="toolbarStyleSettings" :propModel="labelStyleModel.properties" :disabled="labelStyleDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import { ISelectorWidgetLabelStyle } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectorWidget'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../SelectorWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'selector-widget-label-style',
    components: { InputSwitch, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, toolbarStyleSettings: { type: Array, required: true } },
    data() {
        return {
            descriptor,
            labelStyleModel: null as ISelectorWidgetLabelStyle | null
        }
    },
    computed: {
        labelStyleDisabled() {
            return !this.labelStyleModel || !this.labelStyleModel.enabled
        }
    },
    created() {
        this.loadLabelStyleModel()
    },
    methods: {
        loadLabelStyleModel() {
            if (this.widgetModel.settings.style?.label) this.labelStyleModel = this.widgetModel.settings.style.label
        },
        labelStyleChanged() {
            emitter.emit('labelStyleChanged', this.labelStyleModel)
            emitter.emit('refreshSelector', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.labelStyleModel) return
            this.labelStyleModel.properties = {
                'background-color': model['background-color'] ?? 'rgb(137, 158, 175)',
                color: model.color ?? 'rgb(255, 255, 255)',
                'justify-content': model['justify-content'] ?? 'center',
                'font-size': model['font-size'] ?? '14px',
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? 'normal',
                'font-weight': model['font-weight'] ?? ''
            }
            this.labelStyleChanged()
        }
    }
})
</script>
