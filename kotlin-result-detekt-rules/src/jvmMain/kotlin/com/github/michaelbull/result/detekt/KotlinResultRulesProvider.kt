package com.github.michaelbull.result.detekt

import com.github.michaelbull.result.detekt.rules.DiscardedBindableScopeStatement
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class KotlinResultRulesProvider: RuleSetProvider {
    override val ruleSetId = "KotlinResultRules"

    override fun instance(config: Config) = RuleSet(
        id = ruleSetId,
        rules = listOf(
            DiscardedBindableScopeStatement(config),
        )
    )
}
