package vasiliev.aleksey.bullfinchserver.specific

import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.closeSocketAndStreams
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.readNext
import vasiliev.aleksey.bullfinchserver.specific.RegistrationLogic.signUserUp
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.sendSomethingToUser
import vasiliev.aleksey.bullfinchserver.specific.ChangingLogic.changeUserName
import vasiliev.aleksey.bullfinchserver.specific.FriendsFindingLogic.makeFriend
import java.io.IOException
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyPairGenerator
import java.security.PrivateKey
import javax.crypto.Cipher

class PortListener(portNumber: Int, private val keyGen: KeyPairGenerator) {
    private val serverSocket = ServerSocket(portNumber)
    private var privateKey: PrivateKey? = null
    var clientSocket: Socket? = null
    var writer: OutputStream? = null
    var decipher: Cipher = Cipher.getInstance("RSA")
    private val data = ByteArray(8198)

    init {
        try {
            clientSocket = serverSocket.accept()
            println("Connected.")
            writer = clientSocket!!.getOutputStream()
            understandWhatTheyWantFromYou()
        } catch (e: IOException) {
        }
    }

    private fun understandWhatTheyWantFromYou() {
        when (readNext(data, clientSocket!!).makeString()) {
            "I want to exchange keys." -> sendKeyToUser()
            "I want to sign up." -> signUserUp(data, clientSocket!!, decipher, writer!!, privateKey)
            "I want to change a username." -> changeUserName(data, clientSocket!!, decipher, writer!!, privateKey!!)
            "I want to make friends." -> makeFriend(data, clientSocket!!, decipher, writer!!, privateKey!!)
            else -> closeSocketAndStreams(clientSocket, writer)
        }
    }

    private fun sendKeyToUser() {
        val keys = keyGen.genKeyPair()
        privateKey = keys.private
        sendSomethingToUser(keys.public.encoded, writer!!)
        println("I sent session key to user.")
        understandWhatTheyWantFromYou()
    }
}