<template>
    {{ document }}
    <div>
        <div>
            <!-- <Checkbox v-model="checked" :binary="true" /> -->
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'scheduler-tming-output-document-detail',
    components: {},
    props: { propDocument: { type: Object } },
    data() {
        return {
            document: null as any
        }
    },
    watch: {
        propDocument() {
            this.loadDocument()
        }
    },
    created() {
        this.loadDocument()
    },
    methods: {
        loadDocument() {
            this.document = this.propDocument
        }
    }
})
</script>
