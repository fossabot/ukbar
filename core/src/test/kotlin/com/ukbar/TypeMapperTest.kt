/*
* MIT License
*
* Copyright (c) 2018 Miguel Hernández
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/

@file:Suppress("ClassName")

package com.ukbar

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import io.mockk.spyk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Nested
open class TypeMapperTest {

    protected val parser = spyk<Parser>()
    protected val propertyBuilder = StringBuilder()
    protected val mapper = TypeMapper()

    class transformType : TypeMapperTest() {

        @ParameterizedTest
        @MethodSource("primitiveTypes")
        fun `can transform primitive types to respective KotlinPoet TypeNames`(
            primitiveType: String,
            expectedType: TypeName
        ) {
            propertyBuilder.append("{\"PrimitiveType\": \"$primitiveType\"}")

            val propertyJsonObject = parser.parse(propertyBuilder) as JsonObject
            val actualType = mapper.mapProperty(propertyJsonObject)

            assertEquals(expectedType, actualType)
        }

        @ParameterizedTest
        @MethodSource("listTypes")
        fun `can transform list of primitive types to KotlinPoet list TypeName parameterized by primitive types`(
            primitiveItemType: String,
            expectedType: TypeName
        ) {
            propertyBuilder.append("{")
            propertyBuilder.append("\"Type\": \"List\"")
            propertyBuilder.append("\"PrimitiveItemType\": \"$primitiveItemType\"")
            propertyBuilder.append("}")

            val propertyJsonObject = parser.parse(propertyBuilder) as JsonObject
            val actualType = mapper.mapProperty(propertyJsonObject)

            assertEquals(expectedType, actualType)
        }

        @Test
        fun `can transform list of non primitive types to KotlinPoet list TypeName parameterized by the non primitive type`() {
            propertyBuilder.append("{")
            propertyBuilder.append("\"Type\": \"List\"")
            propertyBuilder.append("\"ItemType\": \"Action\"")
            propertyBuilder.append("}")

            val listClassName = ClassName("kotlin.collections", "List")
            val itemTypeClassname = ClassName("com.ukbar", "Action")
            val expectedType = listClassName.parameterizedBy(itemTypeClassname)

            val propertyJsonObject = parser.parse(propertyBuilder) as JsonObject
            val actualType = mapper.mapProperty(propertyJsonObject)

            assertEquals(expectedType, actualType)
        }

        @Test
        fun `can transform map type to KotlinPoet Map TypeName`() {
            propertyBuilder.append("{")
            propertyBuilder.append("\"Type\": \"Map\"")
            propertyBuilder.append("}")

            val expectedType = Map::class.asClassName()

            val propertyJsonObject = parser.parse(propertyBuilder) as JsonObject
            val actualType = mapper.mapProperty(propertyJsonObject)

            assertEquals(expectedType, actualType)
        }

        @Test
        fun `can transform any non primitive type to KotlinPoet TypeName`() {
            propertyBuilder.append("{")
            propertyBuilder.append("\"Type\": \"FieldToMatch\"")
            propertyBuilder.append("}")

            val expectedType = ClassName("com.ukbar", "FieldToMatch")

            val propertyJsonObject = parser.parse(propertyBuilder) as JsonObject
            val actualType = mapper.mapProperty(propertyJsonObject)

            assertEquals(expectedType, actualType)
        }

        @Test
        fun `should fail if parameter object has List property but no PrimitiveItemType or ItemType property`() {
            val expectedMessage = "Cannot have a parameter type List without a PrimitiveItemType or ItemType property"

            val exception = assertFailsWith<IllegalArgumentException> {
                propertyBuilder.append("{")
                propertyBuilder.append("\"Type\": \"List\"")
                propertyBuilder.append("}")

                val propertyJsonObject = parser.parse(propertyBuilder) as JsonObject
                mapper.mapProperty(propertyJsonObject)
            }

            assertEquals(expectedMessage, exception.message)
        }

        @Test
        fun `should throw IllegalArgumentException when json parameter does not have a type property`() {
            val expectedMessage = "Missing type property in JSON Object"

            val exception = assertFailsWith<IllegalArgumentException> {
                propertyBuilder.append("{}")

                val propertyJsonObject = parser.parse(propertyBuilder) as JsonObject
                mapper.mapProperty(propertyJsonObject)
            }

            assertEquals(expectedMessage, exception.message)
        }

        companion object {
            @JvmStatic
            fun primitiveTypes() = listOf(
                    Arguments.of("String", String::class.asClassName()),
                    Arguments.of("Integer", Int::class.asClassName()),
                    Arguments.of("Boolean", Boolean::class.asClassName()),
                    Arguments.of("Double", Double::class.asClassName()),
                    Arguments.of("Long", Long::class.asClassName()),
                    Arguments.of("Timestamp", String::class.asClassName()),
                    Arguments.of("Json", Map::class.asClassName())
            )

            @JvmStatic
            fun listTypes() = listOf(
                    Arguments.of("String", ClassName("kotlin.collections", "List").parameterizedBy(ClassName("kotlin", "String") as TypeName)),
                    Arguments.of("Integer", ClassName("kotlin.collections", "List").parameterizedBy(ClassName("kotlin", "Int") as TypeName)),
                    Arguments.of("Boolean", ClassName("kotlin.collections", "List").parameterizedBy(ClassName("kotlin", "Boolean") as TypeName)),
                    Arguments.of("Double", ClassName("kotlin.collections", "List").parameterizedBy(ClassName("kotlin", "Double") as TypeName)),
                    Arguments.of("Long", ClassName("kotlin.collections", "List").parameterizedBy(ClassName("kotlin", "Long") as TypeName)),
                    Arguments.of("Timestamp", ClassName("kotlin.collections", "List").parameterizedBy(ClassName("kotlin", "String") as TypeName)),
                    Arguments.of("Json", ClassName("kotlin.collections", "List").parameterizedBy(ClassName("kotlin.collections", "Map") as TypeName))
            )
        }
    }
}