<template>
    <div v-if="columnGroupsModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-px-2 p-pb-4">
            <InputSwitch v-model="columnGroupsModel.enabled" @change="onEnableColumnGroupsChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.columnGroups.enableColumnGroups') }}</label>
        </div>

        <div v-for="(columnGroup, index) in columnGroupsModel.groups" :key="index" class="p-grid p-col-12 p-ai-center p-ai-center p-pt-2">
            <div class="p-col-12 p-sm-12 p-md-4 p-d-flex p-flex-column p-p-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('common.label') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="columnGroup.label" :disabled="columnGroupsDisabled" @change="onColumnGroupLabelChanged(columnGroup)" />
            </div>
            <div class="p-col-11 p-sm-11 p-md-7 p-d-flex p-flex-column p-p-2">
                <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                <WidgetEditorColumnsMultiselect :value="columnGroup.columns" :availableTargetOptions="availableColumnOptions" :widgetColumnsAliasMap="widgetColumnsAliasMap" optionLabel="alias" optionValue="id" :disabled="columnGroupsDisabled" @change="onColumnsSelected($event, columnGroup)">
                </WidgetEditorColumnsMultiselect>
            </div>
            <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', columnGroupsDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addColumnGroup() : removeColumnGroup(index)"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetColumnGroups, IWidgetColumn, ITableWidgetColumnGroup } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { removeColumnGroupFromModel } from '../../../helpers/tableWidget/TableWidgetFunctions'
import cryptoRandomString from 'crypto-random-string'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'

export default defineComponent({
    name: 'table-widget-column-groups',
    components: { InputSwitch, WidgetEditorColumnsMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            columnGroupsModel: null as ITableWidgetColumnGroups | null,
            availableColumnOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any
        }
    },
    computed: {
        columnGroupsDisabled() {
            return !this.columnGroupsModel || !this.columnGroupsModel.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadColumnGroups()
        this.loadWidgetColumnAliasMap()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromColumnGroups', () => this.onColumnRemoved())
            emitter.on('columnAliasRenamed', (column) => this.onColumnAliasRenamed(column))
            emitter.on('columnAdded', (column) => this.onColumnAdded(column))
        },
        loadColumnOptions() {
            this.availableColumnOptions = [...this.widgetModel.columns]
        },
        loadColumnGroups() {
            if (this.widgetModel?.settings?.configuration) this.columnGroupsModel = this.widgetModel.settings.configuration.columnGroups
            this.removeColumnsFromAvailableOptions()
        },
        removeColumnsFromAvailableOptions() {
            for (let i = 0; i < this.widgetModel.settings.configuration.columnGroups.groups.length; i++) {
                for (let j = 0; j < this.widgetModel.settings.configuration.columnGroups.groups[i].columns.length; j++) {
                    this.removeColumnFromAvailableOptions({
                        id: this.widgetModel.settings.configuration.columnGroups.groups[i].columns[j],
                        alias: this.widgetModel.settings.configuration.columnGroups.groups[i].columns[j]
                    })
                }
            }
        },
        removeColumnFromAvailableOptions(tempColumn: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            if (index !== -1) this.availableColumnOptions.splice(index, 1)
        },
        loadWidgetColumnAliasMap() {
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
            })
        },
        columnGroupsConfigurationChanged() {
            emitter.emit('columnGroupsConfigurationChanged', this.columnGroupsModel)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onEnableColumnGroupsChanged() {
            if (!this.columnGroupsModel) return
            if (this.columnGroupsModel.enabled && this.columnGroupsModel.groups.length === 0) {
                this.columnGroupsModel.groups.push({
                    id: cryptoRandomString({ length: 16, type: 'base64' }),
                    label: '',
                    columns: []
                })
            }
            this.columnGroupsConfigurationChanged()
        },
        onColumnsSelected(event: any, columnGroup: ITableWidgetColumnGroup) {
            const intersection = columnGroup.columns.filter((el: string) => !event.value.includes(el))
            columnGroup.columns = event.value
            intersection.length > 0 ? this.onColumnsRemovedFromMultiselect(intersection) : this.onColumnsAddedFromMultiselect(columnGroup)
            this.columnGroupsConfigurationChanged()
        },
        onColumnsRemovedFromMultiselect(intersection: string[]) {
            intersection.forEach((el: string) =>
                this.availableColumnOptions.push({
                    id: el,
                    alias: this.widgetColumnsAliasMap[el]
                })
            )
        },
        onColumnsAddedFromMultiselect(columnGroup: ITableWidgetColumnGroup) {
            columnGroup.columns.forEach((target: string) => {
                const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableColumnOptions.splice(index, 1)
            })
        },
        addColumnGroup() {
            if (!this.columnGroupsModel) return
            this.columnGroupsModel.groups.push({
                id: cryptoRandomString({ length: 16, type: 'base64' }),
                label: '',
                columns: []
            })
            this.columnGroupsConfigurationChanged()
        },
        removeColumnGroup(index: number) {
            if (!this.columnGroupsModel) return
            this.columnGroupsModel.groups[index].columns.forEach((target: string) =>
                this.availableColumnOptions.push({
                    id: target,
                    alias: this.widgetColumnsAliasMap[target]
                })
            )
            removeColumnGroupFromModel(this.widgetModel, this.columnGroupsModel.groups[index])
            this.columnGroupsModel.groups.splice(index, 1)
            this.columnGroupsConfigurationChanged()
        },
        onColumnRemoved() {
            this.loadColumnOptions()
            this.loadColumnGroups()
            this.columnGroupsConfigurationChanged()
        },
        onColumnAliasRenamed(column: IWidgetColumn) {
            if (!this.columnGroupsModel) return
            if (column.id && this.widgetColumnsAliasMap[column.id]) this.widgetColumnsAliasMap[column.id] = column.alias

            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === column.id)
            if (index !== -1) this.availableColumnOptions[index].alias = column.alias
            this.columnGroupsConfigurationChanged()
        },
        onColumnAdded(column: IWidgetColumn) {
            this.availableColumnOptions.push(column)
            if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
        },
        onColumnGroupLabelChanged(columnGroup: ITableWidgetColumnGroup) {
            emitter.emit('columnGroupLabelChanged', columnGroup)
            this.columnGroupsConfigurationChanged()
        }
    }
})
</script>
