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
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.X509EncodedKeySpec
import java.util.logging.Logger
import javax.crypto.Cipher
import vasiliev.aleksey.bullfinchserver.general.Constants.CIPHER_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.Constants.KEY_FACTORY_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.AMOUNT_RECEIVED_COMMAND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CHECKING_FOR_REQUESTS_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.NO_MESSAGES_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.NO_REQUESTS_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.SERVED_SUCCESSFULLY_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.STOP_COMMAND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.WARNING_PHRASE

object RequestLogic {
    private val logger = Logger.getLogger(this.javaClass.name)

    fun checkForFriendsRequestAndNewMessages(clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info(CHECKING_FOR_REQUESTS_PHRASE)
        val login = authoriseUser(clientSocket, decipher, privateKey, writer)
        if (login == null) {
            logger.warning(WARNING_PHRASE)
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val db = DataBase()
        val amountOfNewRequests = db.checkIfThereAreNewRequests(login)
        val reverseKey = readNext(clientSocket)
        val cipher = Cipher.getInstance(CIPHER_ALGORITM)
        cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance(KEY_FACTORY_ALGORITM).generatePublic(X509EncodedKeySpec(reverseKey)))
        val cipheredAmount = cipher.doFinal(amountOfNewRequests.toString().makeByteArray())
        sendSomethingToUser(cipheredAmount, writer)
        val accepted = readNext(clientSocket).makeString()
        if (accepted != AMOUNT_RECEIVED_COMMAND) {
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        if (amountOfNewRequests == 0L) {
            logger.info(NO_REQUESTS_PHRASE)
        }
        val dataList = db.listOfTriples(login)
        for (element in dataList) {
            sendSomethingToUser(cipher.doFinal(element.first.makeByteArray()), writer)
            readNext(clientSocket).makeString()
            sendSomethingToUser(cipher.doFinal(element.second.makeByteArray()), writer)
            readNext(clientSocket).makeString()
            sendSomethingToUser(element.third, writer)
            val hisPublicKey = readNext(clientSocket)
            if (hisPublicKey.makeString() != STOP_COMMAND) db.transferPublicKeyToFriend(hisPublicKey, login, element.first)
        }
        val amountOfNewMessages = db.checkIfThereAreNewMessages(login)
        sendSomethingToUser(cipher.doFinal(amountOfNewMessages.toString().makeByteArray()), writer)
        if (amountOfNewMessages == 0L) {
            logger.info(NO_MESSAGES_PHRASE)
            closeSocketAndStreams(clientSocket, writer)
        }
        val messagesList = db.listOfMessagesTriples(login)
        for (element in messagesList) {
            sendSomethingToUser(cipher.doFinal(element.first.makeByteArray()), writer)
            readNext(clientSocket).makeString()
            sendSomethingToUser(element.second, writer)
            readNext(clientSocket).makeString()
            sendSomethingToUser(element.third, writer)
            readNext(clientSocket).makeString()
        }
        logger.info(SERVED_SUCCESSFULLY_PHRASE)
        closeSocketAndStreams(clientSocket, writer)
    }
}