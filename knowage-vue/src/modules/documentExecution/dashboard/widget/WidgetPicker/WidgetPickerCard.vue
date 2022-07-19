<template>
    <div id="widget-card" class="p-m-2">
        <div id="widget-card-icon-container">{{ widget.cssClass }}</div>
        <div id="widget-card-detail-container">
            <span>Title: {{ widget.name }}</span>
            <!-- <span>Description: {{ widget.descKey }}</span> -->
        </div>
    </div>
</template>

<script lang="ts">
/**
 * ! this component renders the widget cards inside the picker dialog
 */
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'widget-picker-dialog',
    components: { Dialog },
    emits: ['closeWidgetPicker'],
    inject: [],
    props: { widget: { required: true, type: Object } },
    data() {
        return {}
    },
    created() {},
    computed: {},
    methods: {}
})
</script>
<style lang="scss" scoped>
#widget-card {
    border: 1px solid #cccccc;
    width: 285px;
    height: 100px;
    display: flex;
    transition: 0.5s ease;
}
#widget-card:hover {
    border-color: #43749e !important;
    // border-color: #bbd6ed !important;
}
#widget-card-icon-container {
    width: 100px;
    border-right: 1px solid #cccccc;
    background-color: #cccccc;
    flex-shrink: 0;
}
#widget-card-detail-container {
    flex: 1;
    display: flex;
    flex-direction: column;
}
</style>
