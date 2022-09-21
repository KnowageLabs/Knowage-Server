<template>
    <div class="custom-header-container" :style="getHeaderStyle()">
        <div class="custom-header-label">{{ params.displayName }}</div>
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
    },
    mounted() {
        // console.log('\n \n HEADER RENDERER PARAMS \n', this.params)
    },
    methods: {
        getHeaderStyle() {
            const styleSettings = this.params.propWidget.settings.style.headers
            const styleString = Object.entries(styleSettings.properties ?? styleSettings)
                .map(([k, v]) => `${k}:${v}`)
                .join(';')
            return styleString + ';'
        }
    }
})
</script>
