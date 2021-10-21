<template>
    <form class="p-fluid p-ai-end p-formgrid p-grid p-m-2" v-if="selectedFunction">
        <div class="p-field p-col-6 p-mb-6">
            <span class="p-float-label">
                <InputText id="name" class="kn-material-input" v-model.trim="selectedFunction.name" :disabled="readonly" />
                <label for="name" class="kn-material-input-label"> {{ $t('managers.functionsCatalog.functionName') }} </label>
                <small class="hint">{{ $t('managers.functionsCatalog.functionNameHint') }}</small>
            </span>
        </div>
        <div class="p-field p-col-6 p-mb-6">
            <span class="p-float-label">
                <InputText id="label " class="kn-material-input" v-model.trim="selectedFunction.label" :disabled="readonly" />
                <label for="label " class="kn-material-input-label"> {{ $t('common.label') }} </label>
                <small class="hint">{{ $t('managers.functionsCatalog.functionLabelHint') }}</small>
            </span>
        </div>
        <div class="p-field p-col-6 p-mb-6">
            <span class="p-float-label">
                <InputText id="owner " class="kn-material-input" v-model.trim="selectedFunction.owner" :disabled="true" />
                <label for="owner " class="kn-material-input-label"> {{ $t('common.owner') }} </label>
            </span>
        </div>
        <div class="p-field p-col-6 p-mb-6">
            <span>
                <label for="type" class="kn-material-input-label">{{ $t('common.type') }}</label>
                <Dropdown id="type" class="kn-material-input" v-model="selectedFunction.type" :options="functionTypes" optionLabel="valueCd" optionValue="valueCd" :disabled="readonly" />
            </span>
        </div>
        <div class="p-field p-col-12 p-mb-12">
            <span>
                <label for="keywords" class="kn-material-input-label"> {{ $t('managers.functionsCatalog.keywords') }}</label>
                <Chips id="keywords" class="p-inputtext-sm" :multiple="true" v-model="selectedFunction.tags" :disabled="readonly" :placeholder="$t('managers.functionsCatalog.keywords')" />
            </span>
        </div>
        <div class="p-field p-col-12 p-mb-12">
            <Accordion>
                <AccordionTab :header="$t('common.description') + ' *'">
                    <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} *</label>
                    <Textarea
                        v-if="showDescriptionSource"
                        v-model="selectedFunction.description"
                        :style="functionsCatalogGeneralTabDescriptor.editor.style"
                        :class="{
                            'p-invalid': selectedFunction.description.length === 0 && descriptionDirty
                        }"
                        :readonly="readonly"
                        @click="descriptionDirty = true"
                        @input="descriptionDirty = true"
                    ></Textarea>
                    <Editor
                        v-else
                        id="description"
                        :editorStyle="functionsCatalogGeneralTabDescriptor.editor.style"
                        v-model="selectedFunction.description"
                        :class="{
                            'p-invalid': selectedFunction.description.length === 0 && descriptionDirty
                        }"
                        :readonly="readonly"
                        @click="descriptionDirty = true"
                        @input="descriptionDirty = true"
                    />
                    <Button class="editor-switch-button" icon="pi pi-bars" :label="showDescriptionSource ? 'wysiwyg' : $t('common.source')" @click="showDescriptionSource = !showDescriptionSource" />
                </AccordionTab>
            </Accordion>
            <div v-if="selectedFunction.description.length === 0 && descriptionDirty" class="p-error p-grid p-m-2">
                {{ $t('common.validation.required', { fieldName: $t('common.description') }) }}
            </div>
        </div>
        <div class="p-field p-col-12 p-mb-12">
            <Accordion>
                <AccordionTab :header="$t('managers.functionsCatalog.benchmarks')">
                    <label for="benchmarks" class="kn-material-input-label"> {{ $t('managers.functionsCatalog.benchmarks') }}</label>
                    <Textarea v-if="showBenchmarksSource" v-model="selectedFunction.benchmark" :style="functionsCatalogGeneralTabDescriptor.editor.style" :readonly="readonly"></Textarea>
                    <Editor v-else id="benchmarks" :editorStyle="functionsCatalogGeneralTabDescriptor.editor.style" v-model="selectedFunction.benchmark" :readonly="readonly" />
                    <Button class="editor-switch-button" icon="pi pi-bars" :label="showBenchmarksSource ? 'wysiwyg' : $t('common.source')" @click="showBenchmarksSource = !showBenchmarksSource"></Button>
                </AccordionTab>
            </Accordion>
        </div>
    </form>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from '../../FunctionsCatalog'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Chips from 'primevue/chips'
import Dropdown from 'primevue/dropdown'
import Editor from 'primevue/editor'
import functionsCatalogGeneralTabDescriptor from './FunctionsCatalogGeneralTabDescriptor.json'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'function-catalog-general-tab',
    components: { Accordion, AccordionTab, Chips, Dropdown, Editor, Textarea },
    props: { propFunction: { type: Object }, readonly: { type: Boolean }, functionTypes: { type: Array }, propKeywords: { type: Array } },
    data() {
        return {
            functionsCatalogGeneralTabDescriptor,
            selectedFunction: {} as iFunction,
            descriptionDirty: false,
            showDescriptionSource: false,
            showBenchmarksSource: false
        }
    },
    created() {
        this.loadFunction()
    },
    methods: {
        loadFunction() {
            this.selectedFunction = this.propFunction as iFunction
            if (this.selectedFunction.tags && this.selectedFunction.tags[0] === '') {
                this.selectedFunction.tags = []
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.hint {
    color: gray;
    font-size: 0.8rem;
}

.editor-switch-button {
    max-width: 100px;
    background-color: grey;
}
</style>
