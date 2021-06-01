<template>
    <Card :style="tabViewDescriptor.card.style">
        <template #content>
            <form class="p-fluid p-m-3">
                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.schema.name.$model"
                            :class="{
                                'p-invalid': v$.schema.name.$invalid && v$.schema.name.$dirty
                            }"
                            maxLength="100"
                            @blur="v$.schema.name.$touch()"
                            @input="onFieldChange('name', $event.target.value)"
                            data-test="name-input"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.mondrianSchemasManagement.detail.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.schema.name"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.mondrianSchemasManagement.detail.name')
                        }"
                    />
                </div>

                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.schema.description.$model"
                            :class="{
                                'p-invalid': v$.schema.description.$invalid && v$.schema.description.$dirty
                            }"
                            maxLength="500"
                            @blur="v$.schema.description.$touch()"
                            @input="onFieldChange('description', $event.target.value)"
                            data-test="description-input"
                        />
                        <label for="description" class="kn-material-input-label">
                            {{ $t('managers.mondrianSchemasManagement.detail.description') }}
                        </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.schema.description"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.mondrianSchemasManagement.detail.description')
                        }"
                    />
                </div>

                <div class="p-field">
                    <span class="p-float-label">
                        <FileUpload name="file" url="./upload.php" @upload="onUpload">
                            <template #empty>
                                <p>Drag and drop files to here to upload.</p>
                            </template>
                        </FileUpload>
                    </span>
                </div>
            </form>
            <!-- === SCHEMA === <br />{{ this.schema }} <br /><br />
            === SELECTED === <br />{{ this.selectedSchema }} -->
        </template>
    </Card>
    <Card :style="tabViewDescriptor.card.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    Saved versions
                </template>
            </Toolbar>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { iSchema } from '../MondrianSchemas'
import axios from 'axios'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from '../MondrianSchemasTabViewDescriptor.json'
import validationDescriptor from './MondrianSchemasDetailDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Card from 'primevue/card'
import FileUpload from 'primevue/fileupload'

export default defineComponent({
    name: 'detail-tab',
    components: {
        Card,
        KnValidationMessages,
        FileUpload
    },
    props: {
        selectedSchema: {
            type: Object,
            requried: false
        }
    },
    emits: ['fieldChanged'],
    data() {
        return {
            loading: false,
            tabViewDescriptor,
            validationDescriptor,
            v$: useValidate() as any,
            schema: {} as iSchema,
            versions: {} as any,
            selectedVersion: null
        }
    },
    validations() {
        return {
            schema: createValidations('schema', validationDescriptor.validations.schema)
        }
    },
    mounted() {
        if (this.selectedSchema) {
            this.schema = { ...this.selectedSchema } as iSchema
        }
    },
    watch: {
        selectedSchema() {
            this.schema = { ...this.selectedSchema } as iSchema
            this.loadVersions()
        }
    },
    methods: {
        onFieldChange(fieldName: string, value: any) {
            this.$emit('fieldChanged', { fieldName, value })
        },
        async loadVersions() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.schema.id}` + '/versions')
                .then((response) => {
                    this.versions = response.data
                })
                .finally(() => (this.loading = false))
        }
    }
})
</script>
