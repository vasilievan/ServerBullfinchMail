package vasiliev.aleksey.bullfinchserver.general

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object Constants {
    const val KEY_FACTORY_ALGORITM = "PBKDF2WithHmacSHA1"
    const val MAIN_DIR = "/root/BullfinchMail"
    val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8
    const val PING = 5000L
    const val KEY_LENGTH = 1024
    const val SALT_SIZE = 16
}