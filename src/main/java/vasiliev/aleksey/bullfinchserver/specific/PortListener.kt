package vasiliev.aleksey.bullfinchserver.specific

import java.util.logging.Logger
import vasiliev.aleksey.bullfinchserver.general.Constants.CIPHER_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.closeSocketAndStreams
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.readNext
import vasiliev.aleksey.bullfinchserver.specific.RegistrationLogic.signUserUp
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.sendSomethingToUser
import vasiliev.aleksey.bullfinchserver.specific.ChangingLogic.changeUserName
import vasiliev.aleksey.bullfinchserver.specific.FriendsFindingLogic.makeFriend
import vasiliev.aleksey.bullfinchserver.specific.MessagingLogic.sendMessageToSomeone
import vasiliev.aleksey.bullfinchserver.specific.RequestLogic.checkForFriendsRequest
import java.io.IOException
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyPairGenerator
import java.security.PrivateKey
import javax.crypto.Cipher
import kotlin.concurrent.thread

class PortListener(portNumber: Int, private val keyGen: KeyPairGenerator) {
    private val serverSocket = ServerSocket(portNumber)
    private var privateKey: PrivateKey? = null
    var clientSocket: Socket? = null
    var writer: OutputStream? = null
    var decipher: Cipher = Cipher.getInstance(CIPHER_ALGORITM)
    private val data = ByteArray(8198)

    companion object {
        val logger: Logger = Logger.getLogger(PortListener::class.java.name)
    }

    // ok
    init {
        thread {
            try {
                while (true) {
                    clientSocket = serverSocket.accept()
                    if (clientSocket != null) {
                        logger.info("Client connected.")
                        writer = clientSocket!!.getOutputStream()
                        understandWhatTheyWantFromYou()
                    }
                }
            } catch (e: IOException) {
            }
        }
    }

    private fun understandWhatTheyWantFromYou() {
        when (readNext(data, clientSocket!!).makeString()) {
            "I want to exchange keys." -> sendKeyToUser()
            "I want to sign up." -> signUserUp(data, clientSocket!!, decipher, writer!!, privateKey!!)
            "I want to change a username." -> changeUserName(data, clientSocket!!, decipher, writer!!, privateKey!!)
            "I want to make friends." -> makeFriend(data, clientSocket!!, decipher, writer!!, privateKey!!)
            "I want to check for friends requests." -> checkForFriendsRequest(data, clientSocket!!, decipher, writer!!, privateKey!!)
            "I want to send a message." -> sendMessageToSomeone(data, clientSocket!!, decipher, writer!!, privateKey!!)
            else -> closeSocketAndStreams(clientSocket, writer)
        }
    }

    private fun sendKeyToUser() {
        val keys = keyGen.genKeyPair()
        privateKey = keys.private
        sendSomethingToUser(keys.public.encoded, writer!!)
        logger.info("I sent session key to user.")
        understandWhatTheyWantFromYou()
    }
}