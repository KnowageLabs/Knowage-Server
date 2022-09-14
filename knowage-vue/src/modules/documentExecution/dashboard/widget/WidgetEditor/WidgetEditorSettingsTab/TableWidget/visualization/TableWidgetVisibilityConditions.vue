<template>
    <div>
        <div v-for="(visibilityCondition, index) in visibilityConditions" :key="index" class="visibility-condition-container kn-draggable p-d-flex p-flex-column p-my-2 p-pb-2">
            <div v-show="dropzoneTopVisible[index]" class="form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
            <div class="form-list-item-dropzone" :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible[index] }" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent="displayDropzone('top', index)" @dragleave.prevent="hideDropzone('top', index)"></div>
            <div class="p-d-flex p-flex-row" :draggable="true" @dragstart.stop="onDragStart($event, index)">
                <div class="p-d-flex p-flex-column p-jc-center">
                    <i class="pi pi-th-large kn-cursor-pointer p-mr-2"></i>
                </div>
                <div class="kn-flex">
                    <div class="p-d-flex p-flex-row p-ai-center kn-flex p-mt-1">
                        <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                            <label class="kn-material-input-label p-mr-2">{{ $t('common.condition') }}</label>
                            <Dropdown class="kn-material-input" v-model="visibilityCondition.condition.type" :options="descriptor.visibilityConditionsOptions" optionValue="value" @change="onVisibilityConditionTypeChanged(visibilityCondition)">
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
                        <div v-if="visibilityCondition.condition.type === 'variable'" class="p-d-flex p-flex-row p-ai-center kn-flex p-m-2">
                            <div class="kn-flex-2">
                                <label class="kn-material-input-label">{{ $t('common.variable') }}</label>
                                <Dropdown class="kn-material-input" v-model="visibilityCondition.condition.variable" :options="variables" optionValue="name" optionLabel="name" @change="onVariabeSelected(visibilityCondition)"> </Dropdown>
                            </div>
                            <div class="kn-flex-2 p-mx-2">
                                <label class="kn-material-input-label">{{ $t('common.operator') }}</label>
                                <Dropdown class="kn-material-input" v-model="visibilityCondition.condition.operator" :options="descriptor.visibilityConditionOperators" optionValue="value" optionLabel="label" @change="visibilityConditionsChanged"> </Dropdown>
                            </div>
                            <div class="p-d-flex p-flex-column kn-flex-2 p-mx-2">
                                <label class="kn-material-input-label p-mb-2">{{ $t('common.value') }}</label>
                                <InputText class="kn-material-input p-inputtext-sm" v-model="visibilityCondition.condition.value" @change="visibilityConditionsChanged" />
                            </div>
                        </div>

                        <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addVisibilityCondition() : removeVisibilityCondition(index)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row p-ai-center kn-flex p-mt-1">
                        <div class="p-d-flex p-flex-column kn-flex-3 p-m-2">
                            <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                            <MultiSelect v-model="visibilityCondition.target" :options="widgetModel.columns" optionLabel="alias" optionValue="id" @change="visibilityConditionsChanged"> </MultiSelect>
                        </div>
                        <div class="kn-flex p-ml-4 p-mt-4">
                            <InputSwitch v-model="visibilityCondition.hide" @change="visibilityConditionsChanged"></InputSwitch>
                            <label class="kn-material-input-label p-m-3">{{ $t('dashboard.widgetEditor.visibilityConditions.hideColumn') }}</label>
                        </div>
                        <div class="kn-flex p-mr-4 p-mt-4">
                            <InputSwitch v-model="visibilityCondition.hidePdf" @change="visibilityConditionsChanged"></InputSwitch>
                            <label class="kn-material-input-label p-m-3">{{ $t('dashboard.widgetEditor.visibilityConditions.hideOnPdf') }}</label>
                        </div>
                    </div>
                </div>
            </div>

            <div
                class="form-list-item-dropzone"
                :class="{ 'form-list-item-dropzone-active': dropzoneBottomVisible[index] }"
                @drop.stop="onDropComplete($event, 'after', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('bottom', index)"
                @dragleave.prevent="hideDropzone('bottom', index)"
            ></div>
            <div v-show="dropzoneBottomVisible[index]" class="form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'after', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn, ITableWidgetVisibilityCondition } from '@/modules/documentExecution/Dashboard/Dashboard'
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
            visibilityConditions: [] as ITableWidgetVisibilityCondition[],
            widgetColumnsAliasMap: {} as any,
            variableMap: {} as any,
            dropzoneTopVisible: {},
            dropzoneBottomVisible: {},
            getTranslatedLabel
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
            if (this.widgetModel.settings?.visualization?.visibilityConditions) this.visibilityConditions = [...this.widgetModel.settings.visualization.visibilityConditions]
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
            emitter.emit('visibilityConditionsChanged', this.visibilityConditions)
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
            this.visibilityConditions.push({ target: [], hide: false, hidePdf: false, condition: { type: 'Always' } })
        },
        removeVisibilityCondition(index: number) {
            this.visibilityConditions.splice(index, 1)
            this.visibilityConditionsChanged()
        },
        onColumnRemoved() {
            this.loadVisibilityConditions()
            this.visibilityConditionsChanged()
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
        displayDropzone(position: string, index: number) {
            if (position === 'top') {
                this.dropzoneTopVisible[index] = true
            } else {
                this.dropzoneBottomVisible[index] = true
            }
        },
        hideDropzone(position: string, index: number) {
            if (position === 'top') {
                this.dropzoneTopVisible[index] = false
            } else {
                this.dropzoneBottomVisible[index] = false
            }
        },
        onRowsMove(sourceRowIndex: number, targetRowIndex: number, position: string) {
            if (sourceRowIndex === targetRowIndex) return
            const newIndex = sourceRowIndex > targetRowIndex && position === 'after' ? targetRowIndex + 1 : targetRowIndex
            this.visibilityConditions.splice(newIndex, 0, this.visibilityConditions.splice(sourceRowIndex, 1)[0])
            this.visibilityConditionsChanged()
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
