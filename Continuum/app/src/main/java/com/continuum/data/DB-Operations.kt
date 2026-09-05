package com.continuum.data
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.security.SecureRandom
import kotlin.Int
import kotlin.time.Clock
import kotlin.time.Instant


class Database {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://bbwivjungybfetfpnoyu.supabase.co",
        supabaseKey = "sb_publishable_ySkpmX-JfWWJ8mNzYR035A_avt2S9N_" // Public key. Safe to hard code I'm pretty sure. Make sure RLS is on in all tables though
    ) {
        install(Postgrest)
        install(Auth)
    }

    val statOptions = mapOf(
        0.toShort() to "New",
        1.toShort() to "Acknowledged",
        2.toShort() to "In Progress",
        3.toShort() to "Under Review",
        4.toShort() to "Complete"
    )
    val prioOptions = mapOf(
        0.toShort() to "Urgent",
        1.toShort() to "High",
        2.toShort() to "Medium",
        3.toShort() to "Neutral",
        4.toShort() to "Low"
    )

    var uid: String? = null
    var activeTeam: Int? = null

    @Serializable @Parcelize
    data class Team (
        @SerialName("team_name")
        var teamName: String? = null,
        @SerialName("team_id")
        var teamID: Int? = null,
        @SerialName("team_code")
        var teamCode: String? = null,
    ) : Parcelable

    @Serializable
    data class TeamUser(
        @SerialName("team_id")
        val teamID: Int,
        @SerialName("user_id")
        val userID: String?,
        @SerialName("role_id")
        val roleID: Long
    )

    @Serializable
    data class User(
        @SerialName("user_id")
        val userID: String,
        @SerialName("f_name")
        val firstName: String,
        @SerialName("l_name")
        val lastName: String?,
        //@SerialName("pfp_url") // Need to figure out specifics with the storage API first
        //val pfp: String
    )
    @Serializable @Parcelize
    data class Handoff(
        @SerialName("team_id")
        val teamID: Int?,
        @SerialName("user_id")
        val userID: String?,
        val title: String,
        val content: String?,
        val status: Short,
        val priority: Short,
        @SerialName("time_created")
        val timestamp: Instant?,
        @SerialName("time_edited")
        val editTimestamp: Instant? = null,
        @SerialName("handoff_id")
        val handoffID: Int? = null
    ) : Parcelable
    @Serializable
    data class Acknowledgement(
        @SerialName("handoff_id")
        val handoffID: Int,
        @SerialName("user_id")
        val userID: String
    )

    @Serializable
    data class NoteInsert(
        @SerialName("team_id")
        val teamID: Int,
        @SerialName("user_id")
        val userID: String,
        val content: String
    )
    @Serializable
    data class Note(
        @SerialName("note_id")
        val noteID: Long,
        @SerialName("team_id")
        val teamID: Int,
        @SerialName("user_id")
        val userID: String,
        val content: String,
        @SerialName("time_created")
        val timeCreated: Instant,
        @SerialName("time_edited")
        val timeEdited: Instant? = null
    )

    @Serializable
    data class CommentInsert(
        @SerialName("handoff_id")
        val handoffID: Int,
        @SerialName("user_id")
        val userID: String,
        val content: String,
        @SerialName("time_created")
        val timeCreated: Instant,
        @SerialName("is_action")
        val isAction: Boolean
    )

    @Serializable
    data class Comment(
        @SerialName("comment_id")
        val commentID: Long,
        @SerialName("handoff_id")
        val handoffID: Int,
        @SerialName("user_id")
        val userID: String,
        @SerialName("parent_id")
        val parentID: Long? = null,
        val content: String,
        @SerialName("time_created")
        val timeCreated: Instant,
        @SerialName("is_action")
        val isAction: Boolean
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

    suspend fun isLoggedIn(): Boolean {
        supabase.auth.awaitInitialization()

        val currentSession = supabase.auth.currentSessionOrNull()

        if (currentSession != null) {
            // User is already logged in, navigate straight to the Home Screen
            uid = currentSession.user?.id.toString()
            return true
        } else {
            // No cached user found, direct them to the Login Screen
            return false
        }
    }

    fun getFirstName(): String {
        return supabase.auth.currentSessionOrNull()
            ?.user
            ?.userMetadata
            ?.get("f_name")
            ?.toString()
            ?.trim('"')
            ?: "User"
    }

    suspend fun getUserFirstName(userID: String): String {
        return try {
            val result = supabase
                .from("users")
                .select {
                    filter {
                        eq("user_id", userID)
                    }
                }
                .decodeSingle<User>()

            result.firstName

        } catch (e: Exception) {
            e.printStackTrace()
            "User"
        }
    }

    suspend fun createTeam(teamName: String): String {

        val alphanum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()
        val maxAttempts = 8

        var attempts = 0
        var errorMsg = "There was an issue creating the team. Please try again"

        fun generateCode(): String {
            return (1..8)
                .map { alphanum[random.nextInt(alphanum.length)] }
                .joinToString("")
        }
        // create team object
        val newTeam = Team(teamName = teamName)
        // create team code
        while (attempts < maxAttempts) {
            newTeam.teamCode = generateCode()

            try {
                //  add team to table, and add user handled by SQL trigger
                supabase.from("teams").insert(newTeam)
                errorMsg = ""
                break
            } catch (e: Exception) {
                attempts++
            }
        }

        return errorMsg
    }

    suspend fun joinTeam(teamCode: String, roleID: Long = 0): String {
        var errorMsg = ""

        try {
            // get teamID from teamCode
            val teamIDResult = supabase.from("teams").select(columns = Columns.list("team_id")) {
                filter {
                    eq("team_code", teamCode)
                }
            }.decodeSingleOrNull<Team>()
            if (teamIDResult == null) {
                return "Invalid team code"
            }
            val newUser = TeamUser(
                teamIDResult.teamID ?: throw IllegalArgumentException("Invalid team code"),
                uid,
                roleID
            )
            supabase.from("team_members").insert(newUser)

        } catch (e: Exception) {
            errorMsg = "There was an issue creating the team. Please try again"
        }
        return errorMsg
    }

    suspend fun getUserTeams(): List<Team> {

        return try {

            val memberships = supabase.from("team_members").select(columns = Columns.list("team_id")) {
                    filter {
                        eq("user_id", uid.toString())
                    }
                }.decodeList<Team>()

            if (memberships.isEmpty()) {
                emptyList()
            } else {

                val teams = mutableListOf<Team>()

                memberships.forEach { membership ->
                    val team = supabase
                        .from("teams")
                        .select(columns = Columns.list("team_id", "team_name")) {
                            filter {
                                eq("team_id", membership.teamID as Int)
                            }
                        }.decodeSingle<Team>()
                    // add team to list
                    teams.add(team)
                }
                // return list as try-value
                teams
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun newHandoff(title: String, content: String?, status: Short, priority: Short): String {
        var errorMsg = ""

        try {
            val newHandoffObj = Handoff(
                activeTeam,
                uid,
                title,
                content,
                status,
                priority,
                Clock.System.now()
            )
            supabase.from("handoffs").insert(newHandoffObj)
        } catch (e: Exception) {
            errorMsg = "There was an issue creating this record. Please try again"
        }
        return errorMsg
    }
    suspend fun getNotes(): List<Note> {
        return try {
            val currentTeamID = activeTeam ?: return emptyList()
            val currentUserID = uid ?: return emptyList()

            supabase
                .from("notes")
                .select {
                    filter {
                        eq("team_id", currentTeamID)
                        eq("user_id", currentUserID)
                    }
                }
                .decodeList<Note>()

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun saveNote(content: String): String {
        var errorMsg = ""

        try {
            val currentUserID = uid ?: return "User not logged in"
            val currentTeamID = activeTeam ?: return "No team selected"

            if (content.isBlank()) {
                return "Note cannot be empty"
            }

            val newNote = NoteInsert(
                teamID = currentTeamID,
                userID = currentUserID,
                content = content
            )

            supabase
                .from("notes")
                .insert(newNote)

        } catch (e: Exception) {
            e.printStackTrace()
            errorMsg = "There was an issue saving this note. Please try again"
        }

        return errorMsg
    }
    suspend fun updateNote(noteID: Long, content: String): String { // TODO I think there is a function to combine insert and update. May be worth looking into, but not essential
        var errorMsg = ""

        try {
            if (content.isBlank()) {
                return "Note cannot be empty"
            }

            supabase
                .from("notes")
                .update(
                    {
                        set("content", content)
                        set("time_edited", Clock.System.now())
                    }
                ) {
                    filter {
                        eq("note_id", noteID)
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
            errorMsg = "There was an issue updating this note. Please try again"
        }

        return errorMsg
    }

    suspend fun addComment(
        handoffID: Int,
        content: String,
        isAction: Boolean
    ): String {
        var errorMsg = ""

        try {
            val currentUserID = uid ?: return "User not logged in"

            if (content.isBlank()) {
                return "Comment cannot be empty"
            }

            val newComment = CommentInsert(
                handoffID = handoffID,
                userID = currentUserID,
                content = content,
                timeCreated = Clock.System.now(),
                isAction = isAction
            )

            supabase
                .from("comments")
                .insert(newComment)

        } catch (e: Exception) {
            e.printStackTrace()
            errorMsg = "There was an issue saving this entry. Please try again"
        }

        return errorMsg
    }

    suspend fun getComments(handoffID: Int): List<Comment> {
        return try {
            supabase
                .from("comments")
                .select {
                    filter {
                        eq("handoff_id", handoffID)
                    }
                }
                .decodeList<Comment>()

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun getHandoffs(): List<Handoff> {
        return try {
            if (activeTeam == null) {
                emptyList()
            } else {
                supabase
                    .from("handoffs")
                    .select {
                        filter {
                            eq("team_id", activeTeam!!)
                            neq("status", 4)
                        }
                    }.decodeList<Handoff>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun getHandoffsFilter(keyword: String = "", status : Short = -1, priority: Short = -1, includeCompleted: Boolean = false): List<Handoff> {
        return try {
            if (activeTeam == null) {
                emptyList()
            } else {
                supabase
                    .from("handoffs")
                    .select {
                        filter {
                            eq("team_id", activeTeam!!)

                            if (status != (-1).toShort()) {
                                eq("status", status)
                            }
                            else if (!includeCompleted) {
                                neq("status", 4)
                            }
                            if (priority != (-1).toShort()) {
                                eq("priority", priority)
                            }

                            or {
                                if (keyword != "") {
                                    ilike("title", "%$keyword%")
                                    ilike("content", "%$keyword%")
                                }
                            }
                        }
                        // look into compareBy for order. Wait to do that until grouping by date works, though
                    }.decodeList<Handoff>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun acknowledgeHandoff(handoffID: Int): String {
        var errorMsg = ""

        try {
            val currentUserID = uid ?: return "User not logged in"

            val acknowledgement = Acknowledgement(
                handoffID = handoffID,
                userID = currentUserID
            )

            supabase
                .from("Acknowledgements")
                .insert(acknowledgement)

        } catch (e: Exception) {
            e.printStackTrace()
            errorMsg = "Unable to acknowledge handoff"
        }

        return errorMsg
    }

    suspend fun hasAcknowledgedHandoff(handoffID: Int): Boolean {
        return try {
            val currentUserID = uid ?: return false

            val acknowledgements = supabase
                .from("Acknowledgements")
                .select {
                    filter {
                        eq("handoff_id", handoffID)
                        eq("user_id", currentUserID)
                    }
                }.decodeList<Acknowledgement>()

            acknowledgements.isNotEmpty()

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
