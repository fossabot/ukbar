package com.ukbar

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.File
import kotlin.reflect.KClass

class Generator constructor(
        private val parser: Parser
){

    private val primitiveTypeMap = mapOf<String, KClass<*>>(
            "String" to String::class,
            "Integer" to Int::class,
            "Boolean" to Boolean::class,
            "Double" to Double::class,
            "Long" to Long::class,
            "Timestamp" to String::class,
            "Json" to Map::class
    )

    fun processFile(filepath: String): JsonObject {
        val cfSpecInputStream = File(filepath).inputStream()
        return parser.parse(cfSpecInputStream) as JsonObject
    }

    fun createPropertyClass(cfSpec: JsonObject, packageName: String, filepath: File) {
        val propertiesTypes = cfSpec["PropertyType"] as JsonObject

        propertiesTypes
                .keys
                .forEach { resource ->
                    val classnameStr = resource.substringAfterLast(".")
                    val classname = ClassName(packageName, classnameStr)

                    val properties = propertiesTypes[resource] as JsonObject

                    createKotlinFile(classname, properties)
                }
    }

    private fun createKotlinFile(classname: ClassName, properties: JsonObject) {

        val dataClassConstructorBuilder = FunSpec.constructorBuilder()

//        properties.forEach { property ->
//            dataClassConstructorBuilder.addParameter(property)
//        }

        val dataClassTypeSpecBuilder = TypeSpec.classBuilder(classname)
                .addModifiers(KModifier.DATA)
                .primaryConstructor(dataClassConstructorBuilder.build())

        val file = FileSpec
                .builder(packageName = classname.packageName, fileName = classname.simpleName)
                .addType(dataClassTypeSpecBuilder.build())
    }

    fun transformType(value: JsonObject): TypeName {

        val primitiveType = value["PrimitiveType"]

        if (value.containsKey("PrimitiveType")) {
            val type = primitiveTypeMap[primitiveType]
            return type!!.asClassName()
        }

        if (value.containsKey("Type")) {

            when(value["Type"]) {

                "List" -> {

                    if (value.containsKey("PrimitiveItemType")) {
                        val primitiveItemTypeString = value["PrimitiveItemType"]
                        val primitiveItemType = primitiveTypeMap[primitiveItemTypeString]

                        return List::class.parameterizedBy(primitiveItemType!!)
                    }

                    else if (value.containsKey("ItemType")) {
                        val itemTypeString = value["ItemType"] as String
                        val itemType = ClassName("com.ukbar", itemTypeString)

                        return List::class.asClassName().parameterizedBy(itemType)
                    }

                    throw IllegalArgumentException("Cannot have a parameter type List without a PrimitiveItemType property")
                }

                "Map" -> {
                    return Map::class.asClassName()
                }
            }
        }

        return Any::class.asClassName()
    }

}