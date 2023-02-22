<template>
    <Dialog :header="$t('managers.datasetManagement.helpTitle')" :style="detailDescriptor.style.infoDialog" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-mt-3">
            If the JSON response contains the data in simple list of objects format like:
            <pre>
  {
    "contextResponses": [
      {
        "a":3,
        "c":"d",
        "e":"f"
      },{
        "a":2,
        "e":"r",
        "f":"s",
        "b":"q"
       }
     ]
  }
      </pre
            >
            then it will be sufficient to define only the JSON Path Items and check <b>Use directly JSON Attributes</b>
            without defining the attributes. The attributes will be retrieved automatically from the JSON Objects: the names will be the fields names, the values will be the fields values. In this examples, the JSON Path Items will be:
            <pre>
  $.contextResponses[*]
      </pre
            >
            and the result DataSet content will have these objects (name: value):
            <pre>
  "a":3
  "c":"d"
  "e":"f"
  ...
      </pre
            >
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
    props: {
        visible: { type: Boolean }
    },
    emits: ['close'],
    data() {
        return {
            detailDescriptor
        }
    }
})
</script>
