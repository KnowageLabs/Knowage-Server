<template>
    <!-- ENABLE IF NEEDED -->
    <div class="lockButtonContainer" style="width: 32px; height: 32px">
        <i class="fas fa-lock kn-cursor-pointer" @click="$emit('unlockSelection')" />
        <i v-if="playSelectionButtonVisible" class="fas fa-play kn-cursor-pointer" @click="$emit('launchSelection')" />
    </div>

    <div class="widgetButtonBarContainer">
        <SpeedDial class="speed-dial-menu" :model="items" direction="right" :transitionDelay="80" showIcon="fas fa-ellipsis-v" hideIcon="fas fa-ellipsis-v" buttonClass="p-button-text p-button-rounded p-button-plain">
            <template #item>
                <i class="fas fa-arrows-up-down-left-right p-button-text p-button-rounded p-button-plain drag-handle drag-widget-icon buttonHover" style="width: 20px; height: 10px"></i>
                <Button icon="fas fa-pen-to-square" class="p-button-text p-button-rounded p-button-plain" @click="editWidget" />
                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop />
            </template>
        </SpeedDial>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget buttons and visibility.
 */
import { defineComponent } from 'vue'
import SpeedDial from 'primevue/speeddial'

export default defineComponent({
    name: 'widget-button-bar',
    emits: ['editWidget', 'unlockSelection', 'launchSelection'],
    components: { SpeedDial },
    data() {
        return {
            items: [
                {
                    label: 'Add',
                    icon: 'pi pi-pencil',
                    class: 'drag-handle'
                }
            ]
        }
    },
    methods: {
        editWidget() {
            this.$emit('editWidget')
        }
    }
})
</script>
<style lang="scss">
.lockButtonContainer {
    width: 32px;
    height: 32px;
    position: absolute;
    right: -32px;
    background-color: #a9c3db;
    color: rgb(82, 82, 82);
    border: 1px solid #ccc;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    align-content: center;
}
.widgetButtonBarContainer {
    position: absolute;
    bottom: 0;
    left: 0;
}
.drag-widget-icon {
    border-radius: 50% !important;
    height: 2.2rem !important;
    width: 2.25rem !important;
    padding: 0.571rem !important;
    color: rgba(0, 0, 0, 0.6);
}
.speed-dial-menu {
    position: relative;
    .p-speeddial-button {
        width: 3.5rem !important;
        height: 3.5rem !important;
        span {
            font-size: 1.5rem !important;
        }
    }
}
</style>
