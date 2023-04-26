<template>
    <Card :style="tabViewDescriptor.card.style">
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="MULTITENANT_NAME"
                            v-model.trim="v$.tenant.MULTITENANT_NAME.$model"
                            class="kn-material-input"
                            type="text"
                            :disabled="disableField"
                            :class="{
                                'p-invalid': v$.tenant.MULTITENANT_NAME.$invalid && v$.tenant.MULTITENANT_NAME.$dirty
                            }"
                            max-length="20"
                            data-test="name-input"
                            @blur="v$.tenant.MULTITENANT_NAME.$touch()"
                            @input="onFieldChange('MULTITENANT_NAME', $event.target.value)"
                        />
                        <label for="MULTITENANT_NAME" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :v-comp="v$.tenant.MULTITENANT_NAME"
                        :additional-translate-params="{
                            fieldName: $t('common.name')
                        }"
                    />
                </div>
                <div class="p-col-3 kn-height-full">
                    <input id="organizationImage" type="file" accept="image/png, image/jpeg" @change="uploadFile" />
                    <label v-tooltip.bottom="$t('common.upload')" for="organizationImage">
                        <i class="pi pi-upload" />
                    </label>
                    <div class="imageContainer p-d-flex p-jc-center p-ai-center">
                        <i v-if="!tenant.MULTITENANT_IMAGE" class="far fa-image fa-5x icon" />
                        <img v-if="tenant.MULTITENANT_IMAGE" :src="tenant.MULTITENANT_IMAGE" class="kn-no-select" />
                    </div>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import Card from 'primevue/card'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from '../TenantManagementTabViewDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import tenantDetailValidationDescriptor from './TenantDetailValidationDescriptor.json'
import { iMultitenant } from '../../TenantManagement'
import { AxiosResponse } from 'axios'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'detail-tab',
    components: {
        Card,
        KnValidationMessages
    },
    props: {
        selectedTenant: {
            type: Object,
            required: false
        },
        listOfThemes: Array
    },
    emits: ['fieldChanged', 'roleTypeChanged'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            tabViewDescriptor,
            tenantDetailValidationDescriptor,
            v$: useValidate() as any,
            tenant: {} as iMultitenant,
            themes: [] as any
        }
    },
    computed: {
        disableField() {
            if (this.tenant.MULTITENANT_ID) return true
            return false
        }
    },
    validations() {
        return {
            tenant: createValidations('tenant', tenantDetailValidationDescriptor.validations.tenant)
        }
    },
    watch: {
        async selectedTenant() {
            this.v$.$reset()
            this.tenant = { ...this.selectedTenant } as iMultitenant
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `multitenant/image?TENANT=${this.tenant.MULTITENANT_NAME}`).then((response: AxiosResponse<any>) => {
                this.tenant.MULTITENANT_IMAGE = response.data
            })
        },
        listOfThemes() {
            this.themes = [...(this.listOfThemes as any[])]
        }
    },
    created() {
        if (this.selectedTenant && Object.keys(this.selectedTenant).length > 0) {
            this.tenant = { ...this.selectedTenant } as iMultitenant
        } else {
            this.tenant = {} as iMultitenant
            this.tenant.MULTITENANT_THEME = 'sbi_default'
        }
        if (this.listOfThemes) this.themes = [...this.listOfThemes] as any
    },
    methods: {
        onFieldChange(fieldName: string, value: any) {
            this.$emit('fieldChanged', { fieldName, value })
        },
        uploadFile(event): void {
            const reader = new FileReader()
            reader.addEventListener(
                'load',
                () => {
                    this.tenant.MULTITENANT_IMAGE = reader.result || ''
                    this.onFieldChange('MULTITENANT_IMAGE', this.tenant.MULTITENANT_IMAGE)
                },
                false
            )
            if (event.srcElement.files[0] && event.srcElement.files[0].size < import.meta.env.VITE_MAX_UPLOAD_IMAGE_SIZE) {
                reader.readAsDataURL(event.srcElement.files[0])
                this.v$.$touch()
            } else this.store.setError({ title: this.$t('common.error.uploading'), msg: this.$t('common.error.exceededSize', { size: '(200KB)' }) })
        }
    }
})
</script>

<style lang="scss" scoped>
#organizationImage {
    display: none;
}
label[for='organizationImage'] {
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
        background-color: var(--kn-color-secondary);
    }
}
.imageUploader {
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
        color: var(--kn-color-secondary);
    }
    img {
        height: auto;
        max-height: 80px;
        max-width: 80px;
        border-radius: 50%;
    }
}
</style>
