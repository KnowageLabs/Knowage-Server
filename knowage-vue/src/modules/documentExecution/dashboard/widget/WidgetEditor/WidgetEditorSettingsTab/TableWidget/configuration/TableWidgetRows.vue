<template>
    <div v-if="rowsModel">
        <div class="p-d-flex p-flex-row p-ai-center p-m-3">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.rows.enableIndexColumn') }}</label>
                <InputSwitch v-model="rowsModel.indexColumn" @change="onIndexColumnChanged"></InputSwitch>
            </div>
            <i class="pi pi-question-circle kn-cursor-pointer p-ml-auto p-m-2" v-tooltip.top="$t('dashboard.widgetEditor.rows.indexColumnHint')"></i>
        </div>

        <hr />

        <div class="p-d-flex p-flex-row p-ai-center p-m-3">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.rows.enableRowspan') }}</label>
                <InputSwitch v-model="rowsModel.rowSpan.enabled" @change="onRowSpanChanged"></InputSwitch>
            </div>
            <i class="pi pi-question-circle kn-cursor-pointer p-ml-auto p-m-2" v-tooltip.top="$t('dashboard.widgetEditor.rows.rowSpanHint')"></i>
        </div>

        <div class="p-d-flex p-flex-column p-m-3">
            <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.rows.rowSpanColumns') }}</label>
            <MultiSelect v-model="rowsModel.rowSpan.columns" :options="widgetModel.columns" optionLabel="alias" optionValue="id" :disabled="!rowsModel.rowSpan.enabled" @change="onRowSpanChanged"> </MultiSelect>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetRows, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-rows',
    components: { InputSwitch, MultiSelect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            rowsModel: null as ITableWidgetRows | null
        }
    },
    created() {
        this.setEventListeners()
        this.loadRowsModel()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemoved', (column) => this.onColumnRemoved(column))
        },
        loadRowsModel() {
            if (this.widgetModel?.settings?.configuration) this.rowsModel = this.widgetModel.settings.configuration.rows
        },
        onIndexColumnChanged() {
            emitter.emit('indexColumnChanged', this.rowsModel)
        },
        onRowSpanChanged() {
            emitter.emit('rowSpanChanged', this.rowsModel)
        },
        onColumnRemoved(column: IWidgetColumn) {
            console.log('ON COLUMN REMOVED: ', column)
            if (!this.rowsModel) return
            const index = this.rowsModel.rowSpan.columns.findIndex((id: string) => column.id)
            if (index !== -1) {
                this.rowsModel?.rowSpan.columns.splice(index, 1)
                this.onRowSpanChanged()
            }
        }
    }
})
</script>
