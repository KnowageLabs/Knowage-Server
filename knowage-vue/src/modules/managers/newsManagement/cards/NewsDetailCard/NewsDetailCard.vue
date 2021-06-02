<template>
    <Card :style="newsDetailCardDescriptor.card.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('managers.newsManagement.settings') }}
                </template>
                <template #right>
                    <InputSwitch id="active" v-model="news.active" @change="onActiveChange" data-test="active-input" />
                    <label for="active" class="kn-material-input-label p-ml-3"> {{ $t('managers.newsManagement.active') }}</label>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field p-d-flex">
                    <div :style="newsDetailCardDescriptor.input.title.style">
                        <span class="p-float-label">
                            <InputText
                                id="title"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.news.title.$model"
                                :class="{
                                    'p-invalid': v$.news.title.$invalid && v$.news.title.$dirty
                                }"
                                @blur="v$.news.title.$touch()"
                                @input="onFieldChange('title', $event.target.value)"
                                data-test="title-input"
                            />
                            <label for="title" class="kn-material-input-label"> {{ $t('managers.newsManagement.form.title') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.news.title"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.newsManagement.form.title')
                            }"
                        />
                    </div>

                    <div :style="newsDetailCardDescriptor.input.expirationDate.style">
                        <span class="p-float-label">
                            <Calendar
                                id="expirationDate"
                                class="kn-material-input"
                                type="text"
                                v-model="v$.news.expirationDate.$model"
                                :class="{
                                    'p-invalid': v$.news.expirationDate.$invalid && v$.news.expirationDate.$dirty
                                }"
                                :showIcon="true"
                                @blur="v$.news.expirationDate.$touch()"
                                @dateSelect="onFieldChange('expirationDate', moment($event).unix())"
                                data-test="expiration-input"
                            />
                            <label for="expirationDate" id="calendar-label"> {{ $t('managers.newsManagement.form.expirationDate') }} * </label>
                        </span>

                        <KnValidationMessages
                            :vComp="v$.news.expirationDate"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.newsManagement.form.expirationDate')
                            }"
                        />
                    </div>

                    <div :style="newsDetailCardDescriptor.input.type.style">
                        <span class="p-float-label">
                            <Dropdown
                                id="type"
                                class="kn-material-input"
                                :class="{
                                    'p-invalid': v$.news.type.$invalid && v$.news.type.$dirty
                                }"
                                v-model="v$.news.type.$model"
                                :options="newsDetailCardDescriptor.newsTypes"
                                placeholder=" "
                                @before-show="v$.news.type.$touch()"
                                @change="onFieldChange('type', $event.value)"
                            >
                                <template #value="slotProps">
                                    <div v-if="slotProps.value">
                                        <span>{{ slotProps.value.value }}</span>
                                    </div>
                                </template>
                                <template #option="slotProps">
                                    <div>
                                        <span>{{ slotProps.option.value }}</span>
                                    </div>
                                </template></Dropdown
                            >
                            <label for="type" class="kn-material-input-label">{{ $t('managers.newsManagement.form.type') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.news.type"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.newsManagement.form.type')
                            }"
                        >
                        </KnValidationMessages>
                    </div>
                </div>

                <div class="p-field">
                    <span class="p-float-label">
                        <Textarea
                            id="description"
                            class="kn-material-input"
                            v-model.trim="v$.news.description.$model"
                            :class="{
                                'p-invalid': v$.news.description.$invalid && v$.news.description.$dirty
                            }"
                            :autoResize="true"
                            maxLength="140"
                            rows="2"
                            @blur="v$.news.description.$touch()"
                            @input="onFieldChange('description', $event.target.value)"
                            data-test="description-input"
                        />
                        <div class="p-text-right">
                            <small id="description-help">{{ descriptionHelp }}</small>
                        </div>
                        <label for="description" class="kn-material-input-label"> {{ $t('managers.newsManagement.form.description') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.news.description"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.newsManagement.form.description')
                        }"
                    />
                </div>

                <div class="p-field">
                    <span>
                        <Editor
                            id="html"
                            v-model="v$.news.html.$model"
                            editorStyle="height: 320px"
                            :class="{
                                'p-invalid': v$.news.html.$invalid && v$.news.html.$dirty
                            }"
                            @blur="v$.news.html.$touch()"
                            @text-change="onFieldChange('html', $event)"
                        />
                    </span>
                    <KnValidationMessages
                        :vComp="v$.news.html"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.newsManagement.form.html')
                        }"
                    />
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { iNews } from '../../NewsManagement'
import moment from 'moment'
import Calendar from 'primevue/calendar'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Editor from 'primevue/editor'
import InputSwitch from 'primevue/inputswitch'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import newsDetailCardDescriptor from './NewsDetailCardDescriptor.json'
import newsDetailCardValidationDescriptor from './NewsDetailValidationDescriptor.json'
import Textarea from 'primevue/textarea'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'news-detail-card',
    components: {
        Calendar,
        Card,
        Dropdown,
        Editor,
        InputSwitch,
        KnValidationMessages,
        Textarea
    },
    props: {
        selectedNews: {
            type: Object,
            requried: false
        }
    },
    emits: ['fieldChanged'],
    data() {
        return {
            moment,
            newsDetailCardDescriptor,
            newsDetailCardValidationDescriptor,
            news: {} as iNews,
            v$: useValidate() as any
        }
    },
    validations() {
        return {
            news: createValidations('news', newsDetailCardValidationDescriptor.validations.news)
        }
    },
    computed: {
        descriptionHelp(): any {
            return (this.news.description?.length ?? '0') + ' / 140'
        }
    },
    async created() {
        this.news = { ...this.selectedNews } as iNews
        if (!this.news?.type) {
            this.news.type = {
                id: 1,
                value: 'News'
            }
        }
    },
    watch: {
        selectedNews() {
            this.v$.$reset()
            this.news = { ...this.selectedNews } as iNews
            if (!this.news?.type) {
                this.news.type = {
                    id: 1,
                    value: 'News'
                }
            }
        }
    },
    methods: {
        onFieldChange(fieldName: string, value: any) {
            console.log(fieldName + ' ==> ', value)
            this.$emit('fieldChanged', { fieldName, value })
        },
        onActiveChange() {
            console.log('active' + ' ==> ', this.news.active)
            this.$emit('fieldChanged', { fieldName: 'active', value: this.news.active })
        }
    }
})
</script>

<style lang="scss" scoped>
#calendar-label {
    color: $color-primary;
}
</style>
