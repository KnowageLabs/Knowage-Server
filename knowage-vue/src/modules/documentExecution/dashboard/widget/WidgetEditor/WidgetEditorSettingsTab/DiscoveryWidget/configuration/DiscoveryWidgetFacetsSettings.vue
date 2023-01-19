<template>
    <div v-if="facetSettings" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ facetSettings }}
        <div class="p-col-12 p-d-flex p-flex-column p-p-2">
            <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
            <MultiSelect v-model="facetSettings.columns" :options="widgetModel.columns" optionLabel="columnName" optionValue="columnName" :disabled="facetSettingsDisabled" @change="facetsSettingsChanged"> </MultiSelect>
        </div>

        <div class="p-col-12 p-md-6 p-lg-3 p-grid p-ai-center p-pt-4">
            <InputSwitch v-model="facetSettings.selection" :disabled="facetSettingsDisabled" @change="facetsSettingsChanged"></InputSwitch>
            <label class="kn-material-input-label p-pl-2">{{ $t('dashboard.widgetEditor.discoveryWidget.facets.enableSelection') }}</label>
        </div>

        <div class="p-col-12 p-md-6 p-lg-3 p-grid p-ai-center p-pt-4">
            <InputSwitch v-model="facetSettings.closedByDefault" :disabled="facetSettingsDisabled" @change="facetsSettingsChanged"></InputSwitch>
            <label class="kn-material-input-label p-pl-2">{{ $t('dashboard.widgetEditor.discoveryWidget.facets.closedByDefault') }}</label>
        </div>

        <div class="p-col-12 p-md-4 p-lg-2 p-d-flex p-flex-column">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.discoveryWidget.facets.columnWidth') }}</label>
            <InputText class="kn-material-input p-inputtext-sm" v-model="facetSettings.width" :disabled="facetSettingsDisabled" @change="facetsSettingsChanged" />
        </div>

        <div class="p-col-12 p-md-4 p-lg-2 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.discoveryWidget.facets.maxNumber') }}</label>
            <InputNumber class="kn-material-input p-inputtext-sm" v-model="facetSettings.limit" :disabled="facetSettingsDisabled" @blur="onInputNumberChanged" />
        </div>

        <div class="p-col-12 p-md-4 p-lg-2 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.discoveryWidget.facets.decimalPrecision') }}</label>
            <InputNumber class="kn-material-input p-inputtext-sm" v-model="facetSettings.precision" :disabled="facetSettingsDisabled" @blur="onInputNumberChanged" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IDiscoveryWidgetFacetsSettings } from '@/modules/documentExecution/dashboard/interfaces/DashboardDiscoveryWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'discovery-widget-facets-settings',
    components: { InputNumber, InputSwitch, MultiSelect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, dashboardId: { type: String, required: true } },
    data() {
        return {
            facetSettings: null as IDiscoveryWidgetFacetsSettings | null,
            getTranslatedLabel
        }
    },
    computed: {
        facetSettingsDisabled(): boolean {
            return !this.facetSettings || !this.facetSettings.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadFacetsSettings()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnAdded', this.onColumnAdded)
            emitter.on('columnRemoved', this.onColumnRemoved)
        },
        removeEventListeners() {
            emitter.off('columnAdded', this.onColumnAdded)
            emitter.off('columnRemoved', this.onColumnRemoved)
        },
        loadFacetsSettings() {
            if (this.widgetModel.settings?.facets) this.facetSettings = this.widgetModel.settings.facets
        },
        facetsSettingsChanged() {
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onInputNumberChanged() {
            setTimeout(() => this.facetsSettingsChanged(), 250)
        },
        onColumnAdded(column: any) {
            this.loadFacetsSettings()
        },
        onColumnRemoved(column: any) {
            this.loadFacetsSettings()
        }
    }
})
</script>
