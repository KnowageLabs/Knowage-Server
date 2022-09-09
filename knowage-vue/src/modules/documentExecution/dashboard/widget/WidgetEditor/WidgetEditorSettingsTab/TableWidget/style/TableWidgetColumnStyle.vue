<template>
    <div>
        {{ columnStyles }}

        {{ 'AVAL OPTIONS' }}
        {{ availableColumnOptions }}
        <div v-for="(columnStyle, index) in columnStyles" :key="index" class="p-d-flex p-flex-column p-my-2 p-pb-2">
            <div class="p-d-flex p-flex-row p-ai-center">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <TableWidgetVisualizationTypeMultiselect
                        :value="(columnStyle.target as string[])"
                        :availableTargetOptions="availableColumnOptions"
                        :widgetColumnsAliasMap="widgetColumnsAliasMap"
                        :allColumnsSelected="allColumnsSelected"
                        optionLabel="alias"
                        optionValue="id"
                        @change="onColumnsSelected($event, columnStyle)"
                        @allColumnsSelected="onAllColumnsSelected(columnStyle)"
                    >
                    </TableWidgetVisualizationTypeMultiselect>
                </div>
                <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash']" class="kn-cursor-pointer p-ml-2 p-mt-3" @click="index === 0 ? addColumnStyle() : removeColumnStyle(index)"></i>
            </div>

            <div class="p-my-4">
                <WidgetEditorStyleToolbar :options="descriptor.defaultToolbarStyleOptions" :propModel="columnStyle.properties" @change="onStyleToolbarChange($event, columnStyle)"> </WidgetEditorStyleToolbar>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetColumnStyle, IWidgetStyleToolbarModel, IWidgetColumn, ITableWidgetColumnGroup } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import TableWidgetVisualizationTypeMultiselect from '../visualization/TableWidgetVisualizationTypeMultiselect.vue'

export default defineComponent({
    name: 'table-widget-column-style',
    components: { InputNumber, TableWidgetVisualizationTypeMultiselect, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, mode: { type: String } },
    data() {
        return {
            descriptor,
            columnStyles: [] as ITableWidgetColumnStyle[],
            availableColumnOptions: [] as (IWidgetColumn | ITableWidgetColumnGroup | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any,
            allColumnsSelected: false
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadColumnStyles()
        this.loadWidgetColumnMaps()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromColumnStyle', () => this.onColumnRemoved())
            emitter.on('columnGroupRemoved', () => this.onColumnRemoved())
            emitter.on('columnAdded', () => this.onColumnAdded())
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
            const event = this.mode === 'columnGroups' ? 'columnGroupStylesChanged' : 'columnStyleChanged'
            emitter.emit(event, this.columnStyles)
        },
        loadWidgetColumnMaps() {
            const array = this.mode === 'columnGroups' ? this.widgetModel.settings.configuration.columnGroups.groups : this.widgetModel.columns
            array.forEach((column: IWidgetColumn | ITableWidgetColumnGroup) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = this.mode === 'columnGroups' ? (column as ITableWidgetColumnGroup).label : (column as IWidgetColumn).alias
            })
        },
        removeColumnsFromAvailableOptions() {
            const array = this.mode === 'columnGroups' ? this.widgetModel.settings.style.columnGroups : this.widgetModel.settings.style.columns
            for (let i = 0; i < array.length; i++) {
                for (let j = 0; j < array[i].target.length; j++) {
                    if (array[i].target[j] === 'All Columns') this.allColumnsSelected = true
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
        onAllColumnsSelected(columnStyle: ITableWidgetColumnStyle) {
            this.allColumnsSelected = true
            columnStyle.allColumnSelected = true
            this.onColumnsRemovedFromMultiselect(columnStyle.target, columnStyle)
            columnStyle.target = ['All Columns']
        },
        onColumnsSelected(event: any, columnStyle: ITableWidgetColumnStyle) {
            const intersection = columnStyle.target.filter((el: string) => !event.value.includes(el))
            columnStyle.target = event.value

            intersection.length > 0 ? this.onColumnsRemovedFromMultiselect(intersection, columnStyle) : this.onColumnsAddedFromMultiselect(columnStyle)
            this.columnStylesChanged()
        },
        onColumnsAddedFromMultiselect(columnStyle: ITableWidgetColumnStyle) {
            columnStyle.target.forEach((target: string) => {
                const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | ITableWidgetColumnGroup | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableColumnOptions.splice(index, 1)
            })
        },
        onColumnsRemovedFromMultiselect(intersection: string[], columnStyle: ITableWidgetColumnStyle) {
            if (intersection[0] === 'All Columns') {
                this.allColumnsSelected = false
                columnStyle.allColumnSelected = false
                return
            }
            intersection.forEach((el: string) =>
                this.availableColumnOptions.push({
                    id: el,
                    alias: this.widgetColumnsAliasMap[el]
                })
            )
        },
        addColumnStyle() {
            this.columnStyles.push({
                target: [],
                allColumnSelected: false,
                properties: {
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
            if (this.columnStyles[index].target[0] === 'All Columns') this.allColumnsSelected = false
            else
                this.columnStyles[index].target.forEach((target: string) =>
                    this.availableColumnOptions.push({
                        id: target,
                        alias: this.widgetColumnsAliasMap[target]
                    })
                )
            this.columnStyles.splice(index, 1)
            this.columnStylesChanged()
        },
        onColumnAdded() {
            this.reloadModel()
        },
        onColumnRemoved() {
            this.reloadModel()
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, columnStyle: ITableWidgetColumnStyle) {
            columnStyle.properties = {
                'background-color': model['background-color'] ?? 'rgb(0, 0, 0)',
                color: model.color ?? 'rgb(255, 255, 255)',
                'justify-content': model['justify-content'] ?? 'center',
                'font-size': model['font-size'] ?? '14px',
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? 'normal',
                'font-weight': model['font-weight'] ?? ''
            }
            this.columnStylesChanged()
        },
        reloadModel() {
            this.loadColumnOptions()
            this.loadColumnStyles()
            this.loadWidgetColumnMaps()
        }
    }
})
</script>

<style lang="scss" scoped></style>
