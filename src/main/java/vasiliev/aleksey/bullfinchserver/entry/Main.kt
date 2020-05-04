package vasiliev.aleksey.bullfinchserver.entry

import vasiliev.aleksey.bullfinchserver.specific.PortListener
import vasiliev.aleksey.bullfinchserver.general.DataBase
import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.secureRandom
import vasiliev.aleksey.bullfinchserver.general.Server.createKeyPairGenerator
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    val keyGen = createKeyPairGenerator(secureRandom)
    val threadsPull = Executors.newFixedThreadPool(9)
    println("At your service.")
    for (i in 4051..4059) {
        threadsPull.submit {
            PortListener(i, keyGen)
        }
    }
    val db = DataBase()
    db.instance()
}