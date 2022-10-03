<template>
    <div v-if="columnStyles" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-px-2 p-pb-4">
            <InputSwitch v-model="columnStyles.enabled" @change="columnStylesChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('common.enable') }}</label>
        </div>
        <div v-for="(columnStyle, index) in columnStyles.styles" :key="index" class="dynamic-form-item p-col-12 p-grid p-ai-center">
            <div v-if="mode !== 'columnGroups'" class="p-col-12 p-grid">
                <div class="p-col-4 p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.width') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="(columnStyle.properties.width as number)" :disabled="columnStylesDisabled" @blur="columnStylesChanged" />
                </div>
                <div class="p-col-8"></div>
            </div>
            <div class="p-col-12 p-md-12 p-grid p-ai-center">
                <div class="p-col-10 p-md-11 p-d-flex p-flex-column p-p-2">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <Dropdown v-if="index === 0" class="kn-material-input" v-model="columnStyle.target" :options="descriptor.allColumnOption" optionValue="value" optionLabel="label" :disabled="true"> </Dropdown>
                    <WidgetEditorColumnsMultiselect
                        v-else
                        :value="(columnStyle.target as string[])"
                        :availableTargetOptions="availableColumnOptions"
                        :widgetColumnsAliasMap="widgetColumnsAliasMap"
                        optionLabel="alias"
                        optionValue="id"
                        :disabled="columnStylesDisabled"
                        @change="onColumnsSelected($event, columnStyle)"
                    >
                    </WidgetEditorColumnsMultiselect>
                </div>
                <div class="p-col-2 p-md-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                    <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', columnStylesDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer" @click="index === 0 ? addColumnStyle() : removeColumnStyle(index)"></i>
                </div>
            </div>

            <div class="p-col-12 p-md-12 p-py-4">
                <WidgetEditorStyleToolbar :options="descriptor.defaultToolbarStyleOptions" :propModel="columnStyle.properties" :disabled="columnStylesDisabled" @change="onStyleToolbarChange($event, columnStyle)"> </WidgetEditorStyleToolbar>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetColumnStyle, IWidgetStyleToolbarModel, IWidgetColumn, ITableWidgetColumnGroup, ITableWidgetColumnStyles } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'

export default defineComponent({
    name: 'table-widget-column-style',
    components: { Dropdown, InputNumber, InputSwitch, WidgetEditorColumnsMultiselect, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, mode: { type: String } },
    data() {
        return {
            descriptor,
            columnStyles: null as ITableWidgetColumnStyles | null,
            availableColumnOptions: [] as (IWidgetColumn | ITableWidgetColumnGroup | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any
        }
    },
    computed: {
        columnStylesDisabled() {
            return !this.columnStyles || !this.columnStyles.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadColumnStyles()
        this.loadWidgetColumnMaps()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromColumnStyle', this.onColumnOrGroupRemoved)
            emitter.on('columnGroupRemoved', this.onColumnOrGroupRemoved)
            emitter.on('columnAdded', this.onColumnAdded)
            emitter.on('columnAliasRenamed', this.onColumnAliasRenamed)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromColumnStyle', this.onColumnOrGroupRemoved)
            emitter.off('columnGroupRemoved', this.onColumnOrGroupRemoved)
            emitter.off('columnAdded', this.onColumnAdded)
            emitter.off('columnAliasRenamed', this.onColumnAliasRenamed)
        },
        onColumnOrGroupRemoved() {
            this.onColumnRemoved()
        },
        onColumnAliasRenamed() {
            this.updateColumnAliases()
        },
        onColumnAdded() {
            this.addColumnAsOption()
        },
        loadColumnStyles() {
            this.columnStyles = this.mode === 'columnGroups' ? this.widgetModel.settings.style.columnGroups : this.widgetModel.settings.style.columns
            this.removeColumnsFromAvailableOptions()
        },
        loadColumnOptions() {
            this.availableColumnOptions =
                this.mode === 'columnGroups'
                    ? this.widgetModel.settings.configuration.columnGroups.groups?.map((columnGroup: ITableWidgetColumnGroup) => {
                          return { id: columnGroup.id, alias: columnGroup.label }
                      })
                    : [...this.widgetModel.columns]
        },
        columnStylesChanged() {
            const event = this.mode === 'columnGroups' ? 'columnGroupStylesChanged' : 'columnStylesChanged'
            emitter.emit(event, this.columnStyles)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        loadWidgetColumnMaps() {
            const array = this.mode === 'columnGroups' ? this.widgetModel.settings.configuration.columnGroups.groups : this.widgetModel.columns
            array.forEach((column: IWidgetColumn | ITableWidgetColumnGroup) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = this.mode === 'columnGroups' ? (column as ITableWidgetColumnGroup).label : (column as IWidgetColumn).alias
            })
        },
        removeColumnsFromAvailableOptions() {
            const array = this.mode === 'columnGroups' ? this.widgetModel.settings.style.columnGroups.styles : this.widgetModel.settings.style.columns.styles
            for (let i = 1; i < array.length; i++) {
                for (let j = 0; j < array[i].target.length; j++) {
                    this.removeColumnFromAvailableOptions({
                        id: array[i].target[j],
                        alias: array[i].target[j]
                    })
                }
            }
        },
        removeColumnFromAvailableOptions(tempColumn: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | ITableWidgetColumnGroup | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            if (index !== -1) this.availableColumnOptions.splice(index, 1)
        },
        onColumnsSelected(event: any, columnStyle: ITableWidgetColumnStyle) {
            const intersection = (columnStyle.target as string[]).filter((el: string) => !event.value.includes(el))
            columnStyle.target = event.value

            intersection.length > 0 ? this.onColumnsRemovedFromMultiselect(intersection, columnStyle) : this.onColumnsAddedFromMultiselect(columnStyle)
            this.columnStylesChanged()
        },
        onColumnsAddedFromMultiselect(columnStyle: ITableWidgetColumnStyle) {
            ;(columnStyle.target as string[]).forEach((target: string) => {
                const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | ITableWidgetColumnGroup | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableColumnOptions.splice(index, 1)
            })
        },
        onColumnsRemovedFromMultiselect(intersection: string[], columnStyle: ITableWidgetColumnStyle) {
            intersection.forEach((el: string) =>
                this.availableColumnOptions.push({
                    id: el,
                    alias: this.widgetColumnsAliasMap[el]
                })
            )
        },
        addColumnStyle() {
            if (!this.columnStyles || this.columnStylesDisabled) return
            this.columnStyles.styles.push({
                target: [],
                properties: {
                    width: '',
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
        removeColumnStyle(index: number) {
            if (!this.columnStyles || this.columnStylesDisabled) return
            ;(this.columnStyles.styles[index].target as string[]).forEach((target: string) =>
                this.availableColumnOptions.push({
                    id: target,
                    alias: this.widgetColumnsAliasMap[target]
                })
            )
            this.columnStyles.styles.splice(index, 1)
            this.columnStylesChanged()
        },
        addColumnAsOption() {
            this.reloadModel()
        },
        onColumnRemoved() {
            this.reloadModel()
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, columnStyle: ITableWidgetColumnStyle) {
            ;(columnStyle.properties['background-color'] = model['background-color'] ?? 'rgb(0, 0, 0)'),
                (columnStyle.properties.color = model.color ?? 'rgb(255, 255, 255)'),
                (columnStyle.properties['justify-content'] = model['justify-content'] ?? 'center'),
                (columnStyle.properties['font-size'] = model['font-size'] ?? '14px'),
                (columnStyle.properties['font-family'] = model['font-family'] ?? ''),
                (columnStyle.properties['font-style'] = model['font-style'] ?? 'normal'),
                (columnStyle.properties['font-weight'] = model['font-weight'] ?? '')
            this.columnStylesChanged()
        },
        reloadModel() {
            this.loadColumnOptions()
            this.loadColumnStyles()
            this.loadWidgetColumnMaps()
        },
        updateColumnAliases() {
            setTimeout(() => {
                this.loadColumnOptions()
                this.loadColumnStyles()
                this.loadWidgetColumnMaps()
            }, 1000)
        }
    }
})
</script>

<style lang="scss" scoped></style>
