package vasiliev.aleksey.bullfinchserver.general

import org.apache.commons.codec.binary.Hex
import org.json.JSONArray
import org.json.JSONObject
import vasiliev.aleksey.bullfinchserver.general.Constants.DATE
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.countHash
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeByteArray
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeKeyBytesFromJSONArray
import java.nio.file.Files
import java.nio.file.Paths
import vasiliev.aleksey.bullfinchserver.general.Constants.DEFAULT_CHARSET
import vasiliev.aleksey.bullfinchserver.general.Constants.FRIEND_REQUESTS
import vasiliev.aleksey.bullfinchserver.general.Constants.FROM
import vasiliev.aleksey.bullfinchserver.general.Constants.HASHED_PASSWORD
import vasiliev.aleksey.bullfinchserver.general.Constants.JSON_FORMAT
import vasiliev.aleksey.bullfinchserver.general.Constants.LOGIN
import vasiliev.aleksey.bullfinchserver.general.Constants.LOGIN_AND_PASSWORD
import vasiliev.aleksey.bullfinchserver.general.Constants.MAIN_DIR
import vasiliev.aleksey.bullfinchserver.general.Constants.MESSAGE
import vasiliev.aleksey.bullfinchserver.general.Constants.MESSAGES
import vasiliev.aleksey.bullfinchserver.general.Constants.NEW
import vasiliev.aleksey.bullfinchserver.general.Constants.PUBLIC_KEY
import vasiliev.aleksey.bullfinchserver.general.Constants.SALT_SIZE
import vasiliev.aleksey.bullfinchserver.general.Constants.SECRET_SALT
import vasiliev.aleksey.bullfinchserver.general.Constants.USER_NAME

class DataBase {
    fun instance() = Files.createDirectories(Paths.get(MAIN_DIR))

    fun ifLoginIsAlreadyInDb(login: String): Boolean = Files.exists(Paths.get("${Paths.get(MAIN_DIR)}/$login"))

    fun registerUser(login: String, hashedPassword: Pair<String, ByteArray>, userName: String) {
        Files.createDirectories(Paths.get("$MAIN_DIR/$login"))
        val pathToStorage = Paths.get("$MAIN_DIR/$login/$LOGIN_AND_PASSWORD$JSON_FORMAT")
        Files.createFile(pathToStorage)
        val textToWrite = createJSONDataString(login, hashedPassword.first, hashedPassword.second, userName)
        Files.writeString(pathToStorage, textToWrite, DEFAULT_CHARSET)
        createNewFolder(login)
    }

    private fun createNewFolder(login: String) {
        Files.createDirectories(Paths.get("$MAIN_DIR/$login/$NEW/$FRIEND_REQUESTS"))
        Files.createDirectories(Paths.get("$MAIN_DIR/$login/$NEW/$MESSAGES"))
    }

    fun getHashedUserPasswordAndSalt(login: String): Pair<String, ByteArray> {
        val pathToLoginAndPassword = Paths.get("$MAIN_DIR/$login/$LOGIN_AND_PASSWORD$JSON_FORMAT")
        val contentOfFile = JSONObject(Files.readString(pathToLoginAndPassword, DEFAULT_CHARSET))
        val saltArray = contentOfFile.getJSONArray(SECRET_SALT)
        val pureSalt = ByteArray(SALT_SIZE)
        for (index in 0 until SALT_SIZE) {
            pureSalt[index] = saltArray.get(index).toString().toByte()
        }
        return Pair(contentOfFile.getString(HASHED_PASSWORD), pureSalt)
    }

    private fun createJSONDataString(login: String, hashedPassword: String, secretSalt: ByteArray, userName: String): String {
        val contentOfFile = JSONObject()
        val saltArray = JSONArray()
        secretSalt.forEach { saltArray.put(it) }
        contentOfFile.put(LOGIN, login)
        contentOfFile.put(HASHED_PASSWORD, hashedPassword)
        contentOfFile.put(SECRET_SALT, saltArray)
        contentOfFile.put(USER_NAME, userName)
        return contentOfFile.toString()
    }

    private fun countHexForRequest(publicKeyForAFriend: ByteArray): String = Hex.encodeHexString(publicKeyForAFriend)

    fun changeUserName(login: String, userName: String) {
        val pathToLoginAndPassword = Paths.get("$MAIN_DIR/$login/$LOGIN_AND_PASSWORD$JSON_FORMAT")
        val contentOfFile = JSONObject(Files.readString(pathToLoginAndPassword, DEFAULT_CHARSET))
        contentOfFile.remove(USER_NAME)
        contentOfFile.put(USER_NAME, userName)
        Files.writeString(pathToLoginAndPassword, contentOfFile.toString(), DEFAULT_CHARSET)
    }

    fun transferPublicKeyToFriend(publicKeyForAFriend: ByteArray, login: String, friendUserName: String) {
        val hexHash = countHexForRequest(countHash(login.makeByteArray()))
        val pathToKey = Paths.get("$MAIN_DIR/$friendUserName/$NEW/$FRIEND_REQUESTS/$hexHash$JSON_FORMAT")
        if (!Files.exists(pathToKey)) Files.createFile(pathToKey)
        Files.writeString(pathToKey, generatePublicKeyJsonString(publicKeyForAFriend, login) , DEFAULT_CHARSET)
    }

    private fun getUserNameFromDB(login: String): String {
        val pathToLoginAndPassword = Paths.get("$MAIN_DIR/$login/$LOGIN_AND_PASSWORD$JSON_FORMAT")
        val contentOfFile = JSONObject(Files.readString(pathToLoginAndPassword, DEFAULT_CHARSET))
        return contentOfFile.getString(USER_NAME)
    }

    fun listOfTriples(login: String): MutableList<Triple<String, String, ByteArray>> {
        val dataList = mutableListOf<Triple<String, String, ByteArray>>()
        val scheme = "$MAIN_DIR/$login/$NEW/$FRIEND_REQUESTS"
        for (element in Files.list(Paths.get(scheme))) {
            val jsonObject = JSONObject(Files.readString(element, DEFAULT_CHARSET))
            val from = jsonObject.getString(FROM)
            val userName = jsonObject.getString(USER_NAME)
            val publicKey = makeKeyBytesFromJSONArray(jsonObject.getJSONArray(PUBLIC_KEY))
            dataList.add(Triple(from, userName, publicKey))
            Files.delete(element)
        }
        return dataList
    }

    fun listOfMessagesTriples(login: String): MutableList<Triple<String, ByteArray, ByteArray>> {
        val dataList = mutableListOf<Triple<String, ByteArray, ByteArray>>()
        val scheme = "$MAIN_DIR/$login/$NEW/$MESSAGES"
        for (element in Files.list(Paths.get(scheme))) {
            val jsonObject = JSONObject(Files.readString(element, DEFAULT_CHARSET))
            val from = jsonObject.getString(FROM)
            val dateArray = jsonObject.getJSONArray(DATE)
            val dateByteArray = ByteArray(dateArray.length())
            for (byte in 0 until dateArray.length()) {
                dateByteArray[byte] = dateArray[byte].toString().toByte()
            }
            val messageArray = jsonObject.getJSONArray(MESSAGE)
            val messageByteArray = ByteArray(messageArray.length())
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
        jsonObject.put(FROM, login)
        jsonObject.put(USER_NAME, getUserNameFromDB(login))
        jsonObject.put(PUBLIC_KEY, jsonArray)
        return jsonObject.toString()
    }

    fun saveMessageFromUser(from: String, toWhom: String, date: ByteArray, content: ByteArray) {
        val toWrite = createJSONMessage(from, date, content)
        val pathToMessage = Paths.get("$MAIN_DIR/$toWhom/$NEW/$MESSAGES/${System.currentTimeMillis()}$JSON_FORMAT")
        Files.createFile(pathToMessage)
        Files.writeString(pathToMessage, toWrite)
    }

    fun checkIfThereAreNewMessages(login: String): Long {
        val pathToDirectory = Paths.get("$MAIN_DIR/$login/$NEW/$MESSAGES")
        return Files.list(pathToDirectory).count()
    }

    fun checkIfThereAreNewRequests(login: String): Long {
        val pathToDirectory = Paths.get("$MAIN_DIR/$login/$NEW/$FRIEND_REQUESTS")
        return Files.list(pathToDirectory).count()
    }

    private fun createJSONMessage(from: String, date: ByteArray, content: ByteArray): String {
        val jsonObject = JSONObject()
        jsonObject.put(FROM, from)
        val jsonArrayDate = JSONArray()
        date.forEach { jsonArrayDate.put(it) }
        jsonObject.put(DATE, jsonArrayDate)
        val jsonArrayContent = JSONArray()
        content.forEach { jsonArrayContent.put(it) }
        jsonObject.put(MESSAGE, jsonArrayContent)
        return jsonObject.toString()
    }
}