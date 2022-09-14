<template>
    <div>
        {{ tooltips }}
        <div v-for="(tooltip, index) in tooltips" :key="index" class="p-d-flex p-flex-column p-my-2 p-pb-2">
            <div v-show="index !== 0 && dropzoneTopVisible[index]" class="form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
            <div
                v-show="index !== 0"
                class="form-list-item-dropzone"
                :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible[index] }"
                @drop.stop="onDropComplete($event, 'before', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('top', index)"
                @dragleave.prevent="hideDropzone('top', index)"
            ></div>

            <WidgetEditorColumnsMultiselect
                v-if="index !== 0"
                :value="(tooltip.target as string[])"
                :availableTargetOptions="availableColumnOptions"
                :widgetColumnsAliasMap="widgetColumnsAliasMap"
                optionLabel="alias"
                optionValue="id"
                @change="onColumnsSelected($event, tooltip)"
            ></WidgetEditorColumnsMultiselect>

            <div class="p-d-flex p-flex-row" :draggable="true" @dragstart.stop="onDragStart($event, index)"></div>

            <div
                v-show="index !== 0"
                class="form-list-item-dropzone"
                :class="{ 'form-list-item-dropzone-active': dropzoneBottomVisible[index] }"
                @drop.stop="onDropComplete($event, 'after', index)"
                @dragover.prevent
                @dragenter.prevent="displayDropzone('bottom', index)"
                @dragleave.prevent="hideDropzone('bottom', index)"
            ></div>
            <div v-show="index !== 0 && dropzoneBottomVisible[index]" class="form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'after', index)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetTooltipStyle, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'

export default defineComponent({
    name: 'table-widget-conditions',
    components: { WidgetEditorColumnsMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, drivers: { type: Array }, variables: { type: Array } },
    data() {
        return {
            descriptor,
            tooltips: [] as ITableWidgetTooltipStyle[],
            availableColumnOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any,
            dropzoneTopVisible: {},
            dropzoneBottomVisible: {}
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadTooltips()
        this.loadWidgetColumnAliasMap()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromTooltips', () => this.onColumnRemoved())
        },
        loadTooltips() {
            if (this.widgetModel?.settings?.tooltips) this.tooltips = [...this.widgetModel.settings.tooltips]
        },
        loadColumnOptions() {
            this.availableColumnOptions = [...this.widgetModel.columns]
        },
        tooltipsChanged() {
            emitter.emit('tooltipsChanged', this.tooltips)
        },
        loadWidgetColumnAliasMap() {
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
            })
        },
        onColumnsSelected(event: any, tooltip: ITableWidgetTooltipStyle) {
            const intersection = (tooltip.target as string[]).filter((el: string) => !event.value.includes(el))
            tooltip.target = event.value
            intersection.length > 0 ? this.onColumnsRemovedFromMultiselect(intersection) : this.onColumnsAddedFromMultiselect(columnGroup)
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
            if (sourceRowIndex === targetRowIndex) return
            const newIndex = sourceRowIndex > targetRowIndex && position === 'after' ? targetRowIndex + 1 : targetRowIndex
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
