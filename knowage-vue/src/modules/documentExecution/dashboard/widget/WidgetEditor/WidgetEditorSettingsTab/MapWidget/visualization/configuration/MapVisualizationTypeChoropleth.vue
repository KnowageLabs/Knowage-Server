<template>
    <div v-if="choroplethConfiguration" class="p-formgrid p-grid p-p-2">
        <div class="p-field p-float-label p-col-12 p-lg-6 p-fluid">
            <Dropdown v-model="choroplethConfiguration.method" class="kn-material-input" :options="descriptor.classesMethodOptions" option-value="value">
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

        <div v-if="classifyByRanges" class="p-ml-4">
            <Button class="kn-button kn-button--primary" @click="onManageRangesClicked">{{ $t('dashboard.widgetEditor.map.manageRanges') }}</Button>
        </div>
        <div v-if="!classifyByRanges" class="p-float-label p-col-12 p-lg-6 p-fluid p-p-2">
            <InputNumber v-model="choroplethConfiguration.classes" class="kn-material-input p-inputtext-sm" />
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.classesNumber') }}</label>
        </div>

        <div v-if="!classifyByRanges" class="p-col-12 p-lg-4 p-p-2">
            <WidgetEditorColorPicker :initial-value="choroplethConfiguration.fromColor" :label="$t('dashboard.widgetEditor.map.fromColor')" @change="onSelectionColorChanged($event, 'fromColor')"></WidgetEditorColorPicker>
        </div>
        <div v-if="!classifyByRanges" class="p-col-12 p-lg-4 p-p-2">
            <WidgetEditorColorPicker :initial-value="choroplethConfiguration.toColor" :label="$t('dashboard.widgetEditor.map.toColor')" @change="onSelectionColorChanged($event, 'toColor')"></WidgetEditorColorPicker>
        </div>
        <div v-if="!classifyByRanges" class="p-col-12 p-lg-4 p-p-2">
            <WidgetEditorColorPicker :initial-value="choroplethConfiguration.borderColor" :label="$t('dashboard.widgetEditor.iconTooltips.borderColor')" @change="onSelectionColorChanged($event, 'borderColor')"></WidgetEditorColorPicker>
        </div>

        <MapVisualizationRangesDialog v-if="rangesDialogVisible" :visible="rangesDialogVisible" :prop-ranges="choroplethConfiguration.properties ? choroplethConfiguration.properties.thresholds : []" @setRanges="onSetRanges" @close="rangesDialogVisible = false"></MapVisualizationRangesDialog>
    </div>
</template>

<script lang="ts">
import { IMapWidgetVisualizationTypeChoropleth } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'
import descriptor from '../MapVisualizationTypeDescriptor.json'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorColorPicker from '../../../common/WidgetEditorColorPicker.vue'
import MapVisualizationRangesDialog from './MapVisualizationRangesDialog.vue'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'map-visualization-type-choropleth',
    components: { Dropdown, InputNumber, WidgetEditorColorPicker, MapVisualizationRangesDialog },
    props: { propChoroplethConfiguration: { type: Object as PropType<IMapWidgetVisualizationTypeChoropleth | null>, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            choroplethConfiguration: null as IMapWidgetVisualizationTypeChoropleth | null,
            rangesDialogVisible: false,
            getTranslatedLabel
        }
    },
    computed: {
        classifyByRanges() {
            return this.choroplethConfiguration && this.choroplethConfiguration.method === 'CLASSIFY_BY_RANGES'
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
            this.choroplethConfiguration = this.propChoroplethConfiguration
        },
        onSelectionColorChanged(event: string | null, property: 'fromColor' | 'toColor' | 'borderColor') {
            if (this.choroplethConfiguration && event) this.choroplethConfiguration[property] = event
        },
        onManageRangesClicked() {
            this.rangesDialogVisible = true
        },
        onSetRanges(ranges: { color: string; from: number; to: number }[]) {
            if (!this.choroplethConfiguration || !this.choroplethConfiguration.properties) return
            this.choroplethConfiguration.properties.thresholds = deepcopy(ranges)
            this.rangesDialogVisible = false
        }
    }
})
</script>
