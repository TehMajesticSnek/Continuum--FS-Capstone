package com.continuum
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.websocket.WebSocketDeflateExtension.Companion.install
import kotlinx.serialization.Serializable

@Serializable
data class UserObj(
    val userID: String,
    val fName: String,
    val lName: String,
)
val supabase = createSupabaseClient(
    supabaseUrl = "https://nkpxjjlvzbfzcpcxbraf.supabase.co",
    supabaseKey = "sb_publishable_RoUXBvS61E45Lzkiw-7JmA_bKjYHX4F" // Public key. Safe to hard code I'm pretty sure. Make sure RLS is on in all tables though
) {
    install(Postgrest)
    install(Auth)
}

class Database {
    lateinit var uid: String

    suspend fun registerUser(inputEmail: String, inputPassword: String, passwordConfirm: String, fName: String, lName: String) : String {
        // check all are not null
        if (inputPassword != passwordConfirm)
        {
            return "Passwords do not match"
        }
        // check if email is already registered

        // register user
        var errorMsg = ""
        uid = try {
            supabase.auth.signUpWith(Email) {
                email = inputEmail
                password = inputPassword
            }?.id
        } catch (e: AuthRestException) {
            errorMsg = when (e.error) {
                "weak_password" -> {
                    "Password must be at least 6 characters"
                }
                "validation_failed" -> {
                    "One or more fields were invalid. Check text formats"
                }
                "email_address_invalid" -> {
                    "Invalid email address"
                }
                "user_already_exists" -> {
                    "Email is already in use"
                }
                else -> {
                    "Unknown error. Please try again later"
                }
            }
            null
        }.toString()

        if (errorMsg.isEmpty()) {
            // add to users table
            val user = UserObj(userID = uid as String, fName = fName, lName = lName)
            supabase.from("Users").insert(user)
        }
        return errorMsg

    }

    suspend fun login(inputEmail: String, inputPassword: String) : String {

        // validate credentials
        var errorMsg = ""
        try {
            supabase.auth.signInWith(Email) {
                email = inputEmail
                password = inputPassword
            }
        } catch (e: AuthRestException) {
            errorMsg = when (e.error) {
                "invalid_credentials" -> {
                    "Incorrect email or password"
                }
                "validation_failed" -> {
                    "One or more fields were invalid. Check text formats"
                }
                "user_banned" -> {
                    "This user was banned"
                }
                else -> {
                    "Unknown error. Please try again later"
                }
            }
            null
        }.toString()

        if (errorMsg.isEmpty()) {
            // get userID
            uid = supabase.auth.currentSessionOrNull()?.user?.id.toString()
        }
        return errorMsg
    }


//    suspend fun testDB() : String {
//        val result = supabase.from("test").select(columns = Columns.list("Name")).decodeSingle<UserRow>()
//        Log.d(TAG, result.Name)
//        return result.Name
//    }
}
