<template>
    <Card :style="tabViewDescriptor.card.style">
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="TENANT_NAME"
                            v-model.trim="v$.tenant.TENANT_NAME.$model"
                            class="kn-material-input"
                            type="text"
                            :disabled="disableField"
                            :class="{
                                'p-invalid': v$.tenant.TENANT_NAME.$invalid && v$.tenant.TENANT_NAME.$dirty
                            }"
                            max-length="20"
                            data-test="name-input"
                            @blur="v$.tenant.TENANT_NAME.$touch()"
                            @input="onFieldChange('TENANT_NAME', $event.target.value)"
                        />
                        <label for="TENANT_NAME" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :v-comp="v$.tenant.TENANT_NAME"
                        :additional-translate-params="{
                            fieldName: $t('common.name')
                        }"
                    />
                </div>
                <div class="p-col-12 kn-height-full">
                    <label class="kn-material-input-label">Tenant Logo</label>
                    <div>
                        <small>The company/product logo, visible on the menu bar. If not present the default Knowage "K" will be shown.</small>
                    </div>
                   
                    <div class="imageContainer p-d-flex p-jc-center p-ai-center">
                        <i v-if="!tenant.TENANT_IMAGE" class="far fa-image fa-5x icon" />
                        <img v-if="tenant.TENANT_IMAGE" :src="tenant.TENANT_IMAGE" class="kn-no-select" />
                        <input id="organizationImage" type="file" accept="image/png, image/jpeg" @change="uploadFile" />
                        <label v-tooltip.bottom="$t('common.upload')" for="organizationImage">
                            <i class="pi pi-upload"></i>
                        </label>
                    </div>
                </div>
                <div class="p-col-12 kn-height-full">
                    <label class="kn-material-input-label">Tenant Logo Wide</label>
                    <div>
                        <small>This logo will be used within dashboards XLSX exports. If no image is provided it will not be visible in those instances. The images should be below 200KB and in jpg or png format.</small>
                    </div>
                   
                    <div class="imageContainerExtended p-d-flex p-jc-center p-ai-center">
                        <i v-if="!tenant.TENANT_IMAGE_WIDE" class="far fa-image fa-5x icon" />
                        <img v-if="tenant.TENANT_IMAGE_WIDE" :src="tenant.TENANT_IMAGE_WIDE" class="kn-no-select" />
                        <input id="organizationImageExtended" type="file" accept="image/png, image/jpeg" @change="uploadExtendedFile" />
                        <label v-tooltip.bottom="$t('common.upload')" for="organizationImageExtended">
                            <i class="pi pi-upload"></i>
                        </label>
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
import { iTenant } from '../../TenantManagement'
import { AxiosResponse } from 'axios'

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
    data() {
        return {
            tabViewDescriptor,
            tenantDetailValidationDescriptor,
            v$: useValidate() as any,
            tenant: {} as iTenant,
            themes: [] as any
        }
    },
    computed: {
        disableField() {
            if (this.tenant.TENANT_ID) return true
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
            this.tenant = { ...this.selectedTenant } as iTenant
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `multitenant/image?TENANT=${this.tenant.TENANT_NAME}`).then((response: AxiosResponse<any>) => {
                this.tenant.TENANT_IMAGE = response.data
            })
        },
        listOfThemes() {
            this.themes = [...(this.listOfThemes as any[])]
        }
    },
    created() {
        if (this.selectedTenant && Object.keys(this.selectedTenant).length > 0) {
            this.tenant = { ...this.selectedTenant } as iTenant
        } else {
            this.tenant = {} as iTenant
            this.tenant.TENANT_THEME = 'sbi_default'
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
                    this.tenant.TENANT_IMAGE = reader.result || ''
                    this.onFieldChange('TENANT_IMAGE', this.tenant.TENANT_IMAGE)
                },
                false
            )
            if (event.srcElement.files[0] && event.srcElement.files[0].size < process.env.VUE_APP_MAX_UPLOAD_IMAGE_SIZE) {
                reader.readAsDataURL(event.srcElement.files[0])
                this.v$.$touch()
            } else this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('common.error.exceededSize', { size: '(200KB)' }) })
        },

        uploadExtendedFile(event): void {
            const reader = new FileReader()
            reader.addEventListener(
                'load',
                () => {
                    this.tenant.TENANT_IMAGE_WIDE = reader.result || ''
                    this.onFieldChange('TENANT_IMAGE', this.tenant.TENANT_IMAGE_WIDE)
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
#organizationImage, #organizationImageExtended {
    display: none;
}
label[for='organizationImage'], label[for='organizationImageExtended'] {
    position: absolute;
    top: 0;
    right: -36px;
    border-radius: 50%;
    border: 1px solid var(--kn-color-primary);
    color: var(--kn-color-primary);
    cursor: pointer;
    height: 36px;
    width: 36px;
    padding: 8px;
    &:hover {
        background-color: rgba(var(--kn-color-primary),.2);
    }

}
.imageUploader {
    .p-fileupload {
        display: inline-block;
        .p-button {
            background-color: transparent;
            color: black;
        }
    }
}
.imageContainer, .imageContainerExtended {
    height: 90px;
    width: 300px;
    position: relative;
    margin: 16px 0;
    border: 1px solid #aaa;
    padding: 2px;
    .icon {
        color: var(--kn-color-secondary);
    }
    img {
        height: auto;
        max-height: 80px;
        max-width: 80px;
    }
}

.imageContainerExtended {
    height: 200px;
    width: 500px;
    img {
        height: auto;
        max-height: 98%;
        max-width: 98%;
    }
}
</style>
