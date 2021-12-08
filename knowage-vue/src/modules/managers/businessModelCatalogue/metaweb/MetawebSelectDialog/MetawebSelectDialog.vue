<template>
    <Dialog class="metaweb-select-dialog remove-padding p-fluid kn-dialog--toolbar--primary" :contentStyle="metawebSelectDialogDescriptor.style.flex" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <h1>IT WORKS</h1>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import metawebSelectDialogDescriptor from './MetawebSelectDialogDescriptor.json'

export default defineComponent({
    name: 'metaweb-select-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean } },
    data() {
        return {
            metawebSelectDialogDescriptor
        }
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss" scoped>
.metaweb-select-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}

.remove-padding.p-dialog .p-dialog-header,
.remove-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}
</style>
