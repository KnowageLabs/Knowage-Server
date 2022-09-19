<template>
    <div v-if="linksModel" class="p-grid">
        {{ linksModel }}
        <div class="p-col-12">
            <InputSwitch v-model="linksModel.enabled"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.interactions.enableLinkNavigation') }}</label>
        </div>

        <div class="p-col-12">
            <TableWidgetLinkForm v-for="(link, index) in linksModel.links" :key="index" :widgetModel="widgetModel" :link="link" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers" :disabled="linksDisabled"></TableWidgetLinkForm>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, ITableWidgetLinks } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../../DashboardHelpers'
import descriptor from '../../TableWidgetSettingsDescriptor.json'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import TableWidgetLinkForm from './TableWidgetLinkForm.vue'

export default defineComponent({
    name: 'table-widget-interactions-links',
    components: { Checkbox, Dropdown, InputSwitch, TableWidgetLinkForm },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array }
    },
    data() {
        return {
            descriptor,
            linksModel: null as ITableWidgetLinks | null
        }
    },
    computed: {
        linksDisabled() {
            return !this.linksModel || !this.linksModel.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadLinksModel()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromLinks', () => this.onColumnRemoved())
        },
        loadLinksModel() {
            if (this.widgetModel?.settings?.interactions?.link) this.linksModel = this.widgetModel.settings.interactions.link
        },
        onColumnRemoved() {
            this.loadLinksModel()
        }
    }
})
</script>
