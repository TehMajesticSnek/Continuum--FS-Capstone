package com.continuum
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.Serializable

@Serializable
data class UserObj(
    val userID: UserInfo?,
    val fName: String,
    val lName: String,
)
val supabase = createSupabaseClient(
    supabaseUrl = "https://nkpxjjlvzbfzcpcxbraf.supabase.co",
    supabaseKey = "sb_publishable_RoUXBvS61E45Lzkiw-7JmA_bKjYHX4F" // Public key. Safe to hard code I'm pretty sure. Make sure RLS is on in all tables though
) {
    install(Postgrest)
}

class Database {

    suspend fun registerUser(email: String, password: String, fName: String, lName: String) : String {
        // check all are not null
        if (email.isEmpty() || password.isEmpty() || fName.isEmpty() || lName.isEmpty()) {
            return "Please fill out all fields"
        }

        // check if email is already registered (Should be handled automatically, but the docs don't seem super up to date)

        // register user
        val result = supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        // add to users table
        val user = UserObj(userID = result, fName = fName, lName = lName)
        //supabase.from("Users").insert(user)
        return "Success"
    }

    suspend fun login(email: String, password: String) : String {
        // check all are not null
        if (email.isEmpty() || password.isEmpty()) {
            return "Please fill out all fields"
        }

        // check account exists


        // validate credentials
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return "Success"
    }


//    suspend fun testDB() : String {
//        val result = supabase.from("test").select(columns = Columns.list("Name")).decodeSingle<UserRow>()
//        Log.d(TAG, result.Name)
//        return result.Name
//    }
}
