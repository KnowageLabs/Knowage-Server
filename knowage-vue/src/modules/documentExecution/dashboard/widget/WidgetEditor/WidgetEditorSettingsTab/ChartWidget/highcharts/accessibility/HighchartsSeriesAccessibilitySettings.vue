<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-for="(serieSetting, index) in seriesSettings" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center">
            <div class="p-col-12 p-md-6 p-d-flex p-flex-column p-p-2">
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.series.title') }}</label>
                <Dropdown v-if="index === 0 && allSeriesOptionEnabled" v-model="serieSetting.names[0]" class="kn-material-input" :options="descriptor.allSerieOption" option-value="value" option-label="label" :disabled="true"> </Dropdown>
                <HighchartsSeriesMultiselect v-else :value="serieSetting.names" :available-series-options="availableSeriesOptions" :disabled="!allSeriesOptionEnabled" @change="onSeriesSelected($event, serieSetting)"> </HighchartsSeriesMultiselect>
            </div>

            <div class="p-col-5 p-pt-4 p-px-4">
                <InputSwitch v-model="serieSetting.accessibility.enabled" @change="modelChanged"></InputSwitch>
                <label class="kn-material-input-label p-m-3">{{ $t('common.enabled') }}</label>
            </div>
            <div v-if="allSeriesOptionEnabled" class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash']" class="kn-cursor-pointer p-ml-2 p-mt-4" @click="index === 0 ? addSerieSetting() : removeSerieSetting(index)"></i>
            </div>

            <div class="p-col-12">
                <label class="kn-material-input-label">{{ $t('common.description') }}</label>
                <Textarea v-model="serieSetting.accessibility.description" class="kn-material-input kn-width-full" rows="4" :auto-resize="true" maxlength="250" :disabled="!serieSetting.accessibility.enabled" @change="modelChanged" />
            </div>
            <div class="p-col-6 p-pt-2 p-px-4">
                <InputSwitch v-model="serieSetting.accessibility.exposeAsGroupOnly" :disabled="!serieSetting.accessibility.enabled" @change="modelChanged"></InputSwitch>
                <label class="kn-material-input-label p-m-3">{{ $t('dashboard.widgetEditor.accessibility.exposeAsGroupOnly') }}</label>
                <i v-tooltip.top="$t('dashboard.widgetEditor.accessibility.exposeAsGroupOnlyHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
            <div class="p-col-6 p-pt-2 p-px-4">
                <InputSwitch v-model="serieSetting.accessibility.keyboardNavigation.enabled" :disabled="!serieSetting.accessibility.enabled" @change="modelChanged"></InputSwitch>
                <label class="kn-material-input-label p-m-3">{{ $t('dashboard.widgetEditor.accessibility.enabelKeyboardNavigation') }}</label>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IHighchartsChartModel, IHighchartsChartSerie, ISerieAccessibilitySetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import Textarea from 'primevue/textarea'
import HighchartsSeriesMultiselect from '../common/HighchartsSeriesMultiselect.vue'
import * as highchartsDefaultValues from '../../../../helpers/chartWidget/highcharts/HighchartsDefaultValues'

export default defineComponent({
    name: 'hihgcharts-series-accessibility-settings',
    components: { Dropdown, InputSwitch, Textarea, HighchartsSeriesMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IHighchartsChartModel | null,
            seriesSettings: [] as ISerieAccessibilitySetting[],
            availableSeriesOptions: [] as string[]
        }
    },
    computed: {
        allSeriesOptionEnabled() {
            return this.model && this.model.chart.type !== 'pie' && this.model.chart.type !== 'solidgauge'
        }
    },
    created() {
        this.setEventListeners()
        this.loadModel()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('seriesAdded', this.loadModel)
            emitter.on('seriesRemoved', this.loadModel)
            emitter.on('chartTypeChanged', this.onChartTypeChanged)
        },
        removeEventListeners() {
            emitter.off('seriesAdded', this.loadModel)
            emitter.off('seriesRemoved', this.loadModel)
            emitter.off('chartTypeChanged', this.onChartTypeChanged)
        },
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            if (this.widgetModel.settings?.accesssibility?.seriesAccesibilitySettings) this.seriesSettings = this.widgetModel.settings.accesssibility.seriesAccesibilitySettings
            this.loadSeriesOptions()
            this.removeSeriesFromAvailableOptions()
            this.removeAllSerieSettingsFromModel()
            if (this.seriesSettings.length === 0) this.addFirstSeriesSetting()
        },
        removeAllSerieSettingsFromModel() {
            if (this.seriesSettings[0]?.names[0] && this.seriesSettings[0].names[0] === 'all' && !this.allSeriesOptionEnabled) {
                this.seriesSettings.splice(0, 1)
                this.widgetModel.settings.accesssibility.seriesAccesibilitySettings.splice(0, 1)
            }
        },
        removeSeriesFromAvailableOptions() {
            for (let i = 1; i < this.widgetModel.settings.accesssibility.seriesAccesibilitySettings.length; i++) {
                for (let j = 0; j < this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[i].names.length; j++) {
                    this.removeSerieFromAvailableOptions(this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[i].names[j])
                }
            }
        },
        removeSerieFromAvailableOptions(seriesName: string) {
            const index = this.availableSeriesOptions.findIndex((tempSerieName: string) => tempSerieName === seriesName)
            if (index !== -1) this.availableSeriesOptions.splice(index, 1)
        },
        loadSeriesOptions() {
            this.availableSeriesOptions = []
            if (!this.model) return
            this.model.series.forEach((serie: IHighchartsChartSerie) => {
                this.availableSeriesOptions.push(serie.name)
            })
        },
        addFirstSeriesSetting() {
            if (!this.model) return
            this.seriesSettings = []
            if (this.availableSeriesOptions.length >= 1) {
                const name = this.allSeriesOptionEnabled ? 'all' : this.availableSeriesOptions[0]
                const formattedSeriesSettings = {
                    names: [name],
                    accessibility: highchartsDefaultValues.getDefaultSerieAccessibilitySetting()
                } as ISerieAccessibilitySetting

                this.seriesSettings.push(formattedSeriesSettings)
                this.widgetModel.settings.accesssibility.seriesAccesibilitySettings.push(formattedSeriesSettings)
            }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onSeriesSelected(event: any, serieSetting: ISerieAccessibilitySetting) {
            const intersection = serieSetting.names.filter((el: string) => !event.value.includes(el))
            serieSetting.names = event.value
            intersection.length > 0 ? this.onSeriesRemovedFromMultiselect(intersection) : this.onSeriesAddedFromMultiselect(serieSetting)
            this.modelChanged()
        },
        onSeriesAddedFromMultiselect(serieSetting: ISerieAccessibilitySetting) {
            serieSetting.names.forEach((serieName: string) => {
                const index = this.availableSeriesOptions.findIndex((tempSerieName: string) => tempSerieName === serieName)
                if (index !== -1) this.availableSeriesOptions.splice(index, 1)
            })
        },
        onSeriesRemovedFromMultiselect(intersection: string[]) {
            intersection.forEach((serieName: string) => this.availableSeriesOptions.push(serieName))
        },
        addSerieSetting() {
            const serieSetting = { names: [], accessibility: highchartsDefaultValues.getDefaultSeriesAccessibilitySettings() }
            this.widgetModel.settings.accesssibility.seriesAccesibilitySettings.push(serieSetting)
            this.seriesSettings.push(serieSetting)
        },
        removeSerieSetting(index: number) {
            this.seriesSettings[index].names.forEach((serieName: string) => this.availableSeriesOptions.push(serieName))
            this.widgetModel.settings.accesssibility.seriesAccesibilitySettings.splice(index, 1)
            this.seriesSettings.splice(index, 1)
            this.modelChanged()
        },
        onChartTypeChanged() {
            this.widgetModel.settings.accesssibility.seriesAccesibilitySettings = []
            this.loadModel()
        }
    }
})
</script>
