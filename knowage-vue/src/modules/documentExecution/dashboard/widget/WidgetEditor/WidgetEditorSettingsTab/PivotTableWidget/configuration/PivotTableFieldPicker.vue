<template>
    <div v-if="responsiveModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-6 p-d-flex p-flex-column p-pt-2">
            <label class="kn-material-input-label">{{ $t('common.width') }}</label>
            <InputNumber v-model="responsiveModel.width" :disabled="!responsiveModel.enabled" class="kn-material-input p-inputtext-sm" />
        </div>
        <div class="p-col-6 p-d-flex p-flex-column p-pt-2">
            <label class="kn-material-input-label">{{ $t('common.height') }}</label>
            <InputNumber v-model="responsiveModel.height" :disabled="!responsiveModel.enabled" class="kn-material-input p-inputtext-sm" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IPivotFieldPicker } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from './PivotTableConfigDescriptor.json'
import InputNumber from 'primevue/inputnumber'

export default defineComponent({
    name: 'widget-responsive',
    components: { InputNumber },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            responsiveModel: null as IPivotFieldPicker | null
        }
    },
    created() {
        this.loadResponsiveModel()
    },
    methods: {
        loadResponsiveModel() {
            if (this.widgetModel.settings.configuration.fieldPicker) this.responsiveModel = this.widgetModel.settings.configuration.fieldPicker
        }
    }
})
</script>
