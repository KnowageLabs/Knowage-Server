<template>
    <div v-if="exportModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-if="widgetType === 'table' && exportModel.pdf" class="p-col-12 p-p-2 p-grid">
            <div class="p-col-2 p-sm-12 p-md-2">
                <InputSwitch v-model="exportModel.pdf.enabled" @change="onEnableExportChanged"></InputSwitch>
            </div>
            <div class="p-col-11 p-sm-12 p-md-10">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.export.exportPdf') }}</label>
            </div>
        </div>
        <div v-if="widgetType === 'table'" class="p-grid p-col-12 p-ai-center">
            <div class="p-grid p-col-12 p-ai-center p-pt-4">
                <div class="p-col-6 field-radiobutton p-px-2">
                    <RadioButton v-model="selectedExport" name="export" value="a4portrait" :disabled="pdfExportDisabled" @change="onSelectedExportChanged" />
                    <label class="kn-material-input-label p-m-2"> {{ $t('dashboard.widgetEditor.export.a4portrait') }}</label>
                </div>

                <div class="p-col-6 field-radiobutton p-px-2">
                    <RadioButton v-model="selectedExport" name="export" value="a4landscape" :disabled="pdfExportDisabled" @change="onSelectedExportChanged" />
                    <label class="kn-material-input-label p-m-2"> {{ $t('dashboard.widgetEditor.export.a4landscape') }}</label>
                </div>
            </div>
            <div class="p-col-12 p-lg-9 p-grid p-ai-center">
                <div class="p-col-12 p-md-12 p-lg-3 field-radiobutton p-d-flex p-ai-center p-pt-4">
                    <RadioButton v-model="selectedExport" name="export" value="custom" :disabled="pdfExportDisabled" @change="onSelectedExportChanged" />
                    <label class="kn-material-input-label p-m-2"> {{ $t('common.custom') }}</label>
                </div>

                <div v-if="exportModel.pdf" class="p-col-12 p-md-6 p-lg-5 p-d-flex p-flex-column p-px-4">
                    <label class="kn-material-input-label">{{ $t('common.width') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm export-number-input" v-model="exportModel.pdf.custom.width" :disabled="pdfExportDisabled || selectedExport !== 'custom'" @blur="exportConfigurationChanged" />
                </div>

                <div v-if="exportModel.pdf" class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column p-px-4">
                    <label class="kn-material-input-label">{{ $t('common.height') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm export-number-input" v-model="exportModel.pdf.custom.height" :disabled="pdfExportDisabled || selectedExport !== 'custom'" @blur="exportConfigurationChanged" />
                </div>
            </div>
        </div>
        <div class="p-grid p-col-12">
            <div v-if="widgetType === 'table' || widgetType === 'html' || widgetType === 'text'" class="p-col-12 p-lg-6">
                <InputSwitch v-model="exportModel.showScreenshot" @change="onEnableExportChanged"></InputSwitch>
                <label class="kn-material-input-label p-ml-4">{{ $t('dashboard.widgetEditor.export.enableScreenshots') }}</label>
            </div>
            <div class="p-col-12 p-lg-6">
                <InputSwitch v-model="exportModel.showExcelExport" @change="onEnableExportChanged"></InputSwitch>
                <label class="kn-material-input-label p-ml-4">{{ $t('dashboard.widgetEditor.export.showExcelExport') }}</label>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetExports } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'table-widget-export',
    components: { InputNumber, InputSwitch, RadioButton },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            exportModel: null as IWidgetExports | null,
            selectedExport: '',
            widgetType: '' as string
        }
    },
    computed: {
        pdfExportDisabled() {
            return !this.exportModel || (this.exportModel.pdf && !this.exportModel.pdf.enabled)
        }
    },
    created() {
        this.loadExportModel()
    },
    methods: {
        loadExportModel() {
            if (!this.widgetModel) return
            this.widgetType = this.widgetModel.type
            if (this.widgetModel.settings?.configuration) {
                this.exportModel = this.widgetModel.settings.configuration.exports
                this.setSelectedExport()
            }
        },
        setSelectedExport() {
            if (!this.exportModel || this.widgetType !== 'table' || !this.exportModel.pdf) return
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
            if (!this.exportModel || !this.exportModel.pdf) return
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

<style lang="scss" scoped>
.export-number-input {
    max-width: 100px;
}
</style>
