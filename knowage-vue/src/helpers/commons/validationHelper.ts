import { email, maxLength, maxValue, minLength, minValue, required } from '@vuelidate/validators'
import { extendedAlphanumeric } from '@/helpers/commons/regexHelper'

export interface IValidator {
    type: string
    params?: any
}

export interface IValdatorInfo {
    key: string
    validator?: IValidator
}

export interface IValidation {
    fieldName: string
    validators: IValdatorInfo[]
}

export function createValidations(key: string, validations: IValidation[]) {
    const validationObject = {}
    validations.forEach((validation) => {
        validationObject[validation.fieldName] = addValidators(validation.validators)
    })
    console.log('validators object', validationObject)
    return validationObject
}

function addValidators(validations: IValdatorInfo[]) {
    const validatorsObject = {}
    validations.forEach((validatorInfo) => {
        validatorsObject[validatorInfo.key] = getValidatorFunction(validatorInfo.validator ? validatorInfo.validator.type : validatorInfo.key, validatorInfo.validator)
    })

    return validatorsObject
}

function getValidatorFunction(validatorName: string, validator?: IValidator) {
    console.log(validatorName)
    switch (validatorName) {
        case 'required':
            console.log('vracam required')
            return required
        case 'maxLength':
            console.log('maxLength: params:', validator?.params)
            return maxLength(validator?.params.max)
        case 'minLength':
            return minLength(validator?.params.length)
        case 'minValue':
            return minValue(validator?.params.min)
        case 'maxValue':
            return maxValue(validator?.params.max)
        case 'extendedAlphanumericRegex':
            return extendedAlphanumericRegex
        case 'valueListValidator':
            return valueListValidator(validator?.params.valueList)
        case 'email':
            return email
    }
}

const extendedAlphanumericRegex = (value: any) => {
    return extendedAlphanumeric.test(value)
}

const valueListValidator = (list: string[] | number[]) => (value: string | number) => {
    return list.findIndex((item) => item === value) >= 0
}
