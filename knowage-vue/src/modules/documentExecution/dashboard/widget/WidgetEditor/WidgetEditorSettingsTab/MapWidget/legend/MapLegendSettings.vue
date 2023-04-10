<template>
    <div v-if="legendSettings">
        {{ legendSettings }}
        <div class="p-formgrid p-grid">
            <span class="p-field p-float-label p-col-12 p-lg-5">
                <Dropdown v-model="legendSettings.visualizationType" class="kn-material-input" :options="descriptor.visualizationTypes" option-value="value">
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
                <label for="attributes" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.attribute') }} </label>
            </span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IMapWidgetLegend } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import Dropdown from 'primevue/dropdown'
import descriptor from './MapLegendSettingsDescriptor.json'

export default defineComponent({
    name: 'map-legend-settings',
    components: { Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            legendSettings: null as IMapWidgetLegend | null,
            getTranslatedLabel
        }
    },
    created() {
        this.loadLegendSettings()
    },
    methods: {
        loadLegendSettings() {
            if (this.widgetModel?.settings?.tooltips) this.legendSettings = this.widgetModel.settings.legend
        }
    }
})
</script>
