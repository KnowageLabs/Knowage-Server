<template>
    <div v-if="shadowsStyleModel" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12 p-d-flex p-flex-row p-ai-center p-mb-2">
            <InputSwitch v-model="shadowsStyleModel.enabled" @change="shadowStyleChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-2">{{ $t('dashboard.widgetEditor.shadows.enableShadows') }}</label>
        </div>

        <div class="p-col-12 p-grid p-ai-center p-p-0">
            <div class="p-col-12 p-md-6 p-d-flex p-flex-column p-pb-3">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.shadows.shadowSize') }}</label>
                <Dropdown class="kn-material-input" v-model="shadowSize" :options="descriptor.shadowsSizeOptions" optionValue="value" :disabled="shadowsStyleDisabled" @change="onShadowsSizeChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.shadowsSizeOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>

            <div class="p-col-12 p-md-6 p-px-2 p-pt-3">
                <WidgetEditorColorPicker :initialValue="shadowsStyleModel.properties.color" :label="$t('dashboard.widgetEditor.iconTooltips.backgroundColor')" :disabled="shadowsStyleDisabled" @change="onBackroundColorChanged"></WidgetEditorColorPicker>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetShadowsStyle } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../../WidgetEditorSettingsTabDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorColorPicker from '../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'widget-shadows-style',
    components: { Dropdown, InputSwitch, WidgetEditorColorPicker },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            descriptor,
            shadowsStyleModel: null as IWidgetShadowsStyle | null,
            shadowSize: '',
            shadowSizeOptionsMap: { small: '0px 1px 1px', medium: '0px 2px 3px', large: '0px 8px 19px', extraLarge: '0px 8px 19px' },
            widgetType: '' as string,
            getTranslatedLabel
        }
    },
    computed: {
        shadowsStyleDisabled() {
            return !this.shadowsStyleModel || !this.shadowsStyleModel.enabled
        }
    },
    created() {
        this.loadShadowsStyle()
    },
    methods: {
        loadShadowsStyle() {
            if (!this.widgetModel) return
            this.widgetType = this.widgetModel.type
            if (this.widgetModel.settings?.style?.shadows) this.shadowsStyleModel = this.widgetModel.settings.style.shadows
            this.getShadowSize()
        },
        shadowStyleChanged() {
            emitter.emit('shadowStyleChanged', this.shadowsStyleModel)
            switch (this.widgetType) {
                case 'table':
                    emitter.emit('refreshTable', this.widgetModel.id)
                    break
                case 'selector':
                    emitter.emit('refreshSelector', this.widgetModel.id)
                    break
                case 'selector':
                    emitter.emit('refreshSelection', this.widgetModel.id)
            }
        },
        getShadowSize() {
            if (!this.shadowsStyleModel) return
            switch (this.shadowsStyleModel.properties['box-shadow']) {
                case '0px 1px 1px':
                    this.shadowSize = 'small'
                    break
                case '0px 2px 3px':
                    this.shadowSize = 'medium'
                    break
                case '0px 4px 5px':
                    this.shadowSize = 'large'
                    break
                case '0px 8px 19px':
                    this.shadowSize = 'extraLarge'
                    break
                default:
                    this.shadowSize = 'medium'
            }
        },
        onShadowsSizeChanged() {
            if (!this.shadowsStyleModel) return
            this.shadowsStyleModel.properties['box-shadow'] = this.shadowSizeOptionsMap[this.shadowSize]
            this.shadowStyleChanged()
        },
        onBackroundColorChanged(event: string | null) {
            if (!event || !this.shadowsStyleModel) return
            this.shadowsStyleModel.properties.color = event
            this.shadowStyleChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
#padding-left-container {
    max-width: 300px;
}
</style>
