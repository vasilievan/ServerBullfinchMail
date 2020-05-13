package vasiliev.aleksey.bullfinchserver.specific

import vasiliev.aleksey.bullfinchserver.general.Constants.CIPHER_ALGORITM
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

    fun checkForFriendsRequest (data: ByteArray, clientSocket: Socket, decipher: Cipher, writer: OutputStream, privateKey: PrivateKey) {
        logger.info("Someone wants to check for friend requests.")
        val login = authoriseUser(data, clientSocket, decipher, privateKey, writer)
        if (login == null) {
            logger.warning("Something went wrong.")
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val myPublicKey = readNext(data, clientSocket)
        val cipherHelper = Cipher.getInstance(CIPHER_ALGORITM)
        cipherHelper.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance(CIPHER_ALGORITM).generatePublic(X509EncodedKeySpec(myPublicKey)))
        val db = DataBase()
        val amountOfNewRequests = db.checkIfThereAreNewRequests(login)
        sendSomethingToUser(cipherHelper.doFinal(amountOfNewRequests.toString().makeByteArray()), writer)
        if (amountOfNewRequests == 0L) {
            logger.info("No requests yet.")
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        val dataList = db.listOfTriples(login)
        for (element in dataList) {
            sendSomethingToUser(cipherHelper.doFinal(element.first.makeByteArray()), writer)
            sendSomethingToUser(cipherHelper.doFinal(element.second.makeByteArray()), writer)
            sendSomethingToUser(element.third, writer)
            val hisPublicKeyCiphered = readNext(data, clientSocket)
            val hisPublicKey = decipher.doFinal(hisPublicKeyCiphered)
            db.transferPublicKeyToFriend(hisPublicKey, login, element.first)
        }
        logger.info("Served successfully.")
        closeSocketAndStreams(clientSocket, writer)
    }
}