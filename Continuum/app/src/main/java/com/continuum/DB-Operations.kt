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

    @Serializable
    data class TeamMembershipID(
        val team_id: Int
    )

    @Serializable
    // TODO This can probably be consolidated into the Team class, but I'd prefer to handle that myself just to make sure nothing else breaks. Likewise, I think TeamMembership can use GetIDResult
    data class TeamResult(
        val team_id: Int,
        val team_name: String
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
                println("SCRUM53 createTeam ERROR: ${e.message}")
                e.printStackTrace()
                attempts++
            }
        }

        return errorMsg
    }

    suspend fun JoinTeam(teamCode: String, roleID: Int = 0): String {
        var errorMsg = ""

        // TODO Probably redundant. I would remove this val once debug outputs aren't necessary
        val currentUid = uid
            ?: supabase.auth.currentSessionOrNull()?.user?.id
            ?: return "User not logged in"

        uid = currentUid

        try {
            // get teamID from teamCode
            val teamIDResult = supabase.from("teams").select(columns = Columns.list("team_id")) {
                filter {
                    eq("team_code", teamCode)
                }
            }.decodeSingle<GetIDResult>()

            val newUser = User(
                teamIDResult?.team_id ?: throw IllegalArgumentException("Invalid team code"),
                currentUid,
                roleID
            )
            println("SCRUM53 inserting membership: team=${teamIDResult.team_id}, user=$currentUid")

            supabase.from("team_members").insert(newUser)

            println("SCRUM53 membership insert completed")

        } catch (e: Exception) {
            println("SCRUM53 JoinTeam ERROR: ${e.message}")
            e.printStackTrace()
        }
        return errorMsg
    }

    suspend fun getUserTeams(): List<TeamResult> {

        // TODO Probably redundant. I would remove this val once debug outputs aren't necessary
        val currentUid = uid
            ?: supabase.auth.currentSessionOrNull()?.user?.id
            ?: return emptyList()

        uid = currentUid

        println("SCRUM53 currentUid: $currentUid")

        return try {

            val memberships = supabase.from("team_members").select(columns = Columns.list("team_id")) {
                    filter {
                        eq("user_id", currentUid)
                    }
                }.decodeList<TeamMembershipID>()

            println("SCRUM53 memberships: $memberships")

            if (memberships.isEmpty()) {
                emptyList()
            } else {

                val teams = mutableListOf<TeamResult>()

                memberships.forEach { membership ->

                    val team = supabase
                        .from("teams")
                        .select(columns = Columns.list("team_id", "team_name")) {
                            filter {
                                eq("team_id", membership.team_id)
                            }
                        }.decodeSingle<TeamResult>()

                    teams.add(team)
                }

                teams
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
