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

package com.ukbar

import com.beust.klaxon.JsonObject
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

class TypeMapper {

    private val primitiveTypeMap = mapOf<String, KClass<*>>(
            "String" to String::class,
            "Integer" to Int::class,
            "Boolean" to Boolean::class,
            "Double" to Double::class,
            "Long" to Long::class,
            "Timestamp" to String::class,
            "Json" to Map::class
    )

    fun mapProperty(property: JsonObject): TypeName {

        if (property.containsKey("PrimitiveType")) {
            val primitiveTypeKey = property["PrimitiveType"]
            val kotlinType = primitiveTypeMap[primitiveTypeKey]

            return kotlinType!!.asClassName()
        } else {

            requireNotNull(property["Type"]) {
                "Missing type property in JSON Object"
            }

            return when (property["Type"]) {

                "List" -> {

                    require(property.containsKey("PrimitiveItemType") ||
                            property.containsKey("ItemType")) {
                        "Cannot have a parameter type List without a PrimitiveItemType or ItemType property"
                    }

                    if (property.containsKey("PrimitiveItemType")) {
                        val primitiveItemTypeKey = property["PrimitiveItemType"]
                        val primitiveItemType = primitiveTypeMap[primitiveItemTypeKey]

                        List::class.parameterizedBy(primitiveItemType!!)
                    } else {
                        val itemTypeKey = property["ItemType"] as String
                        val itemType = ClassName("com.ukbar", itemTypeKey)

                        List::class.asClassName().parameterizedBy(itemType)
                    }
                }

                "Map" -> Map::class.asClassName()

                else -> ClassName("com.ukbar", property["Type"] as String)
            }
        }
    }
}
