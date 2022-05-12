<template>
    <Dialog :header="$t('managers.datasetManagement.helpTitle')" :style="detailDescriptor.style.infoDialog" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-mt-3">
            This attribute is related to <a href="http://www.fiware.org" target="_blank">Fiware</a> project, more specifically to <a href="http://catalogue.fiware.org/enablers/publishsubscribe-context-broker-orion-context-broker" target="_blank"> Orion Context Broker (OCB)</a>. It permits to NOT
            define JSON path Items, Attributes and Headers if the response is from OBC, a NGSI implementation. So if you use the REST DataSet to retrieve information from OCB then you don't need to define items and attributes because the format of JSON Response is well known.
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
