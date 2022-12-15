<template>
    <div v-if="selectionModel" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ selectionModel }}
        <div class="p-col-12 p-grid">
            <div class="p-col-12 p-grid p-ai-center">
                <div class="p-col-12 p-md-4 p-pt-4 p-pr-4">
                    <InputSwitch v-model="selectionModel.enabled" @change="selectionChanged"></InputSwitch>
                    <label class="kn-material-input-label p-m-3">{{ $t('dashboard.widgetEditor.interactions.enableSelection') }}</label>
                </div>
                <div v-if="selectionModel.multiselection" class="p-col-12 p-md-4 p-pt-4 p-pr-4">
                    <InputSwitch v-model="selectionModel.multiselection.enabled" @change="selectionChanged"></InputSwitch>
                    <label class="kn-material-input-label p-m-3">{{ $t('dashboard.widgetEditor.interactions.enableMultiselection') }}</label>
                </div>
                <div v-if="selectionModel.multiselection" class="p-col-12 p-md-4 style-toolbar-container p-pt-3 p-pr-5">
                    <WidgetEditorStyleToolbar
                        :options="descriptor.styleToolbarSelectionOptions"
                        :propModel="{
                            color: selectionModel.multiselection.properties.color,
                            'background-color': selectionModel.multiselection.properties['background-color']
                        }"
                        :disabled="!selectionModel.multiselection.enabled"
                        @change="onStyleToolbarChange($event)"
                    ></WidgetEditorStyleToolbar>
                </div>
            </div>
            <div v-if="selectionModel.modalColumn || selectionModel.modalColumn === ''" class="p-col-12 p-d-flex p-flex-row p-ai-center p-p-3">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.interactions.modalColumn') }}</label>
                    <Dropdown class="kn-material-input" v-model="selectionModel.modalColumn" :options="widgetModel.columns" :showClear="true" optionLabel="alias" optionValue="id" @change="selectionChanged"> </Dropdown>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetSelection, IWidgetStyleToolbarModel } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../../../DashboardHelpers'
import descriptor from '../WidgetInteractionsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-selection',
    components: { Dropdown, InputSwitch, WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            descriptor,
            selectionModel: null as IWidgetSelection | null
        }
    },
    created() {
        this.loadSelectionModel()
    },
    methods: {
        loadSelectionModel() {
            if (this.widgetModel?.settings?.interactions?.selection) this.selectionModel = this.widgetModel.settings.interactions.selection
        },
        selectionChanged() {
            emitter.emit('selectionChanged', this.selectionModel)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.selectionModel) return
            if (this.selectionModel.multiselection) {
                this.selectionModel.multiselection.properties.color = model.color ?? ''
                this.selectionModel.multiselection.properties['background-color'] = model['background-color'] ?? ''
            }
            this.selectionChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
.style-toolbar-container {
    max-width: 120px;
}
</style>
