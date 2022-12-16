<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-for="(serieSetting, index) in seriesSettings" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center">
            <div class="p-col-12 p-md-6 p-d-flex p-flex-column p-p-2">
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.series.title') }}</label>
                <Dropdown v-if="index === 0 && allSeriesOptionEnabled" class="kn-material-input" v-model="serieSetting.names[0]" :options="descriptor.allSerieOption" optionValue="value" optionLabel="label" :disabled="true"> </Dropdown>
                <HighchartsSeriesMultiselect v-else :value="serieSetting.names" :availableSeriesOptions="availableSeriesOptions" :disabled="!allSeriesOptionEnabled" @change="onSeriesSelected($event, serieSetting)"> </HighchartsSeriesMultiselect>
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
                <Textarea class="kn-material-input kn-width-full" rows="4" :autoResize="true" v-model="serieSetting.accessibility.description" maxlength="250" :disabled="!serieSetting.accessibility.enabled" @change="onSerieSettingUpdated(serieSetting)" />
            </div>
            <div class="p-col-6 p-pt-2 p-px-4">
                <InputSwitch v-model="serieSetting.accessibility.exposeAsGroupOnly" :disabled="!serieSetting.accessibility.enabled" @change="modelChanged"></InputSwitch>
                <label class="kn-material-input-label p-m-3">{{ $t('dashboard.widgetEditor.accessibility.exposeAsGroupOnly') }}</label>
                <i class="pi pi-question-circle kn-cursor-pointer  p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.accessibility.exposeAsGroupOnlyHint')"></i>
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
import { HighchartsPieChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'
import { IHighchartsChartSerie, ISerieAccessibilitySetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import Textarea from 'primevue/textarea'
import HighchartsSeriesMultiselect from '../common/HighchartsSeriesMultiselect.vue'

export default defineComponent({
    name: 'hihgcharts-series-accessibility-settings',
    components: { Dropdown, InputSwitch, Textarea, HighchartsSeriesMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as HighchartsPieChartModel | null,
            seriesSettings: [] as ISerieAccessibilitySetting[],
            availableSeriesOptions: [] as string[]
        }
    },
    computed: {
        allSeriesOptionEnabled() {
            return this.model?.chart.type !== 'pie'
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
        },
        removeEventListeners() {
            emitter.off('seriesAdded', this.loadModel)
            emitter.off('seriesRemoved', this.loadModel)
        },
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            if (this.widgetModel.settings?.accesssibility?.seriesAccesibilitySettings) this.seriesSettings = this.widgetModel.settings.accesssibility.seriesAccesibilitySettings
            this.loadSeriesOptions()
        },
        loadSeriesOptions() {
            this.availableSeriesOptions = []
            if (!this.model) return
            this.model.series.forEach((serie: IHighchartsChartSerie) => {
                this.availableSeriesOptions.push(serie.name)
            })
            if (!this.allSeriesOptionEnabled && this.availableSeriesOptions.length === 1 && this.seriesSettings.length === 0) {
                this.widgetModel.settings.accesssibility.seriesAccesibilitySettings.push({
                    names: [this.availableSeriesOptions[0]],
                    accessibility: {
                        enabled: false,
                        description: '',
                        exposeAsGroupOnly: false,
                        keyboardNavigation: { enabled: false }
                    }
                })
                this.availableSeriesOptions = []
            }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onSeriesSelected(event: any, serieSetting: ISerieAccessibilitySetting) {
            const intersection = serieSetting.names.filter((el: string) => !event.value.includes(el))
            serieSetting.names = event.value
            intersection.length > 0 ? this.onSeriesRemovedFromMultiselect(intersection) : this.onSeriesAddedFromMultiselect(serieSetting)
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
            this.seriesSettings.push({
                names: [],
                accessibility: {
                    enabled: true,
                    description: '',
                    exposeAsGroupOnly: false,
                    keyboardNavigation: { enabled: false }
                } // TODO - move to default serie accebility helper
            })
        },
        removeSerieSetting(index: number) {
            this.seriesSettings[index].names.forEach((serieName: string) => this.availableSeriesOptions.push(serieName))
            this.seriesSettings.splice(index, 1)
        },
        onSerieSettingUpdated(serieSetting: ISerieAccessibilitySetting) {
            this.modelChanged()
        }
    }
})
</script>
