<template>
    <div class="p-d-flex p-flex-column">
        <div class="p-d-flex p-flex-row p-jc-sb p-mb-2">
            <div class="p-fluid p-formgrid p-grid kn-flex">
                <div class="p-col-12 p-lg-3">
                    <span class="p-float-label kn-flex">
                        <InputNumber v-model="clusterConfig.radiusSize" class="kn-material-input" :max="500" />
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.radius') }}</label>
                    </span>
                </div>
                <div class="p-col-12 p-lg-3">
                    <span class="p-float-label kn-flex">
                        <InputText id="fontSize" v-model="clusterConfig.style['font-size']" class="kn-material-input" />
                        <label for="fontSize" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.iconTooltips.fontSize') }} </label>
                    </span>
                </div>
                <div class="p-col-12 p-lg-3">
                    <WidgetEditorColorPicker class="kn-flex" :initial-value="clusterConfig.style['background-color']" :label="$t('dashboard.widgetEditor.iconTooltips.backgroundColor')" :disabled="false" @change="updateMarkerColor($event, 'background-color')" />
                </div>
                <div class="p-col-12 p-lg-3">
                    <WidgetEditorColorPicker class="kn-flex" :initial-value="clusterConfig.style.color" :label="$t('dashboard.widgetEditor.iconTooltips.fontColor')" :disabled="false" @change="updateMarkerColor($event, 'color')" />
                </div>
            </div>
            <div class="config-preview p-ml-2">
                <div class="p-d-flex p-flex-row p-jc-center p-ai-center" :style="getClusterPreviewStyle()">10</div>
            </div>
        </div>
        <MarkersConfiguration :marker-config-prop="markerConfigProp" />
    </div>
</template>

<script lang="ts">
import { IMapWidgetVisualizationTypeCluster, IMapWidgetVisualizationTypeMarker } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'

import MarkersConfiguration from '../markers/MapVisualizationTypeMarkers.vue'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorColorPicker from '../../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'map-visualization-type',
    components: { MarkersConfiguration, InputNumber, WidgetEditorColorPicker },
    props: { markerConfigProp: { type: Object as PropType<IMapWidgetVisualizationTypeMarker | undefined>, required: true }, clusterConfigProp: { type: Object as PropType<IMapWidgetVisualizationTypeCluster | undefined>, required: true } },
    emits: [],
    data() {
        return {
            clusterConfig: {} as IMapWidgetVisualizationTypeCluster
        }
    },
    watch: {
        clusterConfigProp() {
            this.loadClusterConfig()
        }
    },
    created() {
        this.loadClusterConfig()
    },
    methods: {
        loadClusterConfig() {
            this.clusterConfig = this.clusterConfigProp as IMapWidgetVisualizationTypeCluster
        },
        updateMarkerColor(event: string | null, colorToUpdate) {
            if (!event || !this.clusterConfig) return
            this.clusterConfig.style[colorToUpdate] = event
        },
        getClusterPreviewStyle() {
            return `border-radius: 50px; font-size: ${this.clusterConfig.style['font-size']}; color:${this.clusterConfig.style.color}; background-color:${this.clusterConfig.style['background-color']}; width:${this.clusterConfig.radiusSize * 2}px; height:${this.clusterConfig.radiusSize * 2}px;`
        }
    }
})
</script>

<style lang="scss" scoped>
.config-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 0 0 100px;
    max-height: 100px;
    height: 89px;
    border: 1px solid #cccccc;
    overflow: hidden;
    .i {
        overflow: clip;
    }
}
</style>
