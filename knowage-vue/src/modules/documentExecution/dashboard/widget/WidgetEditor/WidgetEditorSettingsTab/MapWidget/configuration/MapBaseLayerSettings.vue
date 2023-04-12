<template>
    <div v-if="baseLayerSettings">
        <div class="p-formgrid p-grid p-p-3">
            <span class="p-field p-float-label p-col-12 p-lg-6 p-fluid">
                <Dropdown v-model="baseLayerSettings.backgroundLayerId" class="kn-material-input" :options="layers" option-value="layerId" option-label="label" :disabled="baseLayerSettingsDisabled"></Dropdown>
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.baseLayer.selectBackgroundLayer') }} </label>
            </span>
            <span class="p-field p-float-label p-col-12 p-lg-6 p-fluid">
                <Dropdown v-model="baseLayerSettings.zoomFactor" class="kn-material-input" :options="descriptor.zoomFactorOptions" option-value="value" option-label="label" :disabled="baseLayerSettingsDisabled"></Dropdown>
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.baseLayer.zoomFactor') }} </label>
            </span>

            <span class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3 p-pl-3">
                <InputSwitch v-model="baseLayerSettings.showScale" :disabled="baseLayerSettingsDisabled" />
                <label class="kn-material-input-label p-ml-3"> {{ $t('dashboard.widgetEditor.map.baseLayer.showScale') }} </label>
            </span>
            <span class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3 p-pl-3">
                <InputSwitch v-model="baseLayerSettings.autoCentering" :disabled="baseLayerSettingsDisabled" />
                <label class="kn-material-input-label p-ml-3"> {{ $t('dashboard.widgetEditor.map.baseLayer.autoCentering') }} </label>
            </span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IMapWidgetBaseLayer, ILayer } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import descriptor from './MapBaseLayerSettingsDescriptor.json'

export default defineComponent({
    name: 'map-base-layer-settings',
    components: { Dropdown, InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, layers: { type: Array as PropType<ILayer[]>, required: true } },
    data() {
        return {
            descriptor,
            baseLayerSettings: null as IMapWidgetBaseLayer | null,
            getTranslatedLabel
        }
    },
    computed: {
        baseLayerSettingsDisabled() {
            return !this.widgetModel || !this.widgetModel.settings.configuration.baseLayer.enabled
        }
    },
    created() {
        this.loadBaseLayerSettings()
    },
    methods: {
        loadBaseLayerSettings() {
            if (this.widgetModel?.settings?.configuration?.baseLayer) this.baseLayerSettings = this.widgetModel.settings.configuration.baseLayer
        }
    }
})
</script>
