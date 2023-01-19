<template>
    <div v-if="searchSettings" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ searchSettings }}
        <div class="p-col-12 p-d-flex p-flex-column p-p-2">
            <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
            <MultiSelect v-model="searchSettings.columns" :options="widgetModel.columns" optionLabel="columnName" optionValue="columnName" :disabled="searchSettingsDisabled" @change="searchSettingsChanged"> </MultiSelect>
        </div>

        <div class="p-col-12 p-md-3 p-grid p-ai-center p-pt-4">
            <InputSwitch v-model="searchSettings.default" :disabled="searchSettingsDisabled" @change="searchSettingsChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-2">{{ $t('dashboard.widgetEditor.discoveryWidget.search.defaultTextSearch') }}</label>
        </div>

        <div class="p-col-12 p-md-9 p-grid p-ai-center">
            <div class="p-sm-12 p-md-4 p-lg-2 p-d-flex p-flex-column p-p-2 value-type-dropdown">
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.conditions.compareValueType') }}</label>
                <Dropdown class="kn-material-input" v-model="searchSettings.defaultType" :options="descriptor.searchSettingsTypes" optionValue="value" :disabled="defaultSearchDisabled" @change="onDefaultTypeChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.searchSettingsTypes, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>
            <div v-if="searchSettings.defaultType === 'static'" class="p-sm-12 p-md-4 p-lg-8 p-d-flex p-flex-column kn-flex p-pl-2 p-pt-2">
                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="searchSettings.defaultValue" :disabled="defaultSearchDisabled" @change="searchSettingsChanged" />
            </div>
            <div v-else-if="searchSettings.defaultType === 'driver'" class="p-sm-12 p-md-4 p-lg-8 p-d-flex p-flex-column kn-flex p-pl-2">
                <label class="kn-material-input-label">{{ $t('common.driver') }}</label>
                <Dropdown class="kn-material-input" v-model="searchSettings.driverLabel" :options="drivers" optionLabel="name" optionValue="driverLabel" :disabled="defaultSearchDisabled" @change="onDriverChanged"> </Dropdown>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDashboardDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import { IDiscoveryWidgetSearchSettings } from '@/modules/documentExecution/dashboard/interfaces/DashboardDiscoveryWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { mapActions } from 'pinia'
import descriptor from '../DiscoveryWidgetSettingsDescriptor.json'
import dashboardStore from '@/modules/documentExecution/dashboard/Dashboard.store'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'discovery-widget-search-settings',
    components: { Dropdown, InputSwitch, MultiSelect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, dashboardId: { type: String, required: true } },
    data() {
        return {
            descriptor,
            searchSettings: null as IDiscoveryWidgetSearchSettings | null,
            driverValuesMap: {},
            drivers: [] as IDashboardDriver[],
            getTranslatedLabel
        }
    },
    computed: {
        searchSettingsDisabled(): boolean {
            return !this.searchSettings || !this.searchSettings.enabled
        },
        defaultSearchDisabled() {
            return this.searchSettingsDisabled || !this.searchSettings?.default
        }
    },
    created() {
        this.setEventListeners()
        this.loadDrivers()
        this.loadDriverValuesMap()
        this.loadSearchSettings()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        ...mapActions(dashboardStore, ['getDashboardDrivers']),
        setEventListeners() {
            emitter.on('columnAdded', this.onColumnAdded)
            emitter.on('columnRemoved', this.onColumnRemoved)
        },
        removeEventListeners() {
            emitter.off('columnAdded', this.onColumnAdded)
            emitter.off('columnRemoved', this.onColumnRemoved)
        },
        loadDrivers() {
            this.drivers = this.getDashboardDrivers(this.dashboardId)
        },
        loadDriverValuesMap() {
            if (!this.drivers) return
            this.drivers.forEach((driver: IDashboardDriver) => (this.driverValuesMap[driver.driverLabel] = driver.value))
        },
        loadSearchSettings() {
            if (this.widgetModel.settings?.search) this.searchSettings = this.widgetModel.settings.search
        },
        searchSettingsChanged() {
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onDefaultTypeChanged() {
            if (!this.searchSettings) return
            if (this.searchSettings.defaultType === 'static') delete this.searchSettings.driverLabel
            this.searchSettings.defaultValue = ''
            this.searchSettingsChanged()
        },
        onDriverChanged() {
            if (!this.searchSettings || !this.searchSettings.driverLabel) return
            this.searchSettings.defaultValue = this.driverValuesMap[this.searchSettings.driverLabel]
            this.searchSettingsChanged()
        },
        onColumnAdded(column: any) {
            this.loadSearchSettings()
        },
        onColumnRemoved(column: any) {
            this.loadSearchSettings()
        }
    }
})
</script>
