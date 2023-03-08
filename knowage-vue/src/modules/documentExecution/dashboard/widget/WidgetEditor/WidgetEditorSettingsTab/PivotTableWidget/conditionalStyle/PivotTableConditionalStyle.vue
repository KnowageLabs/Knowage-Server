<template>
    <div v-if="conditionalStylesModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-for="(conditionalStyle, index) in conditionalStylesModel.conditions" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center p-pt-2">
            <div class="p-col-12">
                {{ conditionalStyle }}
            </div>
            <div v-show="dropzoneTopVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
            <div
                class="p-col-12 form-list-item-dropzone p-p-0"
                :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible[index] }"
                @drop.stop="onDropComplete($event, 'before', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('top', index)"
                @dragleave.prevent="hideDropzone('top', index)"
            ></div>
            <div class="p-col-12 p-grid p-p-0" :draggable="!conditionalStylesDisabled" @dragstart.stop="onDragStart($event, index)">
                <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center">
                    <i class="pi pi-th-large kn-cursor-pointer" :class="[conditionalStylesDisabled ? 'icon-disabled' : '']"></i>
                </div>

                <div class="p-grid p-col-10 p-ai-center">
                    <div class="p-sm-12 p-md-6 p-lg-6 p-d-flex p-flex-column">
                        <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                        <Dropdown v-model="conditionalStyle.target" class="kn-material-input" :options="widgetModel.fields?.data ?? []" option-label="alias" option-value="id" :disabled="conditionalStylesDisabled"> </Dropdown>
                    </div>
                    <div class="p-sm-12 p-md-2 p-lg-2 p-d-flex p-flex-column">
                        <label class="kn-material-input-label"> {{ $t('common.operator') }}</label>
                        <Dropdown v-model="conditionalStyle.condition.operator" class="kn-material-input" :options="tableWidgetDescriptor.columnConditionOptions" option-label="label" option-value="value" :disabled="conditionalStylesDisabled"> </Dropdown>
                    </div>

                    <div class="p-sm-12 p-md-4 p-lg-4 p-d-flex p-flex-column">
                        <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                        <InputNumber v-model="conditionalStyle.condition.value" class="kn-material-input p-inputtext-sm" :disabled="conditionalStylesDisabled" />
                    </div>

                    <div class="p-col-12 p-grid p-ai-center">
                        <WidgetEditorStyleToolbar :options="descriptor.columnHeadersToolbarStyleOptions" :prop-model="conditionalStyle.properties" :disabled="conditionalStylesDisabled" @change="onStyleToolbarChange($event, conditionalStyle)"> </WidgetEditorStyleToolbar>
                    </div>
                </div>

                <div class="p-col-1 p-grid p-jc-center p-ai-center">
                    <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', conditionalStylesDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addConditionalStyle() : removeConditionalStyle(index)"></i>
                </div>
            </div>
            <div
                class="p-col-12 form-list-item-dropzone p-p-0"
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
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/dashboard/Dashboard'
import { IPivotTableWidgetConditionalStyle, IPivotTableWidgetConditionalStyles } from '@/modules/documentExecution/dashboard/interfaces/pivotTable/DashboardPivotTableWidget'
import { defineComponent, PropType } from 'vue'
import * as pivotTableDefaultValues from '../../../helpers/pivotTableWidget/PivotTableDefaultValues'
import descriptor from '../PivotTableSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import tableWidgetDescriptor from '../../TableWidget/TableWidgetSettingsDescriptor.json'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'pivot-table-conditional-style',
    components: { Dropdown, InputNumber, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            tableWidgetDescriptor,
            conditionalStylesModel: null as IPivotTableWidgetConditionalStyles | null,
            dropzoneTopVisible: {},
            dropzoneBottomVisible: {}
        }
    },
    computed: {
        conditionalStylesDisabled() {
            return !this.conditionalStylesModel || !this.conditionalStylesModel.enabled
        }
    },
    created() {
        this.loadConditionalStyles()
    },
    methods: {
        loadConditionalStyles() {
            if (this.widgetModel?.settings?.conditionalStyles) this.conditionalStylesModel = this.widgetModel.settings.conditionalStyles
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, conditionalStyle: IPivotTableWidgetConditionalStyle) {
            const defaultConditionalStyle = pivotTableDefaultValues.getDefaultConditionalStyle()
            conditionalStyle.properties = {
                'background-color': model['background-color'] ?? defaultConditionalStyle.properties['background-color'],
                color: model.color ?? defaultConditionalStyle.properties.color,
                'text-align': model['text-align'] ?? defaultConditionalStyle.properties['text-align'],
                'font-size': model['font-size'] ?? defaultConditionalStyle.properties['font-size'],
                'font-family': model['font-family'] ?? defaultConditionalStyle.properties['font-family'],
                'font-style': model['font-style'] ?? defaultConditionalStyle.properties['font-style'],
                'font-weight': model['font-weight'] ?? defaultConditionalStyle.properties['font-weight'],
                icon: model.icon ?? defaultConditionalStyle.properties.icon
            }
        },
        addConditionalStyle() {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            this.conditionalStylesModel.conditions.push(pivotTableDefaultValues.getDefaultConditionalStyle())
        },
        removeConditionalStyle(index: number) {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            this.conditionalStylesModel.conditions.splice(index, 1)
        },
        onDragStart(event: any, index: number) {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            event.dataTransfer.setData('text/plain', JSON.stringify(index))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        onDropComplete(event: any, position: 'before' | 'after', index: number) {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            this.hideDropzone('bottom', index)
            this.hideDropzone('top', index)
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            this.onRowsMove(eventData, index, position)
        },
        onRowsMove(sourceRowIndex: number, targetRowIndex: number, position: string) {
            if (sourceRowIndex === targetRowIndex) return
            if (this.conditionalStylesModel) {
                const newIndex = sourceRowIndex > targetRowIndex && position === 'after' ? targetRowIndex + 1 : targetRowIndex
                this.conditionalStylesModel.conditions.splice(newIndex, 0, this.conditionalStylesModel.conditions.splice(sourceRowIndex, 1)[0])
            }
        },
        displayDropzone(position: string, index: number) {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            position === 'top' ? (this.dropzoneTopVisible[index] = true) : (this.dropzoneBottomVisible[index] = true)
        },
        hideDropzone(position: string, index: number) {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            position === 'top' ? (this.dropzoneTopVisible[index] = false) : (this.dropzoneBottomVisible[index] = false)
        }
    }
})
</script>
