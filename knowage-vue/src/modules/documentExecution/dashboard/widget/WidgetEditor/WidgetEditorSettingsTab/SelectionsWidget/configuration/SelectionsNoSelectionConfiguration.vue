<template>
    <div v-if="noSelectionsModel" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12">
            <InputSwitch v-model="noSelectionsModel.enabled" @change="noSelectionsConfigurationChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-2">{{ $t('dashboard.widgetEditor.noSelections.enable') }}</label>
        </div>

        <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-px-2 p-pt-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.noSelections.customMessage') }}</label>
            <InputText class="kn-material-input p-inputtext-sm" v-model="noSelectionsModel.customText" :disabled="noSelectionsDisabled" @change="noSelectionsConfigurationChanged" />
            <small>{{ $t('dashboard.widgetEditor.noSelections.customMessageHint') }}</small>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { ISelectionsWidgetNoSelections } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectionsWidget'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../SelectionsWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'selections-no-selection-configuration',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            noSelectionsModel: null as ISelectionsWidgetNoSelections | null
        }
    },
    computed: {
        noSelectionsDisabled() {
            return !this.noSelectionsModel || !this.noSelectionsModel.enabled
        }
    },
    created() {
        this.loadNoSelectionsConfiguration()
    },
    methods: {
        loadNoSelectionsConfiguration() {
            if (this.widgetModel.settings?.configuration?.noSelections) this.noSelectionsModel = this.widgetModel.settings.configuration.noSelections
        },
        noSelectionsConfigurationChanged() {
            emitter.emit('noSelectionsConfigurationChanged', this.noSelectionsModel)
            emitter.emit('refreshSelections', this.widgetModel.id)
        }
    }
})
</script>
