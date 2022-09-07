<template>
    <div v-if="columnGroupsModel">
        <div class="kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.columnGroups.enableColumnGroups') }}</label>
            <InputSwitch v-model="columnGroupsModel.enabled" @change="onEnableColumnGroupsChanged"></InputSwitch>
        </div>

        <div v-for="(columnGroup, index) in columnGroupsModel.groups" :key="index" class="p-d-flex p-flex-row p-ai-center">
            <div class="p-d-flex p-flex-column kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('common.label') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="columnGroup.label" :disabled="!columnGroupsModel.enabled" @change="columnGroupsConfigurationChanged" />
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-m-2">
                <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                <!-- <MultiSelect v-model="columnGroup.columns" :options="widgetModel.columns" optionLabel="alias" optionValue="id" :disabled="!columnGroupsModel.enabled" @change="columnGroupsConfigurationChanged"> </MultiSelect> -->
                <WidgetEditorColumnsMultiselect :value="columnGroup.columns" :availableTargetOptions="availableColumnOptions" :widgetColumnsAliasMap="widgetColumnsAliasMap" optionLabel="alias" optionValue="id" :disabled="!columnGroupsModel.enabled" @change="onColumnsSelected($event, columnGroup)">
                </WidgetEditorColumnsMultiselect>
            </div>
            <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', !columnGroupsModel.enabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addColumnGroup() : removeColumnGroup(index)"></i>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetColumnGroups, IWidgetColumn, ITableWidgetColumnGroup } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
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
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadColumnGroups()
        this.loadWidgetColumnAliasMap()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemoved', (column) => this.onColumnRemoved(column))
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
                    this.removeColumnFromAvailableTargetOptions({ id: this.widgetModel.settings.configuration.columnGroups.groups[i].columns[j], alias: this.widgetModel.settings.configuration.columnGroups.groups[i].columns[j] })
                }
            }
        },
        removeColumnFromAvailableTargetOptions(tempColumn: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            console.log('INDEX: ', index)
            if (index !== -1) this.availableColumnOptions.splice(index, 1)
        },
        loadWidgetColumnAliasMap() {
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
            })
        },
        columnGroupsConfigurationChanged() {
            emitter.emit('columnGroupsConfigurationChanged', this.columnGroupsModel)
        },
        onEnableColumnGroupsChanged() {
            if (!this.columnGroupsModel) return
            if (this.columnGroupsModel.enabled && this.columnGroupsModel.groups.length === 0) {
                this.columnGroupsModel.groups.push({ id: cryptoRandomString({ length: 16, type: 'base64' }), label: '', columns: [] })
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
            intersection.forEach((el: string) => this.availableColumnOptions.push({ id: el, alias: this.widgetColumnsAliasMap[el] }))
        },
        onColumnsAddedFromMultiselect(columnGroup: ITableWidgetColumnGroup) {
            columnGroup.columns.forEach((target: string) => {
                const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableColumnOptions.splice(index, 1)
            })
        },
        addColumnGroup() {
            if (!this.columnGroupsModel) return
            this.columnGroupsModel.groups.push({ id: cryptoRandomString({ length: 16, type: 'base64' }), label: '', columns: [] })
            this.columnGroupsConfigurationChanged()
        },
        removeColumnGroup(index: number) {
            if (!this.columnGroupsModel) return
            this.columnGroupsModel.groups[index].columns.forEach((target: string) => this.availableColumnOptions.push({ id: target, alias: this.widgetColumnsAliasMap[target] }))
            this.columnGroupsModel.groups.splice(index, 1)
            this.columnGroupsConfigurationChanged()
        },
        onColumnRemoved(column: IWidgetColumn) {
            if (!this.columnGroupsModel) return
            for (let i = this.columnGroupsModel.groups.length - 1; i >= 0; i--) {
                for (let j = this.columnGroupsModel.groups[i].columns.length; j >= 0; j--) {
                    const tempColumn = this.columnGroupsModel.groups[i].columns[j]
                    if (column.id === tempColumn) this.columnGroupsModel.groups[i].columns.splice(j, 1)
                }
            }
            const index = this.availableColumnOptions.findIndex((columnOption: IWidgetColumn | { id: string; alias: string }) => columnOption.id === column.id)
            if (index !== -1) this.availableColumnOptions.splice(index, 1)
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
        }
    }
})
</script>
