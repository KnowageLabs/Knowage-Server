<template>
    <Card :style="newsDetailCardDescriptor.card.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('managers.newsManagement.settings') }}
                </template>
                <template #end>
                    <InputSwitch id="active" v-model="news.active" data-test="active-input" @change="onActiveChange" />
                    <label for="active" class="kn-material-input-label p-ml-3"> {{ $t('managers.newsManagement.active') }}</label>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <form class="p-fluid p-m-2">
                <div class="p-field p-d-flex">
                    <div :style="newsDetailCardDescriptor.input.title.style">
                        <span class="p-float-label">
                            <InputText
                                id="title"
                                v-model.trim="v$.news.title.$model"
                                class="kn-material-input"
                                type="text"
                                :class="{
                                    'p-invalid': v$.news.title.$invalid && v$.news.title.$dirty
                                }"
                                data-test="title-input"
                                @blur="v$.news.title.$touch()"
                                @input="onFieldChange('title', $event.target.value)"
                            />
                            <label for="title" class="kn-material-input-label"> {{ $t('managers.newsManagement.form.title') }} * </label>
                        </span>
                        <KnValidationMessages
                            :v-comp="v$.news.title"
                            :additional-translate-params="{
                                fieldName: $t('managers.newsManagement.form.title')
                            }"
                        />
                    </div>

                    <div :style="newsDetailCardDescriptor.input.expirationDate.style">
                        <span class="p-float-label">
                            <Calendar
                                id="expirationDate"
                                v-model="v$.news.expirationDate.$model"
                                class="kn-material-input"
                                type="text"
                                :class="{
                                    'p-invalid': v$.news.expirationDate.$invalid && v$.news.expirationDate.$dirty
                                }"
                                :show-icon="true"
                                data-test="expiration-input"
                                @blur="v$.news.expirationDate.$touch()"
                                @input="onManualDateChange"
                                @dateSelect="onFieldChange('expirationDate', $event.valueOf())"
                            />
                            <label id="calendar-label" for="expirationDate"> {{ $t('managers.newsManagement.form.expirationDate') }} * </label>
                        </span>

                        <KnValidationMessages
                            :v-comp="v$.news.expirationDate"
                            :additional-translate-params="{
                                fieldName: $t('managers.newsManagement.form.expirationDate')
                            }"
                        />
                    </div>

                    <div :style="newsDetailCardDescriptor.input.type.style">
                        <span class="p-float-label">
                            <Dropdown
                                id="type"
                                v-model="v$.news.type.$model"
                                class="kn-material-input"
                                :class="{
                                    'p-invalid': v$.news.type.$invalid && v$.news.type.$dirty
                                }"
                                :options="newsDetailCardDescriptor.newsTypes"
                                option-label="name"
                                option-value="value"
                                @before-show="v$.news.type.$touch()"
                                @change="onFieldChange('type', $event.value)"
                            >
                            </Dropdown>
                            <label for="type" class="kn-material-input-label">{{ $t('managers.newsManagement.form.type') }} * </label>
                        </span>
                        <KnValidationMessages
                            :v-comp="v$.news.type"
                            :additional-translate-params="{
                                fieldName: $t('managers.newsManagement.form.type')
                            }"
                        >
                        </KnValidationMessages>
                    </div>
                </div>

                <div class="p-field">
                    <label for="description" class="kn-material-input-label"> {{ $t('managers.newsManagement.form.description') }} * </label>
                    <span class="p-float-label">
                        <Textarea
                            id="description"
                            v-model.trim="v$.news.description.$model"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.news.description.$invalid && v$.news.description.$dirty
                            }"
                            :auto-resize="true"
                            max-length="140"
                            rows="2"
                            :placeholder="$t('managers.newsManagement.form.descriptionPlaceholder')"
                            data-test="description-input"
                            @blur="v$.news.description.$touch()"
                            @input="onFieldChange('description', $event.target.value)"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <KnValidationMessages
                                :v-comp="v$.news.description"
                                :additional-translate-params="{
                                    fieldName: $t('managers.newsManagement.form.description')
                                }"
                            />
                        </div>
                        <small id="description-help">{{ descriptionHelp }}</small>
                    </div>
                </div>

                <div class="p-field">
                    <span>
                        <Editor id="html" v-model="news.html" :editor-style="newsDetailCardDescriptor.editor.style" @text-change="onFieldChange('html', $event.htmlValue)" />
                    </span>
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
        watch: {
            selectedNews() {
                this.v$.$reset()
                this.loadNews()
            }
        },
        async created() {
            this.loadNews()
        },
        methods: {
            onFieldChange(fieldName: string, value: any) {
                this.$emit('fieldChanged', { fieldName, value })
            },
            onActiveChange() {
                this.$emit('fieldChanged', { fieldName: 'active', value: this.news.active })
            },
            loadNews() {
                this.news = { ...this.selectedNews } as iNews
                if (!this.news?.type) {
                    this.news.type = 1
                }
            },
            onManualDateChange() {
                setTimeout(() => this.$emit('fieldChanged', { fieldName: 'expirationDate', value: this.news.expirationDate }), 250)
            }
        }
    })
</script>

<style lang="scss" scoped>
    #calendar-label {
        color: var(--kn-color-primary);
    }
</style>
