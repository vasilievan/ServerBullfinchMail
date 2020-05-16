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

object FriendsFindingLogic {
    private val logger = Logger.getLogger(this.javaClass.name)

    fun makeFriend(data:ByteArray, clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info("Someone wants to make friends.")
        val login = authoriseUser(data, clientSocket, decipher, privateKey, writer)
        if (login == null) {
            logger.info("Oops. Incorret login.")
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val friendUserNameBytes = readNext(data, clientSocket)
        val friendUserName = decipher.doFinal(friendUserNameBytes).makeString()
        val db = DataBase()
        if(!db.ifLoginIsAlreadyInDb(friendUserName)) {
            logger.info("Oops. Unknown user.")
            sendSomethingToUser("Oops. Unknown user.".makeByteArray(), writer)
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        sendSomethingToUser("I know this user.".makeByteArray(), writer)
        val publicKeyForAFriend = readNext(data, clientSocket)
        db.transferPublicKeyToFriend(publicKeyForAFriend, login, friendUserName)
        logger.info("I sent friend request.")
        closeSocketAndStreams(clientSocket, writer)
    }
}