<template>
    <div v-if="chipsStyleModel" class="p-grid p-ai-center kn-flex p-p-4">
        <div id="height-input-container" class="p-col-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.height') }}</label>
            <InputNumber class="kn-material-input p-inputtext-sm" v-model="chipsStyleModel.height" :disabled="chipsStyleDisabled" @blur="chipsStyleChanged" />
        </div>

        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.chipsToolbarStyleOptions" :propModel="chipsStyleModel.properties" :disabled="chipsStyleDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import { ISelectionWidgetChipsStyle } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectionsWidget'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../SelectionsWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'selections-widget-chips-style',
    components: { InputNumber, InputSwitch, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            chipsStyleModel: null as ISelectionWidgetChipsStyle | null
        }
    },
    computed: {
        chipsStyleDisabled() {
            return !this.widgetModel || this.widgetModel.settings.configuration.type !== 'chips'
        }
    },
    created() {
        this.loadChipsStyleModel()
    },
    methods: {
        loadChipsStyleModel() {
            if (this.widgetModel.settings?.style?.chips) this.chipsStyleModel = this.widgetModel.settings.style.chips
        },
        chipsStyleChanged() {
            emitter.emit('chipsStyleChanged', this.chipsStyleModel)
            emitter.emit('refreshSelection', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.chipsStyleModel) return
            this.chipsStyleModel.properties = {
                'background-color': model['background-color'] ?? 'rgb(137, 158, 175)',
                color: model.color ?? 'rgb(255, 255, 255)',
                'justify-content': model['justify-content'] ?? 'center',
                'font-size': model['font-size'] ?? '14px',
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? 'normal',
                'font-weight': model['font-weight'] ?? ''
            }
            this.chipsStyleChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
#height-input-container {
    max-width: 200px;
}
</style>
