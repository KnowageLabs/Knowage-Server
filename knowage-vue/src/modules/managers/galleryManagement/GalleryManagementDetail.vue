<template>
  <div class="managerDetail">
    <Toolbar>
        <template #left>
            Template {{template.label}} - {{id}}
        </template>

        <template #right>
            <Button icon="pi pi-save" class="p-button-danger p-button-text" @click="saveTemplate" />
            <Button icon="pi pi-times" class="p-button-danger p-button-text" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0">
        <div>
            <span class="p-float-label">
                <InputText id="label" type="text" v-model="template.label" />
                <label for="label">Label</label>
            </span>
            <span class="p-float-label">
                <Textarea classv-model="template.description" id="description" rows="5" cols="30" />
                <label for="description">Description</label>
            </span>
            <span class="p-float-label">
                <Chips v-model="template.tags" />
            </span>
        </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Chips from 'primevue/chips';

interface GalleryTemplate {
    id: string
    label: string
    description?: string
    code?: string
    tags?: Array<string>
} 

export default defineComponent({
    name: 'gallery-management-detail',
    components: {
        Chips,
        InputText,
        Textarea
    },
    props: {
        id: Number
    },
    data() {
        return {
            template: {} as GalleryTemplate,
            cmOptions : {
                tabSize: 4,
                mode: 'text/javascript',
                theme: 'base16-dark',
                lineNumbers: true,
                line: true,
            }
        }
    },
    created() {
        this.template = {id:uuidv4(),label:'test', tags:["tag1", "tag2"]}
    },
    methods: {
        saveTemplate(){
            console.log('test', this.template)
        }
    }
})
</script>