<template>
    <div v-if="rowsModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div id="index-column-switch" class="p-col-12 p-grid">
            <div class="p-col-2 p-sm-12 p-md-2">
                <InputSwitch v-model="rowsModel.indexColumn" @change="onIndexColumnChanged"></InputSwitch>
            </div>
            <div class="p-col-11 p-sm-12 p-md-10">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.rows.enableIndexColumn') }}</label>
            </div>
        </div>

        <div class="p-col-12 p-grid p-mt-1">
            <div class="p-col-2 p-sm-12 p-md-2">
                <InputSwitch v-model="rowsModel.rowSpan.enabled" @change="onRowSpanChanged"></InputSwitch>
            </div>
            <div class="p-col-10 p-sm-12 p-md-10">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.rows.enableRowspan') }}</label>
            </div>
        </div>

        <div class="p-col-12 p-fluid p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mb-1"> {{ $t('dashboard.widgetEditor.rows.rowSpanColumn') }}</label>
            <Dropdown v-model="rowsModel.rowSpan.column" class="kn-material-input" :options="widgetModel.columns" option-label="alias" option-value="id" :disabled="!rowsModel.rowSpan.enabled" @change="onRowSpanChanged"> </Dropdown>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetRows } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'table-widget-rows',
    components: { InputSwitch, Dropdown },
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
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromRows', this.onColumnRemovedFromRows)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromRows', this.onColumnRemovedFromRows)
        },
        onColumnRemovedFromRows() {
            this.onColumnRemoved()
        },
        loadRowsModel() {
            if (this.widgetModel?.settings?.configuration?.rows) this.rowsModel = this.widgetModel.settings.configuration.rows
        },
        onIndexColumnChanged() {
            emitter.emit('indexColumnChanged', this.rowsModel)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onRowSpanChanged() {
            emitter.emit('rowSpanChanged', this.rowsModel)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onColumnRemoved() {
            this.loadRowsModel()
            this.onRowSpanChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
#index-column-switch {
    border-bottom: 1px solid #c2c2c2;
}
</style>
