package vasiliev.aleksey.bullfinchserver.specific

import vasiliev.aleksey.bullfinchserver.general.DataBase
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.authoriseUser
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.closeSocketAndStreams
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeByteArray
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.readNext
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.sendSomethingToUser
import java.io.OutputStream
import java.net.Socket
import java.security.PrivateKey
import java.util.logging.Logger
import javax.crypto.Cipher

object MessagingLogic {
    val logger: Logger = Logger.getLogger(this.javaClass.name)

    fun sendMessageToSomeone(data: ByteArray, clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info("Someone wants to send a message.")
        val login = authoriseUser(data, clientSocket, decipher, privateKey, writer)
        if (login == null) {
            logger.info("Oops. Incorret login.")
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val db = DataBase()
        val toWhom = decipher.doFinal(readNext(data, clientSocket)).makeString()
        sendSomethingToUser("Accepted.".makeByteArray(), writer)
        val date = readNext(data, clientSocket)
        sendSomethingToUser("Accepted.".makeByteArray(), writer)
        val content = readNext(data, clientSocket)
        db.saveMessageFromUser(login, toWhom, date, content)
        sendSomethingToUser("Accepted.".makeByteArray(), writer)
        closeSocketAndStreams(clientSocket, writer)
    }
}