<template>
    <Dialog class="kn-dialog--toolbar--primary" :visible="visible" :header="$t('dashboard.widgetEditor.thresholds')" :style="descriptor.style.rangesDialog" :closable="false" modal :breakpoints="descriptor.style.rangesDialogBreakpoints">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #end>
                    <Button id="add-range-button" class="kn-button kn-button--primary" @click="addRange"> {{ $t('dashboard.widgetEditor.map.addThreshold') }} </Button>
                </template>
            </Toolbar>
        </template>
        <div v-for="(range, index) in ranges" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center">
            <div class="p-col-12">
                {{ range }}
            </div>
            <div class="p-col-6 p-lg-4 p-p-2">
                <WidgetEditorColorPicker :initial-value="range.color" :label="$t('common.color')" @change="onSelectionColorChanged($event, range)"></WidgetEditorColorPicker>
            </div>
            <div class="p-float-label p-col-6 p-lg-3 p-fluid p-p-2">
                <InputNumber v-model="range.from" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.fromThreshold') }}</label>
            </div>
            <div class="p-float-label p-col-6 p-lg-3 p-fluid p-p-2">
                <InputNumber v-model="range.to" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.toThreshold') }}</label>
            </div>
            <div class="p-col-6 p-lg-2 p-text-right">
                <Button v-tooltip.top="$t('common.delete')" icon="pi pi-trash" class="p-button-link" @click.stop="deleteRange(index)"></Button>
            </div>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="setRanges">{{ $t('common.set') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dialog from 'primevue/dialog'
import descriptor from '../MapVisualizationTypeDescriptor.json'
import deepcopy from 'deepcopy'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorColorPicker from '../../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'map-visualization-type-choropleth-ranges-dialog',
    components: { Dialog, InputNumber, WidgetEditorColorPicker },
    props: { visible: { required: true, type: Boolean }, propRanges: { required: true, type: Array as PropType<{ color: string; from: number; to: number }[]> } },
    emits: ['setRanges', 'close'],
    data() {
        return {
            descriptor,
            ranges: [] as { color: string; from: number; to: number }[]
        }
    },
    watch: {
        propRanges() {
            this.loadRanges()
        }
    },
    created() {
        this.loadRanges()
    },
    methods: {
        loadRanges() {
            this.ranges = deepcopy(this.propRanges)
        },
        onSelectionColorChanged(event: string | null, range: { color: string; from: number; to: number }) {
            if (event) range.color = event
        },
        addRange() {
            this.ranges.push({ color: '', from: 0, to: 0 })
        },
        deleteRange(index: number) {
            this.ranges.splice(index, 1)
        },
        setRanges() {
            this.$emit('setRanges', deepcopy(this.ranges))
            this.ranges = []
        },
        closeDialog() {
            this.ranges = []
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss" scoped>
#add-range-button {
    font-size: 0.8rem;
}
</style>
