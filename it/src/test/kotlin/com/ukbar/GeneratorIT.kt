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

@file:Suppress("Classname")

package com.ukbar

import com.beust.klaxon.Parser
import com.squareup.kotlinpoet.ClassName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GeneratorIT {

    private val cfSpecFilepath = javaClass.getResource("/cf-spec.json").path
    private val generator = Generator(Parser(), TypeMapper())

    @Test
    fun `can create json object from CloudFormation spec String`() {
        val cfSpec = generator.processFile(cfSpecFilepath)

        assertNotNull(cfSpec)
        assertNotNull(cfSpec["PropertyTypes"])
        assertNotNull(cfSpec["ResourceTypes"])
        assertNotNull(cfSpec["ResourceSpecificationVersion"])
    }

    @Test
    fun `can create PropertyType data class with primitive properties`() {
        val cfSpecJustPrimitivesPath = javaClass.getResource("/cf-spec-just-primitives.json").path
        val cfSpec = generator.processFile(cfSpecJustPrimitivesPath)
        val packageName = "com.ukbar.aws"
        val tempDir = createTempDir()

        generator.createPropertyClass(cfSpec, packageName, tempDir)

        val existingFilepath = "/tmp/TimeToLiveSpecification.kt"
//        val existingFilepath = "$tempDir/TimeToLiveSpecification.kt"
        assertTrue(File(existingFilepath).exists())
    }
}