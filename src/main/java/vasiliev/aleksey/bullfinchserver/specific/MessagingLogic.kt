package vasiliev.aleksey.bullfinchserver.specific

import vasiliev.aleksey.bullfinchserver.general.DataBase
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.authoriseUser
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.closeSocketAndStreams
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.readNext
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.sendSomethingToUser
import java.io.OutputStream
import java.net.Socket
import java.security.PrivateKey
import java.util.logging.Logger
import javax.crypto.Cipher
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.ACCEPTED_RESPONSE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.INCORRECT_LOGIN_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.NEW_MESSAGE_PHRASE

object MessagingLogic {
    val logger: Logger = Logger.getLogger(this.javaClass.name)

    fun sendMessageToSomeone(clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info(NEW_MESSAGE_PHRASE)
        val login = authoriseUser(clientSocket, decipher, privateKey, writer)
        if (login == null) {
            logger.info(INCORRECT_LOGIN_PHRASE)
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val db = DataBase()
        val toWhom = decipher.doFinal(readNext(clientSocket)).makeString()
        sendSomethingToUser(ACCEPTED_RESPONSE, writer)
        val date = readNext(clientSocket)
        sendSomethingToUser(ACCEPTED_RESPONSE, writer)
        val content = readNext(clientSocket)
        db.saveMessageFromUser(login, toWhom, date, content)
        sendSomethingToUser(ACCEPTED_RESPONSE, writer)
        closeSocketAndStreams(clientSocket, writer)
    }
}