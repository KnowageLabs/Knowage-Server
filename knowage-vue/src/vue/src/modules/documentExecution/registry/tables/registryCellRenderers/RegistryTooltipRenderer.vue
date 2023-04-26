<template>
    <div class="custom-tooltip-container">
        <span id="custom-tooltip-suffix">{{ params.value }}</span>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    props: {
        params: {
            required: true,
            type: Object
        }
    },
    data() {
        return {}
    }
})
</script>
<style lang="scss">
.custom-tooltip-container {
    border: 1px solid cornflowerblue;
    overflow: hidden;
    background-color: white;
}

.custom-tooltip-container span {
    margin: 5px;
    white-space: nowrap;
}

.custom-tooltip-container span:nth-of-type(2) {
    font-weight: bold;
}
</style>
