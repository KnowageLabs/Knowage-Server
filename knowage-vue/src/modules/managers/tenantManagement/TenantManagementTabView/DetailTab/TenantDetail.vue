<template>
    <Card :style="tabViewDescriptor.card.style">
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="MULTITENANT_NAME"
                            class="kn-material-input"
                            type="text"
                            :disabled="disableField"
                            v-model.trim="v$.tenant.MULTITENANT_NAME.$model"
                            :class="{
                                'p-invalid': v$.tenant.MULTITENANT_NAME.$invalid && v$.tenant.MULTITENANT_NAME.$dirty
                            }"
                            maxLength="20"
                            @blur="v$.tenant.MULTITENANT_NAME.$touch()"
                            @input="onFieldChange('MULTITENANT_NAME', $event.target.value)"
                            data-test="name-input"
                        />
                        <label for="MULTITENANT_NAME" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.tenant.MULTITENANT_NAME"
                        :additionalTranslateParams="{
                            fieldName: $t('common.name')
                        }"
                    />
                </div>

                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <Dropdown
                            id="MULTITENANT_THEME"
                            class="kn-material-input"
                            :options="themes"
                            optionLabel="VALUE_CHECK"
                            optionValue="VALUE_CHECK"
                            v-model="v$.tenant.MULTITENANT_THEME.$model"
                            :class="{
                                'p-invalid': v$.tenant.MULTITENANT_THEME.$invalid && v$.tenant.MULTITENANT_THEME.$dirty
                            }"
                            @before-show="v$.tenant.MULTITENANT_THEME.$touch()"
                            @change="onFieldChange('MULTITENANT_THEME', $event.value)"
                        >
                        </Dropdown>
                        <label for="MULTITENANT_THEME" class="kn-material-input-label"> {{ $t('managers.tenantManagement.detail.theme') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.tenant.MULTITENANT_THEME"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.tenantManagement.detail.theme')
                        }"
                    />
                </div>

                <div class="p-col-3 kn-height-full">
                    <input id="organizationImage" type="file" @change="uploadFile" accept="image/png, image/jpeg" />
                    <label for="organizationImage" v-tooltip.bottom="$t('common.upload')">
                        <i class="pi pi-upload" />
                    </label>
                    <div class="imageContainer p-d-flex p-jc-center p-ai-center">
                        <i class="far fa-image fa-5x icon" v-if="!tenant.MULTITENANT_IMAGE" />
                        <img :src="tenant.MULTITENANT_IMAGE" v-if="tenant.MULTITENANT_IMAGE" class="kn-no-select" />
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
import Dropdown from 'primevue/dropdown'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from '../TenantManagementTabViewDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import tenantDetailValidationDescriptor from './TenantDetailValidationDescriptor.json'
import { iMultitenant } from '../../TenantManagement'
import { AxiosResponse } from 'axios'

export default defineComponent({
    name: 'detail-tab',
    components: {
        Card,
        Dropdown,
        KnValidationMessages
    },
    props: {
        selectedTenant: {
            type: Object,
            required: false
        },
        listOfThemes: Array
    },
    computed: {
        disableField() {
            if (this.tenant.MULTITENANT_ID) return true
            return false
        }
    },
    emits: ['fieldChanged', 'roleTypeChanged'],
    data() {
        return {
            tabViewDescriptor,
            tenantDetailValidationDescriptor,
            v$: useValidate() as any,
            tenant: {} as iMultitenant,
            themes: [] as any
        }
    },
    validations() {
        return {
            tenant: createValidations('tenant', tenantDetailValidationDescriptor.validations.tenant)
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
    watch: {
        async selectedTenant() {
            this.v$.$reset()
            this.tenant = { ...this.selectedTenant } as iMultitenant
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `multitenant/image?TENANT=${this.tenant.MULTITENANT_NAME}`).then((response: AxiosResponse<any>) => {
                this.tenant.MULTITENANT_IMAGE = response.data
            })
        },
        listOfThemes() {
            this.themes = [...(this.listOfThemes as any[])]
        }
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
            if (event.srcElement.files[0] && event.srcElement.files[0].size < process.env.VUE_APP_MAX_UPLOAD_IMAGE_SIZE) {
                reader.readAsDataURL(event.srcElement.files[0])
                this.v$.$touch()
            } else this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('common.error.exceededSize', { size: '(200KB)' }) })
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
