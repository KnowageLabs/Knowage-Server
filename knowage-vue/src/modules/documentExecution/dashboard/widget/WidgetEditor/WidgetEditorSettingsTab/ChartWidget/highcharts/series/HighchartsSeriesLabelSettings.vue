<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ seriesSettings }}
        <div v-for="(serieSetting, index) in seriesSettings" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center">
            <div class="p-col-12 p-md-6 p-d-flex p-flex-column p-p-2">
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.series.title') }}</label>
                <Dropdown v-if="index === 0 && allSeriesOptionEnabled" class="kn-material-input" v-model="serieSetting.names[0]" :options="descriptor.allSerieOption" optionValue="value" optionLabel="label" :disabled="true"> </Dropdown>
                <HighchartsSeriesMultiselect v-else :value="serieSetting.names" :availableSeriesOptions="availableSeriesOptions" :disabled="!allSeriesOptionEnabled" @change="onSeriesSelected($event, serieSetting)"> </HighchartsSeriesMultiselect>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { HighchartsPieChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'
import { IHighchartsChartSerie, IHighchartsSeriesLabelsSetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import Textarea from 'primevue/textarea'
import HighchartsSeriesMultiselect from '../common/HighchartsSeriesMultiselect.vue'

export default defineComponent({
    name: 'hihgcharts-series-label-settings',
    components: { Dropdown, InputSwitch, Textarea, HighchartsSeriesMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as HighchartsPieChartModel | null,
            seriesSettings: [] as IHighchartsSeriesLabelsSetting[],
            availableSeriesOptions: [] as string[]
        }
    },
    computed: {
        modelSerieNames() {
            return this.model ? this.model.series.map((serie: IHighchartsChartSerie) => serie.name) : []
        },
        allSeriesOptionEnabled() {
            //  return this.model?.chart.type !== 'pie'
            return true
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            if (this.widgetModel.settings?.series?.seriesLabelsSettings) this.seriesSettings = this.widgetModel.settings.series.seriesLabelsSettings
            console.log('>>>>>>>>> LOADED WIDHET MODEL SETTINGS: ', this.seriesSettings)
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
        onSeriesSelected(event: any, serieSetting: IHighchartsSeriesLabels) {
            const intersection = serieSetting.names.filter((el: string) => !event.value.includes(el))
            serieSetting.names = event.value
            intersection.length > 0 ? this.onSeriesRemovedFromMultiselect(intersection) : this.onSeriesAddedFromMultiselect(serieSetting)
        },
        onSeriesAddedFromMultiselect(serieSetting: IHighchartsSeriesLabels) {
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
                label: {
                    enabled: false,
                    style: {
                        fontFamily: '',
                        fontSize: '',
                        fontWeight: '',
                        color: '',
                        backgroundColor: ''
                    },
                    format: ''
                } // TODO - move to default serie accebility helper
            })
        },
        removeSerieSetting(index: number) {
            this.seriesSettings[index].names.forEach((serieName: string) => this.availableSeriesOptions.push(serieName))
            this.seriesSettings.splice(index, 1)
        },
        onSerieSettingUpdated(serieSetting: IHighchartsSeriesLabels) {
            this.modelChanged()
        }
    }
})
</script>
