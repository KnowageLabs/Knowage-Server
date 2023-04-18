<template>
    <div v-if="visualizationTypeConfiguration" class="p-formgrid p-grid p-p-2">
        <div class="p-field p-float-label p-col-12 p-lg-6 p-fluid">
            <Dropdown v-model="visualizationTypeConfiguration.method" class="kn-material-input" :options="descriptor.classesMethodOptions" option-value="value">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.classesMethodOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
            <label for="attributes" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.classesMethodOptions.title') }} </label>
        </div>

        <div v-if="classifyByRanges" class="p-col-12 p-lg-6">
            <Button class="kn-button kn-button--primary" @click="onManageRangesClicked">{{ $t('dashboard.widgetEditor.map.manageRanges') }}</Button>
        </div>
        <div v-if="!classifyByRanges" class="p-float-label p-col-12 p-lg-6 p-fluid p-p-1">
            <InputNumber v-model="visualizationTypeConfiguration.classes" class="kn-material-input p-inputtext-sm" />
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.classesNumber') }}</label>
        </div>

        <div v-if="!classifyByRanges || type === 'balloons'" class="p-col-12 p-p-2" :class="{ 'p-lg-4': type === 'choropleth', 'p-lg-3': type === 'balloons' }">
            <WidgetEditorColorPicker :initial-value="visualizationTypeConfiguration.fromColor" :label="$t('dashboard.widgetEditor.map.fromColor')" @change="onSelectionColorChanged($event, 'fromColor')"></WidgetEditorColorPicker>
        </div>
        <div v-if="!classifyByRanges || type === 'balloons'" class="p-col-12 p-p-2" :class="{ 'p-lg-4': type === 'choropleth', 'p-lg-3': type === 'balloons' }">
            <WidgetEditorColorPicker :initial-value="visualizationTypeConfiguration.toColor" :label="$t('dashboard.widgetEditor.map.toColor')" @change="onSelectionColorChanged($event, 'toColor')"></WidgetEditorColorPicker>
        </div>
        <div v-if="!classifyByRanges || type === 'balloons'" class="p-col-12 p-p-2" :class="{ 'p-lg-4': type === 'choropleth', 'p-lg-3': type === 'balloons' }">
            <WidgetEditorColorPicker :initial-value="visualizationTypeConfiguration.borderColor" :label="$t('dashboard.widgetEditor.iconTooltips.borderColor')" @change="onSelectionColorChanged($event, 'borderColor')"></WidgetEditorColorPicker>
        </div>

        <div v-if="type === 'balloons'" class="p-col-12 p-lg-3 p-p-2">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.rangeLabel') }}</label>
            <Slider v-model="rangeValue" class="p-mt-2" range :min="1" :max="100" @change="onRangeSizeChange" />
        </div>

        <MapVisualizationRangesDialog
            v-if="rangesDialogVisible"
            :visible="rangesDialogVisible"
            :prop-ranges="visualizationTypeConfiguration.properties ? visualizationTypeConfiguration.properties.thresholds : []"
            @setRanges="onSetRanges"
            @close="rangesDialogVisible = false"
        ></MapVisualizationRangesDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IMapWidgetVisualizationTypeChoropleth, IMapWidgetVisualizationTypeBalloons } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../MapVisualizationTypeDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorColorPicker from '../../../common/WidgetEditorColorPicker.vue'
import MapVisualizationRangesDialog from './MapVisualizationRangesDialog.vue'
import deepcopy from 'deepcopy'
import Slider from 'primevue/slider'

export default defineComponent({
    name: 'map-visualization-type-choropleth',
    components: { Dropdown, InputNumber, WidgetEditorColorPicker, MapVisualizationRangesDialog, Slider },
    props: { propVisualizationTypeConfiguration: { type: Object as PropType<IMapWidgetVisualizationTypeChoropleth | null>, required: true }, type: { type: String, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            visualizationTypeConfiguration: null as IMapWidgetVisualizationTypeChoropleth | IMapWidgetVisualizationTypeBalloons | null,
            rangesDialogVisible: false,
            rangeValue: [1, 100] as number[],
            getTranslatedLabel
        }
    },
    computed: {
        classifyByRanges() {
            return this.visualizationTypeConfiguration && this.visualizationTypeConfiguration.method === 'CLASSIFY_BY_RANGES'
        }
    },
    watch: {
        propChoroplethConfiguration() {
            this.loadChoroplethConfiguration()
        }
    },
    created() {
        this.loadChoroplethConfiguration()
    },
    methods: {
        loadChoroplethConfiguration() {
            this.visualizationTypeConfiguration = this.propVisualizationTypeConfiguration
            if (this.type === 'balloons') this.loadRangeValue()
        },
        loadRangeValue() {
            if (!this.visualizationTypeConfiguration) return
            const configuration = this.visualizationTypeConfiguration as IMapWidgetVisualizationTypeBalloons
            this.rangeValue = [configuration.minSize, configuration.maxSize]
        },

        onSelectionColorChanged(event: string | null, property: 'fromColor' | 'toColor' | 'borderColor') {
            if (this.visualizationTypeConfiguration && event) this.visualizationTypeConfiguration[property] = event
        },
        onRangeSizeChange(event: any) {
            if (!this.visualizationTypeConfiguration) return
            const configuration = this.visualizationTypeConfiguration as IMapWidgetVisualizationTypeBalloons
            configuration.minSize = event[0]
            configuration.maxSize = event[1]
        },
        onManageRangesClicked() {
            this.rangesDialogVisible = true
        },
        onSetRanges(ranges: { color: string; from: number; to: number }[]) {
            if (!this.visualizationTypeConfiguration || !this.visualizationTypeConfiguration.properties) return
            this.visualizationTypeConfiguration.properties.thresholds = deepcopy(ranges)
            this.rangesDialogVisible = false
        }
    }
})
</script>
