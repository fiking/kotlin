/*
 * Copyright (C) 2021 Brian Norman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bnorm.power.delegate

import com.bnorm.power.irLambda
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSamConversion
import org.jetbrains.kotlin.ir.builders.typeOperator
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

class SamConversionLambdaFunctionDelegate(
  private val overload: IrSimpleFunctionSymbol,
  override val messageParameter: IrValueParameter,
) : FunctionDelegate {
  override val function = overload.owner

  override fun buildCall(
    builder: IrBuilderWithScope,
    original: IrCall,
    extensionReceiver: IrExpression?,
    valueArguments: List<IrExpression?>,
    messageArgument: IrExpression
  ): IrExpression = with(builder) {
    val lambda = irLambda(context.irBuiltIns.stringType, messageParameter.type) {
      +irReturn(messageArgument)
    }
    val expression = irSamConversion(lambda, messageParameter.type)
    irCallCopy(overload, original, extensionReceiver, valueArguments, expression)
  }
}
