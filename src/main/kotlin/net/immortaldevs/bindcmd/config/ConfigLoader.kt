package net.immortaldevs.bindcmd.config

import java.io.File

class ConfigLoader(private val runDirectory: File) {
    companion object {
        private const val CONFIG_DIR = "config"
        private const val CONFIG_FILE = "$CONFIG_DIR/bind_cmd.ini"
        private const val CONFIG_BACKUP_FILE = "$CONFIG_DIR/bind_cmd.ini.bak"
    }

    fun read(): List<Pair<String, String>>? {
        ensureConfigDirExists()
        val file = runDirectory.resolve(CONFIG_FILE)
        if (!file.exists()) {
            file.createNewFile()
            return null
        }
        try {
            val stream = file.inputStream()
            val content = stream.readBytes().decodeToString()
            return decode(content)
        } catch (e: Exception) {
            return null
        }
    }

    fun write(data: List<Pair<String, String>>, backup: Boolean = false) {
        ensureConfigDirExists()
        val file = runDirectory.resolve(backup.let {
            if (it) CONFIG_BACKUP_FILE else CONFIG_FILE
        })
        file.writeText(encode(data))
    }

    private fun ensureConfigDirExists() {
        val configDir = runDirectory.resolve(CONFIG_DIR)
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
    }

    private fun decode(input: String): List<Pair<String, String>> {
        val lines = input.split("\n").filter { it.isNotEmpty() }
        val data = mutableListOf<Pair<String, String>>()
        for (line in lines) {
            val parts = line.split("=\"")
            if (parts.size == 2) {
                val translationKey = parts[0]
                val command = parts[1].substring(0, parts[1].length - 1)
                data.add(Pair(translationKey, command))
            }
        }
        return data
    }

    private fun encode(data: List<Pair<String, String>>): String {
        return data.joinToString("\n") { "${it.first}=\"${it.second}\"" }
    }
}