<template>
    <div v-if="legendSettings">
        <div class="p-formgrid p-grid p-p-3">
            <span class="p-field p-float-label p-col-12 p-lg-4 p-fluid">
                <Dropdown v-model="legendSettings.visualizationType" class="kn-material-input" :options="descriptor.visualizationTypes" option-value="value" :disabled="legendSettingsDisabled">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.visualizationTypes, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
                <label for="attributes" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.visualizationType.title') }} </label>
            </span>
            <span class="p-field p-float-label p-col-12 p-lg-4 p-fluid">
                <Dropdown v-model="legendSettings.position" class="kn-material-input" :options="descriptor.positionOptions" option-value="value" :disabled="legendSettingsDisabled">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.positionOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
                <label for="attributes" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.visualizationType.alignment') }} </label>
            </span>
            <span class="p-field p-float-label p-col-12 p-lg-4 p-fluid">
                <Dropdown v-model="legendSettings.alignment" class="kn-material-input" :options="descriptor.alignmentOptions" option-value="value" :disabled="legendSettingsDisabled">
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
                <label for="attributes" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.position') }} </label>
            </span>
            <div class="p-float-label p-col-12 p-lg-4">
                <InputText v-model="legendSettings.prefix" class="kn-material-input kn-width-full" :disabled="legendSettingsDisabled" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.prefix') }}</label>
            </div>
            <div class="p-float-label p-col-12 p-lg-4">
                <InputText v-model="legendSettings.suffix" class="kn-material-input kn-width-full" :disabled="legendSettingsDisabled" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.suffix') }}</label>
            </div>
            <div class="p-float-label p-col-12 p-lg-4 p-fluid">
                <InputNumber v-model="legendSettings.precision" class="kn-material-input" :min="0" :disabled="legendSettingsDisabled" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.precision') }}</label>
            </div>
        </div>

        <hr />

        <div class="p-formgrid p-grid p-p-4">
            <div class="p-float-label p-col-12">
                <InputText v-model="legendSettings.title.text" class="kn-material-input kn-width-full" :disabled="legendSettingsDisabled" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.legendTitle') }}</label>
            </div>

            <WidgetEditorStyleToolbar class="p-col-12 p-mx-2 p-my-3" :options="descriptor.toolbarStyleOptions" :prop-model="legendSettings.title.style" :disabled="legendSettingsDisabled" @change="onStyleToolbarChange($event, 'title')"> </WidgetEditorStyleToolbar>
        </div>

        <hr />

        <div class="p-formgrid p-grid p-p-4">
            <div class="p-float-label p-col-12">
                <Textarea v-model="legendSettings.text.text" class="kn-material-input kn-width-full" rows="2" maxlength="250" :auto-resize="true" :disabled="legendSettingsDisabled" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.legendText') }}</label>
            </div>

            <WidgetEditorStyleToolbar class="p-col-12 p-mx-2 p-my-3" :options="descriptor.toolbarStyleOptions" :prop-model="legendSettings.text.style" :disabled="legendSettingsDisabled" @change="onStyleToolbarChange($event, 'text')"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/dashboard/Dashboard'
import { IMapWidgetLegend } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import descriptor from './MapLegendSettingsDescriptor.json'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import Textarea from 'primevue/textarea'
import * as mapWidgetDefaultValues from '../../../helpers/mapWidget/MapWidgetDefaultValues'

export default defineComponent({
    name: 'map-legend-settings',
    components: { Dropdown, InputNumber, WidgetEditorStyleToolbar, Textarea },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            legendSettings: null as IMapWidgetLegend | null,
            getTranslatedLabel
        }
    },
    computed: {
        legendSettingsDisabled() {
            return !this.widgetModel || !this.widgetModel.settings.legend.enabled
        }
    },
    created() {
        this.loadLegendSettings()
    },
    methods: {
        loadLegendSettings() {
            if (this.widgetModel?.settings?.tooltips) this.legendSettings = this.widgetModel.settings.legend
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, property: 'title' | 'text') {
            if (!this.legendSettings || !this.legendSettings[property]) return
            const legendDefaultSettings = mapWidgetDefaultValues.getDefaultLegendSettings()
            this.legendSettings[property].style = {
                'font-family': model['font-family'] ?? legendDefaultSettings[property].style['font-family'],
                'font-style': model['font-style'] ?? legendDefaultSettings[property].style['font-style'],
                'font-size': model['font-size'] ?? legendDefaultSettings[property].style['font-size'],
                'font-weight': model['font-weight'] ?? legendDefaultSettings[property].style['font-weight'],
                'justify-content': model['justify-content'] ?? legendDefaultSettings[property].style['justify-content'],
                color: model.color ?? legendDefaultSettings[property].style.color,
                'background-color': model['background-color'] ?? legendDefaultSettings[property].style['background-color']
            }
        }
    }
})
</script>
