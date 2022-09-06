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
                <MultiSelect v-model="columnGroup.columns" :options="widgetModel.columns" optionLabel="alias" optionValue="id" :disabled="!columnGroupsModel.enabled" @change="columnGroupsConfigurationChanged"> </MultiSelect>
            </div>
            <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', !columnGroupsModel.enabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addColumnGroup() : removeColumnGroup(index)"></i>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetColumnGroups, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import cryptoRandomString from 'crypto-random-string'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-column-groups',
    components: { InputSwitch, MultiSelect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            columnGroupsModel: null as ITableWidgetColumnGroups | null
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnGroups()
    },
    methods: {
        setEventListeners() {
            emitter.on('collumnRemoved', (column) => this.onColumnRemoved(column))
        },
        loadColumnGroups() {
            if (this.widgetModel?.settings?.configuration) this.columnGroupsModel = this.widgetModel.settings.configuration.columnGroups
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
        addColumnGroup() {
            if (!this.columnGroupsModel) return
            this.columnGroupsModel.groups.push({ id: cryptoRandomString({ length: 16, type: 'base64' }), label: '', columns: [] })
            this.columnGroupsConfigurationChanged()
        },
        removeColumnGroup(index: number) {
            if (!this.columnGroupsModel) return
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
            this.columnGroupsConfigurationChanged()
        }
    }
})
</script>
