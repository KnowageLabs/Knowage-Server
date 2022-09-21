<template>
    <div v-if="headersModel" class="p-grid p-ai-center p-p-4">
        <div id="input-switches-container" class="p-grid p-col-12 p-p-3">
            <div class="p-col-12 p-md-6 p-p-2">
                <InputSwitch v-model="headersModel.enabled" @change="headersConfigurationChanged"></InputSwitch>
                <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.headers.enableHeader') }}</label>
            </div>
            <div class="p-col-12 p-md-6 p-p-2">
                <InputSwitch v-model="headersModel.enabledMultiline" :disabled="headersDisabled" @change="headersConfigurationChanged"></InputSwitch>
                <label class="kn-material-input-label p-ml-3"> {{ $t('dashboard.widgetEditor.headers.enableMultiline') }}</label>
            </div>
        </div>

        <div class="p-col-12 p-p-3">
            <div class="p-col-12 p-p-2">
                <InputSwitch v-model="headersModel.custom.enabled" @change="onCustomHeadersEnabledChange"></InputSwitch>
                <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.headers.enableCustomHeaders') }}</label>
            </div>

            <div v-for="(rule, index) in headersModel.custom.rules" :key="index" class="p-grid p-ai-center p-pt-2">
                <div class="p-col-12 p-sm-12 p-md-3 p-d-flex p-flex-column p-pt-1">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <WidgetEditorColumnsMultiselect :value="rule.target" :availableTargetOptions="availableTargetOptions" :widgetColumnsAliasMap="widgetColumnsAliasMap" optionLabel="alias" optionValue="id" :disabled="headersCustomDisabled" @change="onColumnsSelected($event, rule)">
                    </WidgetEditorColumnsMultiselect>
                </div>
                <div class="p-col-11 p-sm-11 p-md-8 p-grid">
                    <div class="p-col-12 p-sm-12 p-md-4 kn-flex p-d-flex p-flex-column p-p-2">
                        <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.headers.action') }}</label>
                        <Dropdown class="kn-material-input" v-model="rule.action" :options="descriptor.customHeadersActionOptions" optionValue="value" :disabled="headersCustomDisabled" @change="onHeadersRuleActionChanged(rule)">
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
                    <div v-if="rule.action === 'setLabel'" class="p-col-12 p-sm-12 p-md-4 p-d-flex p-flex-column">
                        <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.compareValueType') }}</label>
                        <Dropdown class="kn-material-input" v-model="rule.compareType" :options="descriptor.headersCompareValueType" optionValue="value" :disabled="headersCustomDisabled" @change="onCompareValueTypeChanged(rule)">
                            <template #value="slotProps">
                                <div>
                                    <span>{{ getTranslatedLabel(slotProps.value, descriptor.headersCompareValueType, $t) }}</span>
                                </div>
                            </template>
                            <template #option="slotProps">
                                <div>
                                    <span>{{ $t(slotProps.option.label) }}</span>
                                </div>
                            </template>
                        </Dropdown>
                    </div>
                    <div v-if="rule.action === 'setLabel'" class="p-col-12 p-sm-12 p-md-4 p-d-flex p-flex-row p-ai-center">
                        <div v-if="rule.compareType === 'static'" class="p-d-flex p-flex-column kn-flex">
                            <label class="kn-material-input-label p-mr-2">{{ $t('common.value') }}</label>
                            <InputText class="kn-material-input p-inputtext-sm" v-model="rule.value" :disabled="headersCustomDisabled" @change="headersConfigurationChanged" />
                        </div>
                        <div v-else-if="rule.compareType === 'variable'" class="p-d-flex p-flex-column kn-flex">
                            <label class="kn-material-input-label p-mr-2">{{ $t('common.variable') }}</label>
                            <Dropdown class="kn-material-input" v-model="rule.variable" :options="variables" optionValue="name" optionLabel="name" :disabled="headersCustomDisabled" @change="onVariableChanged(rule)"> </Dropdown>
                        </div>
                        <div v-else-if="rule.compareType === 'parameter'" class="p-d-flex p-flex-column kn-flex">
                            <label class="kn-material-input-label p-mr-2">{{ $t('common.parameter') }}</label>
                            <Dropdown class="kn-material-input" v-model="rule.parameter" :options="drivers" optionValue="name" optionLabel="name" :disabled="headersCustomDisabled" @change="onDriverChanged(rule)"> </Dropdown>
                        </div>
                    </div>
                </div>
                <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                    <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', headersCustomDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addHeadersRule() : removeHeadersRule(index)"></i>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetHeaders, ITableWidgetHeadersRule, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'

export default defineComponent({
    name: 'table-widget-headers',
    components: { Dropdown, InputSwitch, WidgetEditorColumnsMultiselect },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        drivers: { type: Array },
        variables: { type: Array }
    },
    data() {
        return {
            descriptor,
            headersModel: null as ITableWidgetHeaders | null,
            availableTargetOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any,
            parameterValuesMap: {},
            variableValuesMap: {},
            getTranslatedLabel
        }
    },
    computed: {
        headersDisabled() {
            return !this.headersModel || !this.headersModel.enabled
        },
        headersCustomDisabled() {
            return !this.headersModel || !this.headersModel.custom.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadTargetOptions()
        this.loadHeadersModel()
        this.loadWidgetColumnAliasMap()
        this.loadParameterValuesMap()
        this.loadVariableValuesMap()
    },
    methods: {
        setEventListeners() {
            emitter.on('headersColumnRemoved', () => this.onColumnRemoved())
            emitter.on('columnAliasRenamed', (column) => this.onColumnAliasRenamed(column))
            emitter.on('columnAdded', (column) => this.onColumnAdded(column))
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
        removeColumnsFromTargetOptions() {
            if (!this.headersModel) return
            for (let i = 0; i < this.headersModel.custom.rules.length; i++) {
                for (let j = 0; j < this.headersModel.custom.rules[i].target.length; j++) {
                    this.removeColumnFromAvailableTargetOptions({
                        id: this.headersModel.custom.rules[i].target[j],
                        alias: this.widgetColumnsAliasMap[this.headersModel.custom.rules[i].target[j]]
                    })
                }
            }
        },
        removeColumnFromAvailableTargetOptions(tempColumn: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableTargetOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            if (index !== -1) this.availableTargetOptions.splice(index, 1)
        },
        loadWidgetColumnAliasMap() {
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
            })
        },
        loadParameterValuesMap() {
            if (!this.drivers) return
            this.drivers.forEach((driver: any) => (this.parameterValuesMap[driver.name] = driver.value))
        },
        loadVariableValuesMap() {
            if (!this.variables) return
            this.variables.forEach((variables: any) => (this.variableValuesMap[variables.name] = variables.value))
        },
        onDriverChanged(rule: ITableWidgetHeadersRule) {
            const temp = rule.parameter
            if (temp) rule.value = this.parameterValuesMap[temp]
            this.headersConfigurationChanged()
        },
        onVariableChanged(rule: ITableWidgetHeadersRule) {
            const temp = rule.variable
            if (temp) rule.value = this.variableValuesMap[temp]
            this.headersConfigurationChanged()
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
        onCompareValueTypeChanged(rule: ITableWidgetHeadersRule) {
            rule.value = ''
            switch (rule.compareType) {
                case 'static':
                    delete rule.parameter
                    delete rule.variable
                    break
                case 'parameter':
                    delete rule.variable
                    break
                case 'variable':
                    delete rule.parameter
            }
            this.headersConfigurationChanged()
        },
        onColumnsSelected(event: any, rule: ITableWidgetHeadersRule) {
            const intersection = rule.target.filter((el: string) => !event.value.includes(el))
            rule.target = event.value
            intersection.length > 0 ? this.onColumnsRemovedFromMultiselect(intersection) : this.onColumnsAddedFromMultiselect(rule)
            this.headersConfigurationChanged()
        },
        onColumnsRemovedFromMultiselect(intersection: string[]) {
            intersection.forEach((el: string) =>
                this.availableTargetOptions.push({
                    id: el,
                    alias: this.widgetColumnsAliasMap[el]
                })
            )
        },
        onColumnsAddedFromMultiselect(rule: ITableWidgetHeadersRule) {
            rule.target.forEach((target: string) => {
                const index = this.availableTargetOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableTargetOptions.splice(index, 1)
            })
        },
        addHeadersRule() {
            if (!this.headersModel) return
            this.headersModel.custom.rules.push({ target: [], action: '' })
            this.headersConfigurationChanged()
        },
        removeHeadersRule(index: number) {
            if (!this.headersModel) return
            this.headersModel.custom.rules[index].target.forEach((target: string) =>
                this.availableTargetOptions.push({
                    id: target,
                    alias: this.widgetColumnsAliasMap[target]
                })
            )
            this.headersModel.custom.rules.splice(index, 1)
            this.headersConfigurationChanged()
        },
        onColumnRemoved() {
            this.loadHeadersModel()
            this.loadTargetOptions()
            this.headersConfigurationChanged()
        },
        onColumnAliasRenamed(column: IWidgetColumn) {
            if (!this.headersModel) return
            if (column.id && this.widgetColumnsAliasMap[column.id]) this.widgetColumnsAliasMap[column.id] = column.alias

            const index = this.availableTargetOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === column.id)
            if (index !== -1) this.availableTargetOptions[index].alias = column.alias
            this.headersConfigurationChanged()
        },
        onColumnAdded(column: IWidgetColumn) {
            this.availableTargetOptions.push(column)
            if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
        }
    }
})
</script>

<style lang="scss" scoped>
#input-switches-container {
    border-bottom: 1px solid #c2c2c2;
}
</style>
