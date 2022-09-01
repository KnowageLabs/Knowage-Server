<template>
    <div v-if="headersModel">
        {{ headersModel }}
        <div class="p-d-flex p-flex-row p-ai-center p-m-3">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.headers.enableHeader') }}</label>
                <InputSwitch v-model="headersModel.enabled" @change="headersConfigurationChanged"></InputSwitch>
            </div>
            <div class="p-ml-auto p-mr-2">
                <label class="kn-material-input-label p-mr-2"> {{ $t('dashboard.widgetEditor.headers.enableMultiline') }}</label>
                <InputSwitch v-model="headersModel.enabledMultiline" :disabled="!headersModel.enabled" @change="headersConfigurationChanged"></InputSwitch>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetHeaders } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-headers',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            headersModel: null as ITableWidgetHeaders | null
        }
    },
    created() {
        this.loadHeadersModel()
    },
    methods: {
        loadHeadersModel() {
            if (this.widgetModel?.settings?.configuration) this.headersModel = this.widgetModel.settings.configuration.headers
        },
        headersConfigurationChanged() {
            emitter.emit('headersConfigurationChanged', this.headersModel)
        }
    }
})
</script>
