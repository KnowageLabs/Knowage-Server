<template>
    <div class="p-field p-col-12 p-d-flex">
        <div class="kn-flex">
            <span class="p-float-label">
                <InputText id="fileName" class="kn-material-input kn-width-full" v-model="customColorValue" :disabled="true" />
                <label for="fileName" class="kn-material-input-label"> Custom color </label>
            </span>
        </div>
        <Button class="kn-button kn-button--primary click-outside p-mx-1" :style="`background-color:${customColorValue}; padding: 0; flex: 1`" @click="colorPickerVisible = !colorPickerVisible"></Button>
        <Button icon="fas fa-plus fa-1x" class="p-button-text p-button-plain p-ml-2" @click="addColor" />
    </div>

    <ColorPicker v-if="colorPickerVisible" class="dashboard-color-picker click-outside" theme="light" :color="customColorValue" :colors-default="descriptor.defaultColors" :sucker-hide="true" @changeColor="changeColor" />

    <DataTable class="pallete-table p-m-2" style="border: 1px solid" :value="arrayColors" :reorderableColumns="false" responsiveLayout="scroll" @rowReorder="onRowReorder">
        <Column :rowReorder="true" :reorderableColumn="false" style="padding: 0 !important; border: none !important">
            <template #body="slotProps">
                <span :style="`background-color: ${slotProps.data.value}; height:100%`">
                    <i class="p-datatable-reorderablerow-handle pi pi-bars p-m-2"></i>
                </span>
            </template>
        </Column>
        <Column :sortable="false" style="padding: 0 !important; border: none !important; display: flex">
            <template #body="slotProps">
                <span :style="`background-color: ${slotProps.data.value}; flex: 1`">{{ slotProps.data }}</span>
            </template>
        </Column>
        <Column :rowReorder="true" :reorderableColumn="false" style="padding: 0 !important; border: none !important">
            <template #body="slotProps">
                <span :style="`background-color: ${slotProps.data.value}; height:100%`">
                    <i class="pi pi-trash p-mr-2" @click="deleteColor(slotProps.index)"></i>
                </span>
            </template>
        </Column>
    </DataTable>

    <br />
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IHighchartColor } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { useClickOutside } from '../../../common/styleToolbar/useClickOutside'
import { ColorPicker } from 'vue-color-kit'
import 'vue-color-kit/dist/vue-color-kit.css'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'hihgcharts-color-settings',
    components: { DataTable, Column, ColorPicker },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            colors: [] as IHighchartColor[],
            arrayColors: [] as IHighchartColor[],
            customColorValue: '#8D8D8D',
            useClickOutside
        }
    },
    setup() {
        const knowageStyleIcon = ref(null)
        let colorPickerVisible = ref(false)
        let contextMenuVisible = ref(false)
        useClickOutside(knowageStyleIcon, () => {
            colorPickerVisible.value = false
            contextMenuVisible.value = false
        })
        return { colorPickerVisible, contextMenuVisible, knowageStyleIcon }
    },
    created() {
        this.loadColorSettings()
    },
    methods: {
        loadColorSettings() {
            if (this.widgetModel.settings.chart.colors) this.colors = this.widgetModel.settings.chart.colors
            if (this.widgetModel.settings.chart.colors) this.arrayColors = Object.values(this.widgetModel.settings.chart.colors)
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onRowReorder(event) {
            this.arrayColors = event.value
        },
        addColor() {
            console.log('toadd', this.customColorValue)
            const colorToAdd = { gradient: '', name: this.customColorValue.replace('#', ''), order: `${this.arrayColors.length + 1}`, value: this.customColorValue }
            this.arrayColors.push(colorToAdd)
        },
        deleteColor(index) {
            this.arrayColors.splice(index, 1)
        },
        changeColor(color) {
            //TODO: RGBA ili HEX? Dal moram da prevodim unutar objekta RGBA u HEX?
            const { r, g, b, a } = color.rgba
            // this.customColorValue = `rgba(${r}, ${g}, ${b}, ${a})`
            this.customColorValue = color.hex
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
