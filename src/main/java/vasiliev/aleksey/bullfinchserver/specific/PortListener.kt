package vasiliev.aleksey.bullfinchserver.specific

import java.util.logging.Logger
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.closeSocketAndStreams
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.readNext
import vasiliev.aleksey.bullfinchserver.specific.RegistrationLogic.signUserUp
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.sendSomethingToUser
import vasiliev.aleksey.bullfinchserver.specific.ChangingLogic.changeUserName
import vasiliev.aleksey.bullfinchserver.specific.FriendsFindingLogic.makeFriend
import vasiliev.aleksey.bullfinchserver.specific.MessagingLogic.sendMessageToSomeone
import vasiliev.aleksey.bullfinchserver.specific.RequestLogic.checkForFriendsRequestAndNewMessages
import java.io.IOException
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyPairGenerator
import java.security.PrivateKey
import javax.crypto.Cipher
import kotlin.concurrent.thread
import vasiliev.aleksey.bullfinchserver.general.Constants.CIPHER_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CHANGE_USERNAME_COMMAND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.CLIENT_CONNECTED_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.EXCHANGE_KEYS_COMMAND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.MAKE_FRIENDS_COMMAND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.SEND_MESSAGE_COMMAND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.SESSION_KEY_SENT_PHRASE
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.SIGN_UP_COMMAND
import vasiliev.aleksey.bullfinchserver.general.ProtocolPhrases.UPDATE_REQUEST_COMMAND

class PortListener(portNumber: Int, private val keyGen: KeyPairGenerator) {
    private val serverSocket = ServerSocket(portNumber)
    private var privateKey: PrivateKey? = null
    var clientSocket: Socket? = null
    var writer: OutputStream? = null
    var decipher: Cipher = Cipher.getInstance(CIPHER_ALGORITM)

    companion object {
        val logger: Logger = Logger.getLogger(PortListener::class.java.name)
    }

    init {
        thread {
            try {
                while (true) {
                    clientSocket = serverSocket.accept()
                    if (clientSocket != null) {
                        logger.info(CLIENT_CONNECTED_PHRASE)
                        writer = clientSocket!!.getOutputStream()
                        understandWhatTheyWantFromYou()
                    }
                }
            } catch (e: IOException) {
            }
        }
    }

    fun closeServerSocket() {
        writer?.close()
        clientSocket?.close()
        serverSocket.close()
    }

    private fun understandWhatTheyWantFromYou() {
        when (readNext(clientSocket!!).makeString()) {
            EXCHANGE_KEYS_COMMAND -> sendKeyToUser()
            SIGN_UP_COMMAND -> signUserUp(clientSocket!!, decipher, writer!!, privateKey!!)
            CHANGE_USERNAME_COMMAND -> changeUserName(clientSocket!!, decipher, writer!!, privateKey!!)
            MAKE_FRIENDS_COMMAND -> makeFriend(clientSocket!!, decipher, writer!!, privateKey!!)
            UPDATE_REQUEST_COMMAND -> checkForFriendsRequestAndNewMessages(clientSocket!!, decipher, writer!!, privateKey!!)
            SEND_MESSAGE_COMMAND -> sendMessageToSomeone(clientSocket!!, decipher, writer!!, privateKey!!)
            else -> closeSocketAndStreams(clientSocket, writer)
        }
    }

    private fun sendKeyToUser() {
        val keys = keyGen.genKeyPair()
        privateKey = keys.private
        sendSomethingToUser(keys.public.encoded, writer!!)
        logger.info(SESSION_KEY_SENT_PHRASE)
        understandWhatTheyWantFromYou()
    }
}