<template>
    <div v-if="summaryRowsModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-grid">
            <div class="p-col-12 p-text-left p-text-md-left p-pr-4">
                <label class="kn-material-input-label p-mr-3"> {{ $t('dashboard.widgetEditor.summaryRows.pinnedColumnsOnly') }}</label>
                <Checkbox v-model="summaryRowsModel.style.pinnedOnly" :binary="true" :disabled="summaryRowsDiabled" @change="summaryRowsChanged" />
            </div>
        </div>

        <div class="p-col-12">
            <div v-for="(summaryRow, index) in summaryRowsModel.list" :key="index" class="p-grid p-ai-center">
                <div class="p-col-12 p-md-4 p-d-flex p-flex-column p-pt-1">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.label') }}</label>
                    <InputText v-model="summaryRow.label" class="kn-material-input p-inputtext-sm" :disabled="summaryRowsDiabled" @change="summaryRowsChanged" />
                </div>
                <div class="p-col-12 p-md-8 p-grid p-p-2">
                    <div class="p-col-10 p-d-flex p-flex-column">
                        <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.aggregation') }}</label>
                        <Dropdown v-model="summaryRow.aggregation" class="kn-material-input" :options="getAggregationOptions(index)" option-value="value" option-label="label" :disabled="index === 0 || !summaryRowsModel.enabled" @change="summaryRowsChanged"> </Dropdown>
                    </div>
                    <div class="p-col-2 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-3">
                        <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', summaryRowsDiabled ? 'icon-disabled' : '']" class="kn-cursor-pointer" @click="index === 0 ? addSummaryRow() : removeSummaryRow(index)"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetSummaryRows } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'table-widget-summary-rows',
    components: { Checkbox, Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            summaryRowsModel: null as ITableWidgetSummaryRows | null
        }
    },
    computed: {
        summaryRowsDiabled() {
            return !this.summaryRowsModel || !this.summaryRowsModel.enabled
        }
    },
    watch: {
        summaryRowsDiabled() {
            this.onSummarRowEnabledChange()
        }
    },
    created() {
        this.loadSummaryRowsModel()
    },
    methods: {
        loadSummaryRowsModel() {
            if (this.widgetModel?.settings?.configuration) this.summaryRowsModel = this.widgetModel.settings.configuration.summaryRows
        },
        summaryRowsChanged() {
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        onSummarRowEnabledChange() {
            if (!this.summaryRowsModel) return
            if (this.summaryRowsModel.enabled && this.summaryRowsModel.list.length === 0) {
                this.summaryRowsModel.list.push({ label: '', aggregation: 'Columns Default Aggregation' })
            }
            this.summaryRowsChanged()
        },
        getAggregationOptions(index: number) {
            return index === 0 ? [{ value: 'Columns Default Aggregation', label: 'Columns Default Aggregation' }] : this.descriptor.aggregationOptions
        },
        addSummaryRow() {
            if (!this.summaryRowsModel || this.summaryRowsDiabled) return
            this.summaryRowsModel.list.push({ label: '', aggregation: '' })
            this.summaryRowsChanged()
        },
        removeSummaryRow(index: number) {
            if (!this.summaryRowsModel || this.summaryRowsDiabled) return
            this.summaryRowsModel.list.splice(index, 1)
            this.summaryRowsChanged()
        }
    }
})
</script>
