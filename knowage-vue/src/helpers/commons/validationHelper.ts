import { email, maxLength, maxValue, minLength, minValue, required } from '@vuelidate/validators'
import { extendedAlphanumeric, alphanumericNoSpaces, fullnameRegex } from './regexHelper'
import { ValidationRule } from '@vuelidate/core'

export interface IValidator {
    type: string
    params?: any
}

export interface IValdatorInfo {
    key: string
    translateBaseKey?: string
    validator?: IValidator
}

export interface IValidation {
    fieldName: string
    validators: IValdatorInfo[]
}

export interface ICustomValidatorMap {
    [type: string]: Function | ValidationRule
}

export function createValidations(key: string, validations: IValidation[], customValidators: ICustomValidatorMap = {}) {
    const validationObject = {}
    validations.forEach((validation) => {
        validationObject[validation.fieldName] = addValidators(validation.validators, customValidators)
    })
    return validationObject
}

function addValidators(validations: IValdatorInfo[], customValidators: ICustomValidatorMap) {
    const validatorsObject = {}
    validations.forEach((validatorInfo) => {
        validatorsObject[validatorInfo.key] = getValidatorFunction(validatorInfo.validator ? validatorInfo.validator.type : validatorInfo.key, customValidators, validatorInfo.validator)
    })

    return validatorsObject
}

function getValidatorFunction(validatorName: string, customValidators: ICustomValidatorMap, validator?: IValidator) {
    switch (validatorName) {
        case 'required':
            return required
        case 'maxLength':
            return maxLength(validator?.params.max)
        case 'minLength':
            return minLength(validator?.params.min)
        case 'minValue':
            return minValue(validator?.params.min)
        case 'maxValue':
            return maxValue(validator?.params.max)
        case 'extendedAlphanumericRegex':
            return testRegex(extendedAlphanumeric)
        case 'alphanumericNoSpaces':
            return testRegex(alphanumericNoSpaces)
        case 'fullnameRegex':
            return testRegex(fullnameRegex)
        case 'valueListValidator':
            return valueListValidator(validator?.params.valueList)
        case 'email':
            return email
        default: {
            const customValidator = customValidators[validatorName]
            if (customValidators[validatorName]) {
                return customValidator
            } else {
                console.log(`Validator ${validatorName} with name NOT FOUND`)
            }
            break
        }
    }
}

function testRegex(regex: RegExp) {
    return (value: any) => regex.test(value)
}

const valueListValidator = (list: string[] | number[]) => (value: string | number) => {
    return list.findIndex((item) => item === value) >= 0
}
