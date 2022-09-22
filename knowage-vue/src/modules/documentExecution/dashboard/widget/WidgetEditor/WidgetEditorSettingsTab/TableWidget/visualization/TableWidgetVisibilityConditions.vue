<template>
    <div v-if="visibilityConditionsModel" class="p-grid p-ai-center p-p-4">
        <div class="p-col-12 p-px-2 p-pb-4">
            <InputSwitch v-model="visibilityConditionsModel.enabled" @change="visibilityConditionsChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('common.enable') }}</label>
        </div>
        <div v-for="(visibilityCondition, index) in visibilityConditionsModel.conditions" :key="index" class="p-grid p-col-12 p-ai-center p-ai-center p-pt-2">
            <div class="p-grid p-col-12 p-ai-center">
                <div v-show="dropzoneTopVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
                <div
                    class="p-col-12 form-list-item-dropzone"
                    :class="[dropzoneTopVisible[index] ? 'form-list-item-dropzone-active' : '', visibilityConditionsDisabled ? 'icon-disabled' : '']"
                    @drop.stop="onDropComplete($event, 'before', index)"
                    @dragover.prevent
                    @dragenter.prevent="displayDropzone('top', index)"
                    @dragleave.prevent="hideDropzone('top', index)"
                ></div>
                <div class="p-col-12 p-grid" :draggable="true" @dragstart.stop="onDragStart($event, index)">
                    <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center">
                        <i class="pi pi-th-large kn-cursor-pointer"></i>
                    </div>
                    <div class="p-col-11 p-grid p-ai-center">
                        <div class="p-col-12 p-grid p-ai-center p-pt-1">
                            <div class="p-col-12 p-md-4 p-d-flex p-flex-column kn-flex p-p-2">
                                <label class="kn-material-input-label p-mr-2">{{ $t('common.condition') }}</label>
                                <Dropdown class="kn-material-input" v-model="visibilityCondition.condition.type" :options="descriptor.visibilityConditionsOptions" optionValue="value" :disabled="visibilityConditionsDisabled" @change="onVisibilityConditionTypeChanged(visibilityCondition)">
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
                                <div class="p-col-12 p-md-4 p-pt-3">
                                    <label class="kn-material-input-label">{{ $t('common.variable') }}</label>
                                    <Dropdown class="kn-material-input" v-model="visibilityCondition.condition.variable" :options="variables" optionValue="name" optionLabel="name" :disabled="visibilityConditionsDisabled" @change="onVariabeSelected(visibilityCondition)"> </Dropdown>
                                </div>
                                <div class="p-col-12 p-md-2 p-d-flex p-flex-column p-px-2 p-pt-3">
                                    <label class="kn-material-input-label">{{ $t('common.operator') }}</label>
                                    <Dropdown class="kn-material-input" v-model="visibilityCondition.condition.operator" :options="descriptor.visibilityConditionOperators" optionValue="value" optionLabel="label" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged">
                                    </Dropdown>
                                </div>
                                <div class="p-col-12 p-md-6 p-d-flex p-flex-column p-fluid p-px-4 p-pt-1">
                                    <label class="kn-material-input-label p-mb-2">{{ $t('common.value') }}</label>
                                    <InputText class="kn-material-input p-inputtext-sm" v-model="visibilityCondition.condition.value" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged" />
                                </div>
                            </div>
                            <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                                <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', visibilityConditionsDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addVisibilityCondition() : removeVisibilityCondition(index)"></i>
                            </div>
                        </div>
                        <div class="p-col-12 p-grid p-ai-center p-ai-center p-pt-1">
                            <div class="p-col-12 p-md-5 p-d-flex p-flex-column p-p-2">
                                <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                                <MultiSelect v-model="visibilityCondition.target" :options="widgetModel.columns" optionLabel="alias" optionValue="id" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged"> </MultiSelect>
                            </div>
                            <div class="p-col-6 p-md-4 p-pl-4 p-pt-4">
                                <InputSwitch v-model="visibilityCondition.hide" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged"></InputSwitch>
                                <label class="kn-material-input-label p-p-3">{{ $t('dashboard.widgetEditor.visibilityConditions.hideColumn') }}</label>
                            </div>
                            <div class="p-col-6 p-md-3 p-pr-4 p-pt-4">
                                <InputSwitch v-model="visibilityCondition.hidePdf" :disabled="visibilityConditionsDisabled" @change="visibilityConditionsChanged"></InputSwitch>
                                <label class="kn-material-input-label p-p-3">{{ $t('dashboard.widgetEditor.visibilityConditions.hideOnPdf') }}</label>
                            </div>
                        </div>
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
import { IWidget, IWidgetColumn, ITableWidgetVisibilityCondition, ITableWidgetVisibilityConditions } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-visibility-condition',
    components: { Dropdown, InputSwitch, MultiSelect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, variables: { type: Array } },
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
    created() {
        this.setEventListeners()
        this.loadVisibilityConditions()
        this.variablesMap()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromVisibilityConditions', () => this.onColumnRemoved())
        },
        loadVisibilityConditions() {
            if (this.widgetModel.settings?.visualization?.visibilityConditions) this.visibilityConditionsModel = this.widgetModel.settings.visualization.visibilityConditions
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
            emitter.emit('visibilityConditionsChanged', this.visibilityConditionsModel)
        },
        onVisibilityConditionTypeChanged(visibilityCondition: ITableWidgetVisibilityCondition) {
            if (visibilityCondition.condition.type === 'always') {
                const fields = ['variable', 'variableValue', 'operator', 'value']
                fields.forEach((field: string) => delete visibilityCondition.condition[field])
            }
            this.visibilityConditionsChanged()
        },
        onVariabeSelected(visibilityCondition: ITableWidgetVisibilityCondition) {
            if (visibilityCondition.condition.variable) visibilityCondition.condition.variableValue = this.variableMap[visibilityCondition.condition.variable] ?? ''
            this.visibilityConditionsChanged()
        },
        addVisibilityCondition() {
            if (!this.visibilityConditionsModel || this.visibilityConditionsDisabled) return
            this.visibilityConditionsModel.conditions.push({ target: [], hide: false, hidePdf: false, condition: { type: 'Always' } })
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
