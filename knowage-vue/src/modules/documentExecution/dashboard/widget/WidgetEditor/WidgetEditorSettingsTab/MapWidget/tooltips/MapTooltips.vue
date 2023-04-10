<template>
    <div v-if="tooltips" class="p-grid p-jc-center p-ai-center p-p-4">
        <Message class="p-col-12 p-d-flex p-jc-center p-mx-4" severity="info" :closable="false">
            {{ $t('dashboard.widgetEditor.map.tooltipHint') }}
        </Message>
        <div v-for="(tooltip, index) in tooltips.layers" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center p-py-2 p-pb-2">
            <div class="p-col-12">
                {{ tooltip }}
            </div>
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

            <div class="p-grid p-col-12">
                <div class="p-col-1 p-md-2 p-d-flex p-flex-column p-jc-center p-pr-4">
                    <i class="pi pi-th-large kn-cursor-pointer"></i>
                </div>
                <div class="p-col-11 p-md-4 p-d-flex p-flex-column" :draggable="true" @dragstart.stop="onDragStart($event, index)">
                    <label class="kn-material-input-label">{{ $t('common.layer') }}</label>
                    <Dropdown v-model="tooltip.name" class="kn-material-input" :options="widgetModel.layers" option-value="name" option-label="name" :disabled="tooltipsDisabled"> </Dropdown>
                </div>

                <div class="p-col-11 p-md-4 p-d-flex p-flex-column">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <MultiSelect v-model="tooltip.columns" :options="getColumnOptionsFromLayer(tooltip)" option-label="alias" option-value="name" :disabled="tooltipsDisabled"> </MultiSelect>
                </div>
                <div class="p-col-1 p-md-2 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                    <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash']" class="kn-cursor-pointer p-ml-2 p-mt-4" @click="index === 0 ? addTooltip() : removeTooltip(index)"></i>
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
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IMapTooltipSettings } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import Message from 'primevue/message'

export default defineComponent({
    name: 'map-tooltips',
    components: { Dropdown, MultiSelect, Message },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            tooltips: null as IMapTooltipSettings | null,
            dropzoneTopVisible: {},
            dropzoneBottomVisible: {}
        }
    },
    computed: {
        tooltipsDisabled() {
            return !this.widgetModel || !this.widgetModel.settings.tooltips.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadTooltips()
    },

    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {},
        removeEventListeners() {},
        loadTooltips() {
            if (this.widgetModel?.settings?.tooltips) this.tooltips = this.widgetModel.settings.tooltips
        },
        addTooltip() {
            this.tooltips?.layers.push({ name: '', columns: [] })
        },
        removeTooltip(index: number) {
            this.tooltips?.layers.splice(index, 1)
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
            this.tooltips?.layers.splice(newIndex, 0, this.tooltips.layers.splice(sourceRowIndex, 1)[0])
        },
        displayDropzone(position: string, index: number) {
            position === 'top' ? (this.dropzoneTopVisible[index] = true) : (this.dropzoneBottomVisible[index] = true)
        },
        hideDropzone(position: string, index: number) {
            position === 'top' ? (this.dropzoneTopVisible[index] = false) : (this.dropzoneBottomVisible[index] = false)
        },
        getColumnOptionsFromLayer(tooltip: { name: string; columns: string[] }) {
            const index = this.widgetModel.layers.findIndex((layer: any) => layer.name === tooltip.name)
            return index !== -1 ? this.widgetModel.layers[index].content.columnSelectedOfDataset : []
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
