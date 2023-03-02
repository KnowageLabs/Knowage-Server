<template>
    <div v-if="responsiveModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-for="(field, index) in descriptor.rowTotals" :key="index" class="p-col-12 p-grid p-d-flex p-flex-row p-jc-start">
            <div class="p-sm-12 p-md-2">
                <InputSwitch v-model="responsiveModel[field]"></InputSwitch>
            </div>
            <div class="p-sm-12 p-md-10">
                <label class="kn-material-input-label">{{ getLabel(field) }}</label>
            </div>
        </div>
        <div class="p-col-12 p-d-flex p-flex-column p-pt-2">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.pivot.configuration.grandTotalLabel') }}</label>
            <InputText v-model="responsiveModel.grandTotalLabel" class="kn-material-input p-inputtext-sm" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IPivotRowsConfiguration } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from './PivotTableConfigDescriptor.json'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'widget-responsive',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            responsiveModel: null as IPivotRowsConfiguration | null
        }
    },
    created() {
        this.loadResponsiveModel()
    },
    methods: {
        loadResponsiveModel() {
            if (this.widgetModel.settings.configuration.rows) this.responsiveModel = this.widgetModel.settings.configuration.rows
        },
        getLabel(field: string) {
            switch (field) {
                case 'grandTotal':
                    return this.$t('dashboard.widgetEditor.pivot.configuration.grandTotal')
                case 'subTotal':
                    return this.$t('dashboard.widgetEditor.pivot.configuration.subtotal')
            }
        }
    }
})
</script>
