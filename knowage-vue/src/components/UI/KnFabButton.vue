<template>
    <div class="kn-fab-button-container">
        <div class="kn-fab-button kn-no-select">
            <span :class="icon"></span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'fab-button',
    props: {
        icon: String
    }
})
</script>

<style scoped lang="scss">
.kn-fab-button-container {
    position: relative;
    width: 40px;
    .kn-fab-button {
        display: flex;
        position: absolute;
        width: 40px;
        height: 40px;
        justify-content: center;
        align-items: center;
        top: 0;
        transform: translateY(-30%);
        left: 0;
        border-radius: 50%;
        background-color: var(--kn-button-fab-button-background-color);
        z-index: 100;
        cursor: pointer;
        box-shadow: 0px 3px 5px -1px rgba(black, 0.2), 0px 6px 10px 0px rgba(black, 0.14), 0px 1px 18px 0px rgba(black, 0.12);
        transition: background-color 0.3s cubic-bezier(0.445, 0.05, 0.55, 0.95);
        &:hover {
            background-color: var(--kn-button-fab-button-hover-color);
            box-shadow: 0px 5px 5px -3px rgba(black, 0.2), 0px 8px 10px 1px rgba(black, 0.14), 0px 3px 14px 2px rgba(black, 0.12);
        }
        &:active {
            background-color: var(--kn-button-fab-button-hover-color);
        }
        &.kn-fab-dark {
            background-color: black;
            color: white;
            &:hover {
                background-color: lighten(black, 10%);
            }
        }
    }
}
</style>
