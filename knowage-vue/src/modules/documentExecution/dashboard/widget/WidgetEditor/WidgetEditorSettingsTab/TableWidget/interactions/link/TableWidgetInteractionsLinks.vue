<template>
    <div v-if="linksModel" class="p-grid p-p-4">
        <div class="p-col-12">
            <InputSwitch v-model="linksModel.enabled" @change="onLinkEnabledChange"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('dashboard.widgetEditor.interactions.enableLinkNavigation') }}</label>
        </div>

        <div v-for="(link, index) in linksModel.links" :key="index" class="dynamic-form-item p-grid p-ai-center p-col-12">
            <div class="p-sm-11 p-md-10 p-d-flex p-flex-column">
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

            <div class="p-sm-1 p-md-2 p-text-center">
                <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash']" class="kn-cursor-pointer" @click="index === 0 ? addLink() : removeLink(index)"></i>
            </div>

            <div class="p-sm-12 p-md-4 p-d-flex p-flex-column p-pt-2">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.interactions.basicUrl') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="link.baseurl" :disabled="linksDisabled" />
            </div>

            <div v-if="link.type === 'singleColumn'" class="p-sm-12 p-md-3">
                <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="link.column" :options="widgetModel.columns" optionLabel="alias" optionValue="columnName" :disabled="linksDisabled"> </Dropdown>
                </div>
            </div>
            <div v-else-if="link.type === 'icon'" class="p-sm-6 p-md-3 p-p-4">
                <WidgetEditorStyleToolbar :options="[{ type: 'icon' }]" :propModel="{ icon: link.icon }" :disabled="linksDisabled" @change="onStyleToolbarChange($event, link)"> </WidgetEditorStyleToolbar>
            </div>

            <div class="p-sm-6 p-md-4 p-d-flex p-flex-column">
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.interactions.linkType') }}</label>
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

            <div class="p-sm-12 p-md-12">
                <TableWidgetLinkParameterList
                    class="kn-flex p-mr-2"
                    :widgetModel="widgetModel"
                    :propParameters="link.parameters"
                    :selectedDatasetsColumnsMap="selectedDatasetColumnNameMap"
                    :drivers="drivers"
                    :disabled="linksDisabled"
                    @change="onParametersChanged($event, link)"
                    @addParameter="onAddParameter(link)"
                    @delete="onParameterDelete($event, link)"
                ></TableWidgetLinkParameterList>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, ITableWidgetLinks, ITableWidgetLink, IWidgetStyleToolbarModel, ITableWidgetParameter } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../../TableWidgetSettingsDescriptor.json'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import TableWidgetLinkParameterList from './TableWidgetLinkParameterList.vue'

export default defineComponent({
    name: 'table-widget-interactions-links',
    components: { Checkbox, Dropdown, InputSwitch, TableWidgetLinkParameterList, WidgetEditorStyleToolbar },
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
            selectedDatasetColumnNameMap: {},
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
        this.loadSelectedDatasetColumnNames()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromLinks', this.onColumnRemovedFromLinks)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromLinks', this.onColumnRemovedFromLinks)
        },
        onColumnRemovedFromLinks() {
            this.onColumnRemoved()
        },
        loadLinksModel() {
            if (this.widgetModel?.settings?.interactions?.link) this.linksModel = this.widgetModel.settings.interactions.link
        },
        loadSelectedDatasetColumnNames() {
            if (!this.selectedDatasets || this.selectedDatasets.length === 0) return
            this.selectedDatasets.forEach((dataset: IDataset) => this.loadSelectedDatasetColumnName(dataset))
        },
        loadSelectedDatasetColumnName(dataset: IDataset) {
            this.selectedDatasetColumnNameMap[dataset.name] = []
            for (let i = 0; i < dataset.metadata.fieldsMeta.length; i++) {
                this.selectedDatasetColumnNameMap[dataset.name].push(dataset.metadata.fieldsMeta[i].name)
            }
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
        },
        onLinkEnabledChange() {
            if (!this.linksModel) return
            if (this.linksModel.enabled && this.linksModel.links.length === 0) {
                this.linksModel.links.push({ type: '', baseurl: '', action: '', parameters: [] })
            }
        },
        addLink() {
            if (!this.linksModel || this.linksDisabled) return
            this.linksModel.links.push({ type: '', baseurl: '', action: '', parameters: [] })
        },
        removeLink(index: number) {
            if (!this.linksModel || this.linksDisabled) return
            this.linksModel.links.splice(index, 1)
        },
        onParametersChanged(parameters: ITableWidgetParameter[], link: ITableWidgetLink) {
            link.parameters = parameters
        },
        onAddParameter(link: ITableWidgetLink) {
            link.parameters.push({ enabled: true, name: '', type: '' })
        },
        onParameterDelete(index: number, link: ITableWidgetLink) {
            link.parameters.splice(index, 1)
        }
    }
})
</script>
