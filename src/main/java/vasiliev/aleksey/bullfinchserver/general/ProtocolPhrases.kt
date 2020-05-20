package vasiliev.aleksey.bullfinchserver.general

import vasiliev.aleksey.bullfinchserver.general.GlobalLogic.makeByteArray

object ProtocolPhrases {
    const val GREETING_PHRASE = "At your service."
    const val CLOSE_ALL_PHRASE = "All sockets were closed."
    const val CLOSE_CLIENT_SOCKET_PHRASE = "Client socket was closed."
    const val USERNAME_CHANGING_PHRASE = "Someone wants to change a username."
    const val USERNAME_CHANGED_SUCCESSFULLY_PHRASE = "Someone changed his username."
    const val FRIENDS_MAKING_PHRASE = "Someone wants to make friends."
    const val INCORRECT_LOGIN_PHRASE = "Oops. Incorret login."
    const val UNKNOWN_USER_PHRASE = "Oops. Unknown user."
    const val FRIEND_REQUEST_SENT_PHRASE = "I sent friend request."
    const val NEW_MESSAGE_PHRASE = "Someone wants to send a message."
    const val CLIENT_CONNECTED_PHRASE = "Client connected."
    const val SESSION_KEY_SENT_PHRASE = "I sent session key to user."
    const val SIGN_UP_PHRASE = "Someone wants to sign up."
    const val WARNING_PHRASE = "Something went wrong."
    const val NEW_USER_REGISTERED_PHRASE = "I registered a new user."
    const val CHECKING_FOR_REQUESTS_PHRASE = "Someone wants to check for friend requests."
    const val NO_REQUESTS_PHRASE = "No requests yet."
    const val NO_MESSAGES_PHRASE = "No messages yet."
    const val SERVED_SUCCESSFULLY_PHRASE = "Served successfully."

    val WELLKNOWN_USER_RESPONSE = "I know this user.".makeByteArray()
    val CORRECT_LOGIN_RESPONSE = "Login is correct.".makeByteArray()
    val CORRECT_PASSWORD_RESPONSE = "Password is correct.".makeByteArray()
    val UNKNOWN_USER_RESPONSE = "Oops. Unknown user.".makeByteArray()
    val ACCEPTED_RESPONSE = "Accepted.".makeByteArray()
    val NOT_UNIQUE_USER_RESPONSE = "Oops. This user is already in db.".makeByteArray()
    val SUCCESS_RESPOND = "Success!".makeByteArray()

    const val AMOUNT_RECEIVED_COMMAND = "Amount received."
    const val CLOSE_ALL_COMMAND = "Close all sockets."
    const val STOP_COMMAND = "Stop it."
    const val EXCHANGE_KEYS_COMMAND = "I want to exchange keys."
    const val SIGN_UP_COMMAND = "I want to sign up."
    const val CHANGE_USERNAME_COMMAND = "I want to change a username."
    const val UPDATE_REQUEST_COMMAND = "I want to check for friends requests and new messages."
    const val SEND_MESSAGE_COMMAND = "I want to send a message."
    const val MAKE_FRIENDS_COMMAND = "I want to make friends."
}