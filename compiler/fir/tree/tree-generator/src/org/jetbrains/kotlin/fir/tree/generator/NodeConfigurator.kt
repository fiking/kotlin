/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator

import org.jetbrains.kotlin.fir.tree.generator.FieldSets.annotations
import org.jetbrains.kotlin.fir.tree.generator.FieldSets.declarations
import org.jetbrains.kotlin.fir.tree.generator.FieldSets.declaredSymbol
import org.jetbrains.kotlin.fir.tree.generator.FieldSets.name
import org.jetbrains.kotlin.fir.tree.generator.FieldSets.referencedSymbol
import org.jetbrains.kotlin.fir.tree.generator.FieldSets.typeArguments
import org.jetbrains.kotlin.fir.tree.generator.FieldSets.typeParameters
import org.jetbrains.kotlin.fir.tree.generator.context.AbstractFieldConfigurator
import org.jetbrains.kotlin.fir.tree.generator.context.type
import org.jetbrains.kotlin.fir.tree.generator.model.*
import org.jetbrains.kotlin.generators.tree.AbstractField
import org.jetbrains.kotlin.generators.tree.StandardTypes
import org.jetbrains.kotlin.generators.tree.TypeRef
import org.jetbrains.kotlin.generators.tree.withArgs
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

object NodeConfigurator : AbstractFieldConfigurator<FirTreeBuilder>(FirTreeBuilder) {
    fun configureFields() = configure {
        baseFirElement.configure {
            +field("source", sourceElementType, nullable = true)
        }

        annotationContainer.configure {
            +annotations
        }

        typeParameterRef.configure {
            +referencedSymbol(typeParameterSymbolType)
        }

        typeParametersOwner.configure {
            +typeParameters {
                withTransform = true
            }
        }

        typeParameterRefsOwner.configure {
            +listField("typeParameters", typeParameterRef, withTransform = true)
        }

        resolvable.configure {
            +field("calleeReference", reference, withReplace = true, withTransform = true)
        }

        diagnosticHolder.configure {
            +field("diagnostic", coneDiagnosticType)
        }

        controlFlowGraphOwner.configure {
            +field("controlFlowGraphReference", controlFlowGraphReference, withReplace = true, nullable = true)
        }

        contextReceiver.configure {
            +field(typeRef, withReplace = true, withTransform = true)
            +field("customLabelName", nameType, nullable = true)
            +field("labelNameFromTypeRef", nameType, nullable = true)
        }

        elementWithResolveState.configure {
            +field("resolvePhase", resolvePhaseType) { isParameter = true; }
            +field("resolveState", resolveStateType) {
                isMutable = true; isVolatile = true; isFinal = true;
                implementationDefaultStrategy = AbstractField.ImplementationDefaultStrategy.Lateinit
                customInitializationCall = "resolvePhase.asResolveState()"
                arbitraryImportables += phaseAsResolveStateExtentionImport
                optInAnnotation = resolveStateAccessAnnotation
            }

            +field("moduleData", firModuleDataType)
            shouldBeAbstractClass()
        }

        declaration.configure {
            +declaredSymbol(firBasedSymbolType.withArgs(declaration))
            +field("moduleData", firModuleDataType)
            +field("origin", declarationOriginType)
            +field("attributes", declarationAttributesType)
            shouldBeAbstractClass()
        }

        callableDeclaration.configure {
            +field("returnTypeRef", typeRef, withReplace = true, withTransform = true)
            +field("receiverParameter", receiverParameter, nullable = true, withReplace = true, withTransform = true)
            +field("deprecationsProvider", deprecationsProviderType, withReplace = true) {
                isMutable = true
            }
            +referencedSymbol(callableSymbolType.withArgs(callableDeclaration))

            +field("containerSource", type<DeserializedContainerSource>(), nullable = true)
            +field("dispatchReceiverType", coneSimpleKotlinTypeType, nullable = true)

            +listField(contextReceiver, useMutableOrEmpty = true, withReplace = true)
        }

        function.configure {
            +declaredSymbol(functionSymbolType.withArgs(function))
            +listField(valueParameter, withReplace = true, withTransform = true)
            +field("body", block, nullable = true, withReplace = true, withTransform = true)
        }

        errorExpression.configure {
            +field("expression", expression, nullable = true)
            +field("nonExpressionElement", baseFirElement, nullable = true)
        }

        errorFunction.configure {
            +declaredSymbol(errorFunctionSymbolType)
        }

        memberDeclaration.configure {
            +field("status", declarationStatus, withReplace = true, withTransform = true)
        }

        expression.configure {
            +field("coneTypeOrNull", coneKotlinTypeType, nullable = true, withReplace = true) {
                optInAnnotation = unresolvedExpressionTypeAccessAnnotation
            }
            +annotations
        }

        argumentList.configure {
            +listField("arguments", expression, withTransform = true)
        }

        call.configure {
            +field(argumentList, withReplace = true)
        }

        block.configure {
            +listField(statement, withTransform = true)
            needTransformOtherChildren()
        }

        binaryLogicExpression.configure {
            +field("leftOperand", expression, withTransform = true)
            +field("rightOperand", expression, withTransform = true)
            +field("kind", operationKindType)
            needTransformOtherChildren()
        }

        jump.configure {
            val e = withArg("E", targetElement)
            +field("target", jumpTargetType.withArgs(e))
        }

        loopJump.configure {
            parentArgs(jump, "E" to loop)
        }

        returnExpression.configure {
            parentArgs(jump, "E" to function)
            +field("result", expression, withTransform = true)
            needTransformOtherChildren()
        }

        label.configure {
            +field("name", string)
        }

        loop.configure {
            +field(block, withTransform = true)
            +field("condition", expression, withTransform = true)
            +field(label, nullable = true)
            needTransformOtherChildren()
        }

        whileLoop.configure {
            +field("condition", expression, withTransform = true)
            +field(block, withTransform = true)
        }

        catchClause.configure {
            +field("parameter", property, withTransform = true)
            +field(block, withTransform = true)
            needTransformOtherChildren()
        }

        tryExpression.configure {
            +field("tryBlock", block, withTransform = true)
            +listField("catches", catchClause, withTransform = true)
            +field("finallyBlock", block, nullable = true, withTransform = true)
            needTransformOtherChildren()
        }

        elvisExpression.configure {
            +field("lhs", expression, withTransform = true)
            +field("rhs", expression, withTransform = true)
        }

        contextReceiverArgumentListOwner.configure {
            +listField("contextReceiverArguments", expression, useMutableOrEmpty = true, withReplace = true)
        }

        qualifiedAccessExpression.configure {
            +typeArguments {
                withTransform = true
            }
            +field("explicitReceiver", expression, nullable = true, withReplace = true, withTransform = true)
            +field("dispatchReceiver", expression, nullable = true, withReplace = true)
            +field("extensionReceiver", expression, nullable = true, withReplace = true)
            +field("source", sourceElementType, nullable = true, withReplace = true)
            +listField("nonFatalDiagnostics", coneDiagnosticType, useMutableOrEmpty = true, withReplace = true)
        }

        qualifiedErrorAccessExpression.configure {
            +field("selector", errorExpression)
            +field("receiver", expression)
        }

        literalExpression.configure {
            +field("kind", constKindType, withReplace = true)
            +field("value", anyType, nullable = true)
            +field("prefix", stringType, nullable = true)
        }

        functionCall.configure {
            +field("calleeReference", namedReference)
            +field("origin", functionCallOrigin)
        }

        integerLiteralOperatorCall.configure {
            // we need methods for transformation of receivers
            +field("dispatchReceiver", expression, nullable = true, withReplace = true, withTransform = true)
            +field("extensionReceiver", expression, nullable = true, withReplace = true, withTransform = true)
        }

        comparisonExpression.configure {
            +field("operation", operationType)
            +field("compareToCall", functionCall)
        }

        typeOperatorCall.configure {
            +field("operation", operationType)
            +field("conversionTypeRef", typeRef, withTransform = true)
            +field("argFromStubType", boolean, withReplace = true)
            needTransformOtherChildren()
        }

        augmentedAssignment.configure {
            +field("operation", operationType)
            +field("leftArgument", expression, withTransform = true)
            +field("rightArgument", expression, withTransform = true)

            element.kDoc = """
                Represents an augmented assignment statement (e.g. `x += y`) **before** it gets resolved.
                After resolution, it will be either represented as an assignment (`x = x.plus(y)`) or a call (`x.plusAssign(y)`). 
                
                Augmented assignments with an indexed access as receiver are represented as [${indexedAccessAugmentedAssignment.render()}]. 
            """.trimIndent()
        }

        incrementDecrementExpression.configure {
            +field("isPrefix", boolean)
            +field("operationName", nameType)
            +field("expression", expression)
            +field("operationSource", sourceElementType, nullable = true)
        }

        equalityOperatorCall.configure {
            +field("operation", operationType)
        }

        whenBranch.configure {
            +field("condition", expression, withTransform = true)
            +field("result", block, withTransform = true)
            +field("hasGuard", boolean)
            needTransformOtherChildren()
        }

        classLikeDeclaration.configure {
            +declaredSymbol(classLikeSymbolType.withArgs(classLikeDeclaration))
            +field("deprecationsProvider", deprecationsProviderType, withReplace = true) {
                isMutable = true }
        }

        klass.configure {
            +declaredSymbol(classSymbolType.withArgs(klass))
            +field(classKindType)
            +listField("superTypeRefs", typeRef, withReplace = true, withTransform = true)
            +declarations {
                withTransform = true
            }
            +annotations
            +field("scopeProvider", firScopeProviderType)
        }

        regularClass.configure {
            +name
            +declaredSymbol(regularClassSymbolType)
            +field("hasLazyNestedClassifiers", boolean)
            +referencedSymbol("companionObjectSymbol", regularClassSymbolType, nullable = true, withReplace = true)
            +listField("superTypeRefs", typeRef, withReplace = true)
            +listField(contextReceiver, useMutableOrEmpty = true)
        }

        anonymousObject.configure {
            +declaredSymbol(anonymousObjectSymbolType)
        }

        anonymousObjectExpression.configure {
            +field(anonymousObject, withTransform = true)
        }

        typeAlias.configure {
            +typeParameters
            +name
            +declaredSymbol(typeAliasSymbolType)
            +field("expandedTypeRef", typeRef, withReplace = true, withTransform = true)
            +annotations
        }

        anonymousFunction.configure {
            +declaredSymbol(anonymousFunctionSymbolType)
            +field(label, nullable = true)
            +field("invocationKind", eventOccurrencesRangeType, nullable = true, withReplace = true) {
                isMutable = true
            }
            +field("inlineStatus", inlineStatusType, withReplace = true) {
                isMutable = true
            }
            +field("isLambda", boolean)
            +field("hasExplicitParameterList", boolean)
            +typeParameters
            +field(typeRef, withReplace = true)
        }

        anonymousFunctionExpression.configure {
            +field(anonymousFunction, withTransform = true)
            +field("isTrailingLambda", boolean, withReplace = true) {
                replaceOptInAnnotation = rawFirApi
            }
        }

        typeParameter.configure {
            +name
            +declaredSymbol(typeParameterSymbolType)
            +referencedSymbol("containingDeclarationSymbol", firBasedSymbolType.withArgs(TypeRef.Star)) {
                withBindThis = false
            }
            +field(varianceType)
            +field("isReified", boolean)
            // TODO: `useMutableOrEmpty = true` is a workaround for KT-60324 until KT-60445 has been fixed.
            +listField("bounds", typeRef, withReplace = true, useMutableOrEmpty = true)
            +annotations
        }

        simpleFunction.configure {
            +name
            +declaredSymbol(namedFunctionSymbolType)
            +annotations
            +typeParameters
        }

        contractDescriptionOwner.configure {
            +field(contractDescription, withReplace = true, nullable = true, withTransform = true)
        }

        property.configure {
            +listField(contextReceiver, useMutableOrEmpty = true, withReplace = true, withTransform = true)
            +declaredSymbol(propertySymbolType)
            +referencedSymbol("delegateFieldSymbol", delegateFieldSymbolType, nullable = true)
            +field("isLocal", boolean)
            +field("bodyResolveState", propertyBodyResolveStateType, withReplace = true)
            +typeParameters
        }

        propertyAccessor.configure {
            +declaredSymbol(propertyAccessorSymbolType)
            +referencedSymbol("propertySymbol", firPropertySymbolType) {
                withBindThis = false
            }
            +field("isGetter", boolean)
            +field("isSetter", boolean)
            +annotations
            +typeParameters
        }

        backingField.configure {
            +declaredSymbol(backingFieldSymbolType)
            +referencedSymbol("propertySymbol", firPropertySymbolType) {
                withBindThis = false
            }
            +field("initializer", expression, nullable = true, withReplace = true, withTransform = true)
            +annotations
            +typeParameters
            +field("status", declarationStatus, withReplace = true, withTransform = true)
        }

        declarationStatus.configure {
            +field(visibilityType)
            +field(modalityType, nullable = true)
            generateBooleanFields(
                "expect", "actual", "override", "operator", "infix", "inline", "tailRec",
                "external", "const", "lateInit", "inner", "companion", "data", "suspend", "static",
                "fromSealedClass", "fromEnumClass", "fun", "hasStableParameterNames",
            )
            +field("defaultVisibility", visibilityType, nullable = false)
            +field("defaultModality", modalityType, nullable = false)
        }

        resolvedDeclarationStatus.configure {
            +field(modalityType, nullable = false)
            +field("effectiveVisibility", effectiveVisibilityType)
            shouldBeAnInterface()
        }

        implicitInvokeCall.configure {
            +field("isCallWithExplicitReceiver", boolean)
        }

        constructor.configure {
            +annotations
            +declaredSymbol(constructorSymbolType)
            +field("delegatedConstructor", delegatedConstructorCall, nullable = true, withReplace = true, withTransform = true)
            +field("body", block, nullable = true)
            +field("isPrimary", boolean)
        }

        delegatedConstructorCall.configure {
            +field("constructedTypeRef", typeRef, withReplace = true)
            +field("dispatchReceiver", expression, nullable = true, withReplace = true, withTransform = true)
            +field("calleeReference", reference, withReplace = true)
            +field("source", sourceElementType, nullable = true, withReplace = true)
            generateBooleanFields("this", "super")
        }

        multiDelegatedConstructorCall.configure {
            +listField("delegatedConstructorCalls", delegatedConstructorCall, withReplace = true, withTransform = true)
        }

        valueParameter.configure {
            +declaredSymbol(valueParameterSymbolType)
            +field("defaultValue", expression, nullable = true, withReplace = true)
            +referencedSymbol("containingFunctionSymbol", functionSymbolType.withArgs(TypeRef.Star)) {
                withBindThis = false
            }
            generateBooleanFields("crossinline", "noinline", "vararg")
        }

        receiverParameter.configure {
            +field(typeRef, withReplace = true, withTransform = true)
            +annotations
        }

        scriptReceiverParameter.configure {
            +field(typeRef, withReplace = true, withTransform = true)
            // means coming from ScriptCompilationConfigurationKeys.baseClass (could be deprecated soon, see KT-68540)
            +field("isBaseClassReceiver", boolean)
        }

        variable.configure {
            +name
            +declaredSymbol(variableSymbolType.withArgs(variable))
            +field("initializer", expression, nullable = true, withReplace = true, withTransform = true)
            +field("delegate", expression, nullable = true, withReplace = true, withTransform = true)
            generateBooleanFields("var", "val")
            +field("getter", propertyAccessor, nullable = true, withReplace = true, withTransform = true)
            +field("setter", propertyAccessor, nullable = true, withReplace = true, withTransform = true)
            +field("backingField", backingField, nullable = true, withTransform = true)
            +annotations
            needTransformOtherChildren()
        }

        functionTypeParameter.configure {
            +field("name", nameType, nullable = true)
            +field("returnTypeRef", typeRef)
        }

        errorProperty.configure {
            +declaredSymbol(errorPropertySymbolType)
        }

        enumEntry.configure {
            +declaredSymbol(enumEntrySymbolType)
        }

        field.configure {
            +declaredSymbol(fieldSymbolType)
            generateBooleanFields("hasConstantInitializer")
        }

        anonymousInitializer.configure {
            +field("body", block, nullable = true, withReplace = true)
            +declaredSymbol(anonymousInitializerSymbolType)
            // the containing declaration is nullable, because it is not immediately clear how to obtain it in all places in the fir builder
            // TODO: review and consider making not-nullable (KT-64195)
            +referencedSymbol("containingDeclarationSymbol", firBasedSymbolType.withArgs(TypeRef.Star), nullable = true) {
                withBindThis = false
            }
        }

        danglingModifierList.configure {
            +declaredSymbol(danglingModifierSymbolType)
        }

        file.configure {
            +field("packageDirective", packageDirective)
            +listField(import, withTransform = true)
            +declarations {
                withTransform = true
            }
            +field("name", string)
            +field("sourceFile", sourceFileType, nullable = true)
            +field("sourceFileLinesMapping", sourceFileLinesMappingType, nullable = true)
            +declaredSymbol(fileSymbolType)
        }

        script.configure {
            +name
            +declarations {
                withTransform = true
                withReplace = true
            }
            +declaredSymbol(scriptSymbolType)
            +listField("parameters", property, withTransform = true)
            +listField("receivers", scriptReceiverParameter, useMutableOrEmpty = true, withTransform = true)
            +field("resultPropertyName", nameType, nullable = true)
        }

        codeFragment.configure {
            +declaredSymbol(codeFragmentSymbolType)
            +field(block, withReplace = true, withTransform = true)
        }

        packageDirective.configure {
            +field("packageFqName", fqNameType)
        }

        import.configure {
            +field("importedFqName", fqNameType, nullable = true)
            +field("isAllUnder", boolean)
            +field("aliasName", nameType, nullable = true)
            +field("aliasSource", sourceElementType, nullable = true)
            shouldBeAbstractClass()
        }

        resolvedImport.configure {
            +field("delegate", import, isChild = false)
            +field("packageFqName", fqNameType)
            +field("relativeParentClassName", fqNameType, nullable = true)
            +field("resolvedParentClassId", classIdType, nullable = true)
            +field("importedName", nameType, nullable = true)
        }

        annotation.configure {
            +field("useSiteTarget", annotationUseSiteTargetType, nullable = true, withReplace = true)
            +field("annotationTypeRef", typeRef, withReplace = true, withTransform = true)
            +field("argumentMapping", annotationArgumentMapping, withReplace = true)
            +typeArguments {
                withTransform = true
            }
        }

        annotationCall.configure {
            +field("argumentMapping", annotationArgumentMapping, withReplace = true, isChild = false)
            +field("annotationResolvePhase", annotationResolvePhaseType, withReplace = true)
            +referencedSymbol("containingDeclarationSymbol", firBasedSymbolType.withArgs(TypeRef.Star)) {
                withBindThis = false
            }
        }

        errorAnnotationCall.configure {
            +field("argumentMapping", annotationArgumentMapping, withReplace = true, isChild = false)
        }

        annotationArgumentMapping.configure {
            +field("mapping", StandardTypes.map.withArgs(nameType, expression))
        }

        indexedAccessAugmentedAssignment.configure {
            +field("lhsGetCall", functionCall)
            +field("rhs", expression)
            +field("operation", operationType)
            // Used for resolution errors reporting in case
            +field("calleeReference", reference, withReplace = true)
            +field("arrayAccessSource", sourceElementType, nullable = true)

            element.kDoc = """
                    Represents an augmented assignment with an indexed access as the receiver (e.g., `arr[i] += 1`)
                    **before** it gets resolved.
                    
                    After resolution, the call will be desugared into regular function calls,
                    either of the form `arr.set(i, arr.get(i).plus(1))` or `arr.get(i).plusAssign(1)`.
                """.trimIndent()
        }

        classReferenceExpression.configure {
            +field("classTypeRef", typeRef)
        }

        componentCall.configure {
            +field("explicitReceiver", expression)
            +field("componentIndex", int)
        }

        smartCastExpression.configure {
            +field("originalExpression", expression, withReplace = true, withTransform = true)
            +field("typesFromSmartCast", StandardTypes.collection.withArgs(coneKotlinTypeType))
            +field("smartcastType", typeRef)
            +field("smartcastTypeWithoutNullableNothing", typeRef, nullable = true)
            +field("isStable", boolean)
            +field(smartcastStabilityType)
        }

        safeCallExpression.configure {
            +field("receiver", expression, withTransform = true)
            // Special node that might be used as a reference to receiver of a safe call after null check
            +field("checkedSubjectRef", safeCallCheckedSubjectReferenceType)
            // One that uses checkedReceiver as a receiver
            +field("selector", statement, withReplace = true, withTransform = true)
        }

        checkedSafeCallSubject.configure {
            +field("originalReceiverRef", referenceToSimpleExpressionType)
        }

        callableReferenceAccess.configure {
            +field("calleeReference", namedReference, withReplace = true, withTransform = true)
            +field("hasQuestionMarkAtLHS", boolean, withReplace = true)
        }

        getClassCall.configure {
            +field("argument", expression)
        }

        wrappedArgumentExpression.configure {
            +field("isSpread", boolean)
        }

        spreadArgumentExpression.configure {
            +field("isNamed", boolean)
            +field("isFakeSpread", boolean)

            element.kDoc = """
                |### Up to and including body resolution phase
                |
                |Represents a spread expression `*foo`. If a spread expression is passed as named argument `foo = *bar`, it will be
                |represented as an [${namedArgumentExpression.render()}] with [${namedArgumentExpression.render()}.isSpread] set to `true`.
                |  
                |### After body resolution phase
                |
                |Represents spread expressions `*foo` and named argument expressions for vararg parameters `foo = bar` and `foo = *bar`.
                |
                |If [isNamed] is `true`, it means the argument was passed in named form. The name is not saved since it's not required.
                |To retrieve the argument mapping of a call, [${firResolvedArgumentListType.render()}.mapping] must be used.
                |
                |If [isFakeSpread] is `true`, it means this expression is the argument to a `vararg` parameter that was passed in named form
                |without a spread operator `*`.
                |
                |The information carried by [isNamed] and [isFakeSpread] is only relevant for some checkers. Otherwise,
                |[FirSpreadArgumentExpression]s should be treated uniformly since they always represent an array that was passed to a
                |`vararg` parameter and don't influence the resulting platform code.
            """.trimMargin()
        }

        namedArgumentExpression.configure {
            +name

            element.kDoc = """
                |Represents a named argument `foo = bar` before and during body resolution phase.
                |
                |After body resolution, all [${namedArgumentExpression.render()}]s are removed from the FIR tree and the argument mapping must be
                |retrieved from [${firResolvedArgumentListType.render()}.mapping].
                |
                |For a named argument with spread operator `foo = *bar`, [isSpread] will be set to `true` but no additional
                |[${spreadArgumentExpression.render()}] will be created as the [expression].
                |
                |**Special case vor varargs**: named arguments for `vararg` parameters are replaced with [${spreadArgumentExpression.render()}] with
                |[${spreadArgumentExpression.render()}.isNamed] set to `true`.
                |
                |See [${varargArgumentsExpression.render()}] for the general structure of arguments of `vararg` parameters after resolution.
            """.trimMargin()
        }

        varargArgumentsExpression.configure {
            +listField("arguments", expression)
            +field("coneElementTypeOrNull", coneKotlinTypeType, nullable = true)

            element.kDoc = """
                |[${varargArgumentsExpression.render()}]s are created during body resolution phase for arguments of `vararg` parameters.
                |
                |If one or multiple elements are passed to a `vararg` parameter, the will be wrapped with a [${varargArgumentsExpression.render()}]
                |and [arguments] will contain the individual elements.
                |
                |If a named argument is passed to a `vararg` parameter, [arguments] will contain a single [${spreadArgumentExpression.render()}]
                |with [${spreadArgumentExpression.render()}.isNamed] set to `true`.
                |
                |[${spreadArgumentExpression.render()}]s are kept as is in [arguments]. 
                |
                |If no element is passed to a `vararg` parameter, no [${varargArgumentsExpression.render()}] is created regardless of whether the
                |parameter has a default value.
            """.trimMargin()
        }

        samConversionExpression.configure {
            +field("expression", expression)
        }

        resolvedQualifier.configure {
            +field("packageFqName", fqNameType)
            +field("relativeClassFqName", fqNameType, nullable = true)
            +field("classId", classIdType, nullable = true)
            +referencedSymbol("symbol", classLikeSymbolType, nullable = true)
            +field("isNullableLHSForCallableReference", boolean, withReplace = true)
            +field("resolvedToCompanionObject", boolean, withReplace = true)
            +field("canBeValue", boolean, withReplace = true)
            +field("isFullyQualified", boolean)
            +listField("nonFatalDiagnostics", coneDiagnosticType, useMutableOrEmpty = true)
            +typeArguments {
                withTransform = true
            }
        }

        resolvedReifiedParameterReference.configure {
            +referencedSymbol(typeParameterSymbolType)
        }

        stringConcatenationCall.configure {
            +field("interpolationPrefix", string)
        }

        throwExpression.configure {
            +field("exception", expression, withTransform = true)
        }

        variableAssignment.configure {
            +field("lValue", expression, withReplace = true, withTransform = true)
            +field("rValue", expression, withTransform = true)
        }

        whenSubjectExpression.configure {
            +field("whenRef", whenRefType)
        }

        desugaredAssignmentValueReferenceExpression.configure {
            +field("expressionRef", referenceToSimpleExpressionType)
        }

        wrappedExpression.configure {
            +field(expression)
        }

        wrappedDelegateExpression.configure {
            +field("provideDelegateCall", functionCall)
        }

        enumEntryDeserializedAccessExpression.configure {
            +field("enumClassId", classIdType)
            +field("enumEntryName", nameType)
        }

        namedReference.configure {
            +name
        }

        namedReferenceWithCandidateBase.configure {
            +referencedSymbol("candidateSymbol", firBasedSymbolType.withArgs(TypeRef.Star))
        }

        resolvedNamedReference.configure {
            +referencedSymbol("resolvedSymbol", firBasedSymbolType.withArgs(TypeRef.Star))
        }

        resolvedCallableReference.configure {
            +listField("inferredTypeArguments", coneKotlinTypeType)
            +field("mappedArguments", callableReferenceMappedArgumentsType)
        }

        delegateFieldReference.configure {
            +referencedSymbol("resolvedSymbol", delegateFieldSymbolType)
        }

        backingFieldReference.configure {
            +referencedSymbol("resolvedSymbol", backingFieldSymbolType)
        }

        superReference.configure {
            +field("labelName", string, nullable = true)
            +field("superTypeRef", typeRef, withReplace = true)
        }

        thisReference.configure {
            +field("labelName", string, nullable = true)
            +referencedSymbol("boundSymbol", firBasedSymbolType.withArgs(TypeRef.Star), nullable = true, withReplace = true)
            +field("contextReceiverNumber", int, withReplace = true)
            +field("isImplicit", boolean)
            +field("diagnostic", coneDiagnosticType, nullable = true, withReplace = true)
        }

        typeRef.configure {
            +annotations
        }

        resolvedTypeRef.configure {
            +field("type", coneKotlinTypeType)
            +field("delegatedTypeRef", typeRef, nullable = true, isChild = false)
            element.otherParents.add(typeRefMarkerType)
        }

        typeRefWithNullability.configure {
            +field("isMarkedNullable", boolean)
        }

        userTypeRef.configure {
            +listField("qualifier", firQualifierPartType)
            +field("customRenderer", boolean)
        }

        functionTypeRef.configure {
            +field("receiverTypeRef", typeRef, nullable = true)
            +listField("parameters", functionTypeParameter)
            +field("returnTypeRef", typeRef)
            +field("isSuspend", boolean)

            +listField("contextReceiverTypeRefs", typeRef)
        }

        errorTypeRef.configure {
            +field("partiallyResolvedTypeRef", typeRef, nullable = true, withTransform = true)
        }

        resolvedErrorReference.configure {
            element.customParentInVisitor = resolvedNamedReference
        }

        intersectionTypeRef.configure {
            +field("leftType", typeRef)
            +field("rightType", typeRef)
        }

        thisReceiverExpression.configure {
            +field("calleeReference", thisReference)
            +field("isImplicit", boolean)
        }

        inaccessibleReceiverExpression.configure {
            +field("calleeReference", thisReference)
        }

        whenExpression.configure {
            +field("subject", expression, nullable = true, withTransform = true)
            +field("subjectVariable", variable, nullable = true)
            +listField("branches", whenBranch, withTransform = true)
            +field("exhaustivenessStatus", exhaustivenessStatusType, nullable = true, withReplace = true)
            +field("usedAsExpression", boolean)
            needTransformOtherChildren()
        }

        typeProjectionWithVariance.configure {
            +field(typeRef)
            +field(varianceType)
        }

        contractElementDeclaration.configure {
            +field("effect", coneContractElementType)
        }

        effectDeclaration.configure {
            +field("effect", coneEffectDeclarationType)
        }

        rawContractDescription.configure {
            +listField("rawEffects", expression)
        }

        resolvedContractDescription.configure {
            +listField("effects", effectDeclaration)
            +listField("unresolvedEffects", contractElementDeclaration)
            +field("diagnostic", coneDiagnosticType, nullable = true)
        }

        legacyRawContractDescription.configure {
            +field("contractCall", functionCall)
            +field("diagnostic", coneDiagnosticType, nullable = true)
        }
    }
}
