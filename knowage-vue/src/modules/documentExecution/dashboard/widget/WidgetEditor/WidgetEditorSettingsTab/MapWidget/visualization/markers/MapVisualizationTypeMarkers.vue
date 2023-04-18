<template>
    <div class="p-d-flex p-flex-row">
        <div class="p-d-flex p-flex-column p-jc-sb kn-flex p-mr-2">
            {{ markerConfig }}
            <div class="p-grid gap-1 p-m-0" style="column-gap: 0.5em; row-gap: 0.5em">
                <div
                    v-for="(markerTypeConfig, visTypeConfigIndex) in descriptor.markerTypes"
                    :key="visTypeConfigIndex"
                    v-tooltip.bottom="$t(markerTypeConfig.tooltip)"
                    class="markerTypeCards"
                    :class="{ selected: markerConfig.type === markerTypeConfig.name }"
                    @click="selectMarkerType(markerTypeConfig.name)"
                >
                    <i :class="markerTypeConfig.class" />
                </div>
            </div>
            <div class="config-form p-mt-2">
                <div v-if="markerConfig.type === 'default' || markerConfig.type === 'icon'" class="p-d-flex p-flex-row">
                    <WidgetEditorColorPicker class="kn-flex" :initial-value="markerConfig.style.color" :label="$t('dashboard.widgetEditor.iconTooltips.backgroundColor')" :disabled="false" @change="updateMarkerColor" />
                    <span class="p-float-label p-ml-2">
                        <InputNumber v-model="markerConfig.size" class="kn-material-input" :max="500" />
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.markerTypes.scale%') }}</label>
                    </span>
                    <Button v-if="markerConfig.type === 'icon'" icon="fas fa-icons fa-2x" class="p-button-text p-button-plain p-p-0 p-ml-2" @click="toggleIconPicker" />
                </div>
                <div v-else-if="markerConfig.type === 'img'" class="p-d-flex p-flex-row">
                    <span class="p-float-label kn-flex">
                        <InputNumber v-model="markerConfig.scale" class="kn-material-input kn-width-full" :max="500" />
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.markerTypes.scale%') }}</label>
                    </span>
                    <Button icon="fas fa-images fa-2x" class="p-button-text p-button-plain p-p-0 p-ml-2" @click="toggleImagePicker" />
                </div>
                <div v-else-if="markerConfig.type === 'url'" class="p-d-flex p-flex-row">
                    <span class="p-float-label kn-flex">
                        <InputText id="fileName" v-model="markerConfig.url" v-tooltip.bottom="markerConfig.url" class="kn-material-input kn-width-full" />
                        <label for="fileName" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.markerTypes.iconUrl') }} </label>
                    </span>
                    <span class="p-float-label p-ml-2">
                        <InputNumber v-model="markerConfig.scale" class="kn-material-input" :max="500" />
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.markerTypes.scale%') }}</label>
                    </span>
                </div>
            </div>
        </div>
        <div class="config-preview" :style="getPreviewStyle()">
            <i v-if="markerConfig.type === 'default' || markerConfig.type === 'icon'" :class="getIconClass()" />
            <img v-if="markerConfig.type === 'img' || markerConfig.type === 'web'" :src="markerConfig.type === 'img' ? markerConfig.img : markerConfig.url" :style="`width: ${markerConfig.scale}%; height: ${markerConfig.scale}%;`" />
        </div>
    </div>

    <WidgetEditorStyleIconPickerDialog v-if="iconPickerDialogVisible" :prop-model="markerConfig.icon" used-from="markers" @close="iconPickerDialogVisible = false" @save="onIconSelected" />
    <MapVisualizationImagePickerDialog v-if="imagePickerDialogVisible" :visible="imagePickerDialogVisible" @close="imagePickerDialogVisible = false" @setImage="onSetImage"></MapVisualizationImagePickerDialog>
</template>

<script lang="ts">
import { IMapWidgetVisualizationTypeMarker } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'
import { IIcon } from '@/modules/documentExecution/dashboard/Dashboard'
import { IImage } from '@/modules/documentExecution/dashboard/interfaces/DashboardImageWidget'
import descriptor from './MapVisualizationTypeMarkersDescriptor.json'
import WidgetEditorColorPicker from '../../../common/WidgetEditorColorPicker.vue'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorStyleIconPickerDialog from '../../../common/styleToolbar/WidgetEditorStyleIconPickerDialog.vue'
import MapVisualizationImagePickerDialog from './MapVisualizationImagePickerDialog.vue'

export default defineComponent({
    name: 'map-visualization-type',
    components: { WidgetEditorColorPicker, InputNumber, WidgetEditorStyleIconPickerDialog, MapVisualizationImagePickerDialog },
    props: { markerConfigProp: { type: Object as PropType<IMapWidgetVisualizationTypeMarker | undefined>, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            iconPickerDialogVisible: false,
            imagePickerDialogVisible: false,
            markerConfig: {} as IMapWidgetVisualizationTypeMarker
        }
    },
    watch: {
        widgetModel() {
            this.loadVisType()
        }
    },
    created() {
        this.loadVisType()
    },
    methods: {
        loadVisType() {
            this.markerConfig = this.markerConfigProp as IMapWidgetVisualizationTypeMarker
        },
        selectMarkerType(markerType: string) {
            this.markerConfig.type = markerType
        },
        updateMarkerColor(event: string | null) {
            if (!event || !this.markerConfig) return
            this.markerConfig.style.color = event
        },
        getPreviewStyle() {
            switch (this.markerConfig.type) {
                case 'default':
                    return `color:${this.markerConfig.style.color}; font-size: 1.5rem;`
                case 'icon':
                    return `color:${this.markerConfig.style.color}; font-size: ${this.markerConfig.size}%;`
            }
        },
        getIconClass() {
            switch (this.markerConfig.type) {
                case 'default':
                    return `fas fa-circle`
                case 'icon':
                    return this.markerConfig.icon?.className
                default:
                    return 'fas fa-cross'
            }
        },
        toggleIconPicker() {
            this.iconPickerDialogVisible = !this.iconPickerDialogVisible
        },
        toggleImagePicker() {
            this.imagePickerDialogVisible = !this.imagePickerDialogVisible
        },
        onIconSelected(icon: IIcon) {
            this.markerConfig.icon = icon
            this.iconPickerDialogVisible = false
        },
        onSetImage(image: IImage) {
            this.markerConfig.img = import.meta.env.VITE_RESTFUL_SERVICES_PATH + image.url
            this.imagePickerDialogVisible = false
        }
    }
})
</script>

<style lang="scss" scoped>
.markerTypeCards {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border: 1px solid #cccccc;
    height: 42px;
    width: 42px;
    &.selected {
        background-color: #bbd6ed;
    }
    &:hover {
        background-color: darken(#bbd6ed, 15%);
    }
    &:hover,
    &.selected {
        .visTypeIcon {
            background-color: #deecf8;
        }
    }
}
.config-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 0 0 100px;
    max-height: 100px;
    border: 1px solid #cccccc;
    overflow: hidden;
    .i {
        overflow: clip;
    }
}
</style>
