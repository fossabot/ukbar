@file:Suppress("Classname")

package com.ukbar

import com.beust.klaxon.Parser
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Nested
open class GeneratorIT {

    private val cfSpecFilepath = javaClass.getResource("/cf-spec.json").path
    private val generator = Generator(Parser())

    @Test
    fun `can create json object from CloudFormation spec String`() {
        val cfSpec = generator.processFile(cfSpecFilepath)

        assertNotNull(cfSpec)
        assertNotNull(cfSpec["PropertyTypes"])
        assertNotNull(cfSpec["ResourceTypes"])
        assertNotNull(cfSpec["ResourceSpecificationVersion"])
    }

    @Test
    fun `can create property class from CloudFormation specification object`() {
        val cfSpec = generator.processFile(cfSpecFilepath)
        val packageName = "com.ukbar.aws"
        val tempDir = createTempDir()

        generator.createPropertyClass(cfSpec, packageName, tempDir)

        val existingFilepath = "$tempDir/BucketEncryption.kt"
        assertTrue(File(existingFilepath).exists())
    }

}