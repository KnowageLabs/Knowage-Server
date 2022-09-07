<template>
    <div v-if="headersModel">
        {{ headersModel }}

        <hr />

        {{ availableTargetOptions }}

        <hr />
        {{ widgetColumnsAliasMap }}
        <div class="p-d-flex p-flex-row p-ai-center p-m-3">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.headers.enableHeader') }}</label>
                <InputSwitch v-model="headersModel.enabled" @change="headersConfigurationChanged"></InputSwitch>
            </div>
            <div class="p-ml-auto p-mr-2">
                <label class="kn-material-input-label p-mr-2"> {{ $t('dashboard.widgetEditor.headers.enableMultiline') }}</label>
                <InputSwitch v-model="headersModel.enabledMultiline" :disabled="!headersModel.enabled" @change="headersConfigurationChanged"></InputSwitch>
            </div>
        </div>

        <hr />

        <div class="p-d-flex p-flex-column p-m-3">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.headers.enableCustomHeaders') }}</label>
                <InputSwitch v-model="headersModel.custom.enabled" @change="onCustomHeadersEnabledChange"></InputSwitch>
            </div>

            <div v-for="(rule, index) in headersModel.custom.rules" :key="index" class="p-d-flex p-flex-row p-ai-center">
                <div class="p-d-flex p-flex-column kn-flex p-mt-1">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <!-- <MultiSelect v-model="rule.target" :options="getTargetOptions(rule)" optionLabel="alias" optionValue="id" :disabled="!headersModel.custom.enabled" @change="onColumnsSelected(rule)"> </MultiSelect> -->
                    <WidgetEditorMultiselect :value="rule.target" :availableTargetOptions="availableTargetOptions" :widgetColumnsAliasMap="widgetColumnsAliasMap" optionLabel="alias" optionValue="id" :disabled="!headersModel.custom.enabled" @change="onColumnsSelected(rule)">
                    </WidgetEditorMultiselect>
                </div>
                <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.headers.action') }}</label>
                    <Dropdown class="kn-material-input" v-model="rule.action" :options="descriptor.customHeadersActionOptions" optionValue="value" :disabled="!headersModel.custom.enabled" @change="onHeadersRuleActionChanged(rule)">
                        <template #value="slotProps">
                            <div>
                                <span>{{ slotProps.value }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </div>
                        </template>
                    </Dropdown>
                </div>
                <div v-if="rule.action === 'setLabel'" class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.compareValueType') }}</label>
                    <Dropdown class="kn-material-input" v-model="rule.compareType" :options="descriptor.headersCompareValueType" optionValue="value" :disabled="!headersModel.custom.enabled" @change="headersConfigurationChanged">
                        <template #value="slotProps">
                            <div>
                                <span>{{ slotProps.value }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </div>
                        </template>
                    </Dropdown>
                </div>

                <div v-if="rule.action === 'setLabel'" class="p-d-flex p-flex-row kn-flex">
                    <div v-if="rule.compareType === 'static'" class="p-d-flex p-flex-column kn-flex p-mr-2">
                        <label class="kn-material-input-label p-mr-2">{{ $t('common.value') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="rule.value" :disabled="!headersModel.custom.enabled" @change="headersConfigurationChanged" />
                    </div>
                    <div v-else-if="rule.compareType === 'variable'" class="p-d-flex p-flex-column kn-flex p-m-2">
                        <label class="kn-material-input-label p-mr-2">{{ $t('common.variable') }}</label>
                        <Dropdown class="kn-material-input" v-model="rule.value" :options="variables" optionValue="value" optionLabel="name" :disabled="!headersModel.custom.enabled" @change="headersConfigurationChanged"> </Dropdown>
                    </div>
                    <div v-else-if="rule.compareType === 'parameter'" class="p-d-flex p-flex-column kn-flex p-m-2">
                        <label class="kn-material-input-label p-mr-2">{{ $t('common.parameter') }}</label>
                        <Dropdown class="kn-material-input" v-model="rule.value" :options="drivers" optionValue="value" optionLabel="name" :disabled="!headersModel.custom.enabled" @change="headersConfigurationChanged"> </Dropdown>
                    </div>
                </div>
                <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', !headersModel.custom.enabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addHeadersRule() : removeHeadersRule(index)"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetHeaders, ITableWidgetHeadersRule, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getColumnById } from '@/modules/documentExecution/dashboard/helpers/TableWidgetCompatibilityHelper'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'
import WidgetEditorMultiselect from '../../common/WidgetEditorMultiselect.vue'

export default defineComponent({
    name: 'table-widget-headers',
    components: { Dropdown, InputSwitch, MultiSelect, WidgetEditorMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, drivers: { type: Array }, variables: { type: Array } },
    data() {
        return {
            descriptor,
            headersModel: null as ITableWidgetHeaders | null,
            availableTargetOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any
        }
    },
    created() {
        this.setEventListeners()
        this.loadTargetOptions()
        this.loadHeadersModel()
        this.loadWidgetColumnAliasMap()
    },
    methods: {
        setEventListeners() {
            emitter.on('collumnRemoved', (column) => this.onColumnRemoved(column))
        },
        loadTargetOptions() {
            this.availableTargetOptions = [...this.widgetModel.columns]
        },
        loadHeadersModel() {
            if (this.widgetModel?.settings?.configuration) {
                this.headersModel = this.widgetModel.settings.configuration.headers
            }
            if (this.headersModel?.custom.enabled) this.removeColumnsFromTargetOptions()
        },
        loadWidgetColumnAliasMap() {
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
            })
        },
        removeColumnsFromTargetOptions() {
            if (!this.headersModel) return
            for (let i = 0; i < this.headersModel.custom.rules.length; i++) {
                for (let j = 0; j < this.headersModel.custom.rules[i].target.length; j++) {
                    const tempColumn = getColumnById(this.widgetModel, this.headersModel.custom.rules[i].target[j])
                    if (tempColumn) this.removeColumnFromAvailableTargetOptions(tempColumn)
                }
            }
        },
        removeColumnFromAvailableTargetOptions(tempColumn: IWidgetColumn) {
            console.log('TEMP COLUMN: ', tempColumn)
            const index = this.availableTargetOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            console.log('INDEX: ', index)
            if (index !== -1) this.availableTargetOptions.splice(index, 1)
        },
        headersConfigurationChanged() {
            emitter.emit('headersConfigurationChanged', this.headersModel)
        },
        onHeadersRuleActionChanged(rule: ITableWidgetHeadersRule) {
            if (rule.action === 'hide') {
                delete rule.value
            }
            this.headersConfigurationChanged()
        },
        onCustomHeadersEnabledChange() {
            if (!this.headersModel) return
            if (this.headersModel.custom.enabled && this.headersModel.custom.rules.length === 0) {
                this.headersModel.custom.rules.push({ target: [], action: '' })
            }
            this.headersConfigurationChanged()
        },
        getTargetOptions(rule: ITableWidgetHeadersRule) {
            const targetOptions = [] as (IWidgetColumn | { id: string; alias: string })[]
            rule.target.forEach((target: string) => {
                const tempColumn = { id: target, alias: this.widgetColumnsAliasMap[target] }
                if (tempColumn) targetOptions.push(tempColumn)
            })
            console.log('TARGET OPTIONS: ', targetOptions)
            return targetOptions.concat(this.availableTargetOptions)
        },
        onColumnsSelected(rule: ITableWidgetHeadersRule) {
            // rule.target.forEach((target: string) => {
            //     const index = this.availableTargetOptions.findIndex((targetOption: IWidgetColumn) => targetOption.id === target)
            //     if (index !== 1) this.availableTargetOptions.splice(index, 1)
            // })
        },
        addHeadersRule() {
            if (!this.headersModel) return
            this.headersModel.custom.rules.push({ target: [], action: '' })
            this.headersConfigurationChanged()
        },
        removeHeadersRule(index: number) {
            if (!this.headersModel) return
            this.headersModel.custom.rules.splice(index, 1)
            this.headersConfigurationChanged()
        },
        onColumnRemoved(column: IWidgetColumn) {
            if (!this.headersModel) return
            for (let i = this.headersModel.custom.rules.length - 1; i >= 0; i--) {
                for (let j = this.headersModel.custom.rules[i].target.length; j >= 0; j--) {
                    const tempTarget = this.headersModel.custom.rules[i].target[j]
                    if (column.id === tempTarget) this.headersModel.custom.rules[i].target.splice(j, 1)
                }
                if (this.headersModel.custom.rules[i].target.length === 0) this.headersModel.custom.rules.splice(i, 1)
            }
            this.headersConfigurationChanged()
        }
    }
})
</script>
