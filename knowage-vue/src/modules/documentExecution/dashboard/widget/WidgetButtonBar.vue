<template>
    <div class="widgetButtonBarContainer">
        <Button @click="editWidget" data-test="edit-widget-button">Edit</Button>
    </div>
    <div class="widgetButtonBarContainer2">
        <div class="drag-handle">DRAG</div>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget buttons and visibility.
 */
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'widget-button-bar',
    emits: ['editWidget'],
    methods: {
        editWidget() {
            this.$emit('editWidget')
        }
    }
})
</script>
<style lang="scss">
.widgetButtonBarContainer {
    position: absolute;
    top: 0;
    right: 0;
}
.widgetButtonBarContainer2 {
    position: absolute;
    bottom: 0;
    left: 0;
}
</style>
