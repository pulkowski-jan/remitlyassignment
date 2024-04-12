package me.janpulkowski.remitly

import kotlinx.serialization.json.*
import java.io.File
import java.util.Optional


fun main(args: Array<String>) {
    val json = if (args.isNotEmpty()) {
        try {
            readFromFile(args.first())
        } catch(e: Exception) {
            println("Input Error: ${e.message}")
            return
        }
    } else {
        readFromStdIn()
    }
    try {
        println(verify(json))
    } catch (e: IllegalArgumentException) {
        println("Malformed data: ${e.message}")
    }
}

/**
 * Checks if resources field contains a single asterisk.
 * @param json data in AWS::IAM::Role Policy JSON format.
 * @return false if the field contains a single asterisk, true otherwise.
 */
fun verify(json: String) = isNotStar(getResource(json))

/**
 * Checks if given optional string is an asterisk
 * @param resource value in Optional container
 * @return false if the optional is asterisk, true otherwise.
 */
fun isNotStar(resource: Optional<String>): Boolean =
    resource.isEmpty || resource.get() != "*"

/**
 * Parses given json data and returns value of resources field
 * @param json data in AWS::IAM::Role Policy JSON format.
 * @return Optional containing sole value of the resources field if it's a string.
 * Empty optional if it doesn't exist, the field is multivalued, or it's not a string.
 */
fun getResource(json: String): Optional<String> {
    with(Json.parseToJsonElement(json)) {
        val document = jsonObject["PolicyDocument"]
        requireNotNull(document) {
            "PolicyDocument field is required in the format."
        }
        val statement = document.jsonObject["Statement"]
        val resource: JsonElement? = when (statement) {
            is JsonObject -> statement.jsonObject["Resource"]
            is JsonArray -> {
                val firstStatement = statement.jsonArray.firstOrNull()
                firstStatement?.jsonObject?.get("Resource")
            }
            else -> null
        }
        return when (resource) {
            is JsonPrimitive -> getFromPrimitive(resource.jsonPrimitive)
            is JsonArray -> getFromArray(resource.jsonArray)
            else -> Optional.empty()
        }
    }
}

/**
 * Extracts sole value from the array
 * @return Optional of the value if the array contains only 1 element, which is a string
 * Empty optional otherwise.
 */
private fun getFromArray(array: JsonArray): Optional<String> =
    if (array.size == 1 && array.first() is JsonPrimitive) {
        getFromPrimitive(array.first() as JsonPrimitive)
    }  else {
        Optional.empty()
    }

/**
 * Extracts value from the field
 * @return Optional of the value if the field contains a string
 * Empty optional otherwise.
 */
private fun getFromPrimitive(primitive: JsonPrimitive): Optional<String> =
    if (primitive.isString) {
        Optional.of(primitive.content)
    } else {
        Optional.empty()
    }

/**
 * Reads all text from the file
 * @param path path to the file
 * @return contents of the file under `path`
 */
private fun readFromFile(path: String): String {
    val file = File(path)
    require(file.exists()) {
        "File $path not found"
    }
    require(file.isFile) {
        "$path is a directory"
    }
    require(file.canRead()) {
        "Can't read from $path"
    }
    return file.readText()
}

/**
 * Reads from the standard input stream until EOF is reached.
 */
private fun readFromStdIn() =
    buildString {
        var line: String?
        while (readlnOrNull().also { line = it } != null) {
            appendLine(line!!)
        }
    }