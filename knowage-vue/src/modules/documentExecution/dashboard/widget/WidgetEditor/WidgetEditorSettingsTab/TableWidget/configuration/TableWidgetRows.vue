<template>
    <div v-if="rowsModel" class="p-grid p-ai-center p-p-4">
        <div id="index-column-switch" class="p-col-12 p-grid p-p-3">
            <div class="p-col-3 p-sm-12 p-md-3">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.rows.enableIndexColumn') }}</label>
            </div>
            <div class="p-col-9 p-sm-12 p-md-9">
                <InputSwitch v-model="rowsModel.indexColumn" @change="onIndexColumnChanged"></InputSwitch>
            </div>
        </div>

        <div class="p-col-12 p-grid p-px-3 p-py-4">
            <div class="p-col-3 p-sm-12 p-md-3">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.rows.enableRowspan') }}</label>
            </div>
            <div class="p-col-9 p-sm-12 p-md-9">
                <InputSwitch v-model="rowsModel.rowSpan.enabled" @change="onRowSpanChanged"></InputSwitch>
            </div>
        </div>

        <div class="p-col-12 p-fluid p-d-flex p-flex-column p-px-3 p-py-2">
            <label class="kn-material-input-label p-mb-1"> {{ $t('dashboard.widgetEditor.rows.rowSpanColumn') }}</label>
            <Dropdown class="kn-material-input" v-model="rowsModel.rowSpan.column" :options="widgetModel.columns" optionLabel="alias" optionValue="id" :disabled="!rowsModel.rowSpan.enabled" @change="onRowSpanChanged"> </Dropdown>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetRows } from '@/modules/documentExecution/Dashboard/Dashboard'
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
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromRows', () => this.onColumnRemoved())
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
