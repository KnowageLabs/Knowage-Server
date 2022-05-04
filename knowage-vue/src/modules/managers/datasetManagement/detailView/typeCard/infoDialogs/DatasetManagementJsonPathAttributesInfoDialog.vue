<template>
    <Dialog :header="$t('managers.datasetManagement.helpTitle')" :style="detailDescriptor.style.infoDialog" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-mt-3">
            <b>JSON Path Attributes</b> indicates the fields of DataSet items in the <a href="https://github.com/jayway/JsonPath" target="_blank">JsonPath</a> format. For each attribute to retrieve you need to indicate:
            <ul>
                <li>name: the name of field. In this case if you use <b>id</b> name then it will have a particular meaning: it will be used as unique identifier</li>
                <li>
                    value: where to retrieve the value in the JSON response. It uses JSON Path format <b>related to the JSON Items previously specified</b> (the $. start if from the item object). For example, if you have this response:
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
                    and you use this JSON Path Items:
                    <pre>
	$.contextResponses[*].contextElement
</pre
                    >
                    then to retrieve the <i>downstreamActivePower</i> attribute for every item you need to specify this value:
                    <pre>
	$.attributes[?(@.name==downstreamActivePower)].value
</pre
                    >
                </li>
                <li>
                    type: the type of attribute (int, string, etc..). It can be directly specified or it can be in the JsonPath format if the type is contained in the response.
                </li>
            </ul>
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
