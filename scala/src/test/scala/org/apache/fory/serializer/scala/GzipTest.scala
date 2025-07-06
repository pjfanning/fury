/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fory.serializer.scala

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.nio.charset.StandardCharsets
import java.util.zip.{ GZIPInputStream, GZIPOutputStream }

import scala.annotation.tailrec

import org.apache.fory.Fory
import org.apache.fory.config.Language
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GZipTest extends AnyWordSpec with Matchers {
  def fory: Fory = Fory.builder()
    .withLanguage(Language.JAVA)
    .withRefTracking(true)
    .withScalaOptimizationEnabled(true)
    .requireClassRegistration(false)
    .suppressClassRegistrationWarnings(false).build()

  "fory scala support" should {
    "serialize/deserialize package object (gzipped)" in {
      val p = SomePackageObject.SomeClass(1)
      val bytes = gzip(fory.serialize(p))
      val unzipped = gunzip(bytes)
      fory.deserialize(unzipped) shouldEqual p
    }
  }

  "validate gzip compression" in {
    val original = "This is a test string to be compressed using GZIP."
    val compressed = gzip(original.getBytes(StandardCharsets.UTF_8))
    val decompressed = new String(gunzip(compressed), StandardCharsets.UTF_8)
    decompressed shouldEqual original
  }

  private def gzip(bytes: Array[Byte]): Array[Byte] = {
    val bos = new ByteArrayOutputStream(1024)
    val zip = new GZIPOutputStream(bos)
    try zip.write(bytes)
    finally zip.close()
    bos.toByteArray
  }

  private def gunzip(bytes: Array[Byte]): Array[Byte] = {
    val in = new GZIPInputStream(new ByteArrayInputStream(bytes))
    val out = new ByteArrayOutputStream()
    val buffer = new Array[Byte](1024)

    @tailrec def readChunk(): Unit = in.read(buffer) match {
      case -1 => ()
      case n =>
        out.write(buffer, 0, n)
        readChunk()
    }

    try readChunk()
    finally in.close()
    out.toByteArray
  }
}
