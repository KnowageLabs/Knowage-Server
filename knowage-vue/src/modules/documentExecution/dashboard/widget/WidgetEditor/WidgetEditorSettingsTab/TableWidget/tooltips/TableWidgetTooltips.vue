<template>
    <div class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-for="(tooltip, index) in tooltips" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center p-py-2 p-pb-2">
            <div v-show="index !== 0 && dropzoneTopVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
            <div
                v-show="index !== 0"
                class="p-col-12 form-list-item-dropzone"
                :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible[index] }"
                @drop.stop="onDropComplete($event, 'before', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('top', index)"
                @dragleave.prevent="hideDropzone('top', index)"
            ></div>

            <div class="p-col-12 p-grid p-d-flex p-flex-column" :draggable="true" @dragstart.stop="onDragStart($event, index)">
                <div class="p-d-flex p-flex-row p-ai-center">
                    <div v-if="index !== 0" class="p-col-1 p-d-flex p-flex-column p-jc-center p-pr-4">
                        <i class="pi pi-th-large kn-cursor-pointer"></i>
                    </div>
                    <div class="p-col-5 p-d-flex p-flex-column">
                        <label class="kn-material-input-label">{{ $t('common.columns') }}</label>
                        <Dropdown v-if="index === 0" class="kn-material-input" v-model="tooltip.target" :options="descriptor.allColumnOption" optionValue="value" optionLabel="label" :disabled="true"> </Dropdown>
                        <WidgetEditorColumnsMultiselect
                            v-else
                            :value="(tooltip.target as string[])"
                            :availableTargetOptions="availableColumnOptions"
                            :widgetColumnsAliasMap="widgetColumnsAliasMap"
                            optionLabel="alias"
                            optionValue="id"
                            @change="onColumnsSelected($event, tooltip)"
                        ></WidgetEditorColumnsMultiselect>
                    </div>
                    <div class="p-col-5 p-pt-4 p-px-4">
                        <InputSwitch v-model="tooltip.enabled" @change="tooltipsChanged"></InputSwitch>
                        <label class="kn-material-input-label p-m-3">{{ $t('common.enabled') }}</label>
                    </div>
                    <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                        <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash']" class="kn-cursor-pointer p-ml-2 p-mt-4" @click="index === 0 ? addTooltip() : removeTooltip(index)"></i>
                    </div>
                </div>
                <div class="p-d-flex p-flex-row p-flex-wrap p-ai-center p-mt-3">
                    <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.prefix') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="tooltip.prefix" :disabled="!tooltip.enabled" @change="tooltipsChanged" />
                    </div>
                    <div class="p-d-flex p-flex-column kn-flex p-mx-2">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.suffix') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="tooltip.suffix" :disabled="!tooltip.enabled" @change="tooltipsChanged" />
                    </div>
                    <div v-if="optionsContainMeasureColumn(tooltip)" class="p-d-flex p-flex-column p-mx-2">
                        <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.precision') }}</label>
                        <InputNumber class="kn-material-input p-inputtext-sm" v-model="tooltip.precision" :disabled="!tooltip.enabled" @blur="tooltipsChanged" />
                    </div>
                </div>
                <div class="p-grid p-ai-center p-pt-3">
                    <div class="p-col-12 p-md-3 p-mt-4 p-px-4">
                        <InputSwitch v-model="tooltip.header.enabled" :disabled="!tooltip.enabled" @change="tooltipsChanged"></InputSwitch>
                        <label class="kn-material-input-label p-m-3">{{ $t('dashboard.widgetEditor.tooltips.customHeader') }}</label>
                    </div>
                    <div class="p-col-12 p-md-9 p-d-flex p-flex-column p-px-2">
                        <label class="kn-material-input-label">{{ $t('common.text') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="tooltip.header.text" :disabled="!tooltip.enabled || !tooltip.header.enabled" @change="tooltipsChanged" />
                    </div>
                </div>
            </div>

            <div
                v-show="index !== 0"
                class="p-col-12 form-list-item-dropzone"
                :class="{ 'form-list-item-dropzone-active': dropzoneBottomVisible[index] }"
                @drop.stop="onDropComplete($event, 'after', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('bottom', index)"
                @dragleave.prevent="hideDropzone('bottom', index)"
            ></div>
            <div v-show="index !== 0 && dropzoneBottomVisible[index]" class="p-col-12 form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'after', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetTooltipStyle, IWidgetColumn, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'

export default defineComponent({
    name: 'table-widget-tooltips',
    components: { Dropdown, InputSwitch, InputNumber, WidgetEditorColumnsMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, drivers: { type: Array }, variables: { type: Array as PropType<IVariable[]> } },
    data() {
        return {
            descriptor,
            tooltips: [] as ITableWidgetTooltipStyle[],
            availableColumnOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any,
            widgetColumnsTypeMap: {} as any,
            dropzoneTopVisible: {},
            dropzoneBottomVisible: {}
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadTooltips()
        this.loadWidgetColumnMaps()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromTooltips', this.onColumnRemovedFromTooltips)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromTooltips', this.onColumnRemovedFromTooltips)
        },
        onColumnRemovedFromTooltips() {
            this.onColumnRemoved()
        },
        loadTooltips() {
            if (this.widgetModel?.settings?.tooltips) this.tooltips = this.widgetModel.settings.tooltips
            this.removeColumnsFromAvailableOptions()
        },

        removeColumnsFromAvailableOptions() {
            for (let i = 1; i < this.widgetModel.settings.tooltips.length; i++) {
                for (let j = 0; j < this.widgetModel.settings.tooltips[i].target.length; j++) {
                    this.removeColumnFromAvailableOptions({
                        id: this.widgetModel.settings.tooltips[i].target[j],
                        alias: this.widgetModel.settings.tooltips[i].target[j]
                    })
                }
            }
        },
        removeColumnFromAvailableOptions(tempColumn: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            if (index !== -1) this.availableColumnOptions.splice(index, 1)
        },
        loadColumnOptions() {
            this.availableColumnOptions = [...this.widgetModel.columns]
        },
        loadWidgetColumnMaps() {
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
                if (column.id && column.fieldType) this.widgetColumnsTypeMap[column.id] = column.fieldType
            })
        },
        tooltipsChanged() {
            emitter.emit('tooltipsChanged', this.tooltips)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onColumnsSelected(event: any, tooltip: ITableWidgetTooltipStyle) {
            const intersection = (tooltip.target as string[]).filter((el: string) => !event.value.includes(el))
            tooltip.target = event.value
            intersection.length > 0 ? this.onColumnsRemovedFromMultiselect(intersection) : this.onColumnsAddedFromMultiselect(tooltip)
            this.tooltipsChanged()
        },
        onColumnsRemovedFromMultiselect(intersection: string[]) {
            intersection.forEach((el: string) =>
                this.availableColumnOptions.push({
                    id: el,
                    alias: this.widgetColumnsAliasMap[el]
                })
            )
        },
        onColumnsAddedFromMultiselect(tooltip: ITableWidgetTooltipStyle) {
            ;(tooltip.target as string[]).forEach((target: string) => {
                const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableColumnOptions.splice(index, 1)
            })
        },
        optionsContainMeasureColumn(tooltip: ITableWidgetTooltipStyle) {
            let found = false
            for (let i = 0; i < tooltip.target.length; i++) {
                if (this.widgetColumnsTypeMap[tooltip.target[i]] === 'MEASURE') {
                    found = true
                    break
                }
            }
            return found
        },
        addTooltip() {
            this.tooltips.push({
                target: [],
                enabled: true,
                prefix: '',
                suffix: '',
                precision: 0,
                header: {
                    enabled: false,
                    text: ''
                }
            })
        },
        removeTooltip(index: number) {
            ;(this.tooltips[index].target as string[]).forEach((target: string) =>
                this.availableColumnOptions.push({
                    id: target,
                    alias: this.widgetColumnsAliasMap[target]
                })
            )
            this.tooltips.splice(index, 1)
            this.tooltipsChanged()
        },
        onDragStart(event: any, index: number) {
            event.dataTransfer.setData('text/plain', JSON.stringify(index))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        onDropComplete(event: any, position: 'before' | 'after', index: number) {
            this.hideDropzone('bottom', index)
            this.hideDropzone('top', index)
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            this.onRowsMove(eventData, index, position)
        },
        onRowsMove(sourceRowIndex: number, targetRowIndex: number, position: string) {
            console.log('SORUCE ROW INDEX: ', sourceRowIndex)
            console.log('TARGET ROW INDEX: ', targetRowIndex)
            console.log('position: ', position)
            if (sourceRowIndex === targetRowIndex) return
            const newIndex = sourceRowIndex > targetRowIndex && position === 'after' ? targetRowIndex + 1 : targetRowIndex
            console.log('newIndex: ', newIndex)
            this.tooltips.splice(newIndex, 0, this.tooltips.splice(sourceRowIndex, 1)[0])
            this.tooltipsChanged()
        },
        displayDropzone(position: string, index: number) {
            if (position === 'top') {
                this.dropzoneTopVisible[index] = true
            } else {
                this.dropzoneBottomVisible[index] = true
            }
        },
        hideDropzone(position: string, index: number) {
            if (position === 'top') {
                this.dropzoneTopVisible[index] = false
            } else {
                this.dropzoneBottomVisible[index] = false
            }
        },
        onColumnRemoved() {
            this.loadColumnOptions()
            this.loadTooltips()
        }
    }
})
</script>

<style lang="scss" scoped>
.form-list-item-dropzone {
    height: 20px;
    width: 100%;
    background-color: white;
}

.form-list-item-dropzone-active {
    height: 10px;
    background-color: #aec1d3;
}
</style>
