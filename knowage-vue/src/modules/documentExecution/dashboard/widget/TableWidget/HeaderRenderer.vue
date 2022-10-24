<template>
    <div class="custom-header-container" :style="getHeaderStyle()">
        <div class="custom-header-label" @click="onSortRequested">{{ params.displayName }}</div>
        <i v-if="showAscending" class="pi pi-arrow-up p-button-text p-button-rounded p-button-plain p-p-0 p-ml-1" />
        <i v-if="showDescending" class="pi pi-arrow-down p-button-text p-button-rounded p-button-plain p-p-0 p-ml-1" />
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
    mounted() {},
    computed: {
        showAscending(): boolean {
            return this.params.propWidget.settings.sortingColumn == this.params.colId && this.params.propWidget.settings.sortingOrder == 'DESC'
        },
        showDescending(): boolean {
            return this.params.propWidget.settings.sortingColumn == this.params.colId && this.params.propWidget.settings.sortingOrder == 'ASC'
        }
    },
    data() {
        return {}
    },
    methods: {
        getHeaderStyle() {
            const styleSettings = this.params.propWidget.settings.style.headers
            const styleString = Object.entries(styleSettings.properties ?? styleSettings)
                .map(([k, v]) => `${k}:${v}`)
                .join(';')
            return styleString + ';'
        },
        onSortRequested() {
            var sortingColumn = this.params.propWidget.settings.sortingColumn
            var sortingOrder = this.params.propWidget.settings.sortingOrder
            if (sortingColumn == this.params.colId) {
                sortingOrder == 'ASC' ? (sortingOrder = 'DESC') : (sortingOrder = 'ASC')
                this.params.context.componentParent.sortingChanged({ colId: this.params.colId, order: sortingOrder })
            } else this.params.context.componentParent.sortingChanged({ colId: this.params.colId, order: 'ASC' })
        }
    }
})
</script>
