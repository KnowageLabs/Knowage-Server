<template>
    <div v-if="customMessagesModel" class="p-grid p-ai-center p-p-4">
        <div class="p-col-12 p-md-3 p-pt-4">
            <Checkbox v-model="customMessagesModel.hideNoRowsMessage" :binary="true" @change="customMessagesChanged" />
            <label class="kn-material-input-label p-ml-3"> {{ $t('dashboard.widgetEditor.customMessages.hideNoRowsAvailable') }}</label>
        </div>
        <div class="p-col-12 p-md-9 p-d-flex p-flex-column p-pt-2">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.customMessages.customEmptyRowsMessage') }}</label>
            <InputText class="kn-material-input p-inputtext-sm" v-model="customMessagesModel.noRowsMessage" :disabled="customMessagesModel.hideNoRowsMessage" @change="customMessagesChanged" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetCustomMessages } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import Checkbox from 'primevue/checkbox'
import descriptor from '../TableWidgetSettingsDescriptor.json'

export default defineComponent({
    name: 'table-widget-custom-messages',
    components: { Checkbox },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            customMessagesModel: null as ITableWidgetCustomMessages | null
        }
    },
    created() {
        this.loadCustomMessages()
    },
    methods: {
        loadCustomMessages() {
            if (this.widgetModel?.settings?.configuration) this.customMessagesModel = this.widgetModel.settings.configuration.customMessages
        },
        customMessagesChanged() {
            emitter.emit('customMessagesChanged', this.customMessagesModel)
            emitter.emit('refreshTable', this.widgetModel.id)
        }
    }
})
</script>
