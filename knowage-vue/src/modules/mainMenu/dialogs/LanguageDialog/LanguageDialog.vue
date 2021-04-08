<template>
	<Dialog class="kn-dialog--toolbar--primary" v-bind:visible="visibility" footer="footer" :header="$t('language.languageSelection')" :closable="false" modal>
		<Listbox class="kn-list countryList" :options="languages" optionDisabled="disabled">
			<template #option="slotProps">
				<div :class="['p-d-flex', 'p-ai-center', 'countryItem', slotProps.option.locale]" @click="changeLanguage(slotProps.option)">
					<img :alt="slotProps.option.locale" :src="require('@/assets/images/flags/icon-' + slotProps.option.locale.toLowerCase() + '.png')" width="40" />
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
	import axios from 'axios'

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
		created() {},
		props: {
			visibility: Boolean
		},
		emits: ['update:visibility'],
		methods: {
			changeLanguage(language) {
				store.commit('setLocale', language.locale)
				localStorage.setItem('locale', language.locale)
				this.$i18n.locale = language.locale

				this.closeDialog()
				this.$router.go(0)
				this.$forceUpdate()
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
					axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/languages').then(
						(response) => {
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
		min-width: 200px;
		max-height: 300px;

		&:deep() li.p-listbox-item {
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
