<template>
    <div v-if="exportModel">
        <div class="kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.export.exportPdf') }}</label>
            <InputSwitch v-model="exportModel.pdf.enabled" @change="onEnableExportChanged"></InputSwitch>
        </div>

        <div class="p-d-flex p-flex-row p-ai-center p-mt-2">
            <div class="field-radiobutton kn-flex p-mx-2">
                <RadioButton v-model="selectedExport" name="export" value="a4portrait" :disabled="!exportModel.pdf.enabled" @change="onSelectedExportChanged" />
                <label class="kn-material-input-label p-m-2"> {{ $t('dashboard.widgetEditor.export.a4portrait') }}</label>
            </div>

            <div class="field-radiobutton kn-flex p-mx-2">
                <RadioButton v-model="selectedExport" name="export" value="a4landscape" :disabled="!exportModel.pdf.enabled" @change="onSelectedExportChanged" />
                <label class="kn-material-input-label p-m-2"> {{ $t('dashboard.widgetEditor.export.a4landscape') }}</label>
            </div>

            <div class="p-d-flex p-flex-row p-ai-center kn-flex">
                <div class="field-radiobutton p-d-flex p-ai-center p-m-2">
                    <RadioButton v-model="selectedExport" name="export" value="custom" :disabled="!exportModel.pdf.enabled" @change="onSelectedExportChanged" />
                    <label class="kn-material-input-label p-m-2"> {{ $t('common.custom') }}</label>
                </div>

                <div class="p-d-flex p-flex-column p-mx-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.width') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="exportModel.pdf.custom.width" :disabled="!exportModel.pdf.enabled || selectedExport !== 'custom'" @change="exportConfigurationChanged" />
                </div>

                <div class="p-d-flex p-flex-column p-mx-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.height') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="exportModel.pdf.custom.height" :disabled="!exportModel.pdf.enabled || selectedExport !== 'custom'" @change="exportConfigurationChanged" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetExports } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'table-widget-export',
    components: { InputNumber, InputSwitch, RadioButton },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            exportModel: null as ITableWidgetExports | null,
            selectedExport: ''
        }
    },
    created() {
        this.loadExportModel()
    },
    methods: {
        loadExportModel() {
            if (this.widgetModel?.settings?.configuration) {
                this.exportModel = this.widgetModel.settings.configuration.exports
                this.setSelectedExport()
            }
        },
        setSelectedExport() {
            if (!this.exportModel) return
            if (this.exportModel.pdf.a4landscape) this.selectedExport = 'a4landscape'
            else if (this.exportModel.pdf.a4portrait) this.selectedExport = 'a4portrait'
            else if (this.exportModel.pdf.custom.enabled) this.selectedExport = 'custom'
        },
        exportConfigurationChanged() {
            emitter.emit('exportModelChanged', this.exportModel)
        },
        onEnableExportChanged() {
            this.exportConfigurationChanged()
        },
        onSelectedExportChanged() {
            if (!this.exportModel) return
            switch (this.selectedExport) {
                case 'a4landscape':
                    this.exportModel.pdf.a4landscape = true
                    this.exportModel.pdf.a4portrait = false
                    this.exportModel.pdf.custom.enabled = false
                    break
                case 'a4portrait':
                    this.exportModel.pdf.a4portrait = true
                    this.exportModel.pdf.a4landscape = false
                    this.exportModel.pdf.custom.enabled = false
                    break
                case 'custom':
                    this.exportModel.pdf.custom.enabled = true
                    this.exportModel.pdf.a4portrait = false
                    this.exportModel.pdf.a4landscape = false
            }
            this.exportConfigurationChanged()
        }
    }
})
</script>
