<template>
    <div class="homeContainer">
        <div class="upperSection p-d-flex">
            <div class="p-d-flex p-flex-column kn-flex">
                <div class="logo">
                    <img src="../assets/images/home/logo-knowage8.svg" />
                </div>
                <div class="text p-grid">
                    <h2 class="p-col-12 p-m-0">{{ $t('home.welcome') }}</h2>
                    <p class="p-col-12 p-xl-6 p-m-0" v-html="$t('home.welcomeText')"></p>
                </div>
            </div>
            <div class="buttons p-d-flex">
                <a href="https://knowage-suite.readthedocs.io/" target="_blank">{{ $t('home.button.documentation') }}</a>
                <a href="https://www.knowage-suite.com/qa/" target="_blank">{{ $t('home.button.qa') }}</a>
            </div>
        </div>
        <div class="lowerSection">
            <div class="border-container">
                <div class="image">
                    <img src="../assets/images/home/kn_arrow_right.svg" />
                    <p>connect your data</p>
                </div>
                <div class="image">
                    <img src="../assets/images/home/kn_bubble.svg" />
                    <p>query your data</p>
                </div>
                <div class="image">
                    <img src="../assets/images/home/kn_add.svg" />
                    <p>create your analysis</p>
                </div>
                <div class="image">
                    <img src="../assets/images/home/kn_flag.svg" />
                    <p>save and share</p>
                </div>
            </div>
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
    padding: 64px;
    background: url('../assets/images/home/home-background.png') no-repeat;
    background-position: bottom right;
    background-size: 120%;
    display: flex;
    flex-direction: column;

    .upperSection {
        flex-wrap: wrap;
        .logo {
            width: 40%;
            min-width: 400px;
            img {
                width: 100%;
            }
        }
        .buttons {
            flex-direction: column;
            a {
                width: 300px;
                height: 60px;
                line-height: 60px;
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
                font-size: 1.5rem;
                color: lighten(black, 40%);
            }
        }
    }
    .upperSection,
    .lowerSection {
        flex: 1;
    }
    .lowerSection {
        display: flex;
        justify-content: center;
        align-items: center;
        .border-container {
            flex: 1;
            max-width: 1100px;
            border-bottom: 10px solid $color-fab;
            display: flex;
            justify-content: space-around;
        }
        .image {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            background-color: #ccc;
            height: 200px;
            width: 200px;
            img {
                height: 50px;
                margin: 10px;
            }
            p {
                text-transform: uppercase;
                font-size: 1.8rem;
                font-weight: 100;
                margin: 10px;
                text-align: center;
                color: $knowageBlueColor;
            }
        }
    }
}

@media screen and (max-width: 1200px) {
    .homeContainer {
        .upperSection {
            .text {
                h2 {
                    font-size: 1.6rem;
                }
                p {
                    font-size: 1.1rem;
                }
            }
        }
    }
}
@media screen and (max-width: 1000px) {
    .homeContainer {
        .upperSection {
            .buttons {
                flex-direction: row;
                width: 100%;
                justify-content: space-around;
                margin-top: 20px;
            }
        }

        .lowerSection {
            .border-container {
                border: 0;
                flex-wrap: wrap;
                .image {
                    width: 40%;
                    margin: 20px 5%;
                }
            }
        }
    }
}
@media screen and (max-width: 800px) {
    .homeContainer {
        padding: 16px;
        .upperSection {
            .buttons {
                flex-direction: column;
                align-items: center;
            }
        }
        .lowerSection {
            .border-container {
                border: 0;
                flex-wrap: wrap;
                .image {
                    width: 80%;
                    margin: 20px 10%;
                }
            }
        }
    }
}
</style>
