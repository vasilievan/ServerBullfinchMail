package vasiliev.aleksey.bullfinchserver.general

import vasiliev.aleksey.bullfinchserver.general.Constants.DEFAULT_CHARSET
import vasiliev.aleksey.bullfinchserver.general.Constants.KEY_FACTORY_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.Constants.PING
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object GlobalLogic {
    val secureRandom = SecureRandom()
    private val messageDigest = MessageDigest.getInstance("SHA-256")

    fun String.makeByteArray(): ByteArray = this.toByteArray(DEFAULT_CHARSET)

    fun ByteArray.makeString(): String = String(this, DEFAULT_CHARSET)

    fun countHash(anything: ByteArray): ByteArray = messageDigest.digest(anything)

    fun readNext(data: ByteArray, clientSocket: Socket): ByteArray {
        val beginningTime = Date().time
        while (true) {
            try {
                if (Date().time - beginningTime > PING) {
                    closeSocketAndStreams(clientSocket, clientSocket.getOutputStream())
                    break
                }
                val count = clientSocket.getInputStream().read(data, 0, data.size)
                if (count > 0) {
                    return data.copyOfRange(0, count)
                } else if (count == -1) {
                    closeSocketAndStreams(clientSocket, clientSocket.getOutputStream())
                    break
                }
            } catch (e: IOException) {
            }
        }
        return ByteArray(0)
    }

    fun sendSomethingToUser(whatToSend: ByteArray, writer: OutputStream) {
        writer.write(whatToSend)
        writer.flush()
    }

    fun closeSocketAndStreams(clientSocket: Socket?, writer: OutputStream?) {
        println("Client socket closed.")
        clientSocket?.getInputStream()?.close()
        writer?.close()
        clientSocket?.close()
    }

    fun generateHashedPassword(password: String, secretSalt: ByteArray): Pair<String, ByteArray> {
        val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITM)
        val keySpec = PBEKeySpec(password.toCharArray(), secretSalt, 65536, 256)
        val hashedPassword = secretKeyFactory.generateSecret(keySpec).encoded
        return Pair(hashedPassword.makeString(), secretSalt)
    }

    fun authoriseUser(data: ByteArray, clientSocket: Socket, decipher: Cipher?, privateKey: PrivateKey, writer: OutputStream): String? {
        val loginBytesCiphered = readNext(data, clientSocket)
        decipher!!.init(Cipher.DECRYPT_MODE, privateKey)
        val login = decipher.doFinal(loginBytesCiphered).makeString()
        val db = DataBase()
        val loginInside = db.ifLoginIsAlreadyInDb(login)
        if (!loginInside) {
            closeSocketAndStreams(clientSocket, writer)
            return null
        }
        sendSomethingToUser("Login is correct.".makeByteArray(), writer)
        val hashedPasswordAndSalt = db.getHashedUserPasswordAndSalt(login)
        val passwordBytesCiphered = readNext(data, clientSocket)
        val password = decipher.doFinal(passwordBytesCiphered).makeString()
        val salt = hashedPasswordAndSalt.second
        val newHashedPassword = generateHashedPassword(password, salt)
        if (newHashedPassword.first == hashedPasswordAndSalt.first) {
            sendSomethingToUser("Password is correct.".makeByteArray(), writer)
            return login
        }
        return null
    }

    fun todaysDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year.$month.$day"
    }
}