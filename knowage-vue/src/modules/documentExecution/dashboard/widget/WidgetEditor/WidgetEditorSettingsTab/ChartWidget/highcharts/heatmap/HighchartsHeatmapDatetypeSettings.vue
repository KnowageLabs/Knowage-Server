<template>
    <div v-if="datetypeSettings" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12">
            {{ datetypeSettings }}
        </div>
        <div class="p-col-10 p-p-d-flex p-flex-column p-p-2 p-fluid">
            <label class="kn-material-input-label p-mr-2">{{ $t('managers.datasetManagement.ckanDateFormat') }}</label>
            <Dropdown v-model="datetypeSettings.format" class="kn-material-input" :options="descriptor.dateFormats" :disabled="datetypeSettingsDisabled" @change="modelChanged">
                <template #option="slotProps">
                    <span>{{ getFormattedDate(new Date(), slotProps.option) }}</span>
                </template>
                <template #value="slotProps">
                    <span>{{ getFormattedDate(new Date(), slotProps.value) }}</span>
                </template>
            </Dropdown>
        </div>
        <div class="p-col-2"></div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { formatDate } from '@/helpers/commons/localeHelper'
import descriptor from './HighchartsHeatmapAxisSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'highcharts-heatmap-datetype-settings',
    components: { Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            datetypeSettings: null as any
        }
    },
    computed: {
        datetypeSettingsDisabled() {
            return !this.datetypeSettings || !this.datetypeSettings.enabled
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            if (this.widgetModel.settings?.configuration) this.datetypeSettings = this.widgetModel.settings.configuration.datetypeSettings
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        getFormattedDate(date: any, format: any) {
            return formatDate(date, format)
        }
    }
})
</script>
