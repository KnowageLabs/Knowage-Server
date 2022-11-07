<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary widget-tags-dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t(`dashboard.widgetEditor.editorTags.${mode}`) }}
                </template>
            </Toolbar>
        </template>

        <div class="tags-dialog-content p-mx-2">
            <p class="kn-material-input-label">TODO: Add guides for each fo the tags.</p>
            {{ widgetType }}
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary"> {{ $t('common.add') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog },
    props: { visible: Boolean, mode: String, widgetType: String },
    computed: {},
    data() {
        return {}
    },
    setup() {},
    created() {},
    watch: {},
    methods: {}
})
</script>
<style lang="scss">
.widget-tags-dialog .p-dialog-content {
    padding: 0;
}
</style>
