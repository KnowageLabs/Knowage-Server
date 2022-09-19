<template>
    <div v-if="linkModel" class="p-grid">
        <div class="p-col-12">
            {{ linkModel }}
        </div>

        <div class="p-sm-12 p-md-4 p-d-flex p-flex-column">
            <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
            <Dropdown class="kn-material-input" v-model="linkModel.type" :options="descriptor.interactionTypes" optionValue="value" :disabled="disabled" @change="onInteractionTypeChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.interactionTypes, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>
        <div class="p-sm-12 p-md-8 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.interactions.basicUrl') }}</label>
            <InputText class="kn-material-input p-inputtext-sm" v-model="linkModel.baseurl" :disabled="disabled" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, ITableWidgetLink } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../../TableWidgetSettingsDescriptor.json'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-link-form',
    components: { Checkbox, Dropdown, InputSwitch },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        link: { type: Object as PropType<ITableWidgetLink>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array },
        disabled: { type: Boolean }
    },
    data() {
        return {
            descriptor,
            linkModel: null as ITableWidgetLink | null,
            getTranslatedLabel
        }
    },
    computed: {},
    created() {
        this.loadLinkModel()
    },
    methods: {
        loadLinkModel() {
            this.linkModel = this.link
        },
        onInteractionTypeChanged() {
            if (!this.linkModel) return
            switch (this.linkModel.type) {
                case 'allRow':
                    delete this.linkModel.column
                    delete this.linkModel.icon
                    break
                case 'singleColumn':
                    delete this.linkModel.icon
                    break
                case 'icon':
                    delete this.linkModel.column
            }
        }
    }
})
</script>
