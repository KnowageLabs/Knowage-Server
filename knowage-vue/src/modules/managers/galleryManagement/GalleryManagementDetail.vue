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
            {{ $t("common.information") }}
          </template>
          <template #content>
            <div class="p-grid">
              <div class="p-col-6">
                <span class="p-float-label">
                  <InputText id="label" class="kn-material-input" type="text" v-model="template.label" @change="setDirty" />
                  <label class="kn-material-input-label" for="label">{{ $t("common.label") }}</label>
                </span>
              </div>
              <div class="p-col-6">
                <span class="p-float-label">
                  <InputText id="type" class="kn-material-input" type="text" v-model="template.type" @change="setDirty" />
                  <label class="kn-material-input-label" for="type">{{ $t("common.type") }}</label>
                </span>
              </div>
              <div class="p-col-12">
                <span class="p-float-label">
                  <Textarea classv-model="template.description" class="kn-material-input" :autoResize="true" id="description" rows="3" @change="setDirty" />
                  <label class="kn-material-input-label" for="description">{{ $t("common.description") }}</label>
                </span>
              </div>
              <div class="p-col-12">
                <span class="p-float-label kn-material-input">
                  <Chips v-model="template.tags" @change="setDirty" />
                  <label class="kn-material-input-label" for="tags">{{ $t("common.tags") }}</label>
                </span>
              </div>
            </div>
          </template>
        </Card>
      </div>
      <div class="p-col-3 kn-height-full">
        <Card>
          <template #title>
            {{ $t("common.image") }}

            <!--             <Button
              icon="fas fa-upload"
              class="p-button-text"
              @click="handleSubmit"
            /> -->
            <FileUpload ref="fileupload" mode="basic" name="demo[]" accept="image/*" :maxFileSize="100000" :fileLimit="1" :customUpload="true" @uploader="uploadFile" invalidFileSizeMessage="Invalid file size message" invalidFileLimitMessage="Invalid file limit message" chooseLabel="" :auto="true" showCancelButton="true" />
          </template>
          <template #content>
            <i class="far fa-image fa-7x" v-if="!template.image" />
            <img :src="template.image" v-if="template.image" />
          </template>
        </Card>
      </div>
      <div class="p-col-12">
        <Splitter style="height: 300px">
          <SplitterPanel :size="100" :minSize="100" v-for="allowedEditor in typeDescriptor.allowedEditors[template.type]" v-bind:key="allowedEditor">
            <h4><i :class="['icon',typeDescriptor.editor[allowedEditor].icon]"></i> {{ $t("common.codingLanguages." + allowedEditor) }}</h4>
            <CodeMirror :ref="`${allowedEditor}Editor_${template.id}`" :value="template.code[allowedEditor]" :options="typeDescriptor.options[allowedEditor]" @ready="onCmReady" @focus="onCmFocus" @input="onCmCodeChange" />
          </SplitterPanel>
        </Splitter>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import Chips from "primevue/chips";
import CodeMirror from "@/components/codemirror/Codemirror.vue";
import FileUpload from "primevue/fileupload";
import InputText from "primevue/inputtext";
import router from "@/App.routes";
import Splitter from "primevue/splitter";
import SplitterPanel from "primevue/splitterpanel";
import Textarea from "primevue/textarea";
import typeDescriptor from "./typeDescriptor.json";

interface GalleryTemplate {
  id: string;
  author: string;
  label: string;
  type: string;
  description?: string;
  code: Code;
  tags?: Array<string>;
  image: string;
}

interface Code {
  html: string;
  python: string;
  javascript: string;
  css: string;
}

export default defineComponent({
  name: "gallery-management-detail",
  components: {
    Chips,
    CodeMirror,
    FileUpload,
    InputText,
    Splitter,
    SplitterPanel,
    Textarea
  },
  props: {
    id: String
  },
  data() {
    return {
      dirty: false,
      files: [],
      galleryTemplates: [],
      template: {} as GalleryTemplate,
      typeDescriptor: typeDescriptor,
      //editorHtml: CodeMirror(),
      //editorJavascript: CodeMirror(),
      //editorPython: CodeMirror(),
      //editorCss: CodeMirror(),
    };
  },
  created() {
    this.dirty = false;
    this.loadTemplate();
  },
  mounted() {
    //this.editorHtml = CodeMirror();
    //this.editorJavascript = CodeMirror();
    //this.editorPython = CodeMirror();
    //this.editorCss = CodeMirror();
  },
  updated() {
    this.loadTemplate();
    this.updateTemplates();
  },
  methods: {
    download(content, fileName, contentType) {
      var a = document.createElement("a");
      var file = new Blob([content], { type: contentType });
      a.href = URL.createObjectURL(file);
      a.download = fileName;
      a.click();
    },
    downloadTemplate() {
      if (this.dirty) {
        this.$confirm.require({
          message: "Are you sure you want to proceed?",
          header: "Confirmation",
          icon: "pi pi-exclamation-triangle",
          accept: () => {
            this.$toast.add({
              severity: "info",
              summary: "Confirmed",
              detail: "You have accepted",
              life: 3000
            });
          },
          reject: () => {
            this.$toast.add({
              severity: "info",
              summary: "Rejected",
              detail: "You have rejected",
              life: 3000
            });
          }
        });
      } else {
        this.download(JSON.stringify(this.template), this.template.id + ".json", "text/plain");
      }
    },
    closeTemplate() {
      router.push("/knowage/gallerymanagement");
    },
    loadTemplate() {
      this.axios
        .get(`/knowage-api/api/1.0/widgetgallery/${this.id}`)
        .then(response => {
          this.template = response.data;
          this.createEditors();
        })
        .catch(error => console.error(error));
    },
    onCmReady(cm) {
      console.log("the editor is readied!", cm);
    },
    onCmFocus(cm) {
      console.log("the editor is focused!", cm);
    },
    onCmCodeChange() {
      console.log("the code is changed ", "");
      this.setDirty();
    },
    saveTemplate() {
      console.log("test", this.template);
    },
    setDirty() {
      this.dirty = true;
    },
    uploadFile(event) {
      this.template.image = event.files[0].objectURL;
      this.$toast.add({
        severity: "info",
        summary: "Success",
        detail: "File Uploaded",
        life: 3000
      });
    },
    updateTemplates() {
      //      this.template.code.html = this.editor1.getValue();
      //      this.template.code.javascript = this.editor2.getValue();
      //      this.template.code.css = this.editor3.getValue();
      //      this.template.code.python = this.editor4.getValue();
    },
    createEditors() {
      /*       this.editorHtml = this.createEditor(this.editorHtml, "html");
      this.editorJavascript = this.createEditor(
        this.editorJavascript,
        "javascript"
      );
      this.editorPython = this.createEditor(this.editorPython, "python");
      this.editorCss = this.createEditor(this.editorCss, "css"); */
    }
    /* createEditor(editorHtml, type) {
      if (
        this.typeDescriptor.allowedEditors[this.template.type].indexOf(type) !=
        -1
      ) {
        if (editorHtml.options.mode == null) {
          editorHtml = CodeMirror(this.typeDescriptor.editor[type].name, {
            lineNumbers: true,
            tabSize: 2,
            value: "",
            mode: { name: "htmlmixed" },
            theme: "monokai",
          });

          editorHtml.on("changes", () => {
            this.template.code[type] = editorHtml.getValue();
          });
        } else {
          if (this.template.code[type]) {
            editorHtml.setValue(this.template.code[type]);
          } else {
            editorHtml.setValue("");
          }

          editorHtml.readOnly = false;
        }
        this.showEditorHtml = true;
      } else {
        editorHtml = CodeMirror();
        this.showEditorHtml = false;
      }
      return editorHtml;
    },
    createEditorHtml() {
      if (
        this.typeDescriptor.allowedEditors[this.template.type].indexOf(
          "html"
        ) != -1
      ) {
        if (this.editorHtml.options.mode == null) {
          this.editorHtml = CodeMirror(this.$refs.htmlCodemirror, {
            lineNumbers: true,
            tabSize: 2,
            value: "",
            mode: { name: "htmlmixed" },
            theme: "monokai",
          });

          this.editorHtml.on("changes", () => {
            this.template.code.html = this.editorHtml.getValue();
          });
        } else {
          if (this.template.code.html) {
            this.editorHtml.setValue(this.template.code.html);
          } else {
            this.editorHtml.setValue("");
          }

          this.editorHtml.readOnly = false;
        }
      } else {
        this.editorHtml = CodeMirror();
      }
    },
    createEditorJavascript() {
      if (
        this.typeDescriptor.allowedEditors[this.template.type].indexOf(
          "javascript"
        ) != -1
      ) {
        if (this.editorJavascript.options.mode == null) {
          this.editorJavascript = CodeMirror(this.$refs.javascriptCodemirror, {
            lineNumbers: true,
            tabSize: 2,
            value: "",
            mode: { name: "text/javascript" },
            theme: "monokai",
          });

          this.editorJavascript.on("changes", () => {
            this.template.code.javascript = this.editorJavascript.getValue();
          });
        } else {
          if (this.template.code.javascript) {
            this.editorJavascript.setValue(this.template.code.javascript);
          } else {
            this.editorJavascript.setValue("");
          }
        }
      } else {
        this.editorJavascript = CodeMirror();
      }
    },
    createEditorPython() {
      if (
        this.typeDescriptor.allowedEditors[this.template.type].indexOf(
          "python"
        ) != -1
      ) {
        if (this.editorPython.options.mode == null) {
          this.editorPython = CodeMirror(this.$refs.pythonCodemirror, {
            lineNumbers: true,
            tabSize: 2,
            value: "",
            mode: { name: "python" },
            theme: "monokai",
          });

          this.editorPython.on("changes", () => {
            this.template.code.python = this.editorPython.getValue();
          });
        } else {
          if (this.template.code.python) {
            this.editorPython.setValue(this.template.code.python);
          } else {
            this.editorPython.setValue("");
          }
        }
      } else {
        this.editorPython = CodeMirror();
      }
    },
    createEditorCss() {
      if (
        this.typeDescriptor.allowedEditors[this.template.type].indexOf("css") !=
        -1
      ) {
        if (this.editorCss.options.mode == null) {
          this.editorCss = CodeMirror(this.$refs.cssCodemirror, {
            lineNumbers: true,
            tabSize: 2,
            value: "",
            mode: { name: "css" },
            theme: "monokai",
          });

          this.editorCss.on("changes", () => {
            this.template.code.css = this.editorCss.getValue();
          });
        } else {
          if (this.template.code.css) {
            this.editorCss.setValue(this.template.code.css);
          } else {
            this.editorCss.setValue("");
          }
        }
      } else {
        this.editorCss = CodeMirror();
      }
    }, */
  }
});
</script>
<style lang="scss" scoped>
  .managerDetail {
    h4{
      margin: 0;
      padding: 8px;
      background-color: #1A1B1F;
      color: #AAAEBC;
      text-transform: uppercase;
    }
  }

</style>
