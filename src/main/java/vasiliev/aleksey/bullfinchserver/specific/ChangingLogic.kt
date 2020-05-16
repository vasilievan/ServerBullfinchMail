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

object ChangingLogic {
    private val logger: Logger = Logger.getLogger(this.javaClass.name)

    fun changeUserName(data:ByteArray, clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info("Someone wants to change a username.")
        val login = authoriseUser(data, clientSocket, decipher, privateKey, writer)
        if (login == null) {
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val newUsernameBytes = readNext(data, clientSocket)
        val userName = decipher.doFinal(newUsernameBytes).makeString()
        val db = DataBase()
        db.changeUserName(login, userName)
        logger.info("Someone changed his username.")
        closeSocketAndStreams(clientSocket, writer)
    }
}