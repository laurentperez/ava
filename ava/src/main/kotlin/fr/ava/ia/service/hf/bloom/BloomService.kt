package fr.ava.ia.service.hf.bloom

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.nio.charset.StandardCharsets
import java.nio.file.Path

@ApplicationScoped
class BloomService(
    @ConfigProperty(name = "hf.bloom.executor")
    private val executor : String,
    @ConfigProperty(name = "hf.bloom.executorModelGgml")
    private val executorModel : String,
    @ConfigProperty(name = "hf.bloom.executorThreads")
    private val executorThreads : String,
) {

    fun prompt(prompt : String) : String {
        val cores = Runtime.getRuntime().availableProcessors()
        if(Integer.valueOf(executorThreads) > cores) {
            return "\uD83E\uDDE0 not enough CPU cores. please lower executorThreads value."
        }
        val cmdArgs = listOf(
            Path.of(executor).toAbsolutePath().toString(),
            "-m", executorModel,
            "-t", executorThreads,
            "-n", "128",
            "-p", prompt
        )
        val proc = Runtime.getRuntime().exec(cmdArgs.toTypedArray())
        val x = String(proc.inputStream.readAllBytes(), StandardCharsets.UTF_8)
        val e = String(proc.errorStream.readAllBytes(), StandardCharsets.UTF_8)
        try {
            return "bloom said: $x $e"
        } finally {
            proc.destroy()
        }
    }
}

data class CommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)
