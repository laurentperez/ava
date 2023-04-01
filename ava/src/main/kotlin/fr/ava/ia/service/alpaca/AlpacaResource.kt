package fr.ava.ia.service.alpaca

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import kotlinx.coroutines.runBlocking
import org.jboss.logging.Logger

@Path("/alpaca")
class AlpacaResource(@Inject var cmdService: CmdService) {

    private var logger = Logger.getLogger(this::class.java.name)

    @GET
    @Produces("text/plain")
    fun alpaca() : String { // TODO suspend reactive fun this
        var cmd : Pair<String, String>
        runBlocking {
            cmd = cmdService.executeCommand("pwd && ls")
            logger.info(cmd.first)
            if (cmd.second.isNotEmpty()) logger.warn(cmd.second)
        }
        return cmd.toString()
    }

}
