<template>
  <div class="managerDetail">
    <Toolbar>
        <template #left>
            Template {{template.label}}
        </template>

        <template #right>
            <Button icon="pi pi-save" class="p-button-danger p-button-text" @click="saveTemplate" />
            <Button icon="pi pi-times" class="p-button-danger p-button-text" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0">
        <div>
            <span class="p-float-label">
                <InputText id="label" class="kn-material-input" type="text" v-model="template.label" />
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
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Chips from 'primevue/chips';

interface GalleryTemplate {
    id: string
    author: string
    label: string
    type: string
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
        id: String
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
            },
            galleryTemplates: [
                {id:'908d9674-ff77-43bd-90e6-fa11eef06c99', label:'colored card', type:'html', author:'davide.vernassa@eng.it', tags:["html","card"]},
                {id:'b206bf60-5622-4432-9e6c-fd4a66bab811', label:'advanced line chart', type:'chart', author:'matteo.massarotto@eng.it', tags:["chart", "highchart"]},
                {id:'b160c219-801e-4030-afa9-b52583a9094f', label:'hierarchy', type:'chart', author:'davide.vernassa@eng.it', tags:["highchart", "MARE"]},
                {id:'27f46bee-442b-4c65-a6ff-4e55a1caa93f', label:'double cards', type:'html', author:'davide.vernassa@eng.it', tags:["html", "card"]},
                {id:'84d99a09-5d07-4fa5-85f3-e0c19c78d508', label:'progression chart', type:'python', author:'alberto.nale@eng.it', tags:["python", "function","ai"]},
                {id:'cba22aa7-444f-4dfb-9d16-ca7df7965360', label:'multicard', type:'html', author:'davide.vernassa@eng.it', tags:["html", "multiple", "card"]},
                {id:'0e5c80b8-8308-48fe-8943-274d7bfa5dfb', label:'header_light', type:'html', author:'alberto.nale@eng.it', tags:["html", "header"]},
                {id:'833c2694-7873-4308-956d-f0a4ccddae08', label:'header_dark', type:'html', author:'alberto.nale@eng.it', tags:["html", "header"]}
            ],
        }
    },
    created() {
        this.loadTemplate()
    },
    updated() {
        this.loadTemplate()
    },
    methods: {
        loadTemplate(){
            /*this.axios.get(`/knowage/restful-services/3.0/gallery/${this.id}`)
            .then(response => this.template = response.data)
            .catch(error => console.error(error))*/
            let loadedTemplate = this.galleryTemplates.find(template => template.id === this.id)
            if(loadedTemplate) this.template = loadedTemplate
        },
        saveTemplate(){
            console.log('test', this.template)
        }
    }
})
</script>
