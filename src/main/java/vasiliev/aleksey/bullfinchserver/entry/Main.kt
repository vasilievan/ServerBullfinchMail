package vasiliev.aleksey.bullfinchserver.entry

import vasiliev.aleksey.bullfinchserver.specific.PortListener
import vasiliev.aleksey.bullfinchserver.general.DataBase
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.secureRandom
import vasiliev.aleksey.bullfinchserver.general.Server.createKeyPairGenerator
import java.util.concurrent.Executors
import java.util.logging.Logger
import kotlin.concurrent.thread
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CLOSE_ALL_COMMAND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CLOSE_ALL_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.GREETING_PHRASE

fun main(args: Array<String>) {
    val logger: Logger = Logger.getLogger(PortListener::class.java.name)
    val keyGen = createKeyPairGenerator(secureRandom)
    val threadsPull = Executors.newFixedThreadPool(9)
    logger.info(GREETING_PHRASE)
    val sockets = mutableListOf<PortListener>()
    for (i in 4051..4059) {
        threadsPull.submit {
            sockets.add(PortListener(i, keyGen))
        }
    }
    val db = DataBase()
    db.instance()
    thread {
        val command = readLine()
        if (command == CLOSE_ALL_COMMAND) {
            logger.info(CLOSE_ALL_PHRASE)
            sockets.forEach { it.closeServerSocket() }
        }
    }
}