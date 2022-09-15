<template>
    <div v-if="responsiveModel" id="input-switches-container">
        <div v-for="(field, index) in descriptor.responsiveInputSwiitches" :key="index" class="p-d-flex p-flex-row p-jc-start kn-flex p-m-4">
            <InputSwitch v-model="responsiveModel[field]"></InputSwitch>
            <label class="kn-material-input-label p-ml-auto">{{ getLabel(field) }}</label>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetResponsive } from '@/modules/documentExecution/Dashboard/Dashboard'
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

<style lang="scss" scoped>
#input-switches-container {
    width: 30%;
}
</style>
