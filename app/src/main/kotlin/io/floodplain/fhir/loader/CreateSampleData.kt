package io.floodplain.fhir.loader

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.utils.IOUtils
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.utilities.Utilities.createDirectory
import java.io.*
import java.net.URL
import java.nio.channels.Channels
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis


class CreateSampleData

fun main(args: Array<String>) {
//    listPatients()
    downloadIfNeeded()
    loadData(10)
}

fun downloadFile(url: URL, outputFileName: String) {
    url.openStream().use {
        Channels.newChannel(it).use { rbc ->
            FileOutputStream(outputFileName).use { fos ->
                fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
            }
        }
    }
}
//
////If we do not set encoding as "ISO-8859-1", European characters will be replaced with '?'.
//fun unzip(files: List<File>, zipFile: ZipFile) {
//    zipFile.use { zip ->
//        zip.entries().asSequence().forEach { entry ->
//            zip.getInputStream(entry).use { input ->
//                BufferedReader(InputStreamReader(input, "ISO-8859-1")).use { reader ->
////                    files.find { it.name.contains(entry.name) }?.run {
//                        BufferedWriter(FileWriter(entry)).use { writer ->
//                            var line: String? = null
//                            while ({ line = reader.readLine(); line }() != null) {
//                                writer.append(line).append('\n')
//                            }
//                            writer.flush()
//                        }
////                    }
//                }
//            }
//        }
//    }
//}
fun extractZip(input: File, destinationFilePath: File) {
//    val input = File(sourceFilePath)
    val stream: InputStream = FileInputStream(input)
    val unzipStream = ArchiveStreamFactory().createArchiveInputStream("zip", stream)
    var entry: ZipArchiveEntry? = null
    // ZipArchiveEntry entry = (ZipArchiveEntry) in.getNextEntry();
    while ((unzipStream.nextEntry as ZipArchiveEntry?)?.also { entry = it } != null) {
        val outputFile = File(destinationFilePath, entry!!.name)
        if(entry!!.isDirectory) {
            outputFile.mkdirs()
        } else {
            val out: OutputStream = FileOutputStream(outputFile)
            IOUtils.copy(unzipStream, out)
            out.close()
        }
    }
    unzipStream.close()
}

fun downloadIfNeeded() {
    val cache = createDirectory("cache")
    val file = File(cache,"synthea.zip")
    if(!file.exists()) {
        downloadFile(URL("https://synthetichealth.github.io/synthea-sample-data/downloads/synthea_sample_data_fhir_r4_sep2019.zip"),file.absolutePath)
    }
    extractZip(file,cache)
}

fun loadData(numberOfFiles: Int = Int.MAX_VALUE, skip: Int = 0) {
    val context = FhirContext.forR4()
    val parser: IParser = context.newJsonParser()
    val baseUrl = "http://localhost:8084/fhir"
    val client = context.restfulClientFactory.newGenericClient(baseUrl)
    val fileCounter = AtomicInteger(0)
    val resourceCounter = AtomicInteger(0)

    val count = AtomicInteger(0)
    val resourceTotal = AtomicInteger(0)
    val totalTime = AtomicLong(0)
    File("cache/fhir/").walkTopDown()
        .filter { it.isFile }
        .sorted()
        .drop( skip )
        .take(numberOfFiles)
        .forEach {
            val resource = parser.parseResource(it.inputStream())
            val bundle = resource as Bundle
            println("Processing ${count.getAndIncrement()}/${numberOfFiles-skip} ${it.absolutePath}")
            val time = measureTimeMillis {
                bundle.entry.forEach {
                    val resource = it.resource
                    val id = resource.id
                    val idTrimmed = if (id.startsWith("urn:uuid:")) id.substring("urn:uuid:".length) else id
                    resource.id = idTrimmed
                    client.update().resource(resource).execute()
                    resourceTotal.incrementAndGet()
                }
            }
            println("Processed ${resourceTotal.incrementAndGet()} resources. Time: ${totalTime.addAndGet(time)} files: ${fileCounter.incrementAndGet()}")
        }
    println("Total time: ${totalTime.get()} total resources ${resourceTotal.get()} total files ${fileCounter.get()}")
}