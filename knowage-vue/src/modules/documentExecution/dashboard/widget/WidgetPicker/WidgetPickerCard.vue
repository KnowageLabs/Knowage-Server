<template>
    <div id="widget-card" class="p-m-2" :style="descriptor.style.widgetCard" :title="$t(`dashboard.widgets.${widget.type}.description`)">
        <div id="widget-card-icon-container" class="p-d-flex p-ai-center p-jc-center">
            <div class="innerIcon" :style="documentImageSource()"></div>
        </div>
        <div class="p-d-flex p-flex-column p-ai-start p-jc-center">
            <span class="p-ml-2 widgetTitle">{{ $t(`dashboard.widgets.${widget.type}.title`) }}</span>
        </div>
    </div>
</template>

<script lang="ts">
/**
 * ! this component renders the widget cards inside the picker dialog
 */
import { defineComponent } from 'vue'
import descriptor from './WidgetPickerDescriptor.json'

export default defineComponent({
    name: 'widget-picker-dialog',
    components: {},
    inject: [],
    props: { widget: { required: true, type: Object } },
    emits: ['closeWidgetPicker'],
    data() {
        return {
            descriptor
        }
    },
    methods: {
        documentImageSource(): any {
            return {
                'mask-image': `url(${descriptor.imagePath}${this.widget.type}${descriptor.imageExtension})`,
                '-webkit-mask-image': `url(${descriptor.imagePath}${this.widget.type}${descriptor.imageExtension})`
            }
        }
    }
})
</script>
<style lang="scss" scoped>
#widget-card {
    cursor: pointer;
    .widgetTitle {
        text-transform: capitalize;
        font-size: 1.1rem;
    }
    #widget-card-icon-container {
        background-color: var(--kn-color-secondary);
        border-right: 1px solid var(--kn-color-borders);
        transition: 0.2s ease-in;
        .innerIcon {
            width: 100px;
            height: 100%;
            mask-size: 80%;
            mask-repeat: no-repeat;
            mask-position: 180% 180%;
            -webkit-mask-size: 80%;
            -webkit-mask-repeat: no-repeat;
            -webkit-mask-position: 180% 180%;
            background-repeat: no-repeat;
            background-color: var(--kn-color-primary);
            position: relative;
            overflow: hidden;
            cursor: pointer;
            transition: 0.2s ease-in;
        }
    }
    &:hover {
        border-color: var(--kn-color-primary) !important;
        #widget-card-icon-container {
            background-color: var(--kn-color-primary);
            .innerIcon {
                background-color: white;
                mask-position: center;
                -webkit-mask-position: center;
            }
        }
    }
}
</style>
