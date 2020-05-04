package vasiliev.aleksey.bullfinchserver.general

import vasiliev.aleksey.bullfinchserver.general.Constants.KEY_LENGTH
import java.security.KeyPairGenerator
import java.security.SecureRandom

object Server {
    fun createKeyPairGenerator(secureRandom: SecureRandom): KeyPairGenerator {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(KEY_LENGTH, secureRandom)
        return keyPairGenerator
    }
}