<template>
    <Dialog class="workspace-cockpit-dialog remove-padding p-fluid kn-dialog--toolbar--primary" :contentStyle="workspaceCockpitDialogDescriptor.style.flex" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start></template>
                <template #end>
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="close" />
                </template>
            </Toolbar>
        </template>

        <iframe ref="iframe" class="workspace-cockpit-iframe" :src="url"></iframe>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import workspaceCockpitDialogDescriptor from './WorkspaceCockpitDialogDescriptor.json'

export default defineComponent({
    name: 'workspace-cockpit-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean } },
    emits: ['close'],
    data() {
        return {
            workspaceCockpitDialogDescriptor,
            url: ''
        }
    },
    computed: {},
    watch: {},
    created() {
        this.createUrl()
    },
    methods: {
        createUrl() {
            const user = (this.store.$state as any).user
            const language = user.locale.split('_')[0]
            const uniqueID = user.userUniqueIdentifier
            const country = user.locale.split('_')[1]

            this.url = import.meta.env.VITE_HOST_URL + `/knowagecockpitengine/api/1.0/pages/edit?NEW_SESSION=TRUE&SBI_LANGUAGE=${language}&SBI_SCRIPT=&user_id=${uniqueID}&SBI_COUNTRY=${country}&SBI_ENVIRONMENT=WORKSPACE&IS_TECHNICAL_USER=true&documentMode=EDIT`
        },
        close() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
.workspace-cockpit-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - var(--kn-mainmenu-width));
    margin: 0;
}
.remove-padding.p-dialog .p-dialog-header,
.remove-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
    overflow-x: hidden;
}

.workspace-cockpit-iframe {
    width: 100%;
    height: calc(100% - 39px);
    border: none;
}
</style>
