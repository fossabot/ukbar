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
import com.beust.klaxon.Parser
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

class Generator constructor(
    private val parser: Parser,
    private val typeMapper: TypeMapper
) {

    fun processFile(filepath: String): JsonObject {
        val cfSpecInputStream = File(filepath).inputStream()
        return parser.parse(cfSpecInputStream) as JsonObject
    }

    fun createPropertyClass(cfSpec: JsonObject, packageName: String, filepath: File) {
        val propertyTypesJson = cfSpec["PropertyTypes"] as JsonObject

        propertyTypesJson
                .keys
                .forEach { resource ->
                    val classnameStr = resource.substringAfterLast(".")
                    val classname = ClassName(packageName, classnameStr)

                    val resourceEntry = propertyTypesJson[resource] as JsonObject
                    val resourceProperties = resourceEntry["Properties"] as JsonObject

                    createDataClass(classname, resourceProperties)
                }
    }

    private fun createDataClass(classname: ClassName, properties: JsonObject) {

        val dataClassConstructorBuilder = FunSpec.constructorBuilder()

        properties.keys.forEach { propertyKey ->
            val property = properties[propertyKey]
            val mappedType = typeMapper.mapProperty(property as JsonObject)

            dataClassConstructorBuilder
                    .addParameter(propertyKey.decapitalize(), mappedType)
        }

        val dataClassTypeSpecBuilder = TypeSpec.classBuilder(classname)
                .addModifiers(KModifier.DATA)
                .primaryConstructor(dataClassConstructorBuilder.build())

        val file = FileSpec
                .builder(packageName = classname.packageName, fileName = classname.simpleName)
                .addType(dataClassTypeSpecBuilder.build())

        file.build().writeTo(System.out)
    }
}