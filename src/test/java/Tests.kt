import org.junit.jupiter.api.Test
import org.json.JSONArray
import org.junit.jupiter.api.Assertions.*
import vasiliev.aleksey.bullfinchserver.general.DataBase
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.countHash
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.generateHashedPassword
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeByteArray
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeKeyBytesFromJSONArray
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeString

class Tests {

    private val string = "Some string."

    @Test
    fun transformationStringToByteArrayAndBack() {
        val byteArray = string.makeByteArray()
        assertEquals(string, byteArray.makeString())
        assertNotEquals(string.toByteArray(Charsets.UTF_16), byteArray)
    }

    @Test
    fun checkHashFunction() {
        val list = byteArrayOf(-121, 45, -4, 107, -74, -81, 47, 90, -108, -96, -80, -37, -7, -5, 127, -74, 0,
                -24, -99, 73, -40, -14, 89, 17, -24, 92, -32, 62, -1, 106, -5, -116)
        val byteArray = ByteArray(list.size)
        for (element in list.indices) {
            byteArray[element] = list[element]
        }
        assertTrue(byteArray.contentEquals(countHash(string.makeByteArray())))
        byteArray[0] = -100
        assertFalse(byteArray.contentEquals(countHash(string.makeByteArray())))
    }

    @Test
    fun checkKeyFromJSONArrayToByteArray() {
        val jsonArray = JSONArray()
        jsonArray.put(12)
        jsonArray.put(13)
        val byteArray = ByteArray(2)
        byteArray[0] = 12
        byteArray[1] = 13
        assertTrue(byteArray.contentEquals(makeKeyBytesFromJSONArray(jsonArray)))
        byteArray[0] = 15
        assertFalse(byteArray.contentEquals(makeKeyBytesFromJSONArray(jsonArray)))
    }

    @Test
    fun passwordHashesChecking() {
        val pairPasswordToSalt= generateHashedPassword(string, byteArrayOf(1, 12, -100))
        val hashedPassword = byteArrayOf(93, 24, 53, 51, -56, 67, 35, 82, 107, 92, 66, 28, 22, -63, -5, 112,
                -40, 77, -19, -69, -115, 76, -29, 118, -79, 65, -73, 88, 10, 125, 25, -84)
        assertTrue(hashedPassword.makeString() == pairPasswordToSalt.first)
    }

    @Test
    fun dbChecking() {
        val db = DataBase()
        assertFalse(db.ifLoginIsAlreadyInDb("vasya"))
        val message = "{\"date\":[50,51,46,48,53,46,50,48,50,48,32,57,58,53,48],\"from\":\"vasya\",\"message\":[72,101,108,108,111,44,32,77,105,115,104,97,33]}"
        assertEquals(message, db.createJSONMessage("vasya", "23.05.2020 9:50".makeByteArray(), "Hello, Misha!".makeByteArray()))
        assertEquals(0L, db.checkIfThereAreNewMessages("vasya"))
        assertEquals(0L, db.checkIfThereAreNewRequests("vasya"))
    }
}