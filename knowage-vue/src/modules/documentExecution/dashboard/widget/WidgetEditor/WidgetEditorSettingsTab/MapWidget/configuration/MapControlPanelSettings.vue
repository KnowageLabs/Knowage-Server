<template>
    <div v-if="controlPanelSettings">
        <div class="p-formgrid p-grid p-p-3">
            <span class="p-field p-col-12 p-lg-3 p-jc-center p-mt-3 p-pl-3">
                <InputSwitch v-model="controlPanelSettings.alwaysShow" />
                <label class="kn-material-input-label p-ml-3"> {{ $t('dashboard.widgetEditor.map.controlPanel.alwaysShow') }} </label>
            </span>

            <div class="p-col-12 p-lg-9 p-d-flex p-flex-row p-ai-center">
                <div class="p-float-label kn-flex">
                    <InputText v-model="controlPanelSettings.dimension" class="kn-material-input kn-width-full" />
                    <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.controlPanel.dimension') }}</label>
                </div>
                <i v-tooltip.top="$t('dashboard.widgetEditor.map.controlPanel.dimensionHint')" class="pi pi-question-circle kn-cursor-pointer p-mx-3"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IMapWidgetControlPanel } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'map-control-panel-settings',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            controlPanelSettings: null as IMapWidgetControlPanel | null
        }
    },
    created() {
        this.loadControlPanelSettings()
    },
    methods: {
        loadControlPanelSettings() {
            if (this.widgetModel?.settings?.configuration?.controlPanel) this.controlPanelSettings = this.widgetModel.settings.configuration.controlPanel
        }
    }
})
</script>
