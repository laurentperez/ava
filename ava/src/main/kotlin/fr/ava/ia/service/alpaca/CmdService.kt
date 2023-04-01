package fr.ava.ia.service.alpaca

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@ApplicationScoped
class CmdService {

    suspend fun executeCommand(command: String): Pair<String, String> = coroutineScope {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val process = ProcessBuilder()
            .command("bash", "-c", command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        // Launch coroutine to read the process stdout and stderr concurrently
        val stdoutJob = launch(Dispatchers.IO) {
            process.inputStream.copyTo(stdout)
        }
        val stderrJob = launch(Dispatchers.IO) {
            process.errorStream.copyTo(stderr)
        }

        // Wait for both coroutines to complete
        stdoutJob.join()
        stderrJob.join()

        // Return the stdout and stderr as a Pair
        Pair(stdout.toString(), stderr.toString())
    }

}
