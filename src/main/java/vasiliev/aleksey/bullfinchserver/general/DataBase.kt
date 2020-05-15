package vasiliev.aleksey.bullfinchserver.general

import org.apache.commons.codec.binary.Hex
import org.json.JSONArray
import org.json.JSONObject
import vasiliev.aleksey.bullfinchserver.general.Constants.DEFAULT_CHARSET
import vasiliev.aleksey.bullfinchserver.general.Constants.MAIN_DIR
import vasiliev.aleksey.bullfinchserver.general.Constants.SALT_SIZE
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.countHash
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeByteArray
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeKeyBytesFromJSONArray
import java.nio.file.Files
import java.nio.file.Paths

class DataBase {
    fun instance() {
        Files.createDirectories(Paths.get(MAIN_DIR))
    }

    fun ifLoginIsAlreadyInDb(login: String): Boolean = Files.exists(Paths.get("${Paths.get(MAIN_DIR)}/$login"))

    fun registerUser(login: String, hashedPassword: Pair<String, ByteArray>, userName: String) {
        Files.createDirectories(Paths.get("${MAIN_DIR}/$login"))
        val pathToStorage = Paths.get("${MAIN_DIR}/$login/loginAndPassword.json")
        Files.createFile(pathToStorage)
        val textToWrite = createJSONDataString(login, hashedPassword.first, hashedPassword.second, userName)
        Files.writeString(pathToStorage, textToWrite, DEFAULT_CHARSET)
        createNewFolder(login)
    }

    private fun createNewFolder(login: String) {
        Files.createDirectories(Paths.get("${MAIN_DIR}/$login/new/friendRequests"))
        Files.createDirectories(Paths.get("${MAIN_DIR}/$login/new/messages"))
    }

    fun getHashedUserPasswordAndSalt(login: String): Pair<String, ByteArray> {
        val pathToLoginAndPassword = Paths.get("${MAIN_DIR}/$login/loginAndPassword.json")
        val contentOfFile = JSONObject(Files.readString(pathToLoginAndPassword, DEFAULT_CHARSET))
        val saltArray = contentOfFile.getJSONArray("secretSalt")
        val pureSalt = ByteArray(SALT_SIZE)
        for (index in 0 until SALT_SIZE) {
            pureSalt[index] = saltArray.get(index).toString().toByte()
        }
        return Pair(contentOfFile.getString("hashedPassword"), pureSalt)
    }

    private fun createJSONDataString(login: String, hashedPassword: String, secretSalt: ByteArray, userName: String): String {
        val contentOfFile = JSONObject()
        val saltArray = JSONArray()
        secretSalt.forEach { saltArray.put(it) }
        contentOfFile.put("login", login)
        contentOfFile.put("hashedPassword", hashedPassword)
        contentOfFile.put("secretSalt", saltArray)
        contentOfFile.put("userName", userName)
        return contentOfFile.toString()
    }

    private fun countHexForRequest(publicKeyForAFriend: ByteArray): String = Hex.encodeHexString(publicKeyForAFriend)

    fun changeUserName(login: String, userName: String) {
        val pathToLoginAndPassword = Paths.get("${MAIN_DIR}/$login/loginAndPassword.json")
        val contentOfFile = JSONObject(Files.readString(pathToLoginAndPassword, DEFAULT_CHARSET))
        contentOfFile.remove("userName")
        contentOfFile.put("userName", userName)
        Files.writeString(pathToLoginAndPassword, contentOfFile.toString(), DEFAULT_CHARSET)
    }

    fun transferPublicKeyToFriend(publicKeyForAFriend: ByteArray, login: String, friendUserName: String) {
        val hexHash = countHexForRequest(countHash(login.makeByteArray()))
        val pathToKey = Paths.get("${MAIN_DIR}/$friendUserName/new/friendRequests/${hexHash}.json")
        if (!Files.exists(pathToKey)) Files.createFile(pathToKey)
        Files.writeString(pathToKey, generatePublicKeyJsonString(publicKeyForAFriend, login) , DEFAULT_CHARSET)
    }

    private fun getUserNameFromDB(login: String): String {
        val pathToLoginAndPassword = Paths.get("${MAIN_DIR}/$login/loginAndPassword.json")
        val contentOfFile = JSONObject(Files.readString(pathToLoginAndPassword, DEFAULT_CHARSET))
        return contentOfFile.getString("userName")
    }

    fun checkIfThereAreNewRequests(login: String): Long {
        val pathToDirectory = Paths.get("${MAIN_DIR}/$login/new/friendRequests")
        return Files.list(pathToDirectory).count()
    }

    fun listOfTriples(login: String): MutableList<Triple<String, String, ByteArray>> {
        val dataList = mutableListOf<Triple<String, String, ByteArray>>()
        val scheme = "${MAIN_DIR}/$login/new/friendRequests"
        for (element in Files.list(Paths.get(scheme))) {
            val jsonObject = JSONObject(Files.readString(element, DEFAULT_CHARSET))
            val from = jsonObject.getString("from")
            val userName = jsonObject.getString("userName")
            val publicKey = makeKeyBytesFromJSONArray(jsonObject.getJSONArray("publicKey"))
            dataList.add(Triple(from, userName, publicKey))
            Files.delete(element)
        }
        return dataList
    }

    fun listOfMessagesTriples(login: String): MutableList<Triple<String, ByteArray, ByteArray>> {
        val dataList = mutableListOf<Triple<String, ByteArray, ByteArray>>()
        val scheme = "${MAIN_DIR}/$login/new/messages"
        for (element in Files.list(Paths.get(scheme))) {
            val jsonObject = JSONObject(Files.readString(element, DEFAULT_CHARSET))
            val from = jsonObject.getString("from")
            val dateArray = jsonObject.getJSONArray("date")
            val dateByteArray = ByteArray(256)
            for (byte in 0 until dateArray.length()) {
                dateByteArray[byte] = dateArray[byte].toString().toByte()
            }
            val messageArray = jsonObject.getJSONArray("message")
            val messageByteArray = ByteArray(16384)
            for (byte in 0 until messageArray.length()) {
                messageByteArray[byte] = messageArray[byte].toString().toByte()
            }
            dataList.add(Triple(from, dateByteArray, messageByteArray))
            Files.delete(element)
        }
        return dataList
    }

    private fun generatePublicKeyJsonString(publicKeyForAFriend: ByteArray, login: String): String {
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        publicKeyForAFriend.forEach { jsonArray.put(it) }
        jsonObject.put("from", login)
        jsonObject.put("userName", getUserNameFromDB(login))
        jsonObject.put("publicKey", jsonArray)
        return jsonObject.toString()
    }

    fun saveMessageFromUser(from: String, toWhom: String, date: ByteArray, content: ByteArray) {
        val toWrite = createJSONMessage(from, date, content)
        val pathToMessage = Paths.get("${MAIN_DIR}/$toWhom/new/messages/${System.currentTimeMillis()}.json")
        Files.createFile(pathToMessage)
        Files.writeString(pathToMessage, toWrite)
    }

    fun checkIfThereAreNewMessages(login: String): Long {
        val pathToDirectory = Paths.get("${MAIN_DIR}/$login/new/messages")
        return Files.list(pathToDirectory).count()
    }

    private fun createJSONMessage(from: String, date: ByteArray, content: ByteArray): String {
        val jsonObject = JSONObject()
        jsonObject.put("from", from)
        val jsonArrayDate = JSONArray()
        date.forEach { jsonArrayDate.put(it) }
        jsonObject.put("date", jsonArrayDate)
        val jsonArrayContent = JSONArray()
        content.forEach { jsonArrayContent.put(it) }
        jsonObject.put("message", jsonArrayContent)
        return jsonObject.toString()
    }
}