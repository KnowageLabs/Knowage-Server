<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-if="model.lang" class="p-col-12">
            <label class="kn-material-input-label">{{ $t('common.message') }}</label>
            <Textarea class="kn-material-input kn-width-full" rows="4" :autoResize="true" v-model="model.lang.noData" maxlength="250" @change="modelChanged" />
        </div>
        <div v-if="model.noData?.position" class="p-col-6 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.align') }}</label>
            <Dropdown class="kn-material-input" v-model="model.noData.position.align" :options="descriptor.alignmentOptions" optionValue="value" @change="modelChanged">
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
        <div v-if="model.noData?.position" class="p-col-6 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.verticalAlign') }}</label>
            <Dropdown class="kn-material-input" v-model="model.noData.position.verticalAlign" :options="descriptor.verticalAlignmentOptions" optionValue="value" @change="modelChanged">
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
            <WidgetEditorStyleToolbar :options="descriptor.noDataToolbarStyleOptions" :propModel="toolbarModel" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '../../../../../../Dashboard'
import { HighchartsPieChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Textarea from 'primevue/textarea'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'hihgcharts-no-data-message-configuration',
    components: { Dropdown, Textarea, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as HighchartsPieChartModel | null,
            toolbarModel: {} as { 'font-family': string; 'font-size': string; 'font-weight': string; color: string; 'background-color': string },
            getTranslatedLabel
        }
    },
    computed: {},
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            if (this.model && this.model.noData)
                this.toolbarModel = {
                    'font-family': this.model.noData.style.fontFamily,
                    'font-size': this.model.noData.style.fontSize,
                    'font-weight': this.model.noData.style.fontWeight,
                    color: this.model.noData.style.color,
                    'background-color': this.model.noData.style.backgroundColor
                }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.model || !this.model.noData.style) return
            this.toolbarModel = {
                'font-family': model['font-family'] ?? '',
                'font-size': model['font-size'] ?? '14px',
                'font-weight': model['font-weight'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
            this.model.noData.style = {
                backgroundColor: this.toolbarModel['background-color'] ?? '',
                color: this.toolbarModel.color ?? '',
                fontSize: this.toolbarModel['font-size'] ?? '14px',
                fontFamily: this.toolbarModel['font-family'] ?? '',
                fontWeight: this.toolbarModel['font-weight'] ?? ''
            }

            this.modelChanged()
        }
    }
})
</script>
