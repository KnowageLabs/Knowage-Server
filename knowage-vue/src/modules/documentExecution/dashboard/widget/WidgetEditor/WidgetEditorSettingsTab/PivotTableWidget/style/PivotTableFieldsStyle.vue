<template>
    <div v-if="fieldStyles" class="p-grid p-p-4">
        <div v-for="(fieldStyle, index) in fieldStyles.styles" :key="index" class="dynamic-form-item p-col-12 p-grid p-ai-center">
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
                <WidgetEditorStyleToolbar :options="descriptor.defaultToolbarStyleOptions" :prop-model="fieldStyle.properties" :disabled="fieldStylesDisabled" @change="onStyleToolbarChange($event, fieldStyle)"> </WidgetEditorStyleToolbar>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IPivotTableColumnStyles, IPivotTableColumnStyle } from '@/modules/documentExecution/dashboard/interfaces/pivotTable/DashboardPivotTableWidget'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../PivotTableSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'

export default defineComponent({
    name: 'pivot-table-fields-style',
    components: { Dropdown, WidgetEditorColumnsMultiselect, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, fieldType: { type: String, required: true } },
    data() {
        return {
            descriptor,
            fieldStyles: null as IPivotTableColumnStyles | null,
            availableFieldOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
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
            emitter.on('columnAdded', this.onFieldAdded)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromColumnStyle', this.onFieldOrGroupRemoved)
            emitter.off('columnAdded', this.onFieldAdded)
        },
        onFieldOrGroupRemoved() {
            this.onFieldRemoved()
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
        loadWidgetFieldMaps() {
            this.combinedArray?.forEach((field: IWidgetColumn) => {
                if (field.id) this.widgetFieldsAliasMap[field.id] = field.alias
            })
        },
        removeFieldsFromAvailableOptions() {
            const array = this.widgetModel.settings.style.fields.styles
            for (let i = 1; i < array.length; i++) {
                for (let j = 0; j < array[i].target.length; j++) {
                    this.removeFieldFromAvailableOptions({ id: array[i].target[j], alias: array[i].target[j] })
                }
            }
        },
        removeFieldFromAvailableOptions(tempField: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableFieldOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempField.id)
            if (index !== -1) this.availableFieldOptions.splice(index, 1)
        },
        onFieldsSelected(event: any, fieldStyle: IPivotTableColumnStyle) {
            const intersection = (fieldStyle.target as string[]).filter((el: string) => !event.value.includes(el))
            fieldStyle.target = event.value
            intersection.length > 0 ? this.onFieldsRemovedFromMultiselect(intersection) : this.onFieldsAddedFromMultiselect(fieldStyle)
        },
        onFieldsAddedFromMultiselect(fieldStyle: IPivotTableColumnStyle) {
            ;(fieldStyle.target as string[]).forEach((target: string) => {
                const index = this.availableFieldOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableFieldOptions.splice(index, 1)
            })
        },
        onFieldsRemovedFromMultiselect(intersection: string[]) {
            intersection.forEach((el: string) => this.availableFieldOptions.push({ id: el, alias: this.widgetFieldsAliasMap[el] }))
        },
        addFieldStyle() {
            if (!this.fieldStyles || this.fieldStylesDisabled) return
            this.fieldStyles.styles.push({
                target: [],
                properties: {
                    'background-color': 'rgb(0, 0, 0)',
                    color: 'rgb(255, 255, 255)',
                    'text-align': '',
                    'font-size': '',
                    'font-family': '',
                    'font-style': '',
                    'font-weight': ''
                }
            })
        },
        removeFieldStyle(index: number) {
            if (!this.fieldStyles || this.fieldStylesDisabled) return
            ;(this.fieldStyles.styles[index].target as string[]).forEach((target: string) => this.availableFieldOptions.push({ id: target, alias: this.widgetFieldsAliasMap[target] }))
            this.fieldStyles.styles.splice(index, 1)
        },
        addFieldAsOption() {
            this.reloadModel()
        },
        onFieldRemoved() {
            this.reloadModel()
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, fieldStyle: IPivotTableColumnStyle) {
            ;(fieldStyle.properties['background-color'] = model['background-color'] ?? 'rgb(0, 0, 0)'),
                (fieldStyle.properties.color = model.color ?? 'rgb(255, 255, 255)'),
                (fieldStyle.properties['text-align'] = model['text-align'] ?? 'center'),
                (fieldStyle.properties['font-size'] = model['font-size'] ?? '14px'),
                (fieldStyle.properties['font-family'] = model['font-family'] ?? ''),
                (fieldStyle.properties['font-style'] = model['font-style'] ?? 'normal'),
                (fieldStyle.properties['font-weight'] = model['font-weight'] ?? '')
        },
        reloadModel() {
            this.loadFieldOptions()
            this.loadFieldStyles()
            this.loadWidgetFieldMaps()
        }
    }
})
</script>

<style lang="scss" scoped></style>
