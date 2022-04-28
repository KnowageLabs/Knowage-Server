<template>
    <Card v-if="font" class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('common.style') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-grid">
                <div class="p-col-6 p-fluid">
                    <span class="p-float-label p-m-2">
                        <Dropdown class="kn-material-input" v-model="font.size" :options="KpiDocumentDesignerStyleCardDescriptor.fontSizeOptions" optionValue="value">
                            <template #value="slotProps">
                                <div v-if="slotProps.value">
                                    <span>{{ getDropdownValueLabel(slotProps.value, KpiDocumentDesignerStyleCardDescriptor.fontSizeOptions) }}</span>
                                </div>
                            </template>
                            <template #option="slotProps">
                                <span :style="'font-size: ' + slotProps.option.value">{{ $t(slotProps.option.label) }}</span>
                            </template>
                        </Dropdown>
                        <label class="kn-material-input-label"> {{ $t('kpi.kpiDocumentDesigner.fontSize') }} </label>
                    </span>
                </div>
                <div class="p-col-6 p-fluid">
                    <span class="p-float-label p-m-2">
                        <Dropdown class="kn-material-input" v-model="font.fontFamily" :options="KpiDocumentDesignerStyleCardDescriptor.fontFamilyOptions" optionValue="value">
                            <template #value="slotProps">
                                <div v-if="slotProps.value">
                                    <span>{{ getDropdownValueLabel(slotProps.value, KpiDocumentDesignerStyleCardDescriptor.fontFamilyOptions) }}</span>
                                </div>
                            </template>
                            <template #option="slotProps">
                                <span :style="'font-family: ' + slotProps.option.value">{{ $t(slotProps.option.label) }}</span>
                            </template>
                        </Dropdown>
                        <label class="kn-material-input-label"> {{ $t('kpi.kpiDocumentDesigner.fontSize') }} </label>
                    </span>
                </div>
                <div class="p-col-6 p-fluid">
                    <span class="p-float-label p-m-2">
                        <Dropdown class="kn-material-input" v-model="font.fontWeight" :options="KpiDocumentDesignerStyleCardDescriptor.fontWeightOptions" optionValue="value">
                            <template #value="slotProps">
                                <div v-if="slotProps.value">
                                    <span>{{ getDropdownValueLabel(slotProps.value, KpiDocumentDesignerStyleCardDescriptor.fontWeightOptions) }}</span>
                                </div>
                            </template>
                            <template #option="slotProps">
                                <span :style="'font-weight: ' + slotProps.option.value">{{ $t(slotProps.option.label) }}</span>
                            </template>
                        </Dropdown>
                        <label class="kn-material-input-label"> {{ $t('kpi.kpiDocumentDesigner.fontWeight.title') }} </label>
                    </span>
                </div>
                <div class="p-col-6">
                    <div class="p-d-flex p-flex-column p-m-2">
                        <label class="kn-material-input-label"> {{ $t('kpi.kpiDocumentDesigner.color') }}</label>
                        <ColorPicker v-model="font.color" format="rgb" :inline="false" />
                    </div>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iFont, iStyle } from '../KpiDocumentDesigner'
import Card from 'primevue/card'
import ColorPicker from 'primevue/colorpicker'
import Dropdown from 'primevue/dropdown'
import KpiDocumentDesignerStyleCardDescriptor from './KpiDocumentDesignerStyleCardDescriptor.json'

export default defineComponent({
    name: 'kpi-edit-style-card',
    components: { Card, ColorPicker, Dropdown },
    props: { propStyle: { type: Object as PropType<iStyle>, required: true } },
    data() {
        return {
            KpiDocumentDesignerStyleCardDescriptor,
            font: null as iFont | null
        }
    },
    watch: {
        propStyle() {
            this.loadFont()
        }
    },
    created() {
        this.loadFont()
    },
    methods: {
        loadFont() {
            this.font = this.propStyle?.font as iFont
        },
        getDropdownValueLabel(value: string, options: { value: string; label: string }[]) {
            for (let i = 0; i < options.length; i++) {
                if (options[i].value === value) {
                    return this.$t(options[i].label)
                }
            }
        }
    }
})
</script>
