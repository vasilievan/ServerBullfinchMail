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
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.FRIENDS_MAKING_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.FRIEND_REQUEST_SENT_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.INCORRECT_LOGIN_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.UNKNOWN_USER_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.UNKNOWN_USER_RESPONSE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.WELLKNOWN_USER_RESPONSE

object FriendsFindingLogic {
    private val logger = Logger.getLogger(this.javaClass.name)

    fun makeFriend(clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info(FRIENDS_MAKING_PHRASE)
        val login = authoriseUser(clientSocket, decipher, privateKey, writer)
        if (login == null) {
            logger.info(INCORRECT_LOGIN_PHRASE)
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val friendUserNameBytes = readNext(clientSocket)
        val friendUserName = decipher.doFinal(friendUserNameBytes).makeString()
        val db = DataBase()
        if(!db.ifLoginIsAlreadyInDb(friendUserName)) {
            logger.info(UNKNOWN_USER_PHRASE)
            sendSomethingToUser(UNKNOWN_USER_RESPONSE, writer)
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        sendSomethingToUser(WELLKNOWN_USER_RESPONSE, writer)
        val publicKeyForAFriend = readNext(clientSocket)
        db.transferPublicKeyToFriend(publicKeyForAFriend, login, friendUserName)
        logger.info(FRIEND_REQUEST_SENT_PHRASE)
        closeSocketAndStreams(clientSocket, writer)
    }
}