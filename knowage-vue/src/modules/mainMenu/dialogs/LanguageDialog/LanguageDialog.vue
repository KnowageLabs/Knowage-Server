<template>
    <Dialog class="kn-dialog--toolbar--primary" v-bind:visible="visibility" footer="footer" :header="$t('language.languageSelection')" :closable="false" modal>
        <Listbox class="countryList" :options="languages" optionDisabled="disabled">
            <template #option="slotProps">
                <div :class="['p-d-flex', 'p-ai-center', 'countryItem', slotProps.option.locale]" class="p-my-1" @click="changeLanguage(slotProps.option)">
                    <img :alt="slotProps.option.locale" :src="require('@/assets/images/flags/' + slotProps.option.locale.toLowerCase().substring(3, 5) + '.svg')" width="40" />
                    <div class="countryLabel">{{ $t(`language.${slotProps.option.locale}`) }}</div>
                    <span class="kn-flex"></span>
                    <i class="fas fa-check" v-if="slotProps.option.locale === $i18n.locale"></i>
                </div>
            </template>
        </Listbox>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'
    import Listbox from 'primevue/listbox'
    import { mapState } from 'vuex'
    import store from '@/App.store'

    import { AxiosResponse } from 'axios'

    interface Language {
        locale: string
        disabled: boolean | false
    }

    export default defineComponent({
        name: 'language-dialog',
        components: {
            Dialog,
            Listbox
        },
        data() {
            return {
                languages: Array<Language>()
            }
        },

        props: {
            visibility: Boolean
        },
        emits: ['update:visibility', 'update:loading'],
        methods: {
            changeLanguage(language) {
                let splittedLanguage = language.locale.split('_')

                let url = '/knowage/servlet/AdapterHTTP?'
                url += 'ACTION_NAME=CHANGE_LANGUAGE'
                url += '&LANGUAGE_ID=' + splittedLanguage[0]
                url += '&COUNTRY_ID=' + splittedLanguage[1].toUpperCase()
                url += '&SCRIPT_ID=' + (splittedLanguage.length > 2 ? splittedLanguage[2].replaceAll('#', '') : '')
                url += '&THEME_NAME=sbi_default'

                this.$emit('update:loading', true)
                this.$http.get(url).then(
                    () => {
                        store.commit('setLocale', language.locale)
                        localStorage.setItem('locale', language.locale)
                        this.$i18n.locale = language.locale

                        this.closeDialog()
                        this.$router.go(0)
                        this.$forceUpdate()
                    },
                    (error) => console.error(error)
                )
                this.$emit('update:loading', false)
            },
            closeDialog() {
                this.$emit('update:visibility', false)
            }
        },
        computed: {
            ...mapState({
                locale: 'locale'
            })
        },
        watch: {
            visibility(newVisibility) {
                if (newVisibility && this.languages.length == 0) {
                    this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/languages').then(
                        (response: AxiosResponse<any>) => {
                            let languagesArray = response.data.sort()

                            for (var idx in languagesArray) {
                                var disabled = false
                                if (languagesArray[idx] === this.$i18n.locale) {
                                    disabled = true
                                }
                                this.languages.push({ locale: languagesArray[idx], disabled: disabled })
                            }
                        },
                        (error) => console.error(error)
                    )
                }
            }
        }
    })
</script>

<style scoped lang="scss">
    .countryList {
        border: none;
        border-radius: 0;
        min-width: 250px;
        max-height: 100%;

        &:deep(li.p-listbox-item) {
            padding: 0rem 0rem;
        }

        .countryItem {
            padding: 0.25rem 0.25rem;

            .countryLabel {
                margin: 0 0 0 15px;
            }
        }
    }
</style>
