package it.roadies.android_app.model

import android.util.Patterns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period

@Entity(tableName = "user")
class User(@PrimaryKey val id: String,
           @ColumnInfo(name="first_name") val firstName: String,
           @ColumnInfo(name="last_name") val lastName: String,
           @ColumnInfo(name="email") val email: String,
           @ColumnInfo(name="username") val username: String,
           @ColumnInfo(name="avatar_url") val avatarUrl: String,
           @ColumnInfo(name="birth_date") val birthDate: LocalDate,
           @ColumnInfo(name="points") val points: Long,
           @ColumnInfo(name="badge") val badge: String) {
    init {
        if (!validateFirstName(firstName))
            throw IllegalArgumentException("First name cannot be empty and greater than 30 characters")
        if (!validateLastName(lastName))
            throw IllegalArgumentException("Last name cannot be empty and greater than 30 characters")
        if (!validateEmail(email))
            throw IllegalArgumentException("Invalid email format")
        if (!validateUsername(username))
            throw IllegalArgumentException("Username must be between 3 and 30 characters")
        if (!validateBirthDate(birthDate))
            throw IllegalArgumentException("Invalid birth date")
    }

    companion object {
        fun validateFirstName(firstName: String) : Boolean {
            return firstName.length in 1..30
        }

        fun validateLastName(lastName: String) : Boolean {
            return lastName.length in 1..30
        }

        fun validateEmail(email: String) : Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun validateUsername(username: String) : Boolean {
            return username.length in 3..30
        }

        fun validateBirthDate(birthDate: LocalDate) : Boolean {
            val today = LocalDate.now()
            if (birthDate.isAfter(today) || birthDate.isEqual(today)) {
                return false
            }

            val age = Period.between(birthDate, today).years
            return age in 0..120
        }
    }
}

