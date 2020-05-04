package vasiliev.aleksey.bullfinchserver.specific

import vasiliev.aleksey.bullfinchserver.general.Constants.SALT_SIZE
import vasiliev.aleksey.bullfinchserver.general.DataBase
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.readNext
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.closeSocketAndStreams
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.generateHashedPassword
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeByteArray
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.secureRandom
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.sendSomethingToUser
import java.io.OutputStream
import java.net.Socket
import java.security.PrivateKey
import javax.crypto.Cipher

object RegistrationLogic {
    fun signUserUp(data: ByteArray, clientSocket: Socket, decipher: Cipher?, writer: OutputStream, privateKey: PrivateKey?) {
        println("Someone wants to sign up.")
        val loginBytes = readNext(data, clientSocket)
        decipher!!.init(Cipher.DECRYPT_MODE, privateKey)
        val login = decipher.doFinal(loginBytes).makeString()
        println("I received login.")
        val db = DataBase()
        if (db.ifLoginIsAlreadyInDb(login)) {
            sendSomethingToUser("Oops. This user is already in db.".makeByteArray(), writer)
            closeSocketAndStreams(clientSocket, writer)
            return
        }
        sendSomethingToUser("Login is correct.".makeByteArray(), writer)

        val passwordBytes = readNext(data, clientSocket)
        val password = decipher.doFinal(passwordBytes).makeString()
        println("I received password.")

        sendSomethingToUser("Password is correct.".makeByteArray(), writer)

        val userNameBytes = readNext(data, clientSocket)
        val userName = decipher.doFinal(userNameBytes).makeString()

        val secretSalt = ByteArray(SALT_SIZE)
        secureRandom.nextBytes(secretSalt)

        db.registerUser(login, generateHashedPassword(password, secretSalt), userName)
        println("I registered a new user.")

        sendSomethingToUser("Success!".makeByteArray(), writer)

        closeSocketAndStreams(clientSocket, writer)
    }
}