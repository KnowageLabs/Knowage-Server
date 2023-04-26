<template>
    <div v-if="noDataConfiguration" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-if="noDataConfiguration.text" class="p-col-12">
            <label class="kn-material-input-label">{{ $t('common.message') }}</label>
            <Textarea v-model="noDataConfiguration.text" class="kn-material-input kn-width-full" rows="4" :auto-resize="true" maxlength="250" @change="modelChanged" />
        </div>
        <div v-if="noDataConfiguration.position" class="p-col-6 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.align') }}</label>
            <Dropdown v-model="noDataConfiguration.position.align" class="kn-material-input" :options="descriptor.alignmentOptions" option-value="value" @change="modelChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.alignmentOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>
        <div v-if="noDataConfiguration.position" class="p-col-6 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.verticalAlign') }}</label>
            <Dropdown v-model="noDataConfiguration.position.verticalAlign" class="kn-material-input" :options="descriptor.verticalAlignmentOptions" option-value="value" @change="modelChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.verticalAlignmentOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>

        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.noDataToolbarStyleOptions" :prop-model="noDataConfiguration.style" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { IVegaChartsNoDataConfiguration } from '@/modules/documentExecution/dashboard/interfaces/vega/VegaChartsWidget'
import descriptor from '../VegaChartsSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Textarea from 'primevue/textarea'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'vega-no-data-message-configuration',
    components: { Dropdown, Textarea, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            noDataConfiguration: null as IVegaChartsNoDataConfiguration | null,
            getTranslatedLabel
        }
    },
    computed: {},
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.noDataConfiguration = this.widgetModel.settings.configuration ? this.widgetModel.settings.configuration.noDataConfiguration : null
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.noDataConfiguration) return
            this.noDataConfiguration.style = {
                'font-family': model['font-family'] ?? '',
                'font-size': model['font-size'] ?? '14px',
                'font-weight': model['font-weight'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
            this.modelChanged()
        }
    }
})
</script>
