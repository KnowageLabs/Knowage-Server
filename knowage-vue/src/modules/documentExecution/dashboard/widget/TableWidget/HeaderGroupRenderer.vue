<template>
    <div class="custom-header-group-container" :style="getHeaderGroupStyle()">
        <div class="custom-header-group-label">{{ params.displayName }}</div>
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
        // console.log('\n \n GROUP RENDERER PARAMS \n', this.params)
    },
    methods: {
        getHeaderGroupStyle() {
            var modelGroups = this.params.propWidget.settings.style.columnGroups
            var columnGroupStyleString = null as any

            columnGroupStyleString = Object.entries(modelGroups[0].properties)
                .map(([k, v]) => `${k}:${v}`)
                .join(';')

            modelGroups.forEach((group) => {
                if (group.target.includes(this.params.colId)) {
                    columnGroupStyleString = Object.entries(group.properties)
                        .map(([k, v]) => `${k}:${v}`)
                        .join(';')
                }
            })

            return columnGroupStyleString
        }
    }
})
</script>
