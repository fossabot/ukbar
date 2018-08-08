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

            when (property["Type"]) {

                "List" -> {

                    require(property.containsKey("PrimitiveItemType") ||
                            property.containsKey("ItemType")) {
                        "Cannot have a parameter type List without a PrimitiveItemType or ItemType property"
                    }

                    return if (property.containsKey("PrimitiveItemType")) {
                        val primitiveItemTypeKey = property["PrimitiveItemType"]
                        val primitiveItemType = primitiveTypeMap[primitiveItemTypeKey]

                        List::class.parameterizedBy(primitiveItemType!!)
                    } else {
                        val itemTypeKey = property["ItemType"] as String
                        val itemType = ClassName("com.ukbar", itemTypeKey)

                        List::class.asClassName().parameterizedBy(itemType)
                    }
                }

                "Map" -> return Map::class.asClassName()

                else -> return ClassName("com.ukbar", property["Type"] as String)
            }
        }
    }

}