package fr.ava.ia.service.hf.bloom

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path

class OSUtils {

    suspend fun executeCommand(
        executable: Path,
        vararg args: String
    ): CommandResult =
        // Blocking I/O should use threads designated for I/O
        withContext(Dispatchers.IO) {
            val cmdArgs = listOf(executable.toAbsolutePath().toString()) + args
            val proc = Runtime.getRuntime().exec(cmdArgs.toTypedArray())
            try {
                // Concurrent execution ensures the stream's buffer doesn't
                // block processing when overflowing
                val stdout = async {
                    runInterruptible {
                        // That `InputStream.read` doesn't listen to thread interruption
                        // signals; but for future development it doesn't hurt
                        String(proc.inputStream.readAllBytes(), UTF_8)
                    }
                }
                val stderr = async {
                    runInterruptible {
                        String(proc.errorStream.readAllBytes(), UTF_8)
                    }
                }
                CommandResult(
                    exitCode = runInterruptible { proc.waitFor() },
                    stdout = stdout.await(),
                    stderr = stderr.await()
                )
            } finally {
                // This interrupts the streams as well, so it terminates
                // async execution, even if thread interruption for that
                // InputStream doesn't work
                proc.destroy()
            }
        }

}

//data class CommandResult(
//    val exitCode: Int,
//    val stdout: String,
//    val stderr: String,
//)
