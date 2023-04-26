<template>
    <Dialog id="qbe-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    <span>{{ $t('qbe.detailView.toolbarMenu.sql') }}</span>
                </template>
            </Toolbar>
        </template>

        <div v-html="sqlFormats.sqlFormatted"></div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.ok') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog },
    props: { sqlData: Object, visible: Boolean },
    data() {
        return {
            sqlFormats: {} as any
        }
    },
    watch: {
        sqlData() {
            this.sqlFormats = this.sqlData
        }
    },
    created() {
        this.sqlFormats = this.sqlData
    },
    methods: {}
})
</script>
