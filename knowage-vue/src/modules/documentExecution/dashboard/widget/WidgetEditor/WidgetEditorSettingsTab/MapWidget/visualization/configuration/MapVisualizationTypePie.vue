<template>
    <div v-if="pieConfiguration" class="p-formgrid p-grid p-p-2">
        <div class="p-col-12">
            {{ pieConfiguration }}
        </div>

        <div class="p-field p-float-label p-col-12 p-lg-6 p-fluid">
            <Dropdown v-model="pieConfiguration.type" class="kn-material-input" :options="descriptor.pieTypeOptions" option-value="value">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.pieTypeOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
            <label class="kn-material-input-label"> {{ $t('common.type') }} </label>
        </div>

        <div class="p-field p-float-label p-col-12 p-lg-6 p-fluid">
            <Dropdown v-model="pieConfiguration.categorizeBy" class="kn-material-input" :options="descriptor.categorizeByOptions" option-value="value">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.categorizeByOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
            <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.categorizeBy') }} </label>
        </div>

        <div class="p-col-12 p-lg-3 p-p-2">
            <WidgetEditorColorPicker :initial-value="pieConfiguration.fromColor" :label="$t('dashboard.widgetEditor.map.fromColor')" @change="onSelectionColorChanged($event, 'fromColor')"></WidgetEditorColorPicker>
        </div>
        <div class="p-col-12 p-lg-3 p-p-2">
            <WidgetEditorColorPicker :initial-value="pieConfiguration.toColor" :label="$t('dashboard.widgetEditor.map.toColor')" @change="onSelectionColorChanged($event, 'toColor')"></WidgetEditorColorPicker>
        </div>
        <div class="p-col-12 p-lg-3 p-p-2">
            <WidgetEditorColorPicker :initial-value="pieConfiguration.borderColor" :label="$t('dashboard.widgetEditor.iconTooltips.borderColor')" @change="onSelectionColorChanged($event, 'borderColor')"></WidgetEditorColorPicker>
        </div>

        <div class="p-col-12 p-lg-3 p-p-2">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.rangeLabel') }}</label>
            <Slider v-model="rangeValue" class="p-mt-2" range :min="1" :max="100" @change="onRangeSizeChange" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IMapWidgetVisualizationTypePie } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../MapVisualizationTypeDescriptor.json'
import Dropdown from 'primevue/dropdown'
import WidgetEditorColorPicker from '../../../common/WidgetEditorColorPicker.vue'
import Slider from 'primevue/slider'

export default defineComponent({
    name: 'map-visualization-type-pie',
    components: { Dropdown, WidgetEditorColorPicker, Slider },
    props: { propPieConfiguration: { type: Object as PropType<IMapWidgetVisualizationTypePie | null>, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            pieConfiguration: null as IMapWidgetVisualizationTypePie | null,
            rangeValue: [1, 100] as number[],
            getTranslatedLabel
        }
    },
    watch: {
        propPieConfiguration() {
            this.loadPieConfiguration()
        }
    },
    created() {
        this.loadPieConfiguration()
    },
    methods: {
        loadPieConfiguration() {
            this.pieConfiguration = this.propPieConfiguration
            this.loadRangeValue()
        },
        loadRangeValue() {
            if (!this.pieConfiguration) return
            this.rangeValue = [this.pieConfiguration.minSize, this.pieConfiguration.maxSize]
        },

        onSelectionColorChanged(event: string | null, property: 'fromColor' | 'toColor' | 'borderColor') {
            if (this.pieConfiguration && event) this.pieConfiguration[property] = event
        },
        onRangeSizeChange(event: any) {
            if (!this.pieConfiguration) return
            this.pieConfiguration.minSize = event[0]
            this.pieConfiguration.maxSize = event[1]
        }
    }
})
</script>
