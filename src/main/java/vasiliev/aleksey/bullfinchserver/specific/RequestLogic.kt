package vasiliev.aleksey.bullfinchserver.specific

import vasiliev.aleksey.bullfinchserver.general.Constants.CIPHER_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.Constants.KEY_FACTORY_ALGORITM
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

object RequestLogic {
    private val logger = Logger.getLogger(this.javaClass.name)

    fun checkForFriendsRequestAndNewMessages(data: ByteArray, clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info("Someone wants to check for friend requests.")
        val login = authoriseUser(data, clientSocket, decipher, privateKey, writer)
        if (login == null) {
            logger.warning("Something went wrong.")
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val db = DataBase()
        val amountOfNewRequests = db.checkIfThereAreNewRequests(login)
        val reverseKey = readNext(data, clientSocket)
        val cipher = Cipher.getInstance(CIPHER_ALGORITM)
        cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance(KEY_FACTORY_ALGORITM).generatePublic(X509EncodedKeySpec(reverseKey)))
        val cipheredAmount = cipher.doFinal(amountOfNewRequests.toString().makeByteArray())
        sendSomethingToUser(cipheredAmount, writer)
        val accepted = readNext(data, clientSocket).makeString()
        if (accepted != "Amount received.") {
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        if (amountOfNewRequests == 0L) {
            logger.info("No requests yet.")
        }
        val dataList = db.listOfTriples(login)
        for (element in dataList) {
            sendSomethingToUser(cipher.doFinal(element.first.makeByteArray()), writer)
            readNext(data, clientSocket).makeString()
            sendSomethingToUser(cipher.doFinal(element.second.makeByteArray()), writer)
            readNext(data, clientSocket).makeString()
            sendSomethingToUser(element.third, writer)
            val hisPublicKey = readNext(data, clientSocket)
            if (hisPublicKey.makeString() != "Stop it.") db.transferPublicKeyToFriend(hisPublicKey, login, element.first)
        }
        val amountOfNewMessages = db.checkIfThereAreNewMessages(login)
        sendSomethingToUser(cipher.doFinal(amountOfNewMessages.toString().makeByteArray()), writer)
        if (amountOfNewMessages == 0L) {
            logger.info("No messages yet.")
            closeSocketAndStreams(clientSocket, writer)
        }
        val messagesList = db.listOfMessagesTriples(login)
        for (element in messagesList) {
            sendSomethingToUser(cipher.doFinal(element.first.makeByteArray()), writer)
            readNext(data, clientSocket).makeString()
            sendSomethingToUser(element.second, writer)
            readNext(data, clientSocket).makeString()
            sendSomethingToUser(element.third, writer)
            readNext(data, clientSocket).makeString()
        }
        logger.info("Served successfully.")
        closeSocketAndStreams(clientSocket, writer)
    }
}