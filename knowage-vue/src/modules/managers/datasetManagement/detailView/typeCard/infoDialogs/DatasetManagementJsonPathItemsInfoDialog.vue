<template>
    <Dialog :header="$t('managers.datasetManagement.helpTitle')" :style="detailDescriptor.style.infoDialog" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-mt-3">
            <b>JSON Path Items</b> must indicate the place of items in the JSON, like rows of query. It uses <a href="https://github.com/jayway/JsonPath" target="_blank">JsonPath</a> language. It needs to be a single or list of JSON Objects. For example, in this JSON:
            <pre>
    {
      "contextResponses": [
        {
          "contextElement": {
            "id": "pros6_Meter",
            "type": "Meter",
            "isPattern": "false",
            "attributes": [
              {
                "name": "atTime",
                "type": "timestamp",
                "value": "2015-07-14T17:19:14.014+0200"
              },
              {
                "name": "downstreamActivePower",
                "type": "double",
                "value": "4.8"
              },
              {
                "name": "prosumerId",
                "type": "string",
                "value": "pros6"
              },
              {
                "name": "unitOfMeasurement",
                "type": "string",
                "value": "kW"
              },
              {
                "name": "upstreamActivePower",
                "type": "double",
                "value": "0"
              }
            ]
          },
          "statusCode": {
            "reasonPhrase": "OK",
            "code": "200"
          }
        },
        {
          "contextElement": {
            "id": "pros1_Meter",
            "type": "Meter",
            "isPattern": "false",
            "attributes": [
              {
                "name": "atTime",
                "type": "timestamp",
                "value": "2015-07-14T17:19:14.014+0200"
              },
              {
                "name": "downstreamActivePower",
                "type": "double",
                "value": "3.5"
              },
              {
                "name": "prosumerId",
                "type": "string",
                "value": "pros1"
              },
              {
                "name": "unitOfMeasurement",
                "type": "string",
                "value": "kW"
              },
              {
                "name": "upstreamActivePower",
                "type": "double",
                "value": "0"
              }
            ]
          },
          "statusCode": {
            "reasonPhrase": "OK",
            "code": "200"
          }
        }
      ]
    }
    </pre
            >
            if I want to take the two ContextElement items then the JSON Path Items will be:
            <pre>
    $.contextResponses[*].contextElement
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
