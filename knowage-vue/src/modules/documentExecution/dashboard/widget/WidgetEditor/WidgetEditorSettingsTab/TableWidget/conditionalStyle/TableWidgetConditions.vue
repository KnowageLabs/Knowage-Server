<template>
    <div v-if="conditionalStylesModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-px-2 p-pb-4">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('common.enable') }}</label>
                <InputSwitch v-model="conditionalStylesModel.enabled" @change="onConditionalStylesEnabledChange"></InputSwitch>
            </div>
        </div>
        <div v-for="(conditionalStyle, index) in conditionalStylesModel.conditions" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center p-pt-2">
            <div class="p-grid p-col-12 p-ai-center">
                <div v-show="dropzoneTopVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
                <div
                    class="p-col-12 form-list-item-dropzone"
                    :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible[index] }"
                    @drop.stop="onDropComplete($event, 'before', index)"
                    @dragover.prevent
                    @dragenter.prevent="displayDropzone('top', index)"
                    @dragleave.prevent="hideDropzone('top', index)"
                ></div>

                <div class="p-col-12 p-grid" :draggable="!conditionalStylesDisabled" @dragstart.stop="onDragStart($event, index)">
                    <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center">
                        <i class="pi pi-th-large kn-cursor-pointer" :class="[conditionalStylesDisabled ? 'icon-disabled' : '']"></i>
                    </div>
                    <div class="p-col-10 p-grid p-ai-center">
                        <div class="p-grid p-col-12 p-ai-center">
                            <div class="p-sm-12 p-md-6 p-lg-3 p-d-flex p-flex-column p-p-2">
                                <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                                <Dropdown class="kn-material-input" v-model="conditionalStyle.target" :options="widgetModel.columns" optionLabel="alias" optionValue="id" :disabled="conditionalStylesDisabled" @change="conditionalStylesChanged"> </Dropdown>
                            </div>
                            <div class="p-sm-12 p-md-6 p-lg-2 p-d-flex p-flex-column p-p-2 operator-dropdown-container">
                                <label class="kn-material-input-label"> {{ $t('common.operator') }}</label>
                                <Dropdown class="kn-material-input" v-model="conditionalStyle.condition.operator" :options="descriptor.columnConditionOptions" optionLabel="label" optionValue="value" :disabled="conditionalStylesDisabled" @change="conditionalStylesChanged"> </Dropdown>
                            </div>
                            <div class="p-sm-12 p-md-6 p-lg-3 p-d-flex p-flex-column p-p-2 value-type-dropdown">
                                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.conditions.compareValueType') }}</label>
                                <Dropdown class="kn-material-input" v-model="conditionalStyle.condition.type" :options="descriptor.conditionCompareValueTypes" optionValue="value" :disabled="conditionalStylesDisabled" @change="onCompareValueTypeChanged(conditionalStyle)">
                                    <template #value="slotProps">
                                        <div>
                                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.conditionCompareValueTypes, $t) }}</span>
                                        </div>
                                    </template>
                                    <template #option="slotProps">
                                        <div>
                                            <span>{{ $t(slotProps.option.label) }}</span>
                                        </div>
                                    </template>
                                </Dropdown>
                            </div>
                            <div v-if="conditionalStyle.condition.type === 'static'" class="p-sm-12 p-md-6 p-lg-4 p-d-flex p-flex-column p-pl-2 p-pt-2">
                                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                                <InputText class="kn-material-input p-inputtext-sm" v-model="conditionalStyle.condition.value" :disabled="conditionalStylesDisabled" @change="conditionalStylesChanged" />
                            </div>
                            <div v-else-if="conditionalStyle.condition.type === 'parameter'" class="p-sm-12 p-md-6 p-lg-4 p-d-flex p-flex-column p-pl-2">
                                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                                <Dropdown class="kn-material-input" v-model="conditionalStyle.condition.parameter" :options="drivers" optionLabel="name" optionValue="name" :disabled="conditionalStylesDisabled" @change="onDriverChanged(conditionalStyle)"> </Dropdown>
                            </div>
                            <div v-else-if="conditionalStyle.condition.type === 'variable'" class="p-sm-12 p-md-6 p-lg-4 p-pl-2 p-d-flex p-flex-column p-pl-2">
                                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                                <Dropdown class="kn-material-input" v-model="conditionalStyle.condition.variable" :options="variables" optionLabel="name" optionValue="name" :disabled="conditionalStylesDisabled" @change="onVariableChanged(conditionalStyle)"> </Dropdown>
                            </div>
                        </div>
                        <div class="p-col-12 p-p-4">
                            <WidgetEditorStyleToolbar :options="descriptor.conditionsToolbarStyleOptions" :propModel="conditionalStyle.properties" :disabled="conditionalStylesDisabled" @change="onStyleToolbarChange($event, conditionalStyle)"> </WidgetEditorStyleToolbar>
                        </div>

                        <div class="p-col-12 p-px-4 p-py-2">
                            <InputSwitch v-model="conditionalStyle.applyToWholeRow" :disabled="conditionalStylesDisabled" @change="conditionalStylesChanged"></InputSwitch>
                            <label class="kn-material-input-label p-ml-4">{{ $t('dashboard.widgetEditor.conditions.applyToWholeRow') }}</label>
                        </div>
                    </div>

                    <div class="p-col-1 p-grid p-jc-center p-ai-center">
                        <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', conditionalStylesDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addConditionalStyle() : removeConditionalStyle(index)"></i>
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
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetConditionalStyle, IWidgetStyleToolbarModel, ITableWidgetConditionalStyles, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { emitter } from '../../../../../DashboardHelpers'
import { getDefaultConditionalStyle } from '../../../helpers/tableWidget/TableWidgetDefaultValues'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-conditions',
    components: { Dropdown, InputSwitch, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, drivers: { type: Array }, variables: { type: Array as PropType<IVariable[]> } },
    data() {
        return {
            descriptor,
            conditionalStylesModel: null as ITableWidgetConditionalStyles | null,
            parameterValuesMap: {},
            variableValuesMap: {},
            dropzoneTopVisible: {},
            dropzoneBottomVisible: {},
            getTranslatedLabel
        }
    },
    computed: {
        conditionalStylesDisabled() {
            return !this.conditionalStylesModel || !this.conditionalStylesModel.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadParameterValuesMap()
        this.loadVariableValuesMap()
        this.loadConditionalStyles()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromConditionalStyles', this.onColumnRemovedFromConditionalStyles)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromConditionalStyles', this.onColumnRemovedFromConditionalStyles)
        },
        onColumnRemovedFromConditionalStyles() {
            this.onColumnRemoved()
        },
        loadConditionalStyles() {
            if (this.widgetModel?.settings?.conditionalStyles) this.conditionalStylesModel = this.widgetModel.settings.conditionalStyles
        },
        loadParameterValuesMap() {
            if (!this.drivers) return
            this.drivers.forEach((driver: any) => (this.parameterValuesMap[driver.name] = driver.value))
        },
        loadVariableValuesMap() {
            if (!this.variables) return
            this.variables.forEach((variables: any) => (this.variableValuesMap[variables.name] = variables.value))
        },
        conditionalStylesChanged() {
            emitter.emit('conditionalStylesChanged', this.conditionalStylesModel)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onConditionalStylesEnabledChange() {
            if (!this.conditionalStylesModel) return
            if (this.conditionalStylesModel.enabled && this.conditionalStylesModel.conditions.length === 0) {
                this.conditionalStylesModel.conditions.push(getDefaultConditionalStyle())
            }
            this.conditionalStylesChanged()
        },
        onCompareValueTypeChanged(conditionalStyle: ITableWidgetConditionalStyle) {
            conditionalStyle.condition.value = ''
            switch (conditionalStyle.condition.type) {
                case 'static':
                    delete conditionalStyle.condition.parameter
                    delete conditionalStyle.condition.variable
                    break
                case 'parameter':
                    delete conditionalStyle.condition.variable
                    break
                case 'variable':
                    delete conditionalStyle.condition.parameter
                    break
            }
            this.conditionalStylesChanged()
        },
        onDriverChanged(conditionalStyle: ITableWidgetConditionalStyle) {
            const temp = conditionalStyle.condition.parameter
            if (temp) conditionalStyle.condition.value = this.parameterValuesMap[temp]
            this.conditionalStylesChanged()
        },
        onVariableChanged(conditionalStyle: ITableWidgetConditionalStyle) {
            const temp = conditionalStyle.condition.variable
            if (temp) conditionalStyle.condition.value = this.variableValuesMap[temp]
            this.conditionalStylesChanged()
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, conditionalStyle: ITableWidgetConditionalStyle) {
            conditionalStyle.properties = {
                'background-color': model['background-color'] ?? 'rgb(137, 158, 175)',
                color: model.color ?? 'rgb(255, 255, 255)',
                'justify-content': model['justify-content'] ?? 'center',
                'font-size': model['font-size'] ?? '14px',
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? 'normal',
                'font-weight': model['font-weight'] ?? '',
                icon: model.icon ?? ''
            }
            this.conditionalStylesChanged()
        },
        addConditionalStyle() {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            this.conditionalStylesModel.conditions.push({
                target: '',
                applyToWholeRow: false,
                condition: { type: '', operator: '', value: '' },
                properties: {
                    'justify-content': '',
                    'font-family': '',
                    'font-size': '',
                    'font-style': '',
                    'font-weight': '',
                    color: '',
                    'background-color': '',
                    icon: ''
                }
            })
        },
        removeConditionalStyle(index: number) {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            this.conditionalStylesModel.conditions.splice(index, 1)
            this.conditionalStylesChanged()
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
                this.conditionalStylesChanged()
            }
        },
        displayDropzone(position: string, index: number) {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            if (position === 'top') {
                this.dropzoneTopVisible[index] = true
            } else {
                this.dropzoneBottomVisible[index] = true
            }
        },
        hideDropzone(position: string, index: number) {
            if (!this.conditionalStylesModel || this.conditionalStylesDisabled) return
            if (position === 'top') {
                this.dropzoneTopVisible[index] = false
            } else {
                this.dropzoneBottomVisible[index] = false
            }
        },
        onColumnRemoved() {
            this.loadConditionalStyles()
        }
    }
})
</script>

<style lang="scss" scoped>
.operator-dropdown-container {
    min-width: 50px;
    max-width: 100px;
}

.value-type-dropdown {
    min-width: 150px;
    max-width: 150px;
}

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
