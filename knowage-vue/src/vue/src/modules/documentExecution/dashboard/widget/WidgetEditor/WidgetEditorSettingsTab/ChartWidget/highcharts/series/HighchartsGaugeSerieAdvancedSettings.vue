<template>
    <div v-if="serieSettings && serieSettings.pivot && serieSettings.dial" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.dial.dialRadius') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber v-model="dialRadius" class="kn-material-input p-inputtext-sm" :disabled="disabled" @blur="onRadiusChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.dial.dialRadiusHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>

        <div class="p-col-12 p-md-6 p-lg-4 p-px-2 p-pt-4">
            <WidgetEditorColorPicker :initial-value="serieSettings.dial.backgroundColor" :label="$t('dashboard.widgetEditor.highcharts.dial.dialColor')" :disabled="disabled" @change="onSelectionColorChanged($event, 'dial')"></WidgetEditorColorPicker>
        </div>

        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column p-fluid">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.dial.dialBaseWitdh') }}</label>
            <InputNumber v-model="serieSettings.dial.baseWidth" class="kn-material-input p-inputtext-sm" :disabled="disabled" @blur="modelChanged" />
        </div>

        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.pivot.pivotRadius') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber v-model="serieSettings.pivot.radius" class="kn-material-input p-inputtext-sm" :disabled="disabled" @blur="modelChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.pivot.pivotRadiusHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>

        <div class="p-col-12 p-md-6 p-lg-6 p-px-2 p-pt-4">
            <WidgetEditorColorPicker :initial-value="serieSettings.pivot.backgroundColor" :label="$t('dashboard.widgetEditor.highcharts.pivot.pivotColor')" :disabled="disabled" @change="onSelectionColorChanged($event, 'pivot')"></WidgetEditorColorPicker>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IHighchartsSeriesLabelsSetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorColorPicker from '../../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'hihgcharts-gauge-serie-advanced-settings',
    components: { InputNumber, WidgetEditorColorPicker },
    props: { serieSettingsProp: { type: Object as PropType<IHighchartsSeriesLabelsSetting>, required: true }, disabled: { type: Boolean, required: true } },
    emits: ['modelChanged'],
    data() {
        return {
            serieSettings: null as IHighchartsSeriesLabelsSetting | null,
            dialRadius: 0
        }
    },
    watch: {
        seriesSettingsProp() {
            this.loadSerieSettings()
        }
    },
    created() {
        this.loadSerieSettings()
    },

    methods: {
        loadSerieSettings() {
            this.serieSettings = this.serieSettingsProp
            this.loadDialRadius()
        },
        loadDialRadius() {
            if (!this.serieSettings) return
            this.dialRadius = this.serieSettings.dial?.radius ? +this.serieSettings.dial.radius.trim().replace('%', '') : 0
        },
        modelChanged() {
            setTimeout(() => this.$emit('modelChanged'), 250)
        },
        onRadiusChanged() {
            setTimeout(() => {
                if (!this.serieSettings || !this.serieSettings.dial) return
                this.serieSettings.dial.radius = this.dialRadius + '%'
                this.modelChanged()
            }, 250)
        },
        onSelectionColorChanged(event: string | null, type: 'dial' | 'pivot') {
            if (!event || !this.serieSettings) return
            const radiusTypeSettings = this.serieSettings[type]
            if (radiusTypeSettings) {
                radiusTypeSettings.backgroundColor = event
                this.modelChanged()
            }
        }
    }
})
</script>
