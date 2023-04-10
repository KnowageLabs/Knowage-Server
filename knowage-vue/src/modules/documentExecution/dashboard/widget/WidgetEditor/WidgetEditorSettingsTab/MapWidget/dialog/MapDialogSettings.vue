<template>
    <div v-if="dialogSettings" class="p-m-3">
        <div class="kn-flex p-d-flex p-flex-row" style="gap: 0.5em">
            <div class="p-float-label kn-flex">
                <InputText v-model="dialogSettings.width" class="kn-material-input kn-width-full p-inputtext-sm" :disabled="dialogSettingsDisabled" />
                <label class="kn-material-input-label">{{ $t('common.width') }}</label>
            </div>
            <div class="p-float-label kn-flex">
                <InputText v-model="dialogSettings.height" class="kn-material-input kn-width-full p-inputtext-sm" :disabled="dialogSettingsDisabled" />
                <label class="kn-material-input-label">{{ $t('common.height') }}</label>
            </div>
        </div>

        <WidgetEditorStyleToolbar class="p-my-3" :options="descriptor.toolbarStyleOptions" :prop-model="dialogSettings.style" :disabled="dialogSettingsDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>

        <Message class="kn-width-full p-d-flex p-jc-center p-m-0 p-mx-2" severity="info" :closable="false">
            {{ $t('dashboard.widgetEditor.map.dialogHint') }}
        </Message>

        <div v-for="(dialogProperty, index) in dialogSettings.properties" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center p-m-0 p-pt-0">
            <div v-show="dropzoneTopVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
            <div
                class="p-col-12 form-list-item-dropzone"
                :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible[index] }"
                @drop.stop="onDropComplete($event, 'before', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('top', index)"
                @dragleave.prevent="hideDropzone('top', index)"
            ></div>

            <div class="p-d-flex kn-flex p-ai-center" :draggable="true" @dragstart.stop="onDragStart($event, index)">
                <i class="pi pi-th-large kn-cursor-pointer"></i>
                <div class="kn-flex p-mx-2 p-d-flex p-flex-row" style="gap: 0.5em">
                    <span class="p-float-label kn-flex">
                        <Dropdown v-model="dialogProperty.layer" :disabled="dialogSettingsDisabled" class="kn-material-input kn-width-full" :options="widgetModel.layers" option-value="name" option-label="name" show-clear @change="onLayerChange(dialogProperty)"> </Dropdown>
                        <label class="kn-material-input-label">{{ $t('common.layer') }}</label>
                    </span>
                    <span class="p-float-label kn-flex">
                        <MultiSelect v-model="dialogProperty.columns" :disabled="dialogSettingsDisabled" class="kn-material-input kn-width-full" :options="getColumnOptionsFromLayer(dialogProperty)" option-label="alias" option-value="name"> </MultiSelect>
                        <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    </span>
                </div>
                <div class="p-d-flex p-flex-row p-jc-center p-ai-center">
                    <i v-if="index === 0" class="pi pi-plus-circle kn-cursor-pointer" @click="addTooltip()"></i>
                    <i v-if="index !== 0" class="pi pi-trash kn-cursor-pointer" @click="removeTooltip(index)"></i>
                </div>
            </div>

            <div
                class="p-col-12 form-list-item-dropzone"
                :class="{ 'form-list-item-dropzone-active': dropzoneBottomVisible[index] }"
                @drop.stop="onDropComplete($event, 'after', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('bottom', index)"
                @dragleave.prevent="hideDropzone('bottom', index)"
            ></div>
            <div v-show="dropzoneBottomVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'after', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/dashboard/Dashboard'
import { IMapDialogSettings } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { IMapDialogSettingsProperty } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import descriptor from './MapDialogSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import Message from 'primevue/message'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import * as mapWidgetDefaultValues from '../../../helpers/mapWidget/MapWidgetDefaultValues'

export default defineComponent({
    name: 'map-dialog-settings',
    components: { Dropdown, MultiSelect, Message, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            dialogSettings: null as IMapDialogSettings | null,
            dropzoneTopVisible: {},
            dropzoneBottomVisible: {}
        }
    },
    computed: {
        dialogSettingsDisabled() {
            return !this.widgetModel || !this.widgetModel.settings.dialog.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadTooltips()
    },

    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {},
        removeEventListeners() {},
        loadTooltips() {
            if (this.widgetModel?.settings?.tooltips) this.dialogSettings = this.widgetModel.settings.dialog
        },
        addTooltip() {
            this.dialogSettings?.properties.push({ layer: '', columns: [] })
        },
        removeTooltip(index: number) {
            if (!this.dialogSettings || !this.dialogSettings.properties) return
            if (index === 0) {
                this.dialogSettings.properties[0].layer = ''
                this.dialogSettings.properties[0].columns = []
            } else {
                this.dialogSettings.properties.splice(index, 1)
            }
        },
        onDragStart(event: any, index: number) {
            event.dataTransfer.setData('text/plain', JSON.stringify(index))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        onDropComplete(event: any, position: 'before' | 'after', index: number) {
            this.hideDropzone('bottom', index)
            this.hideDropzone('top', index)
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            this.onRowsMove(eventData, index, position)
        },
        onRowsMove(sourceRowIndex: number, targetRowIndex: number, position: string) {
            if (sourceRowIndex === targetRowIndex) return
            const newIndex = sourceRowIndex > targetRowIndex && position === 'after' ? targetRowIndex + 1 : targetRowIndex
            this.dialogSettings?.properties.splice(newIndex, 0, this.dialogSettings.properties.splice(sourceRowIndex, 1)[0])
        },
        displayDropzone(position: string, index: number) {
            position === 'top' ? (this.dropzoneTopVisible[index] = true) : (this.dropzoneBottomVisible[index] = true)
        },
        hideDropzone(position: string, index: number) {
            position === 'top' ? (this.dropzoneTopVisible[index] = false) : (this.dropzoneBottomVisible[index] = false)
        },
        getColumnOptionsFromLayer(dialogProperty: IMapDialogSettingsProperty) {
            const index = this.widgetModel.layers.findIndex((layer: any) => layer.name === dialogProperty.layer)
            return index !== -1 ? this.widgetModel.layers[index].content.columnSelectedOfDataset : []
        },
        onLayerChange(dialogProperty: IMapDialogSettingsProperty) {
            dialogProperty.columns = []
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.dialogSettings) return
            const defaultDialogSettings = mapWidgetDefaultValues.getDefaultDialogSettings()
            this.dialogSettings.style = {
                'font-family': model['font-family'] ?? defaultDialogSettings.style['font-family'],
                'font-style': model['font-style'] ?? 'normal',
                'font-size': model['font-size'] ?? '1px',
                'font-weight': model['font-weight'] ?? '',
                'justify-content': model['justify-content'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.form-list-item-dropzone {
    height: 20px;
    width: 100%;
    background-color: white;
}

.form-list-item-dropzone-active {
    height: 10px;
    background-color: #aec1d3;
}
</style>
