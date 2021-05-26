<template>
    <div class="homeContainer">
        <div class="upperSection p-d-flex">
            <div class="p-d-flex p-flex-column kn-flex">
                <div class="logo">
                    <img src="../assets/images/home/logo-knowage8.svg" />
                </div>
                <div class="text p-grid">
                    <h2 class="p-col-12">{{ $t('home.welcome') }}</h2>
                    <p class="p-col-6" v-html="$t('home.welcomeText')"></p>
                </div>
            </div>
            <div class="buttons p-ml-auto p-d-flex p-flex-column">
                <a href="https://knowage-suite.readthedocs.io/" target="_blank">{{ $t('home.button.documentation') }}</a>
                <a href="https://www.knowage-suite.com/qa/" target="_blank">{{ $t('home.button.qa') }}</a>
            </div>
        </div>
        <div class="lowerSection">
            <div class="image1"></div>
            <div class="image2"></div>
            <div class="image3"></div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { mapState } from 'vuex'

export default defineComponent({
    name: 'Home',
    components: {},
    props: {},
    methods: {},
    computed: {
        ...mapState({
            user: 'user'
        })
    }
})
</script>

<style lang="scss" scoped>
$knowageBlueColor: #042d5f;
.homeContainer {
    height: 100vh;
    padding: 16px;
    background: url('../assets/images/home/home-background.png') no-repeat;
    background-position: bottom right;
    background-size: 120%;
    .upperSection {
        .logo {
            width: 40%;
            min-width: 400px;
            img {
                width: 100%;
            }
        }
        .buttons {
            a {
                width: 200px;
                height: 40px;
                line-height: 40px;
                text-transform: uppercase;
                text-decoration: none;
                color: white;
                border-radius: 2px;
                text-align: center;
                background-color: $knowageBlueColor;
                box-shadow: 0px 2px 2px #686868;
                margin-bottom: 20px;
                transition: all 0.3s ease-in;
                &:hover {
                    background-color: lighten($knowageBlueColor, 5%);
                }
                &:active {
                    box-shadow: 0px 0px 2px #686868;
                }
            }
        }
        .text {
            margin-top: 5%;
            h2 {
                color: $knowageBlueColor;
                font-size: 2rem;
            }
            p {
                font-size: 1.2rem;
            }
        }
    }
    .upperSection,
    .lowerSection {
        flex: 1;
    }
}
</style>
