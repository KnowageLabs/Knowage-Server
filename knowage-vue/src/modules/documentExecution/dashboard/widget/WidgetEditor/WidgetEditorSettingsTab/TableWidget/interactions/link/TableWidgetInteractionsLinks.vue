<template>
    <div v-if="linksModel" class="p-grid">
        {{ linksModel }}
        <div class="p-col-12">
            <InputSwitch v-model="linksModel.enabled"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.interactions.enableLinkNavigation') }}</label>
        </div>

        <div v-for="(link, index) in linksModel.links" :key="index" class="p-grid p-ai-center p-col-12">
            <div class="p-sm-12 p-md-4 p-d-flex p-flex-column">
                <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                <Dropdown class="kn-material-input" v-model="link.type" :options="descriptor.interactionTypes" optionValue="value" :disabled="linksDisabled" @change="onInteractionTypeChanged(link)">
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
            <div class="p-sm-12 p-md-8 p-d-flex p-flex-column p-pt-2">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.interactions.basicUrl') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="link.baseurl" :disabled="linksDisabled" />
            </div>

            <div v-if="link.type === 'singleColumn'" class="p-sm-11 p-md-5">
                <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="link.column" :options="widgetModel.columns" optionLabel="alias" optionValue="columnName" :disabled="linksDisabled"> </Dropdown>
                </div>
            </div>
            <div v-else-if="link.type === 'icon'" class="p-sm-11 p-md-5 p-p-4">
                <WidgetEditorStyleToolbar :options="[{ type: 'icon' }]" :propModel="{ icon: link.icon }" :disabled="linksDisabled" @change="onStyleToolbarChange($event, link)"> </WidgetEditorStyleToolbar>
            </div>

            <div class="p-sm-12 p-md-6 p-d-flex p-flex-column">
                <label class="kn-material-input-label"> {{ $t('common.linkType') }}</label>
                <Dropdown class="kn-material-input" v-model="link.action" :options="descriptor.linkTypes" optionValue="value" :disabled="linksDisabled" @change="onInteractionTypeChanged(link)">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.linkTypes, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, ITableWidgetLinks, ITableWidgetLink, IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../../TableWidgetSettingsDescriptor.json'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-interactions-links',
    components: { Checkbox, Dropdown, InputSwitch, WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array }
    },
    data() {
        return {
            descriptor,
            linksModel: null as ITableWidgetLinks | null,
            getTranslatedLabel
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
        onInteractionTypeChanged(link: ITableWidgetLink) {
            switch (link.type) {
                case 'allRow':
                    delete link.column
                    delete link.icon
                    break
                case 'singleColumn':
                    delete link.icon
                    break
                case 'icon':
                    delete link.column
            }
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, link: ITableWidgetLink) {
            link.icon = model.icon
        },
        onColumnRemoved() {
            this.loadLinksModel()
        }
    }
})
</script>
