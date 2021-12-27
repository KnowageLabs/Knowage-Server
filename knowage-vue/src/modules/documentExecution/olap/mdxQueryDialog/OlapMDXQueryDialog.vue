<template>
    <Dialog id="olap-mdx-query-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapMDXQueryDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('documentExecution.olap.showMdxQuery') }}
                </template>
            </Toolbar>
        </template>

        <VCodeMirror ref="codeMirror" class="p-m-2" v-model:value="query" :options="options" />

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dialog from 'primevue/dialog'
import olapMDXQueryDialogDescriptor from './OlapMDXQueryDialogDescriptor.json'
// import { VCodeMirror } from 'vue3-code-mirror'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: {
        Dialog
        // VCodeMirror
    },
    props: { mdxQuery: { type: String as PropType<String | null> } },
    emits: ['close'],
    data() {
        return {
            olapMDXQueryDialogDescriptor,
            query: null as string | null,
            codeMirror: {} as any,
            options: {
                mode: 'text/x-sql',
                lineWrapping: true,
                theme: 'eclipse',
                lineNumbers: true,
                readOnly: true
            },
            loading: false
        }
    },
    watch: {
        mdxQuery() {
            this.loadMdxQuery()
        }
    },
    created() {
        this.loadMdxQuery()
    },
    methods: {
        loadMdxQuery() {
            this.query = this.mdxQuery as string
        },
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirror) return
                this.codeMirror = (this.$refs.codeMirror as any).editor as any
                setTimeout(() => {
                    this.codeMirror.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        }
    }
})
</script>

<style lang="scss">
#olap-mdx-query-dialog .p-dialog-header,
#olap-mdx-query-dialog .p-dialog-content {
    padding: 0;
}
#olap-mdx-query-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
