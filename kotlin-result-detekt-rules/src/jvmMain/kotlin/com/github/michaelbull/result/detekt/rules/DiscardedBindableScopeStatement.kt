package com.github.michaelbull.result.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForReceiver
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.isUsedAsStatement
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes

@RequiresTypeResolution
class DiscardedBindableScopeStatement(config: Config) : Rule(config) {

    override val issue: Issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Defect,
        description = "Discarding the result of statement by not binding may result in potential unhandled errors",
        debt = Debt.FIVE_MINS,
    )

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
        super.visitLambdaExpression(lambdaExpression)
        if (bindingContext == BindingContext.EMPTY) {
            return
        }
        val receiver = bindingContext.getType(lambdaExpression)?.arguments?.firstOrNull()?.type ?: return
        if (receiver.isOrInheritsBindingScope()) {
            BindingScopeVisitor().visitLambdaExpression(lambdaExpression)
        }
    }

    private fun KotlinType.isOrInheritsBindingScope() = isBindingScope() || supertypes().any { it.isBindingScope() }
    private fun KotlinType.isBindingScope() = constructor.declarationDescriptor?.fqNameSafe == bindingScopeFqName

    private inner class BindingScopeVisitor : DetektVisitor() {
        override fun visitExpression(expression: KtExpression) {
            super.visitExpression(expression)

            if (expression is KtBlockExpression) {
                return
            }

            val expressionType = bindingContext.getType(expression) ?: return
            if (!expressionType.isOrInheritsBindable()) {
                return
            }

            if (expression.isLastChainedStatement() && !expression.isPartOfQualifiedChain()) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(expression),
                        "The statement is unbound and it's result will be discarded",
                    ),
                )
            }
        }

        private fun KotlinType.isOrInheritsBindable() = isBindable() || supertypes().any { it.isBindable() }
        private fun KotlinType.isBindable() = constructor.declarationDescriptor?.fqNameSafe == bindableFqName

        private fun KtExpression.isLastChainedStatement() =
            isUsedAsStatement(bindingContext) && getNextChainedCall() == null

        private fun KtExpression.getNextChainedCall(): KtExpression? =
            getQualifiedExpressionForSelectorOrThis()
                .getQualifiedExpressionForReceiver()
                ?.selectorExpression

        private fun KtExpression.isPartOfQualifiedChain(): Boolean {
            val qualifiedExpression = getQualifiedExpressionForSelectorOrThis()
            return qualifiedExpression != this && qualifiedExpression.isLastChainedStatement()
        }
    }

    companion object {
        private val bindingScopeFqName = FqName("com.github.michaelbull.result.BindingScope")
        private val bindableFqName = FqName("com.github.michaelbull.result.Result")
    }
}
