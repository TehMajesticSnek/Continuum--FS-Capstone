package com.continuum
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.security.SecureRandom

val supabase = createSupabaseClient(
    supabaseUrl = "https://bbwivjungybfetfpnoyu.supabase.co",
    supabaseKey = "sb_publishable_ySkpmX-JfWWJ8mNzYR035A_avt2S9N_" // Public key. Safe to hard code I'm pretty sure. Make sure RLS is on in all tables though
) {
    install(Postgrest)
    install(Auth)
}

class Database {
    var uid: String? = null
    var activeTeam: Int? = null

    @Serializable
    class Team (
        val team_name: String,
    ) {
        lateinit var team_code: String
    }
    @Serializable
    class User(
        val team_id: Int,
        val user_id: String?,
        val role_id: Int
    )
    @Serializable
    data class GetIDResult(
        val team_id: Int
    )

    suspend fun registerUser(inputEmail: String, inputPassword: String, passwordConfirm: String, fName: String, lName: String) : String {
        // check all are not null
        if (inputPassword != passwordConfirm)
        {
            return "Passwords do not match"
        }

        // register user
        var errorMsg = ""
        uid = try {
            supabase.auth.signUpWith(Email) {
                email = inputEmail
                password = inputPassword
                data = buildJsonObject {
                    put("f_name", JsonPrimitive(fName))
                    put("l_name", JsonPrimitive(lName))
                }
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
                "over_email_send_rate_limit" -> {
                    "Too many attempts. Please try again later"
                }
                else -> {
                    "Unknown error. Please try again later"
                }
            }
            null
        } catch (e: Exception) {
            errorMsg = "Connection failed. Please check your internet connection."
            null
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
                "over_email_send_rate_limit" -> {
                    "Too many attempts. Please try again later"
                }
                else -> {
                    "Unknown error. Please try again later"
                }
            }
        } catch (e: Exception) {
            errorMsg = "Connection failed. Please check your internet connection."
        }

        if (errorMsg.isEmpty()) {
            // get userID
            uid = supabase.auth.currentSessionOrNull()?.user?.id.toString()
        }
        return errorMsg
    }

    suspend fun createTeam(teamName: String): String {

        val alphanum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()
        val maxAttempts = 8

        var attempts = 0
        var errorMsg = ""

        fun generateCode(): String {
            return (1..8)
                .map { alphanum[random.nextInt(alphanum.length)] }
                .joinToString("")
        }
        // create team object
        val newTeam = Team(teamName)
        // generate code
        while (attempts < maxAttempts) {
            newTeam.team_code = generateCode()

            try {
                //  add team to table
                supabase.from("teams").insert(newTeam)
                //  add user to team members table
                JoinTeam(newTeam.team_code, 1)
                break
            } catch (e: Exception) {
                // if duplicate key
                attempts++
                // TODO add error handling
                // error block
            }
        }

        return errorMsg
    }

    suspend fun JoinTeam(teamCode: String, roleID: Int = 0): String {
        var errorMsg = ""

        try {
            // get teamID from teamCode
            val teamIDResult = supabase.from("teams").select(columns = Columns.list("team_id")) {
                filter {
                    eq("team_code", teamCode)
                }
            }.decodeSingle<GetIDResult>()
            println("Egg")

            val newUser = User(
                teamIDResult?.team_id ?: throw IllegalArgumentException("Invalid team code"),
                uid,
                roleID
            )
            supabase.from("team_members").insert(newUser)

        } catch (e: Exception) {
            // error block
            // TODO add error handling
            val test = 1
        }
        return errorMsg
    }
}
