<template>
    <div v-if="rowsStyleModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-4 p-md-4 p-lg-4 p-d-flex p-flex-column p-pb-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.height') }}</label>
            <InputNumber class="kn-material-input p-inputtext-sm" v-model="rowsStyleModel.height" @blur="rowsStyleChanged" />
        </div>
        <div class="p-col-8"></div>
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.rows.enabledAlternatedRows') }}</label>
            <InputSwitch v-model="rowsStyleModel.alternatedRows.enabled" @change="rowsStyleChanged"></InputSwitch>
        </div>
        <div class="p-col-12 p-grid p-ai-center p-p-0">
            <div class="p-col-12 p-md-6 p-px-2">
                <WidgetEditorColorPicker :initialValue="rowsStyleModel.alternatedRows.evenBackgroundColor" :label="$t('dashboard.widgetEditor.rows.alternatedRowsEven')" :disabled="!rowsStyleModel.alternatedRows.enabled" @change="onBackroundColorChanged($event, 'even')"></WidgetEditorColorPicker>
            </div>
            <div class="p-col-12 p-md-6 p-px-2">
                <WidgetEditorColorPicker :initialValue="rowsStyleModel.alternatedRows.oddBackgroundColor" :label="$t('dashboard.widgetEditor.rows.alternatedRowsOdd')" :disabled="!rowsStyleModel.alternatedRows.enabled" @change="onBackroundColorChanged($event, 'odd')"></WidgetEditorColorPicker>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetRowsStyle } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorColorPicker from '../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'widget-rows-style',
    components: { InputNumber, InputSwitch, WidgetEditorColorPicker },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            rowsStyleModel: null as IWidgetRowsStyle | null,
            widgetType: '' as string
        }
    },
    created() {
        this.loadRowsModel()
    },
    methods: {
        loadRowsModel() {
            if (!this.widgetModel) return
            this.widgetType = this.widgetModel.type
            if (this.widgetModel.settings?.style?.rows) this.rowsStyleModel = this.widgetModel.settings.style.rows
        },
        rowsStyleChanged() {
            emitter.emit('rowsStyleChanged', this.rowsStyleModel)
            switch (this.widgetType) {
                case 'table':
                    emitter.emit('refreshTable', this.widgetModel.id)
                    break
                case 'selection':
                    emitter.emit('refreshSelection', this.widgetModel.id)
            }
        },
        onBackroundColorChanged(event: string | null, type: 'even' | 'odd') {
            if (!event || !this.rowsStyleModel) return
            type === 'even' ? (this.rowsStyleModel.alternatedRows.evenBackgroundColor = event) : (this.rowsStyleModel.alternatedRows.oddBackgroundColor = event)
            this.rowsStyleChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
#height-input-container {
    max-width: 200px;
}
</style>
