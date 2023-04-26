<template>
    <div v-if="visibilityConditionsModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-for="(visibilityCondition, index) in visibilityConditionsModel.conditions" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center">
            <div class="p-grid p-col-12 p-ai-center">
                <div v-show="dropzoneTopVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
                <div
                    class="p-col-12 form-list-item-dropzone"
                    :class="[dropzoneTopVisible[index] ? 'form-list-item-dropzone-active' : '']"
                    @drop.stop="onDropComplete($event, 'before', index)"
                    @dragover.prevent
                    @dragenter.prevent="displayDropzone('top', index)"
                    @dragleave.prevent="hideDropzone('top', index)"
                ></div>
                <div class="p-col-12 p-grid" :draggable="!visibilityConditionsDisabled" @dragstart.stop="onDragStart($event, index)">
                    <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center">
                        <i class="pi pi-th-large kn-cursor-pointer" :class="[visibilityConditionsDisabled ? 'icon-disabled' : '']"></i>
                    </div>
                    <div class="p-col-11 p-grid p-ai-center">
                        <div class="p-col-12 p-grid p-ai-center p-pt-1">
                            <div class="p-col-12 p-md-4 p-d-flex p-flex-column kn-flex p-p-2">
                                <label class="kn-material-input-label p-mr-2">{{ $t('common.condition') }}</label>
                                <Dropdown v-model="visibilityCondition.condition.type" class="kn-material-input" :options="descriptor.visibilityConditionsOptions" option-value="value" :disabled="visibilityConditionsDisabled" @change="onVisibilityConditionTypeChanged(visibilityCondition)">
                                    <template #value="slotProps">
                                        <div>
                                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.visibilityConditionsOptions, $t) }}</span>
                                        </div>
                                    </template>
                                    <template #option="slotProps">
                                        <div>
                                            <span>{{ $t(slotProps.option.label) }}</span>
                                        </div>
                                    </template>
                                </Dropdown>
                            </div>
                            <div v-if="visibilityCondition.condition.type === 'variable'" class="p-col-11 p-md-7 p-grid p-ai-center p-p-2">
                                <div class="p-col-12 p-md-4 p-d-flex p-flex-column p-px-2 p-pt-3">
                                    <label class="kn-material-input-label">{{ $t('common.variable') }}</label>
                                    <Dropdown v-model="visibilityCondition.condition.variable" class="kn-material-input" :options="variables" option-value="name" option-label="name" :disabled="visibilityConditionsDisabled" @change="onVariabeSelected(visibilityCondition)"> </Dropdown>
                                </div>
                                <div v-if="visibilityCondition.condition.type === 'variable' && visibilityCondition.condition.variablePivotDatasetOptions" class="p-col-12 p-md-2 p-d-flex p-flex-column">
                                    <label class="kn-material-input-label p-mr-2">{{ $t('common.key') }}</label>
                                    <Dropdown
                                        v-model="visibilityCondition.condition.variableKey"
                                        class="kn-material-input"
                                        :options="visibilityCondition.condition.variablePivotDatasetOptions ? Object.keys(visibilityCondition.condition.variablePivotDatasetOptions) : []"
                                        :disabled="visibilityConditionsDisabled"
                                        @change="onVariableKeyChanged(visibilityCondition)"
                                    >
                                    </Dropdown>
                                </div>
                                <div class="p-col-12 p-md-2 p-d-flex p-flex-column p-px-2 p-pt-3">
                                    <label class="kn-material-input-label">{{ $t('common.operator') }}</label>
                                    <Dropdown v-model="visibilityCondition.condition.operator" class="kn-material-input" :options="descriptor.visibilityConditionOperators" option-value="value" option-label="label" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged">
                                    </Dropdown>
                                </div>
                                <div class="p-col-12 p-md-4 p-d-flex p-flex-column p-px-2 p-pt-3">
                                    <label class="kn-material-input-label p-pb-1">{{ $t('common.value') }}</label>
                                    <InputText v-model="visibilityCondition.condition.value" class="kn-material-input p-inputtext-sm" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged" />
                                </div>
                            </div>
                            <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                                <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', visibilityConditionsDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addVisibilityCondition() : removeVisibilityCondition(index)"></i>
                            </div>
                        </div>
                        <div class="p-col-12 p-grid p-ai-center p-ai-center p-pt-1">
                            <div class="p-col-12 p-md-3 p-d-flex p-flex-column p-p-2">
                                <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                                <MultiSelect v-model="visibilityCondition.target" :options="widgetModel.columns" option-label="alias" option-value="id" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged"> </MultiSelect>
                            </div>
                            <div class="p-col-4 p-md-3">
                                <InputSwitch v-model="visibilityCondition.hide" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged"></InputSwitch>
                                <label class="kn-material-input-label p-p-1">{{ $t('dashboard.widgetEditor.visibilityConditions.hideColumn') }}</label>
                            </div>
                            <div class="p-col-4 p-md-3">
                                <InputSwitch v-model="visibilityCondition.hideFromSummary" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged"></InputSwitch>
                                <label class="kn-material-input-label p-p-1">{{ $t('dashboard.widgetEditor.visibilityConditions.hideFromSummary') }}</label>
                            </div>
                            <div class="p-col-4 p-md-3">
                                <InputSwitch v-model="visibilityCondition.hidePdf" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged"></InputSwitch>
                                <label class="kn-material-input-label p-p-1">{{ $t('dashboard.widgetEditor.visibilityConditions.hideOnPdf') }}</label>
                            </div>
                        </div>
                    </div>
                </div>
                <div
                    class="p-col-12 form-list-item-dropzone"
                    :class="{
                        'form-list-item-dropzone-active': dropzoneBottomVisible[index]
                    }"
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
import { IWidget, IWidgetColumn, ITableWidgetVisibilityCondition, ITableWidgetVisibilityConditions, IVariable } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { getDefaultVisibilityCondition } from '../../../helpers/tableWidget/TableWidgetDefaultValues'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'
import { getSelectedVariable } from '@/modules/documentExecution/dashboard/generalSettings/VariablesHelper'

export default defineComponent({
    name: 'table-widget-visibility-condition',
    components: { Dropdown, InputSwitch, MultiSelect },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true }
    },
    data() {
        return {
            descriptor,
            visibilityConditionsModel: null as ITableWidgetVisibilityConditions | null,
            widgetColumnsAliasMap: {} as any,
            variableMap: {} as any,
            dropzoneTopVisible: {},
            dropzoneBottomVisible: {},
            getTranslatedLabel
        }
    },
    computed: {
        visibilityConditionsDisabled() {
            return !this.visibilityConditionsModel || !this.visibilityConditionsModel.enabled
        }
    },
    watch: {
        visibilityConditionsDisabled() {
            this.onVisibilityConditionsEnabledChange()
        }
    },
    created() {
        this.setEventListeners()
        this.loadVisibilityConditions()
        this.variablesMap()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromVisibilityConditions', this.onColumnRemovedFromVisibilityConditions)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromVisibilityConditions', this.onColumnRemovedFromVisibilityConditions)
        },
        onColumnRemovedFromVisibilityConditions() {
            this.onColumnRemoved()
        },
        loadVisibilityConditions() {
            if (this.widgetModel.settings?.visualization?.visibilityConditions) {
                this.visibilityConditionsModel = this.widgetModel.settings.visualization.visibilityConditions
                this.visibilityConditionsModel?.conditions.forEach((visibilityCondition: ITableWidgetVisibilityCondition) => {
                    if (visibilityCondition.condition.type === 'variable' && visibilityCondition.condition.variableKey) this.setVisibilityConditionPivotedValues(visibilityCondition)
                })
            }
        },
        setVisibilityConditionPivotedValues(visibilityCondition: ITableWidgetVisibilityCondition) {
            const index = this.variables.findIndex((variable: IVariable) => variable.name === visibilityCondition.condition.variable)
            if (index !== -1) visibilityCondition.condition.variablePivotDatasetOptions = this.variables[index].pivotedValues
        },
        loadWidgetColumnMaps() {
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
            })
        },
        variablesMap() {
            this.variables?.forEach((variable: any) => (this.variableMap[variable.name] = variable.value))
        },
        visibilityConditionsChanged() {
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onVisibilityConditionsEnabledChange() {
            if (!this.visibilityConditionsModel) return
            if (this.visibilityConditionsModel.enabled && this.visibilityConditionsModel.conditions.length === 0) {
                this.visibilityConditionsModel.conditions = [getDefaultVisibilityCondition()]
            }

            this.visibilityConditionsChanged()
        },
        onVisibilityConditionTypeChanged(visibilityCondition: ITableWidgetVisibilityCondition) {
            if (visibilityCondition.condition.type === 'always') {
                const fields = ['variable', 'variableValue', 'operator', 'value', 'variablePivotDatasetOptions']
                fields.forEach((field: string) => delete visibilityCondition.condition[field])
            }
            this.visibilityConditionsChanged()
        },
        onVariabeSelected(visibilityCondition: ITableWidgetVisibilityCondition) {
            if (visibilityCondition.condition.variable) {
                const variable = getSelectedVariable(visibilityCondition.condition.variable, this.variables)
                if (variable && variable.dataset && !variable.column) {
                    visibilityCondition.condition.variablePivotDatasetOptions = variable.pivotedValues ?? {}
                    visibilityCondition.condition.variableValue = ''
                } else {
                    visibilityCondition.condition.variableValue = this.variableMap[visibilityCondition.condition.variable] ?? ''
                    delete visibilityCondition.condition.variablePivotDatasetOptions
                }
                delete visibilityCondition.condition.variableKey
            }
            this.visibilityConditionsChanged()
        },
        onVariableKeyChanged(visibilityCondition: ITableWidgetVisibilityCondition) {
            visibilityCondition.condition.variableValue = visibilityCondition.condition.variableKey ? visibilityCondition.condition.variablePivotDatasetOptions[visibilityCondition.condition.variableKey] : ''
            this.visibilityConditionsChanged()
        },
        addVisibilityCondition() {
            if (!this.visibilityConditionsModel || this.visibilityConditionsDisabled) return
            this.visibilityConditionsModel.conditions.push({
                target: [],
                hide: false,
                hidePdf: false,
                hideFromSummary: false,
                condition: { type: 'Always' }
            })
        },
        removeVisibilityCondition(index: number) {
            if (!this.visibilityConditionsModel || this.visibilityConditionsDisabled) return
            this.visibilityConditionsModel.conditions.splice(index, 1)
            this.visibilityConditionsChanged()
        },
        onColumnRemoved() {
            this.loadVisibilityConditions()
            this.visibilityConditionsChanged()
        },
        onDragStart(event: any, index: number) {
            if (!this.visibilityConditionsModel || this.visibilityConditionsDisabled) return
            event.dataTransfer.setData('text/plain', JSON.stringify(index))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        onDropComplete(event: any, position: 'before' | 'after', index: number) {
            if (!this.visibilityConditionsModel || this.visibilityConditionsDisabled) return
            this.hideDropzone('bottom', index)
            this.hideDropzone('top', index)
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            this.onRowsMove(eventData, index, position)
        },
        displayDropzone(position: string, index: number) {
            if (!this.visibilityConditionsModel || this.visibilityConditionsDisabled) return
            if (position === 'top') {
                this.dropzoneTopVisible[index] = true
            } else {
                this.dropzoneBottomVisible[index] = true
            }
        },
        hideDropzone(position: string, index: number) {
            if (!this.visibilityConditionsModel || this.visibilityConditionsDisabled) return
            if (position === 'top') {
                this.dropzoneTopVisible[index] = false
            } else {
                this.dropzoneBottomVisible[index] = false
            }
        },
        onRowsMove(sourceRowIndex: number, targetRowIndex: number, position: string) {
            if (sourceRowIndex === targetRowIndex) return
            if (this.visibilityConditionsModel) {
                const newIndex = sourceRowIndex > targetRowIndex && position === 'after' ? targetRowIndex + 1 : targetRowIndex
                this.visibilityConditionsModel.conditions.splice(newIndex, 0, this.visibilityConditionsModel.conditions.splice(sourceRowIndex, 1)[0])
                this.visibilityConditionsChanged()
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.visibility-condition-container {
    border-bottom: 1px solid #c2c2c2;
}

.visibility-condition-containerr:last-child {
    border-bottom: none;
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
