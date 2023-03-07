<!-- eslint-disable vue/valid-v-model -->
<template>
    <div v-if="fieldStyles" class="p-grid p-p-4">
        <div v-for="(fieldStyle, index) in fieldStyles.styles" :key="index" class="dynamic-form-item p-col-12 p-grid p-ai-center">
            <!-- TODO: See what to do with field sizing -->
            <!-- <div class="p-col-12 p-grid">
                <div class="p-col-4 p-d-flex p-flex-column kn-flex">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.width') }}</label>
                    <InputNumber v-model="(fieldStyle.properties.width as number)" class="kn-material-input p-inputtext-sm" :disabled="fieldStylesDisabled" @blur="fieldStylesChanged" />
                </div>
                <div class="p-col-8"></div>
            </div> -->
            <div class="p-col-12 p-md-12 p-grid p-ai-center">
                <div class="p-col-10 p-md-11 p-d-flex p-flex-column p-p-2">
                    <label class="kn-material-input-label"> {{ $t('common.fields') }}</label>
                    <Dropdown v-if="index === 0" v-model="fieldStyle.target" class="kn-material-input" :options="descriptor.allColumnOption" option-value="value" option-label="label" :disabled="true"> </Dropdown>
                    <WidgetEditorColumnsMultiselect
                        v-else
                        :value="(fieldStyle.target as string[])"
                        :available-target-options="availableFieldOptions"
                        :widget-columns-alias-map="widgetFieldsAliasMap"
                        option-label="alias"
                        option-value="id"
                        :disabled="fieldStylesDisabled"
                        @change="onFieldsSelected($event, fieldStyle)"
                    >
                    </WidgetEditorColumnsMultiselect>
                </div>
                <div class="p-col-2 p-md-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                    <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', fieldStylesDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer" @click="index === 0 ? addFieldStyle() : removeFieldStyle(index)"></i>
                </div>
            </div>

            <div class="p-col-12 p-md-12 p-py-2">
                <WidgetEditorStyleToolbar :options="settingsDescriptor.defaultToolbarStyleOptions" :prop-model="fieldStyle.properties" :disabled="fieldStylesDisabled" @change="onStyleToolbarChange($event, fieldStyle)"> </WidgetEditorStyleToolbar>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetColumnStyle, IWidgetStyleToolbarModel, IWidgetColumn, ITableWidgetColumnGroup, ITableWidgetColumnStyles } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../PivotTableSettingsDescriptor.json'
import settingsDescriptor from '../../WidgetEditorSettingsTabDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'

export default defineComponent({
    name: 'table-widget-column-style',
    components: { Dropdown, InputNumber, WidgetEditorColumnsMultiselect, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, fieldType: { type: String, required: true } },
    data() {
        return {
            descriptor,
            settingsDescriptor,
            fieldStyles: null as ITableWidgetColumnStyles | null,
            availableFieldOptions: [] as (IWidgetColumn | ITableWidgetColumnGroup | { id: string; alias: string })[],
            widgetFieldsAliasMap: {} as any
        }
    },
    computed: {
        combinedArray(): any {
            const modelFields = this.widgetModel.fields
            const combinedArray = modelFields?.columns.concat(modelFields.rows, modelFields.data, modelFields.filters)
            return combinedArray
        },
        fieldStylesDisabled() {
            return !this.fieldStyles || !this.fieldStyles.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadFieldOptions()
        this.loadFieldStyles()
        this.loadWidgetFieldMaps()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromColumnStyle', this.onFieldOrGroupRemoved)
            emitter.on('columnGroupRemoved', this.onFieldOrGroupRemoved)
            emitter.on('columnAdded', this.onFieldAdded)
            emitter.on('columnAliasRenamed', this.onFieldAliasRenamed)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromColumnStyle', this.onFieldOrGroupRemoved)
            emitter.off('columnGroupRemoved', this.onFieldOrGroupRemoved)
            emitter.off('columnAdded', this.onFieldAdded)
            emitter.off('columnAliasRenamed', this.onFieldAliasRenamed)
        },
        onFieldOrGroupRemoved() {
            this.onFieldRemoved()
        },
        onFieldAliasRenamed() {
            this.updateFieldAliases()
        },
        onFieldAdded() {
            this.addFieldAsOption()
        },
        loadFieldStyles() {
            this.fieldStyles = this.widgetModel.settings.style[this.fieldType]
            this.removeFieldsFromAvailableOptions()
        },
        loadFieldOptions() {
            this.availableFieldOptions = [...this.combinedArray]
        },
        fieldStylesChanged() {
            const event = 'fieldStylesChanged'
            emitter.emit(event, this.fieldStyles)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        loadWidgetFieldMaps() {
            this.combinedArray?.forEach((field: IWidgetColumn) => {
                if (field.id) this.widgetFieldsAliasMap[field.id] = field.alias
                // if (column.id && column.fieldType) this.widgetFieldsAliasMap[column.id] = column.fieldType
            })
        },
        removeFieldsFromAvailableOptions() {
            const array = this.widgetModel.settings.style.fields.styles
            for (let i = 1; i < array.length; i++) {
                for (let j = 0; j < array[i].target.length; j++) {
                    this.removeFieldFromAvailableOptions({
                        id: array[i].target[j],
                        alias: array[i].target[j]
                    })
                }
            }
        },
        removeFieldFromAvailableOptions(tempField: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableFieldOptions.findIndex((targetOption: IWidgetColumn | ITableWidgetColumnGroup | { id: string; alias: string }) => targetOption.id === tempField.id)
            if (index !== -1) this.availableFieldOptions.splice(index, 1)
        },
        onFieldsSelected(event: any, fieldStyle: ITableWidgetColumnStyle) {
            const intersection = (fieldStyle.target as string[]).filter((el: string) => !event.value.includes(el))
            fieldStyle.target = event.value

            intersection.length > 0 ? this.onFieldsRemovedFromMultiselect(intersection) : this.onFieldsAddedFromMultiselect(fieldStyle)
            this.fieldStylesChanged()
        },
        onFieldsAddedFromMultiselect(fieldStyle: ITableWidgetColumnStyle) {
            ;(fieldStyle.target as string[]).forEach((target: string) => {
                const index = this.availableFieldOptions.findIndex((targetOption: IWidgetColumn | ITableWidgetColumnGroup | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableFieldOptions.splice(index, 1)
            })
        },
        onFieldsRemovedFromMultiselect(intersection: string[]) {
            //onFieldsRemovedFromMultiselect(intersection: string[], fieldStyle: ITableWidgetColumnStyle) {
            intersection.forEach((el: string) =>
                this.availableFieldOptions.push({
                    id: el,
                    alias: this.widgetFieldsAliasMap[el]
                })
            )
        },
        addFieldStyle() {
            if (!this.fieldStyles || this.fieldStylesDisabled) return
            this.fieldStyles.styles.push({
                target: [],
                properties: {
                    // width: '',
                    'background-color': 'rgb(0, 0, 0)',
                    color: 'rgb(255, 255, 255)',
                    'justify-content': '',
                    'font-size': '',
                    'font-family': '',
                    'font-style': '',
                    'font-weight': ''
                }
            })
        },
        removeFieldStyle(index: number) {
            if (!this.fieldStyles || this.fieldStylesDisabled) return
            ;(this.fieldStyles.styles[index].target as string[]).forEach((target: string) =>
                this.availableFieldOptions.push({
                    id: target,
                    alias: this.widgetFieldsAliasMap[target]
                })
            )
            this.fieldStyles.styles.splice(index, 1)
            this.fieldStylesChanged()
        },
        addFieldAsOption() {
            this.reloadModel()
        },
        onFieldRemoved() {
            this.reloadModel()
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, fieldStyle: ITableWidgetColumnStyle) {
            ;(fieldStyle.properties['background-color'] = model['background-color'] ?? 'rgb(0, 0, 0)'),
                (fieldStyle.properties.color = model.color ?? 'rgb(255, 255, 255)'),
                (fieldStyle.properties['justify-content'] = model['justify-content'] ?? 'center'),
                (fieldStyle.properties['font-size'] = model['font-size'] ?? '14px'),
                (fieldStyle.properties['font-family'] = model['font-family'] ?? ''),
                (fieldStyle.properties['font-style'] = model['font-style'] ?? 'normal'),
                (fieldStyle.properties['font-weight'] = model['font-weight'] ?? '')
            this.fieldStylesChanged()
        },
        reloadModel() {
            this.loadFieldOptions()
            this.loadFieldStyles()
            this.loadWidgetFieldMaps()
        },
        updateFieldAliases() {
            setTimeout(() => {
                this.loadFieldOptions()
                this.loadFieldStyles()
                this.loadWidgetFieldMaps()
            }, 1000)
        }
    }
})
</script>

<style lang="scss" scoped></style>
