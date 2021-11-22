import { Form, Field, ErrorMessage, defineRule, configure } from 'vee-validate'
import AllRules from '@vee-validate/rules'

/* import store from '@/App.store' */
import { localize, setLocale } from '@vee-validate/i18n'

export default (app, i18n) => {
    Object.keys(AllRules).forEach((rule) => {
        defineRule(rule, AllRules[rule])
    })

    configure({
        generateMessage: localize(i18n.global.messages)
    })

    setLocale('en_US')

    app.component('VeeForm', Form)
    app.component('VeeField', Field)
    app.component('VeeErrorMessage', ErrorMessage)
}
