<template>
    <div class="p-field p-col-12 p-d-flex">
        <div class="kn-flex">
            <span class="p-float-label">
                <InputText id="fileName" v-model="customColorValue" class="kn-material-input kn-width-full" :disabled="true" />
                <label for="fileName" class="kn-material-input-label"> Custom color </label>
            </span>
        </div>
        <Button class="kn-button kn-button--primary click-outside p-mx-1 p-p-0 kn-flex" :style="`background-color:${customColorValue}`" @click="toggleColorPicker(-1)"></Button>
        <Button icon="fas fa-plus fa-1x" class="p-button-text p-button-plain p-ml-2" @click="addColor" />
    </div>

    <ColorPicker v-if="colorPickerVisible" class="dashboard-color-picker click-outside" theme="light" :color="customColorValue" :colors-default="descriptor.defaultColors" :sucker-hide="true" @changeColor="changeColor" />

    <DataTable class="pallete-table p-m-2" :style="descriptor.colorPalleteStyle.table" :value="widgetModel.settings.chart.colors" :reorderable-columns="false" responsive-layout="scroll" @rowReorder="onRowReorder">
        <Column :row-reorder="true" :reorderable-column="false" :style="descriptor.colorPalleteStyle.column">
            <template #body="slotProps">
                <span class="kn-height-full" :style="`background-color: ${slotProps.data}; color:${getContrastYIQ()}`">
                    <i class="p-datatable-reorderablerow-handle pi pi-bars p-m-2"></i>
                </span>
            </template>
        </Column>
        <Column :sortable="false" :style="descriptor.colorPalleteStyle.columnMain">
            <template #body="slotProps">
                <span class="kn-flex" :style="`background-color: ${slotProps.data}; color:${getContrastYIQ()}`">{{ slotProps.data }}</span>
            </template>
        </Column>
        <Column :row-reorder="true" :reorderable-column="false" :style="descriptor.colorPalleteStyle.column">
            <template #body="slotProps">
                <span class="kn-height-full" :style="`background-color: ${slotProps.data}; color:${getContrastYIQ()}`">
                    <i class="pi pi-pencil p-mr-2 click-outside" @click="toggleColorPicker(slotProps.index)"></i>
                    <i class="pi pi-trash p-mr-2" @click="deleteColor(slotProps.index)"></i>
                </span>
            </template>
        </Column>
    </DataTable>

    <br />
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { ColorPicker } from 'vue-color-kit'
import { useClickOutside } from '../../common/styleToolbar/useClickOutside'
import 'vue-color-kit/dist/vue-color-kit.css'
import descriptor from './ChartColorSettingsDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'hihgcharts-color-settings',
    components: { DataTable, Column, ColorPicker },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    setup() {
        const knowageStyleIcon = ref(null)
        const colorPickerVisible = ref(false)
        const contextMenuVisible = ref(false)
        useClickOutside(knowageStyleIcon, () => {
            colorPickerVisible.value = false
            contextMenuVisible.value = false
        })
        return { colorPickerVisible, contextMenuVisible, knowageStyleIcon }
    },
    data() {
        return {
            descriptor,
            customColorValue: '#8D8D8D',
            editIndex: -1,
            colorPickTimer: null as any,
            useClickOutside,
            widget: {} as any
        }
    },
    watch: {
        widgetModel() {
            this.widget = this.widgetModel
        }
    },
    created() {
        this.widget = this.widgetModel
    },
    methods: {
        toggleColorPicker(index) {
            this.colorPickerVisible = !this.colorPickerVisible
            this.editIndex = index
            this.customColorValue = this.widgetModel.settings.chart.colors[this.editIndex]
        },
        onRowReorder(event) {
            this.widget.settings.chart.colors = event.value
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        addColor() {
            this.widget.settings.chart.colors.push(this.customColorValue)
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        changeColor(color) {
            const { r, g, b, a } = color.rgba

            if (this.colorPickTimer) {
                clearTimeout(this.colorPickTimer)
                this.colorPickTimer = null
            }
            this.colorPickTimer = setTimeout(() => {
                if (!this.customColorValue) return
                if (this.editIndex != -1) this.widget.settings.chart.colors[this.editIndex] = `rgba(${r}, ${g}, ${b}, ${a})`
                else this.customColorValue = `rgba(${r}, ${g}, ${b}, ${a})`
                emitter.emit('refreshChart', this.widgetModel.id)
            }, 200)
        },
        deleteColor(index) {
            this.widget.settings.chart.colors.splice(index, 1)
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        getContrastYIQ() {
            //getContrastYIQ(hexcolor) {
            // var getRGBA = function(string) {
            //     var match = string.match(/^rgba\((\d{1,3}),\s*(\d{1,3}),\s*(\d{1,3}),\s*(\d*(?:\.\d+)?)\)$/)
            //     return match
            //         ? {
            //               r: Number(match[1]),
            //               g: Number(match[2]),
            //               b: Number(match[3]),
            //               a: Number(match[4])
            //           }
            //         : {}
            // }
            // var rgba = getRGBA(hexcolor) as any
            // var yiq = (rgba.r * 299 + rgba.g * 587 + rgba.b * 114) / 1000
            // return yiq >= 128 ? 'black' : 'white'
        }
    }
})
</script>

<style lang="scss">
.pallete-table .p-datatable-tbody {
    tr {
        td {
            height: 30px;
            span {
                display: flex;
                align-items: center;
            }
        }
    }
}
</style>
