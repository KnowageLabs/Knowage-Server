<template>
    <div class="p-m-2" :style="descriptor.style.widgetCard">
        <div id="widget-card-icon-container" class="p-d-flex p-ai-center p-jc-center" :style="descriptor.style.cardIconContainer">
            <i :class="widget.cssClass" :style="descriptor.style.mainIcon" />
        </div>
        <div class="p-d-flex p-flex-column p-ai-start p-jc-center">
            <span class="p-ml-2">{{ widget.name }}</span>
        </div>
    </div>
</template>

<script lang="ts">
/**
 * ! this component renders the widget cards inside the picker dialog
 */
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import descriptor from './WidgetPickerDescriptor.json'

export default defineComponent({
    name: 'widget-picker-dialog',
    components: { Dialog },
    emits: ['closeWidgetPicker'],
    inject: [],
    props: { widget: { required: true, type: Object } },
    data() {
        return {
            descriptor
        }
    },
    created() {},
    computed: {},
    methods: {}
})
</script>
<style lang="scss" scoped>
#widget-card:hover {
    border-color: #43749e !important;
    #widget-card-icon-container {
        border-color: #43749e !important;
        transition: 0.5s ease;
    }
}
</style>
