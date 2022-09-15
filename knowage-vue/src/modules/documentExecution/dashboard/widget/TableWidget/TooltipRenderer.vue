<template>
    <div class="custom-tooltip-container">
        <span id="custom-tooltip-prefix">{{ prefix }}</span>
        <span id="custom-tooltip-label">{{ tooltipValue }}</span>
        <span id="custom-tooltip-suffix">{{ suffix }}</span>
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
        return {
            tooltipValue: null as any,
            prefix: null as any,
            suffix: null as any
        }
    },
    beforeMount() {
        switch (this.params.location) {
            case 'header':
                this.tooltipValue = this.params.value
                break
            case 'cell':
                this.prefix = this.params.tooltipConfig.prefix
                this.suffix = this.params.tooltipConfig.suffix
                this.tooltipValue = this.params.value
                break

            default:
                break
        }
    },
    mounted() {
        // console.log('\n \n TOOLTIP PARAMS \n', this.params)
    },
    methods: {}
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
