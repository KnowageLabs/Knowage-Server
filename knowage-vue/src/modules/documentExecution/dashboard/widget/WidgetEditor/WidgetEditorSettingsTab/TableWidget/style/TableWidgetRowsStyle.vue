<template>
    <div v-if="rowsStyleModel">
        <div class="p-d-flex p-flex-row p-ai-center">
            <div class="p-d-flex p-flex-column kn-flex p-mb-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('common.height') }}</label>
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="rowsStyleModel.height" @input="rowsStyleChanged" />
            </div>
            <div class="p-d-flex p-flex-row p-jc-end kn-flex p-mx-4">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.rows.multiselectable') }}</label>
                <InputSwitch v-model="rowsStyleModel.multiselectable" @change="rowsStyleChanged"></InputSwitch>
            </div>
            <div class="kn-flex">
                <WidgetEditorColorPicker :initialValue="rowsStyleModel.selectionColor" :label="$t('dashboard.widgetEditor.rows.selectionColor')" :disabled="!rowsStyleModel.multiselectable" @change="onSelectionColorChanged"></WidgetEditorColorPicker>
            </div>
        </div>
        <div class="p-d-flex p-flex-row p-ai-center p-m-4">
            <div class="p-d-flex p-flex-row kn-flex">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.rows.enabledAlternatedRows') }}</label>
                <InputSwitch v-model="rowsStyleModel.alternatedRows.enabled" @change="rowsStyleChanged"></InputSwitch>
            </div>
        </div>
        <div class="p-d-flex p-flex-row p-ai-center">
            <div class="kn-flex p-mx-2">
                <WidgetEditorColorPicker :initialValue="rowsStyleModel.alternatedRows.evenBackgroundColor" :label="$t('dashboard.widgetEditor.rows.alternatedRowsEven')" :disabled="!rowsStyleModel.alternatedRows.enabled" @change="onBackroundColorChanged($event, 'even')"></WidgetEditorColorPicker>
            </div>
            <div class="kn-flex p-mx-2">
                <WidgetEditorColorPicker :initialValue="rowsStyleModel.alternatedRows.oddBackgroundColor" :label="$t('dashboard.widgetEditor.rows.alternatedRowsOdd')" :disabled="!rowsStyleModel.alternatedRows.enabled" @change="onBackroundColorChanged($event, 'odd')"></WidgetEditorColorPicker>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetRowsStyle } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorColorPicker from '../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'table-widget-rows-style',
    components: { InputNumber, InputSwitch, WidgetEditorColorPicker },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            descriptor,
            rowsStyleModel: null as ITableWidgetRowsStyle | null
        }
    },
    created() {
        this.loadRowsModel()
    },
    methods: {
        loadRowsModel() {
            if (this.widgetModel?.settings?.style?.rows) this.rowsStyleModel = this.widgetModel.settings.style.rows
        },
        rowsStyleChanged() {
            emitter.emit('rowsStyleChanged', this.rowsStyleModel)
        },
        onSelectionColorChanged(event: string | null) {
            if (!event || !this.rowsStyleModel) return
            this.rowsStyleModel.selectionColor = event
            this.rowsStyleChanged()
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
