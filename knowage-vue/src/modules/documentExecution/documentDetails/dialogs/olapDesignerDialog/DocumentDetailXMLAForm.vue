<template>
    <h1>XMLA Form</h1>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iXMLATemplate } from '../../DocumentDetails'

export default defineComponent({
    name: 'document-detail-xmla-form',
    components: {},
    props: { xmlModel: { type: Object as PropType<iXMLATemplate> } },
    data() {
        return {
            model: {} as iXMLATemplate
        }
    },
    watch: {
        xmlModel() {
            this.loadModel()
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.xmlModel as iXMLATemplate
        }
    }
})
</script>
