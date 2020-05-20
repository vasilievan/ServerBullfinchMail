package vasiliev.aleksey.bullfinchserver.specific

import vasiliev.aleksey.bullfinchserver.general.DataBase
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.authoriseUser
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.closeSocketAndStreams
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.readNext
import java.io.OutputStream
import java.net.Socket
import java.security.PrivateKey
import java.util.logging.Logger
import javax.crypto.Cipher
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.USERNAME_CHANGED_SUCCESSFULLY_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.USERNAME_CHANGING_PHRASE

object ChangingLogic {
    private val logger: Logger = Logger.getLogger(this.javaClass.name)

    fun changeUserName(clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info(USERNAME_CHANGING_PHRASE)
        val login = authoriseUser(clientSocket, decipher, privateKey, writer)
        if (login == null) {
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val newUsernameBytes = readNext(clientSocket)
        val userName = decipher.doFinal(newUsernameBytes).makeString()
        val db = DataBase()
        db.changeUserName(login, userName)
        logger.info(USERNAME_CHANGED_SUCCESSFULLY_PHRASE)
        closeSocketAndStreams(clientSocket, writer)
    }
}