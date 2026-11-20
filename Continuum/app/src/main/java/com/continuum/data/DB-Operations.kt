package com.continuum.data
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.continuum.MainActivity
import com.continuum.data.PrioOptions.HIGH
import com.continuum.data.PrioOptions.LOW
import com.continuum.data.PrioOptions.MEDIUM
import com.continuum.data.PrioOptions.NEUTRAL
import com.continuum.data.PrioOptions.URGENT
import com.continuum.data.StatOptions.ACKNOWLEDGED
import com.continuum.data.StatOptions.COMPLETE
import com.continuum.data.StatOptions.IN_PROGRESS
import com.continuum.data.StatOptions.NEW
import com.continuum.data.StatOptions.UNDER_REVIEW
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.CardMutedColor
import com.continuum.ui.theme.Critical
import com.continuum.ui.theme.CyanAccent
import com.continuum.ui.theme.Success
import com.continuum.ui.theme.Warning
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.security.SecureRandom
import java.time.LocalDate
import java.time.ZoneId
import kotlin.Int
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.serialization.json.put

enum class StatOptions(val text: String) {
    NEW("New"),
    ACKNOWLEDGED("Acknowledged"),
    IN_PROGRESS("In Progress"),
    UNDER_REVIEW("Under Review"),
    COMPLETE("Complete");

    override fun toString(): String = text

    fun statColor(): Color {

        val color = when (this) {
            NEW -> Critical
            ACKNOWLEDGED -> Warning
            IN_PROGRESS -> CyanAccent
            UNDER_REVIEW -> BluePrimary
            COMPLETE -> Success
        }

        return color
    }

    companion object {
        fun fromValue(value: Short): StatOptions {
            val index = value.toInt()

            return entries.getOrNull(index)!!
        }
    }
}
enum class PrioOptions(val text: String) {
    URGENT("Urgent"),
    HIGH("High"),
    MEDIUM("Medium"),
    NEUTRAL("Neutral"),
    LOW("Low");

    //override fun toString(): String = text

    fun prioColor(): Color {
        val color = when (this) {
            URGENT -> Critical
            HIGH -> Warning
            MEDIUM -> Success
            NEUTRAL -> CardMutedColor
            LOW -> CyanAccent
        }

        return color
    }

//    0 -> Color(0xFFF87171) // Urgent
//    1 -> Color(0xFFFBBF24) // High
//    2 -> Color(0xFF60A5FA) // Medium
//    3 -> Color(0xFFCBD5E1) // Neutral
//    else -> Color(0xFF94A3B8) // Low

    companion object {
        fun fromValue(value: Short): PrioOptions {
            val index = value.toInt()

            return PrioOptions.entries.getOrNull(index)!!
        }
    }
}

class Database (val session: MainActivity.UserSession) {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://bbwivjungybfetfpnoyu.supabase.co",
        supabaseKey = "sb_publishable_ySkpmX-JfWWJ8mNzYR035A_avt2S9N_" // Public key. Safe to hard code I'm pretty sure. Make sure RLS is on in all tables though
    ) {
        install(Postgrest)
        install(Auth)
        install(Functions)
        install(Storage)
    }

    //Handoff Delimiters
    val START_ISSUE = "<ISSUE_DETAILS_DELIM>"
    val END_ISSUE = "</ISSUE_DETAILS_DELIM>"
    val START_ACTION = "<ATTEMPTED_ACTION_DELIM>"
    val END_ACTION = "</ATTEMPTED_ACTION_DELIM>"
    val START_NEXT = "<NEXT_STEP_DELIM>"
    val END_NEXT = "</NEXT_STEP_DELIM>"

    @Serializable @Parcelize
    data class Team (
        @SerialName("team_name")
        var teamName: String? = null,
        @SerialName("team_id")
        var teamID: Int? = null,
        @SerialName("team_code")
        var teamCode: String? = null,
    ) : Parcelable

    @Serializable @Parcelize
    data class TeamUser(
        @SerialName("team_id")
        val teamID: Int? = null,
        @SerialName("user_id")
        val userID: String? = null,
        @SerialName("role_id")
        val roleID: Long? = null
    ) : Parcelable

    @Serializable @Parcelize
    data class TeamUserDisplay(
        @SerialName("user_id")
        val userID: String,
        @SerialName("f_name")
        val firstName: String,
        @SerialName("l_name")
        val lastName: String,
        @SerialName("team_members")
        val teamData: List<TeamUser> = emptyList(),
        //val pfp: String
    ) : Parcelable
    @Serializable
    data class NewTeamOutput(
        var team: Team? = null,
        var errorCode: String = ""
    )

    @Serializable
    data class User(
        @SerialName("user_id")
        val userID: String,
        @SerialName("f_name")
        val firstName: String,
        @SerialName("l_name")
        val lastName: String?,
        //@SerialName("pfp_url") // TODO profile pics
        //val pfp: String
    )
    @Serializable @Parcelize
    data class Handoff(
        @SerialName("handoff_id")
        val handoffID: Long? = null,
        @SerialName("team_id")
        val teamID: Int?,
        @SerialName("user_id")
        val userID: String?,
        val title: String,
        val content: String?,
        val status: Short,
        val priority: Short,
        @SerialName("time_created")
        val timestamp: Instant? = null,
        @SerialName("time_edited")
        val editTimestamp: Instant? = null,
        @SerialName("sorted_timestamp")
        val sortedTimestamp: Instant? = null,
    ) : Parcelable
    data class ParsedContent(
        val issue: String? = null,
        val action: String? = null,
        val next: String? = null
    )

    data class RecurringIssueResult(
        val sourceHandoff: Handoff,
        val matchingHandoffs: List<Handoff>
    ) {
        val occurrenceCount: Int
            get() = matchingHandoffs.size + 1
    }

    @Serializable
    data class FileAttachment(
        @SerialName("handoff_id")
        val handoffID: Long,
        @SerialName("file_url")
        val fileURL: String,
        val transcription: String? = null,
        @SerialName("extracted_text")
        val extractedText: String? = null
    )

    data class NewHandoffResult(
        val handoff: Handoff? = null,
        val error: String = ""
    )
    @Serializable
    data class Acknowledgement(
        @SerialName("handoff_id")
        val handoffID: Long,
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
    data class GenerateHandoffRequest(
        val note: String
    )

    @Serializable
    data class GenerateHandoffResponse(
        val title: String,
        val issueDetails: String,
        val actionsTaken: String,
        val nextSteps: String
    )

    @Serializable
    data class CommentInsert(
        @SerialName("handoff_id")
        val handoffID: Long,
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
    suspend fun getFileAttachments(handoffID: Long): List<FileAttachment> {
        return try {
            supabase
                .from("file_attachments")
                .select {
                    filter {
                        eq("handoff_id", handoffID)
                    }
                }
                .decodeList<FileAttachment>()

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    data class FileUploadResult(
        val filePath: String? = null,
        val error: String = ""
    )

    suspend fun uploadFileAttachment(
        handoffID: Long,
        fileName: String,
        fileBytes: ByteArray,
        transcription: String? = null,
        extractedText: String? = null
    ): FileUploadResult {
        return try {
            val bucket = supabase.storage.from("handoff_attachments")

            val filePath = "$handoffID/${System.currentTimeMillis()}_$fileName"

            bucket.upload(filePath, fileBytes) {
                upsert = false
            }

            val attachment = FileAttachment(
                handoffID = handoffID,
                fileURL = filePath,
                transcription = transcription,
                extractedText = extractedText
            )

            supabase
                .from("file_attachments")
                .insert(attachment)

            FileUploadResult(
                filePath = filePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            FileUploadResult(
                error = "There was an issue uploading this file. Please try again."
            )
        }
    }

    suspend fun getAttachmentSignedUrl(filePath: String): String? {
        return try {
            supabase.storage
                .from("handoff_attachments")
                .createSignedUrl(
                    path = filePath,
                    expiresIn = 60.seconds
                )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun registerUser(inputEmail: String, inputPassword: String, passwordConfirm: String, fName: String, lName: String) : String {
        // check all are not null
        if (inputPassword != passwordConfirm)
        {
            return "Passwords do not match"
        }

        // register user
        var errorMsg = ""
        session.userID = try {
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
            session.userID = supabase.auth.currentSessionOrNull()?.user?.id.toString()
        }
        return errorMsg
    }

    suspend fun isLoggedIn(): Boolean {
        supabase.auth.awaitInitialization()

        val currentSession = supabase.auth.currentSessionOrNull()

        if (currentSession != null) {
            // User is already logged in, navigate straight to the Home Screen
            session.userID = currentSession.user?.id.toString()
            return true
        } else {
            // No cached user found, direct them to the Login Screen
            return false
        }
    }

    suspend fun logout() : String {
        var errorMsg = ""
        try {
            supabase.auth.signOut()
        } catch (e: AuthRestException) {
            errorMsg = "There was an error signing out. Please try again"
        }

        if (errorMsg.isEmpty()) {
            session.userID = null
            session.activeTeam = null
            session.teamRole = 0L
        }

        return errorMsg
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
    suspend fun getUserInfo(userID: String): User? {
        return try {
            val result = supabase
                .from("users")
                .select {
                    filter {
                        eq("user_id", userID)
                    }
                }
                .decodeSingle<User>()

            result

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateCode(): String {
        val alphanum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()

        return (1..8)
            .map { alphanum[random.nextInt(alphanum.length)] }
            .joinToString("")
    }
    suspend fun newCode(): String {
        val maxAttempts = 8

        var attempts = 0

        var teamCode: String
        var error = "There was an issue generating a new code. Please try again"

        while (attempts < maxAttempts) {
            teamCode = generateCode()

            try {
                supabase.from("teams").update(
                    {
                        set("team_code", teamCode)
                    }
                ) {
                    filter {
                        eq("team_id", session.activeTeam?.teamID!!)
                    }
                }
                error = ""
                return teamCode
            } catch (e: Exception) {
                attempts++
            }
        }
        return error
    }
    suspend fun createTeam(teamName: String): NewTeamOutput {
        val maxAttempts = 8

        var attempts = 0

        val output = NewTeamOutput(errorCode = "There was an issue creating the team. Please try again")

        // create team object
        val newTeam = Team(teamName = teamName)
        // create team code
        while (attempts < maxAttempts) {
            newTeam.teamCode = generateCode()

            try {
                //  add team to table, and add user handled by SQL trigger
                val result = supabase.from("teams").insert(newTeam) {
                    select()
                }.decodeSingle<Team>()
                output.team = result
                output.errorCode = ""
                break
            } catch (e: Exception) {
                attempts++
            }
        }

        return output
    }
    suspend fun joinTeam(teamCode: String, roleID: Long = 0): NewTeamOutput {
        val output = NewTeamOutput()

        try {
            // get teamID from teamCode
            val teamResult = supabase.from("teams").select(columns = Columns.list("team_id", "team_name", "team_code")) {
                filter {
                    eq("team_code", teamCode)
                }
            }.decodeSingleOrNull<Team>()
            if (teamResult == null) {
                output.errorCode = "Invalid team code"
                return output
            }

            val newUser = TeamUser(
                teamResult.teamID,
                session.userID,
                roleID
            )
            supabase.from("team_members").insert(newUser) { }

            output.team = teamResult

        } catch (e: io.github.jan.supabase.postgrest.exception.PostgrestRestException) {

            if (e.code == "23505" || e.message?.contains("duplicate key") == true) {
                output.errorCode = "You are already a member of this team"
            } else {
                output.errorCode = "An unexpected error occurred: ${e.message}"
            }
        } catch (e: Exception) {
            output.errorCode = "There was an issue creating the team. Please try again"
        }
        return output
    }

    suspend fun addUserToTeam(email: String): String { // TODO Invite system or something to prevent advertising/spam/etc
        @Serializable
        data class Parameters(
            val target_email: String,
            val target_team: Int
        )
        @Serializable
        data class userID (
            val id: String
        )

        var result = ""

        //search for user with email, get uid
        var user = ""
        try {
            val emailResult = supabase.postgrest.rpc(
                    function = "get_uid_by_email",
                    parameters = Parameters(email.lowercase(), session.activeTeam?.teamID!!)
                ).decodeSingleOrNull<userID>()

            if (emailResult == null) {
                return "User not found"
            }
            else {
                user = emailResult.id
            }

        } catch (e: Exception) {
            return "Error finding user. Please try again"
        }

        //check if user is already team member
        val userInTeam = supabase.from("team_members").select(columns = Columns.list("user_id")) {
            filter {
                eq("user_id", user)
                eq("team_id", session.activeTeam?.teamID!!)
            }
        }.decodeSingleOrNull<TeamUser>()

        if (userInTeam != null) {
            return "This user is already a member of this team"
        }

        //add user to team
        val newUser = TeamUser(
            session.activeTeam?.teamID!!,
            user,
            0
        )

        result = try {
            supabase.from("team_members").insert(newUser) {
                select()
            }.decodeSingleOrNull<NewTeamOutput>()
            ""
        } catch (e: Exception) {
            "Error adding user to team. Please try again"
        }

        return result
    }
    suspend fun leaveTeam(): String {
        if (session.teamRole == 1L) {
            var adminCount = 0
            getTeamMembers().forEach { member ->
                if (member.teamData[0].roleID == 1L) {
                    adminCount++
                }
            }
            if (adminCount < 2) {
                return "There must be at least 1 admin on a team at all times"
            }
        }
        try {
            supabase.from("team_members").delete {
                filter {
                    eq("user_id", session.userID!!)
                    eq("team_id", session.activeTeam?.teamID!!)
                }
            }
            session.activeTeam = null
            return ""
        } catch (e: Exception) {
            return "Error leaving team. Please try again"
        }
    }
    suspend fun leaveTeam(userID: String?): String {
        if (session.teamRole == 1L && userID == session.userID) {
            return "ERROR: Cannot kick yourself"
        }
        try {
            supabase.from("team_members").delete {
                filter {
                    eq("user_id", userID!!)
                    eq("team_id", session.activeTeam?.teamID!!)
                }
            }
            return ""
        } catch (e: Exception) {
            return "Error removing user. Please try again"
        }
    }
    suspend fun deleteTeam(): String {
        try { supabase.from("teams").delete {
                filter {
                    eq("team_id", session.activeTeam?.teamID!!)
                }
            }
            session.activeTeam = null
            return ""
        } catch (e: Exception) {
            return "Error deleting team. Please try again"
        }

    }
    suspend fun getUserTeams(): List<Team> {

        return try {

            val memberships = supabase.from("team_members").select(columns = Columns.list("team_id")) {
                filter {
                    eq("user_id", session.userID!!)
                }
            }.decodeList<Team>()

            if (memberships.isEmpty()) {
                emptyList()
            } else {

                val teams = mutableListOf<Team>()

                memberships.forEach { membership ->
                    val team = supabase
                        .from("teams")
                        .select(columns = Columns.list("team_id", "team_name", "team_code")) {
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
    suspend fun getActiveTeam(): Team? {
        return try {
            val team = supabase.from("teams").select(columns = Columns.list("team_name", "team_code")) {
                filter {
                    eq("team_id", session.activeTeam?.teamID!!)
                }
            }.decodeSingle<Team>()
            team
        }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun updateTeamName(teamName: String?): String {
        if (teamName == "") {
            return "Please enter a name"
        }
        return try {
            supabase.from("teams").update(
                {
                    set("team_name", teamName)
                }
            ) {
                filter {
                    eq("team_id", session.activeTeam?.teamID!!)
                }
            }
            ""
        } catch (e: Exception) {
            "Error updating name. Please try again"
        }
    }
    suspend fun getTeamRole(): Long {
        try {
            session.teamRole = supabase.from("team_members").select(columns = Columns.list("role_id")) {
                filter {
                    eq("user_id", session.userID!!)
                    eq("team_id", session.activeTeam?.teamID!!)
                }
            }.decodeSingle<TeamUser>().roleID!!
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return session.teamRole
    }
    suspend fun reassignRole(userID: String?, roleID: Long = 0): String {
        if (session.teamRole == 1L) {
            var adminCount = 0
            getTeamMembers().forEach { member ->
                if (member.teamData[0].roleID == 1L) {
                    adminCount++
                }
            }
            if (roleID != 1L && adminCount < 2) {
                return "There must be at least 1 admin on a team at all times"
            }
        }
        return try {
            supabase.from("team_members").update(
                {
                    set("role_id", roleID)
                }
            ) {
                filter {
                    eq("team_id", session.activeTeam?.teamID!!)
                    eq("user_id", userID as String)
                }
            }
            ""
        } catch (e: Exception) {
            "Error updating role. Please try again"
        }
    }
    suspend fun getTeamMembers(): List<TeamUserDisplay> {
        return try {
            val columns = Columns.raw("""
                user_id,
                f_name,
                l_name,
                team_members!inner ( user_id, role_id )
            """.trimIndent())

            val members = supabase.from("users").select(columns = columns) {
                filter {
                    eq("team_members.team_id", session.activeTeam?.teamID!!)
                }
                order("l_name", Order.ASCENDING)
            }.decodeList<TeamUserDisplay>()
            members

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun generateHandoffDraft(noteContent: String): GenerateHandoffResponse? {
        return try {
            val response = supabase.functions.invoke(
                function = "generate-handoff",
                body = GenerateHandoffRequest(noteContent)
            )

            response.body<GenerateHandoffResponse>()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun testHandoffCrypto(): String {
        return try {
            val response = supabase.functions.invoke(
                function = "handoff-crypto",
                body = buildJsonObject { }
            )

            response.body<String>()
        } catch (e: Exception) {
            e.printStackTrace()
            "Test failed: ${e.message}"
        }
    }

    suspend fun runHandoffCryptoTest() {
        val result = testHandoffCrypto()
        android.util.Log.d("HANDOFF_CRYPTO", result)
    }


    fun combineContent(issue: String, action: String, next: String): String {
        return """
            Issue Details:
            $START_ISSUE${issue.trim()}$END_ISSUE
        
            Actions Taken:
            $START_ACTION${action.trim()}$END_ACTION
        
            Next Steps:
            $START_NEXT${next.trim()}$END_NEXT
        """.trimIndent()
    }
    fun separateContent(content: String): ParsedContent {
        val issueRegex = Regex("$START_ISSUE(.*?[\\s\\S]*?)$END_ISSUE")
        val actionRegex = Regex("$START_ACTION(.*?[\\s\\S]*?)$END_ACTION")
        val nextRegex = Regex("$START_NEXT(.*?[\\s\\S]*?)$END_NEXT")

        val issue = issueRegex.find(content)?.groups?.get(1)?.value?.trim().orEmpty()
        val action = actionRegex.find(content)?.groups?.get(1)?.value?.trim().orEmpty()
        val next = nextRegex.find(content)?.groups?.get(1)?.value?.trim().orEmpty()

        return ParsedContent(issue = issue, action = action, next = next)
    }



    @Serializable
    data class EncryptedHandoffRequest(
        val action: String,
        val teamID: Int,
        val title: String,
        val content: String,
        val status: Int,
        val priority: Int
    )


    @Serializable
    data class EncryptedHandoffResponse(
        val handoff: Handoff,
        val encrypted: Boolean
    )


    suspend fun newHandoff(
        title: String,
        content: String?,
        status: Short,
        priority: Short
    ): NewHandoffResult {
        return try {
            val teamID = session.activeTeam?.teamID
                ?: return NewHandoffResult(error = "No team selected")

            val request = EncryptedHandoffRequest(
                action = "create",
                teamID = teamID,
                title = title,
                content = content.orEmpty(),
                status = status.toInt(),
                priority = priority.toInt()
            )

            val response = supabase.functions.invoke(
                function = "handoff-crypto",
                body = request
            )

            val result = response.body<EncryptedHandoffResponse>()

            NewHandoffResult(handoff = result.handoff)

        } catch (e: Exception) {
            e.printStackTrace()

            NewHandoffResult(
                error = "Unable to create encrypted handoff. Please try again."
            )
        }
    }


    suspend fun updateHandoffStatus(
        handoffID: Long,
        status: Short
    ): String {
        var errorMsg = ""

        try {
            supabase
                .from("handoffs")
                .update(
                    {
                        set("status", status.toInt())
                        set("time_edited", Clock.System.now())
                    }
                ) {
                    filter {
                        eq("handoff_id", handoffID)
                    }
                    println("STATUS UPDATE: handoffID=$handoffID status=$status")
                }

        } catch (e: Exception) {
            e.printStackTrace()

            android.util.Log.e(
                "HANDOFF_CRYPTO_ERROR",
                "Encrypted handoff failed: ${e.message}",
                e
            )

            println("STATUS UPDATE ERROR: ${e.message}")
            errorMsg = "Unable to update handoff status. Please try again."
        }

        return errorMsg
    }

    suspend fun updateHandoffOwner(handoffID: Long, userID: String): String {
        var errorMsg = ""
        try {
            supabase
                .from("handoffs")
                .update(
                    {
                        set("user_id", userID)
                    }
                ) {
                    filter {
                        eq("handoff_id", handoffID)
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
            errorMsg = "Unable to update handoff status. Please try again."
        }
        return errorMsg
    }


    suspend fun editHandoff(
        handoffID: Long,
        title: String,
        content: String?,
        status: Short,
        priority: Short
    ): NewHandoffResult {
        return try {
            val response = supabase.functions.invoke(
                function = "handoff-crypto",
                body = buildJsonObject {
                    put("action", "edit")
                    put("handoffID", handoffID)
                    put("title", title)
                    put("content", content.orEmpty())
                    put("status", status.toInt())
                    put("priority", priority.toInt())
                }
            )

            val result = response.body<EncryptedHandoffResponse>()

            NewHandoffResult(
                handoff = result.handoff
            )

        } catch (e: Exception) {
            android.util.Log.e(
                "HANDOFF_CRYPTO",
                "Failed to edit encrypted handoff",
                e
            )

            NewHandoffResult(
                error = "There was an issue editing this record. Please try again"
            )
        }
    }


    suspend fun deleteHandoff(handoffID: Long?): String {
        if (handoffID == null) {
            return "Error: handoffID must be provided"
        }
        return try {
            supabase.from("handoffs").delete {
                filter {
                    eq("handoff_id", handoffID)
                }
            }
            ""
        } catch (e: Exception) {
            "Error deleting handoff. Please try again"
        }
    }

    suspend fun getNotes(): List<Note> {
        return try {
            val currentTeamID = session.activeTeam?.teamID ?: return emptyList()
            val currentUserID = session.userID ?: return emptyList()

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
            val currentUserID = session.userID ?: return "User not logged in"
            val currentTeamID = session.activeTeam?.teamID ?: return "No team selected"

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

    suspend fun deleteNote(noteID: Long): String {
        val currentUserID = session.userID
            ?: return "User not logged in"

        val currentTeamID = session.activeTeam?.teamID
            ?: return "No team selected"

        return try {
            val deletedNotes = supabase
                .from("notes")
                .delete {
                    select()
                    filter {
                        eq("note_id", noteID)
                        eq("user_id", currentUserID)
                        eq("team_id", currentTeamID)
                    }
                }
                .decodeList<Note>()

            if (deletedNotes.isEmpty()) {
                "Note could not be deleted. Please try again."
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "There was an issue deleting this note. Please try again."
        }
    }

    suspend fun addComment(
        handoffID: Long,
        content: String,
        isAction: Boolean
    ): String {
        var errorMsg = ""

        try {
            val currentUserID = session.userID ?: return "User not logged in"

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

    suspend fun getComments(handoffID: Long): List<Comment> {
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

    @Serializable
    data class DecryptedHandoffsResponse(
        val handoffs: List<Handoff>
    )

    suspend fun getHandoffs(): List<Handoff> {
        return try {
            val teamID = session.activeTeam?.teamID
                ?: return emptyList()

            val response = supabase.functions.invoke(
                function = "handoff-crypto",
                body = buildJsonObject {
                    put("action", "read")
                    put("teamID", teamID)
                }
            )


            val result = response.body<DecryptedHandoffsResponse>()

            result.handoffs.filter { handoff ->
                handoff.status.toInt() != 4
            }


        } catch (e: Exception) {
            android.util.Log.e(
                "HANDOFF_CRYPTO",
                "Failed to retrieve handoffs",
                e
            )
            emptyList()
        }
    }



    suspend fun getAllTeamHandoffs(): List<Handoff> {
        return try {
            val currentTeamID = session.activeTeam?.teamID
                ?: return emptyList()

            val response = supabase.functions.invoke(
                function = "handoff-crypto",
                body = buildJsonObject {
                    put("action", "read")
                    put("teamID", currentTeamID)
                }
            )

            val result = response.body<DecryptedHandoffsResponse>()

            result.handoffs

        } catch (e: Exception) {
            android.util.Log.e(
                "HANDOFF_CRYPTO",
                "Failed to retrieve all team handoffs",
                e
            )
            emptyList()
        }
    }




    data class HandoffTrends(
        val totalHandoffs: Int,
        val priorityCounts: Map<Short, Int>,
        val statusCounts: Map<Short, Int>,
        val dailyActivity: Map<LocalDate, Int>
    )


    data class RepeatedIssueTrend(
        val title: String,
        val occurrences: Int
    )


    suspend fun getHandoffTrends(): HandoffTrends {
        val handoffs = getAllTeamHandoffs()

        val dailyActivity = handoffs
            .mapNotNull { handoff ->
                handoff.timestamp?.let { timestamp ->
                    java.time.Instant.parse(timestamp.toString())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
            }
            .groupingBy { it }
            .eachCount()

        return HandoffTrends(
            totalHandoffs = handoffs.size,
            priorityCounts = handoffs.groupingBy { it.priority }.eachCount(),
            statusCounts = handoffs.groupingBy { it.status }.eachCount(),
            dailyActivity = dailyActivity
        )
    }


    private fun normalizeIssueText(handoff: Handoff): Set<String> {
        val stopWords = setOf(
            "the", "a", "an", "and", "or", "but",
            "to", "of", "in", "on", "at", "for",
            "with", "is", "was", "are", "were",
            "be", "been", "being", "this", "that",
            "it", "as", "by", "from"
        )

        return "${handoff.title} ${handoff.content.orEmpty()}"
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { word ->
                word.length >= 3 && word !in stopWords
            }
            .toSet()
    }
    private fun normalizeIssueTitle(handoff: Handoff): Set<String> {
        val stopWords = setOf(
            "the", "a", "an", "and", "or", "but",
            "to", "of", "in", "on", "at", "for",
            "with", "is", "was", "are", "were",
            "be", "been", "being", "this", "that",
            "it", "as", "by", "from",
            "test", "testing"
        )

        return handoff.title
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { word ->
                word.length >= 3 && word !in stopWords
            }
            .toSet()
    }
    private fun calculateIssueSimilarity(
        first: Handoff,
        second: Handoff
    ): Double {
        val firstTitleWords = normalizeIssueTitle(first)
        val secondTitleWords = normalizeIssueTitle(second)

        // Handoffs must share at least one meaningful title word
        // before their full issue text is considered similar.
        if (firstTitleWords.intersect(secondTitleWords).isEmpty()) {
            return 0.0
        }

        val firstWords = normalizeIssueText(first)
        val secondWords = normalizeIssueText(second)

        if (firstWords.isEmpty() || secondWords.isEmpty()) {
            return 0.0
        }

        val sharedWords = firstWords.intersect(secondWords).size
        val totalUniqueWords = firstWords.union(secondWords).size

        return sharedWords.toDouble() / totalUniqueWords.toDouble()
    }

    suspend fun getRecurringIssues(
        handoff: Handoff,
        similarityThreshold: Double = 0.30
    ): RecurringIssueResult {
        val allHandoffs = getAllTeamHandoffs()

        val matches = allHandoffs
            .filter { other ->
                other.handoffID != handoff.handoffID &&
                        calculateIssueSimilarity(handoff, other) >= similarityThreshold
            }
            .sortedByDescending { other ->
                calculateIssueSimilarity(handoff, other)
            }

        return RecurringIssueResult(
            sourceHandoff = handoff,
            matchingHandoffs = matches
        )
    }

    suspend fun getRepeatedIssueTrends(): List<RepeatedIssueTrend> {
        val handoffs = getAllTeamHandoffs()
        val visited = mutableSetOf<Long>()
        val results = mutableListOf<RepeatedIssueTrend>()

        for (handoff in handoffs) {
            val id = handoff.handoffID ?: continue
            if (id in visited) continue

            val matches = handoffs.filter { other ->
                other.handoffID != id &&
                        other.handoffID !in visited &&
                        calculateIssueSimilarity(handoff, other) >= 0.30
            }

            if (matches.isNotEmpty()) {
                visited.add(id)

                matches.mapNotNull { it.handoffID }
                    .forEach { visited.add(it) }

                results.add(
                    RepeatedIssueTrend(
                        title = handoff.title,
                        occurrences = matches.size + 1
                    )
                )
            }
        }

        return results.sortedByDescending { it.occurrences }
    }



    suspend fun getHandoffsFilter(
        keyword: String = "",
        status: Short = -1,
        priority: Short = -1,
        user: String = "",
        age: Int = 0 /* days */,
        includeCompleted: Boolean = false
    ): List<Handoff> {
        return try {
            val teamID = session.activeTeam?.teamID
                ?: return emptyList()

            val response = supabase.functions.invoke(
                function = "handoff-crypto",
                body = buildJsonObject {
                    put("action", "read")
                    put("teamID", teamID)
                }
            )

            val handoffs = response.body<DecryptedHandoffsResponse>().handoffs

            handoffs.filter { handoff ->
                val matchesStatus =
                    if (status != (-1).toShort()) {
                        handoff.status == status
                    } else {
                        includeCompleted || handoff.status.toInt() != 4
                    }

                val matchesPriority =
                    priority == (-1).toShort() ||
                            handoff.priority == priority

                val matchesUser =
                    user.isBlank() || handoff.userID == user

                val matchesKeyword =
                    keyword.isBlank() ||
                            handoff.title.contains(keyword, ignoreCase = true) ||
                            (handoff.content?.contains(keyword, ignoreCase = true) == true)

                val matchesAge =
                    if (age == 0) {
                        true
                    } else {
                        val cutoff = LocalDate.now(ZoneId.systemDefault())
                            .minusDays(age.toLong())
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()

                        val lastUpdated = handoff.editTimestamp
                            ?: handoff.timestamp

                        lastUpdated != null &&
                                java.time.Instant.parse(lastUpdated.toString())
                                    .isBefore(cutoff)
                    }

                matchesStatus &&
                        matchesPriority &&
                        matchesUser &&
                        matchesKeyword &&
                        matchesAge
            }

        } catch (e: Exception) {
            android.util.Log.e(
                "HANDOFF_CRYPTO",
                "Failed to filter decrypted handoffs",
                e
            )
            emptyList()
        }
    }


    suspend fun acknowledgeHandoff(handoffID: Long): String {
        var errorMsg = ""

        try {
            val currentUserID = session.userID ?: return "User not logged in"

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
    suspend fun hasAcknowledgedHandoff(handoffID: Long): Boolean {
        return try {
            val currentUserID = session.userID ?: return false

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
