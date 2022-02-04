<template>
    <Dialog :header="title" :breakpoints="dialogDescriptor.dialog.breakpoints" :style="dialogDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ title }}
                </template>
            </Toolbar>
        </template>
        {{ message }}
        <template #footer>
            <Button :label="$t('common.close')" @click="closeDialog" class="kn-button kn-button--primary" />
        </template>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import dialogDescriptor from './CrossNavigationManagementDialogDescriptor.json'
export default defineComponent({
    name: 'doc-dialog',
    components: {
        Dialog
    },
    props: {
        dialogVisible: {
            type: Boolean,
            default: false
        },
        title: {
            type: String,
            required: true
        },
        message: {
            type: String,
            required: true
        }
    },
    emits: ['close', 'apply'],
    data() {
        return {
            dialogDescriptor
        }
    },
    watch: {},
    methods: {
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>
