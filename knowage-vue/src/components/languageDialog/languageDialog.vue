<template>
	<Dialog v-bind:visible="visibility" footer="footer" :header="$t('language.languageSelection')" :closable="false" modal>
		<Listbox :options="languages" optionLabel="name" listStyle="max-height:250px" style="width:15em">
			<template #option="slotProps">
				<div class="p-d-flex p-ai-center country-item" @click="changeLanguage({ language: slotProps.option.language, country: slotProps.option.country })">
					<img :alt="slotProps.option.country" :src="require(`@/assets/images/flags/icon-${slotProps.option.country.toLowerCase()}.png`)" width="40" />
					<span>{{ $t(`language.${slotProps.option.language}_${slotProps.option.country}`) }}</span>
				</div>
			</template>
		</Listbox>
		<template #footer>
			<Button v-t="'common.close'" autofocus @click="closeDialog" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Dialog from 'primevue/dialog'
	import Listbox from 'primevue/listbox'
	import { mapState } from 'vuex'
	import store from '@/App.store'
	import { concatLocale } from '@/helpers/localeHelper'

	export default defineComponent({
		name: 'language-dialog',
		components: {
			Dialog,
			Listbox
		},
		data() {
			return {
				languages: []
			}
		},
		created() {},
		props: {
			visibility: Boolean
		},
		emits: ['update:visibility'],
		methods: {
			changeLanguage(nextLanguage: { language: string; country: string }) {
				store.commit('setLocale', nextLanguage)
				localStorage.setItem('locale', JSON.stringify(nextLanguage))
				this.$i18n.locale = concatLocale(nextLanguage)

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
					this.axios.get('/knowage/restful-services/2.0/languages').then(
						(response) => {
							this.languages = response.data.sort(function compare(a, b) {
								if (a.language + a.country < b.language + b.country) {
									return -1
								}
								if (a.language + a.country > b.language + b.country) {
									return 1
								}
								return 0
							})
						},
						(error) => console.error(error)
					)
				}
			}
		}
	})
</script>

<style scoped lang="scss"></style>
