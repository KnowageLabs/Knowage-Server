<template>
    <Dialog class="full-screen-dialog" :visible="true" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #start>
                    <span>IT WORKS</span>
                </template>
            </Toolbar>
        </template>
        <ProgressSpinner class="kn-progress-spinner" v-if="loading" />
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import ProgressSpinner from 'primevue/progressspinner'

export default defineComponent({
    name: 'document-detail-olap-designer-dialog',
    components: { Dialog, ProgressSpinner },
    props: { visible: { type: Boolean } },
    data() {
        return {
            loading: false
        }
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{54px});
    margin: 0;
    .p-dialog-content {
        padding: 0;
        margin: 0;
        flex: 1;
        overflow: hidden;
    }
    .p-dialog-header {
        padding: 0;
        margin: 0;
    }
}
</style>
