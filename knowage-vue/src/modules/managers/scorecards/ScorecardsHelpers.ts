import { iKpi, iScorecardCriterion } from "./Scorecards"
import descriptor from './ScorecardsTable/ScorecardsTableDescriptor.json'

export function getSelectedCriteriaTooltip(option: string, $t: any) {
    return descriptor.criteriaTooltipMap[option] ? $t(descriptor.criteriaTooltipMap[option]) : ''
}

export function getKpiIconColorClass(kpi: iKpi) {
    return kpi.status ? descriptor.kpiIconColorClassMap[kpi.status] : descriptor.kpiIconColorClassMap['LIGHT-GREY']
}

export function getDefaultCriterion(criterias: iScorecardCriterion[]) {
    let tempCriterion = {} as iScorecardCriterion
    const index = criterias.findIndex((criteria: iScorecardCriterion) => criteria.valueCd === descriptor.defaultCriteriaValue)
    if (index !== -1) tempCriterion = criterias[index]
    return tempCriterion
}

export function getSelectedCriteria(criterionValue: string) {
    return descriptor.selectedCriteriaMap[criterionValue] ? descriptor.selectedCriteriaMap[criterionValue] : descriptor.selectedCriteriaMap[descriptor.defaultCriteriaValue]
}