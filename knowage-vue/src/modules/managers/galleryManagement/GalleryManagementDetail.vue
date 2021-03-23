<template>
  <div class="managerDetail">
    <Toolbar class="kn-toolbar-secondary p-m-0">
      <template #left> Template {{ template.label }} </template>
      <template #right>
        <Button icon="pi pi-download" class="p-button-text p-button-rounded p-button-plain" @click="downloadTemplate" />
        <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="saveTemplate" />
        <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate($event)" />
      </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid">
      <div class="p-col-9">
        <Card>
          <template #title>
            {{ $t('common.information') }}
          </template>
          <template #content>
            <div class="p-grid">
              <div class="p-col-6">
                <span class="p-float-label">
                  <InputText id="label" class="kn-material-input" type="text" v-model="template.label" @change="setDirty" />
                  <label class="kn-material-input-label" for="label">{{ $t('common.label') }}</label>
                </span>
              </div>
              <div class="p-col-6">
                <span class="p-float-label">
                  <Dropdown
                    id="type"
                    class="kn-material-input"
                    v-model="template.type"
                    @change="setDirty"
                    :options="typeDescriptor.types"
                    optionLabel="name"
                    optionValue="value"
                  />
                  <label class="kn-material-input-label" for="type">{{ $t('common.type') }}</label>
                </span>
              </div>
              <div class="p-col-12">
                <span class="p-float-label">
                  <Textarea classv-model="template.description" class="kn-material-input" :autoResize="true" id="description" rows="3" @change="setDirty" />
                  <label class="kn-material-input-label" for="description">{{ $t('common.description') }}</label>
                </span>
              </div>
              <div class="p-col-12">
                <span class="p-float-label kn-material-input">
                  <Chips v-model="template.tags" @change="setDirty" />
                  <label class="kn-material-input-label" for="tags">{{ $t('common.tags') }}</label>
                </span>
              </div>
            </div>
          </template>
        </Card>
      </div>
      <div class="p-col-3 kn-height-full">
        <Card class="imageUploader">
          <template #title>
            {{ $t('common.image') }}
            <input id="inputImage" type="file" @change="uploadFile" />
            <label for="inputImage">
              <i class="pi pi-upload" />
            </label>
          </template>
          <template #content>
            <div class="imageContainer p-d-flex p-jc-center p-ai-center">
              <i class="far fa-image fa-5x icon" v-if="!template.image" />
              <img :src="template.image" v-if="template.image" height="100%" class="noselect" />
            </div>
          </template>
        </Card>
      </div>
    </div>
    <div class="p-grid p-m-0 flex" v-if="template.type && windowWidth < 1500">
      <TabView class="tabview-custom" style="width:100%">
        <TabPanel v-for="allowedEditor in typeDescriptor.allowedEditors[template.type]" v-bind:key="allowedEditor">
          <template #header>
            <i :class="['icon', typeDescriptor.editor[allowedEditor].icon]"></i>&nbsp;<span style="text-transform:uppercase">{{
              $t('common.codingLanguages.' + allowedEditor)
            }}</span>
          </template>
          <VCodeMirror
            class="flex"
            v-model:value="template.code[allowedEditor]"
            :options="typeDescriptor.options[allowedEditor]"
            @update:value="onCmCodeChange"
          />
        </TabPanel>
      </TabView>
    </div>
    <div class="p-grid p-m-0 flex" v-if="template.type && windowWidth >= 1500">
      <div
        :class="typeDescriptor.allowedEditors[template.type].length === 2 ? 'p-col-6' : 'p-col-4'"
        v-for="allowedEditor in typeDescriptor.allowedEditors[template.type]"
        v-bind:key="allowedEditor"
        style="height:100%;display:flex;flex-direction:column"
      >
        <h4>
          <i :class="['icon', typeDescriptor.editor[allowedEditor].icon]"></i>
          {{ $t('common.codingLanguages.' + allowedEditor) }}
        </h4>
        <VCodeMirror
          class="flex"
          v-model:value="template.code[allowedEditor]"
          :options="typeDescriptor.options[allowedEditor]"
          @update:value="onCmCodeChange"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Chips from 'primevue/chips'
import { VCodeMirror } from 'vue3-code-mirror'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Textarea from 'primevue/textarea'
import typeDescriptor from './typeDescriptor.json'

interface GalleryTemplate {
  id: string
  author: string
  label: string
  type: string
  description?: string
  code: Code
  tags?: Array<string>
  image: string | ArrayBuffer
}

interface Code {
  html: string
  python: string
  javascript: string
  css: string
}

export default defineComponent({
  name: 'gallery-management-detail',
  components: {
    Chips,
    VCodeMirror,
    Dropdown,
    InputText,
    TabView,
    TabPanel,
    Textarea
  },
  props: {
    id: String
  },
  data() {
    return {
      dirty: false,
      files: [],
      test: '',
      galleryTemplates: [],
      template: {} as GalleryTemplate,
      typeDescriptor: typeDescriptor,
      windowWidth: window.innerWidth
    }
  },
  created() {
    this.dirty = false
    this.loadTemplate(this.id)
    window.addEventListener('resize', this.resizeHandler)
  },
  methods: {
    download(content, fileName, contentType) {
      var a = document.createElement('a')
      var file = new Blob([content], { type: contentType })
      a.href = URL.createObjectURL(file)
      a.download = fileName
      a.click()
    },
    downloadTemplate(): void {
      if (this.dirty) {
        this.$confirm.require({
          message: 'Are you sure you want to proceed?',
          header: 'Confirmation',
          icon: 'pi pi-exclamation-triangle',
          accept: () => {
            this.$toast.add({
              severity: 'info',
              summary: 'Confirmed',
              detail: 'You have accepted',
              life: 3000
            })
          },
          reject: () => {
            this.$toast.add({
              severity: 'info',
              summary: 'Rejected',
              detail: 'You have rejected',
              life: 3000
            })
          }
        })
      } else {
        this.download(JSON.stringify(this.template), this.template.id + '.json', 'text/plain')
      }
    },
    closeTemplate(): void {
      this.$router.push('/knowage/gallerymanagement')
    },
    loadTemplate(id?: string): void {
      if (id) {
        this.axios
          .get(`/knowage-api/api/1.0/widgetgallery/${id || this.id}`)
          .then((response) => {
            this.template = response.data
          })
          .catch((error) => console.error(error))
      } else {
        this.template = { code: { html: '', css: '', javascript: '', python: '' } } as GalleryTemplate
      }
    },
    onCmCodeChange(): void {
      console.log('the code is changed ', '')
      this.setDirty()
    },
    saveTemplate() {
      let postUrl = this.id ? '/knowage-api/api/1.0/widgetgallery/' + this.id : '/knowage-api/api/1.0/widgetgallery/'
      this.axios
        .post(postUrl, this.template)
        .then((response) => {
          console.log(response)
        })
        .catch((error) => console.error(error))
    },
    setDirty() {
      this.dirty = true
    },
    uploadFile(event) {
      const reader = new FileReader()
      let self = this

      reader.addEventListener(
        'load',
        function() {
          // convert image file to base64 string
          self.template.image = reader.result || ''
        },
        false
      )

      if (event.srcElement.files[0]) {
        reader.readAsDataURL(event.srcElement.files[0])
      }
    },
    resizeHandler() {
      this.windowWidth = window.innerWidth
    }
  },
  watch: {
    '$route.params.id': function(id) {
      this.loadTemplate(id)
    }
  },
  unmounted() {
    window.removeEventListener('resize', this.resizeHandler)
  }
})
</script>

<style lang="scss" scoped>
.managerDetail {
  #inputImage {
    display: none;
  }
  label[for='inputImage'] {
    float: right;
    transition: background-color 0.3s linear;
    border-radius: 50%;
    width: 2.25rem;
    line-height: 1rem;
    top: -5px;
    height: 2.25rem;
    padding: 0.571rem;
    position: relative;
    cursor: pointer;
    user-select: none;
    &:hover {
      background-color: $color-secondary;
    }
  }
  &:deep() .p-tabview {
    display: flex;
    flex-direction: column;
    .p-tabview-panels {
      padding: 0;
      flex: 1;
      .p-tabview-panel {
        height: 100%;
        .v-code-mirror {
          height: 100%;
        }
      }
    }
  }
  &:deep() .CodeMirror {
    font-size: 0.8rem;
  }
  display: flex;
  height: 100%;
  flex-direction: column;
  .flex {
    flex: 1;
  }
  h4 {
    margin: 0;
    padding: 8px;
    background-color: #1a1b1f;
    color: #aaaebc;
    text-transform: uppercase;
  }
  &:deep() .imageUploader {
    .p-fileupload {
      display: inline-block;
      float: right;
      .p-button {
        background-color: transparent;
        color: black;
      }
    }
  }
  .imageContainer {
    height: 100%;
    .icon {
      color: $color-secondary;
    }
    img {
      height: auto;
      max-height: 100%;
    }
  }
  .codemirrorContainer {
    width: 100%;
    display: inline-flex;
    .editorContainer {
      flex: 1;
    }
  }
  &:deep() .p-card-content {
    height: 210px;
  }
}
</style>
