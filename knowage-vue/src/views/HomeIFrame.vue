<template>
	<iframe :src="`${completeUrl}`"></iframe>
</template>

<script>
	export default {
		name: 'HomeIframe',
		props: {
			url: String,
			to: String
		},
		data() {
			return {
				completeUrl: this.to ? process.env.VUE_APP_HOST_URL + this.to : this.url
			}
		}
	}
</script>

<style lang="scss" scoped>
	iframe {
		border: 0;
		width: 100%;
		height: 100%;
	}
</style>
