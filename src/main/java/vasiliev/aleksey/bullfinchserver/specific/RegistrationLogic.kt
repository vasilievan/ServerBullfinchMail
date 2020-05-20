package vasiliev.aleksey.bullfinchserver.specific

import vasiliev.aleksey.bullfinchserver.general.DataBase
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.readNext
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.closeSocketAndStreams
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.generateHashedPassword
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.secureRandom
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.sendSomethingToUser
import java.io.OutputStream
import java.net.Socket
import java.security.PrivateKey
import java.util.logging.Logger
import javax.crypto.Cipher
import vasiliev.aleksey.bullfinchserver.general.Constants.SALT_SIZE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CORRECT_LOGIN_RESPONSE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CORRECT_PASSWORD_RESPONSE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.NEW_USER_REGISTERED_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.NOT_UNIQUE_USER_RESPONSE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.SIGN_UP_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.SUCCESS_RESPOND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.WARNING_PHRASE

object RegistrationLogic {

    private val logger: Logger = Logger.getLogger(this.javaClass.name)

    fun signUserUp(clientSocket: Socket, decipher: Cipher?, writer: OutputStream, privateKey: PrivateKey) {
        logger.info(SIGN_UP_PHRASE)
        val loginBytes = readNext(clientSocket)
        decipher!!.init(Cipher.DECRYPT_MODE, privateKey)
        val login = decipher.doFinal(loginBytes).makeString()
        val db = DataBase()
        if (db.ifLoginIsAlreadyInDb(login)) {
            logger.warning(WARNING_PHRASE)
            sendSomethingToUser(NOT_UNIQUE_USER_RESPONSE, writer)
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        sendSomethingToUser(CORRECT_LOGIN_RESPONSE, writer)
        val passwordBytes = readNext(clientSocket)
        val password = decipher.doFinal(passwordBytes).makeString()
        sendSomethingToUser(CORRECT_PASSWORD_RESPONSE, writer)
        val userNameBytes = readNext(clientSocket)
        val userName = decipher.doFinal(userNameBytes).makeString()
        val secretSalt = ByteArray(SALT_SIZE)
        secureRandom.nextBytes(secretSalt)
        db.registerUser(login, generateHashedPassword(password, secretSalt), userName)
        logger.info(NEW_USER_REGISTERED_PHRASE)
        sendSomethingToUser(SUCCESS_RESPOND, writer)
        closeSocketAndStreams(clientSocket, writer)
    }
}