<template>
  <div class="managerDetail">
    <Toolbar class="kn-toolbar-secondary p-m-0">
        <template #left>
            Template {{template.label}}
        </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="saveTemplate" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate($event)"/>
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid">
        <div  class="p-col-9">
        <Card >
            <template #title>
                {{$t('common.information')}}
            </template>
            <template #content>
                <div class="p-grid">
                    <div class="p-col-6">
                        <span class="p-float-label">
                            <InputText id="label" class="kn-material-input" type="text" v-model="template.label" />
                            <label class="kn-material-input-label" for="label">{{$t('common.label')}}</label>
                        </span>
                    </div>
                    <div class="p-col-6">
                        <span class="p-float-label">
                            <InputText id="type" class="kn-material-input" type="text" v-model="template.type" />
                            <label class="kn-material-input-label" for="type">{{$t('common.type')}}</label>
                        </span>
                    </div>

                    <div class="p-col-12">
                        <span class="p-float-label">
                            <Textarea classv-model="template.description" class="kn-material-input" :autoResize="true" id="description" rows="3" />
                            <label class="kn-material-input-label" for="description">{{$t('common.description')}}</label>
                        </span>
                    </div>

                    <div class="p-col-12">
                        <span class="p-float-label kn-material-input">
                            <Chips v-model="template.tags"/>
                            <label class="kn-material-input-label" for="tags">{{$t('common.tags')}}</label>
                        </span>
                    </div>
                </div>
            </template>
        </Card>
        </div>
        <div class="p-col-3 kn-height-full">
         <Card>
            <template #title>
                {{$t('common.image')}}
                <Button icon="fas fa-upload" class="p-button-text" />
            </template>
             <template #content>
                <Skeleton size="10rem"></Skeleton>
             </template>
         </Card>
        </div>
        <div  class="p-col-12">
        <Splitter style="height: 300px">
            <SplitterPanel :size="100" >
                {{$t('common.codingLanguages.html')}}
                <div ref="htmlCodemirror" ></div>
            </SplitterPanel>
            <SplitterPanel :size="100">
                {{$t('common.codingLanguages.js')}}
                <div ref="jsCodemirror"></div>
            </SplitterPanel>
            <SplitterPanel :size="100">
                {{$t('common.codingLanguages.css')}}
            </SplitterPanel>
        </Splitter>
        </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Chips from 'primevue/chips'
import CodeMirror from 'codemirror/src/codemirror'
import InputText from 'primevue/inputtext'
import router from '@/App.routes'
import Skeleton from 'primevue/skeleton'
import Splitter from 'primevue/splitter'
import SplitterPanel from 'primevue/splitterpanel'
import Textarea from 'primevue/textarea'

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
        Skeleton,
        Splitter,
        SplitterPanel,
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
                mode: 'javascript',
                theme: 'base16-dark',
                lineNumbers: true,
                line: true,
            },
            code: '<div>ciao</div>',
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
    mounted(){
        CodeMirror(this.$refs.htmlCodemirror, {
            lineNumbers: true,
            tabSize: 2,
            value: this.code,
            mode: ['html'],
            theme: 'monokai'
        });
        CodeMirror(this.$refs.jsCodemirror, {
            lineNumbers: true,
            tabSize: 2,
            value: 'var ciao = 7;',
            mode: 'text/javascript',
            theme: 'monokai'
        });
    },
    updated() {
        this.loadTemplate()
    },
    methods: {
        closeTemplate(){
            router.push('/knowage/gallerymanagement')
        },
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
