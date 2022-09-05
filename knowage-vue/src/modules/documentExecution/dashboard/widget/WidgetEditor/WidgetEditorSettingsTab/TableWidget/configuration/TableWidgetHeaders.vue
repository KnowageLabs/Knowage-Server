<template>
    <div v-if="headersModel">
        {{ headersModel }}
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
                    <MultiSelect v-model="rule.target" :options="widgetModel.columns" optionLabel="alias" optionValue="id" :disabled="!headersModel.custom.enabled" @change="headersConfigurationChanged"> </MultiSelect>
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
                    <Dropdown class="kn-material-input" v-model="rule.compareType" :options="descriptor.headersCompareValueType" optionValue="value" :disabled="!headersModel.custom.enabled" @change="onHeadersCompareValueChanged(rule)">
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
                        <Dropdown class="kn-material-input" v-model="rule.variable" :options="variables" optionValue="value" optionLabel="name" :disabled="!headersModel.custom.enabled" @change="headersConfigurationChanged"> </Dropdown>
                    </div>
                    <div v-else-if="rule.compareType === 'parameter'" class="p-d-flex p-flex-column kn-flex p-m-2">
                        <label class="kn-material-input-label p-mr-2">{{ $t('common.parameter') }}</label>
                        <Dropdown class="kn-material-input" v-model="rule.parameter" :options="drivers" optionValue="value" optionLabel="name" :disabled="!headersModel.custom.enabled" @change="headersConfigurationChanged"> </Dropdown>
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
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-headers',
    components: { Dropdown, InputSwitch, MultiSelect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, drivers: { type: Array }, variables: { type: Array } },
    data() {
        return {
            descriptor,
            headersModel: null as ITableWidgetHeaders | null,
            targetOptions: [] as string[]
        }
    },
    created() {
        this.setEventListeners()
        this.loadHeadersModel()
    },
    methods: {
        setEventListeners() {
            emitter.on('collumnRemoved', (column) => this.onColumnRemoved(column))
        },
        loadHeadersModel() {
            if (this.widgetModel?.settings?.configuration) this.headersModel = this.widgetModel.settings.configuration.headers
        },
        headersConfigurationChanged() {
            emitter.emit('headersConfigurationChanged', this.headersModel)
        },
        onHeadersRuleActionChanged(rule: ITableWidgetHeadersRule) {
            if (rule.action === 'hide') {
                delete rule.variable
            }
            this.headersConfigurationChanged()
        },
        onHeadersCompareValueChanged(rule: ITableWidgetHeadersRule) {
            switch (rule.compareType) {
                case 'static':
                    ;['variable', 'parameter'].forEach((field: string) => delete rule[field])
                    rule.value = ''
                    break
                case 'variable':
                    ;['static', 'parameter'].forEach((field: string) => delete rule[field])
                    rule.variable = ''
                    break
                case 'parameter':
                    ;['static', 'variable'].forEach((field: string) => delete rule[field])
                    rule.parameter = ''
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
        }
    }
})
</script>
