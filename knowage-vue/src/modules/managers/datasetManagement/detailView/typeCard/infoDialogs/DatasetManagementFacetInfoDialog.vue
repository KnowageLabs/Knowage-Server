<template>
    <Dialog :header="$t('managers.datasetManagement.helpTitle')" :style="detailDescriptor.style.infoDialog" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-mt-3">
            If you want to specify a single facet.query parameter just define in this way
            <pre>price:[500 TO *]</pre>
            If you want to specify multiple facet.query parameters you need to add also the prefix facet.query= and separate the values with &. You should also use same encoding like you are writing an URL. For example
            <pre>facet.query=price:[*+TO+500]&facet.query=price:[500+TO+*]</pre>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import detailDescriptor from '../../DatasetManagementDetailViewDescriptor.json'

export default defineComponent({
    name: 'lovs-management-info-dialog',
    components: { Dialog },
    emits: ['close'],
    props: {
        visible: { type: Boolean }
    },
    data() {
        return {
            detailDescriptor
        }
    }
})
</script>
