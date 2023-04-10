<template>
    <div v-if="dialogSettings" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-grid p-col-12">
            <div class="p-col-12 p-md-6 p-d-flex p-flex-column">
                <label class="kn-material-input-label">{{ $t('common.width') }}</label>
                <InputText v-model="dialogSettings.width" class="kn-material-input p-inputtext-sm" :disabled="dialogSettingsDisabled" />
            </div>
            <div class="p-col-12 p-md-6 p-d-flex p-flex-column">
                <label class="kn-material-input-label">{{ $t('common.height') }}</label>
                <InputText v-model="dialogSettings.height" class="kn-material-input p-inputtext-sm" :disabled="dialogSettingsDisabled" />
            </div>

            <div class="p-col-12 p-py-4">
                <WidgetEditorStyleToolbar :options="descriptor.toolbarStyleOptions" :prop-model="dialogSettings.style" :disabled="dialogSettingsDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
            </div>
        </div>

        <Message class="p-col-12 p-d-flex p-jc-center p-mx-4" severity="info" :closable="false">
            {{ $t('dashboard.widgetEditor.map.dialogHint') }}
        </Message>

        <div v-for="(dialogProperty, index) in dialogSettings.properties" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center p-py-2 p-pb-2">
            <div v-show="dropzoneTopVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
            <div
                class="p-col-12 form-list-item-dropzone"
                :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible[index] }"
                @drop.stop="onDropComplete($event, 'before', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('top', index)"
                @dragleave.prevent="hideDropzone('top', index)"
            ></div>

            <div class="p-grid p-col-12" :draggable="true" @dragstart.stop="onDragStart($event, index)">
                <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-pr-4">
                    <i class="pi pi-th-large kn-cursor-pointer"></i>
                </div>
                <div class="p-col-11 p-md-5 p-d-flex p-flex-column">
                    <label class="kn-material-input-label">{{ $t('common.layer') }}</label>
                    <Dropdown v-model="dialogProperty.layer" class="kn-material-input" :options="widgetModel.layers" option-value="name" option-label="name" :disabled="dialogSettingsDisabled" @change="onLayerChange(dialogProperty)"> </Dropdown>
                </div>

                <div class="p-col-11 p-md-5 p-d-flex p-flex-column">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <MultiSelect v-model="dialogProperty.columns" :options="getColumnOptionsFromLayer(dialogProperty)" option-label="alias" option-value="name" :disabled="dialogSettingsDisabled"> </MultiSelect>
                </div>
                <div class="p-col-1 p-d-flex p-flex-row p-jc-center p-ai-center p-pl-2">
                    <i v-if="index === 0" class="pi pi-plus-circle kn-cursor-pointer p-ml-2 p-mt-4" @click="addTooltip()"></i>
                    <i class="pi pi-trash kn-cursor-pointer p-ml-4 p-mt-4" @click="removeTooltip(index)"></i>
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
