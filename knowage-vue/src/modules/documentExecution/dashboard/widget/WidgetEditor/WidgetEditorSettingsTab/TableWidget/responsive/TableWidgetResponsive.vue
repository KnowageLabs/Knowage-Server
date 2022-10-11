<template>
    <div v-if="responsiveModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-for="(field, index) in descriptor.responsiveInputSwiitches" :key="index" class="p-col-12 p-grid p-d-flex p-flex-row p-jc-start p-p-4">
            <div class="p-sm-12 p-md-2">
                <InputSwitch v-model="responsiveModel[field]"></InputSwitch>
            </div>
            <div class="p-sm-12 p-md-5">
                <label class="kn-material-input-label p-ml-auto">{{ getLabel(field) }}</label>
            </div>
            <div class="p-md-5"></div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetResponsive } from '@/modules/documentExecution/dashboard/Dashboard'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-responsive',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            responsiveModel: null as ITableWidgetResponsive | null
        }
    },
    created() {
        this.loadResponsiveModel()
    },
    methods: {
        loadResponsiveModel() {
            if (this.widgetModel.settings?.responsive) this.responsiveModel = this.widgetModel.settings.responsive
        },
        getLabel(field: string) {
            switch (field) {
                case 'xs':
                    return this.$t('dashboard.widgetEditor.responsive.extraSmallDevices')
                case 'sm':
                    return this.$t('dashboard.widgetEditor.responsive.smallerDevices')
                case 'md':
                    return this.$t('dashboard.widgetEditor.responsive.tablets')
                case 'lg':
                    return this.$t('dashboard.widgetEditor.responsive.largeDevices')
                case 'xl':
                    return this.$t('dashboard.widgetEditor.responsive.extraLargeDevices')
            }
        }
    }
})
</script>
