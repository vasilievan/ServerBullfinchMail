package vasiliev.aleksey.bullfinchserver.general

import org.json.JSONArray
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.logging.Logger
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import vasiliev.aleksey.bullfinchserver.general.Constants.DEFAULT_CHARSET
import vasiliev.aleksey.bullfinchserver.general.Constants.MESSAGE_DIGEST_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.Constants.SECRET_KEY_FACTORY_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CLOSE_CLIENT_SOCKET_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CORRECT_LOGIN_RESPONSE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CORRECT_PASSWORD_RESPONSE

object GlobalLogic {
    private val logger = Logger.getLogger(this.javaClass.name)
    val secureRandom = SecureRandom()
    private val messageDigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITM)

    fun String.makeByteArray(): ByteArray = this.toByteArray(DEFAULT_CHARSET)

    fun ByteArray.makeString(): String = String(this, DEFAULT_CHARSET)

    fun countHash(anything: ByteArray): ByteArray = messageDigest.digest(anything)

    fun makeKeyBytesFromJSONArray(jsonArray: JSONArray): ByteArray {
        val byteArray = ByteArray(jsonArray.length())
        for ((counter, element) in jsonArray.withIndex()) {
            byteArray[counter] = element.toString().toByte()
        }
        return byteArray
    }

    fun readNext(clientSocket: Socket): ByteArray {
        val data = ByteArray(8198)
        while (true) {
            try {
                val count = clientSocket.getInputStream().read(data, 0, data.size)
                if (count > 0) {
                    return data.copyOfRange(0, count)
                } else if (count == -1) {
                    closeSocketAndStreams(clientSocket, clientSocket.getOutputStream())
                    break
                }
            } catch (e: IOException) {
                closeSocketAndStreams(clientSocket, clientSocket.getOutputStream())
            }
        }
        return ByteArray(0)
    }

    fun sendSomethingToUser(whatToSend: ByteArray, writer: OutputStream) {
        writer.write(whatToSend)
        writer.flush()
    }

    fun closeSocketAndStreams(clientSocket: Socket?, writer: OutputStream?) {
        logger.info(CLOSE_CLIENT_SOCKET_PHRASE)
        writer?.close()
        clientSocket?.close()
    }

    fun generateHashedPassword(password: String, secretSalt: ByteArray): Pair<String, ByteArray> {
        val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITM)
        val keySpec = PBEKeySpec(password.toCharArray(), secretSalt, 65536, 256)
        val hashedPassword = secretKeyFactory.generateSecret(keySpec).encoded
        return Pair(hashedPassword.makeString(), secretSalt)
    }

    fun authoriseUser(clientSocket: Socket, decipher: Cipher?, privateKey: PrivateKey, writer: OutputStream): String? {
        val loginBytesCiphered = readNext(clientSocket)
        decipher!!.init(Cipher.DECRYPT_MODE, privateKey)
        val login = decipher.doFinal(loginBytesCiphered).makeString()
        val db = DataBase()
        val loginInside = db.ifLoginIsAlreadyInDb(login)
        if (!loginInside) {
            closeSocketAndStreams(clientSocket, writer)
            return null
        }
        sendSomethingToUser(CORRECT_LOGIN_RESPONSE, writer)
        val hashedPasswordAndSalt = db.getHashedUserPasswordAndSalt(login)
        val passwordBytesCiphered = readNext(clientSocket)
        val password = decipher.doFinal(passwordBytesCiphered).makeString()
        val salt = hashedPasswordAndSalt.second
        val newHashedPassword = generateHashedPassword(password, salt)
        if (newHashedPassword.first == hashedPasswordAndSalt.first) {
            sendSomethingToUser(CORRECT_PASSWORD_RESPONSE, writer)
            return login
        }
        closeSocketAndStreams(clientSocket, writer)
        return null
    }
}