<template>
    <div class="custom-header-group-container" :style="getSummaryStyle()">
        <span class="custom-header-group-label">
            <b style="margin-right: 4px">{{ params.value ? params.summaryRows[params.rowIndex] : '' }} </b>
            {{ params.value ?? '' }}
        </span>
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
    mounted() {},
    methods: {
        getSummaryStyle() {
            const styleSettings = this.params.propWidget.settings.style.summary
            const styleString = Object.entries(styleSettings.properties ?? styleSettings)
                .map(([k, v]) => `${k}:${v}`)
                .join(';')
            return styleString + ';'
        }
    }
})
</script>
