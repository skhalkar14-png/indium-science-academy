package com.indium.educationapp

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.indium.educationapp.ui.theme.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*





object CurrentUser {
    var teacherName: String = ""
    var mobile: String = ""
    var name: String = ""
    var role: String = ""
    var className: String = ""
    var division: String = ""
    var board: String = ""
    var rollNo: String = ""
}

// ======================================================
// MAIN ACTIVITY
// ======================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            IndiumEducationAppTheme {
                IndiumApp()
            }
        }
    }
}


// ======================================================
// MAIN APP
// ======================================================

@Composable
fun IndiumApp() {

    var selectedRole by remember {
        mutableStateOf("")
    }

    var loggedIn by remember {
        mutableStateOf(false)
    }

    var showSignup by remember {
        mutableStateOf(false)
    }

    if (showSignup) {

        SignupScreen(
            onBack = {
                showSignup = false
            }
        )

    } else if (loggedIn) {

        IndiumAppTheme {
            DashboardScreen(
                role = selectedRole,
                onLogout = {
                    CurrentUser.teacherName = ""
                    CurrentUser.mobile = ""
                    CurrentUser.name = ""
                    CurrentUser.role = ""
                    CurrentUser.className = ""
                    CurrentUser.division = ""
                    CurrentUser.board = ""
                    CurrentUser.rollNo = ""
                    loggedIn = false
                    selectedRole = ""
                }
            )
        }

    } else {

        LoginScreen(
            selectedRole = selectedRole,

            onRoleSelected = {
                selectedRole = it
            },

            onLoginSuccess = {
                loggedIn = true
            },

            onSignup = {
                showSignup = true
            }
        )
    }
}


// ======================================================
// LOGIN SCREEN
// ======================================================

@Composable
fun LoginScreen(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    onLoginSuccess: (String) -> Unit,
    onSignup: () -> Unit
) {

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    var mobile by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var showPassword by remember {
        mutableStateOf(false)
    }

    var showChangePassword by remember { mutableStateOf(false) }
    var firstLoginDocumentId by remember { mutableStateOf("") }

    var loginMessage by remember {
        mutableStateOf("")
    }

    var isLoggingIn by remember {
        mutableStateOf(false)
    }

    var showForgotPassword by remember {
        mutableStateOf(false)
    }

    if (showForgotPassword) {
        ForgotPasswordScreen(
            selectedRole = selectedRole,
            onBack = {
                showForgotPassword = false
            }
        )
        return
    }

    if (showChangePassword) {
        ChangePasswordScreen(
            documentId = firstLoginDocumentId,
            onPasswordChanged = {
                showChangePassword = false
                onLoginSuccess(selectedRole)
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(
                id = R.drawable.indium_logo
            ),

            contentDescription =
                "Indium Science Academy Logo",

            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Text(
            text = "INDIUM SCIENCE ACADEMY",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "INDIUM EDUCATION APP",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        Text(
            text = "Select User",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            RoleButton(
                role = "Student",
                selectedRole = selectedRole,
                onRoleSelected = {
                    onRoleSelected(it)
                    loginMessage = ""
                }
            )

            RoleButton(
                role = "Teacher",
                selectedRole = selectedRole,
                onRoleSelected = {
                    onRoleSelected(it)
                    loginMessage = ""
                }
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        RoleButton(
            role = "Admin",
            selectedRole = selectedRole,
            onRoleSelected = {
                onRoleSelected(it)
                loginMessage = ""
            }
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OutlinedTextField(
            value = mobile,

            onValueChange = {
                if (
                    it.length <= 10 &&
                    it.all { char -> char.isDigit() }
                ) {
                    mobile = it
                    loginMessage = ""
                }
            },

            label = {
                Text("Mobile Number")
            },

            modifier = Modifier.fillMaxWidth(),

            singleLine = true,

            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.DarkGray
            )
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                    password = it
                }
            },
            label = {
                Text("Password")
            },
            singleLine = true,
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        showPassword = !showPassword
                    }
                ) {
                    Icon(
                        imageVector = if (showPassword) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (showPassword) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {

                if (
                    mobile.isBlank() ||
                    password.isBlank()
                ) {

                    loginMessage =
                        "Mobile number or Password can not be blank"

                    return@Button
                }

                if (
                    mobile.length != 10 ||
                    mobile.firstOrNull() !in '6'..'9'
                ) {

                    loginMessage =
                        "invalid mobile number"

                    return@Button
                }

                if (selectedRole.isBlank()) {

                    loginMessage =
                        "Please select user type"

                    return@Button
                }

                isLoggingIn = true

                loginMessage =
                    "⏳ Checking login..."

                firestore
                    .collection("users")
                    .whereEqualTo(
                        "mobile",
                        mobile
                    )
                    .whereEqualTo(
                        "role",
                        selectedRole
                    )
                    .get()
                    .addOnSuccessListener { documents ->

                        if (documents.isEmpty) {

                            isLoggingIn = false

                            loginMessage =
                                "❌ Invalid mobile number or user type"

                        } else {

                            val document =
                                documents.documents[0]

                            val savedPassword =
                                document.getString("password")

                            if (
                                savedPassword == password
                            ) {

                                CurrentUser.name = document.getString("name") ?: ""
                                CurrentUser.mobile = document.getString("mobile") ?: mobile
                                CurrentUser.role = selectedRole
                                CurrentUser.className = document.getString("class") ?: ""
                                CurrentUser.division = document.getString("division") ?: ""
                                CurrentUser.board = document.getString("board") ?: ""
                                CurrentUser.rollNo = document.getString("rollNo") ?: ""

                                if (selectedRole == "Teacher") {
                                    CurrentUser.teacherName = CurrentUser.name
                                }

                                val firstLogin = document.getBoolean("firstLogin") ?: false

                                if (firstLogin && selectedRole == "Teacher") {
                                    firstLoginDocumentId = document.id
                                    showChangePassword = true
                                    isLoggingIn = false
                                    loginMessage = ""
                                } else {
                                    isLoggingIn = false
                                    loginMessage = ""
                                    onLoginSuccess(selectedRole)
                                }

                            } else {

                                isLoggingIn = false

                                loginMessage =
                                    "❌ Invalid password"
                            }
                        }
                    }
                    .addOnFailureListener { e ->

                        isLoggingIn = false

                        loginMessage =
                            "❌ Firebase error: ${e.message}"
                    }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            enabled = !isLoggingIn
        ) {

            Text(
                text =
                    if (isLoggingIn)
                        "⏳ CHECKING..."
                    else
                        "LOGIN",

                fontSize = 17.sp
            )
        }

        if (loginMessage.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = loginMessage,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
// ======================================================
// ROLE BUTTON
// ======================================================

@Composable
fun SignupScreen(
    onBack: () -> Unit
) {

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    var signupRole by remember {
        mutableStateOf("")
    }

    var userName by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var fullName by remember {
        mutableStateOf("")
    }

    var mobile by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var className by remember {
        mutableStateOf("")
    }

    var division by remember {
        mutableStateOf("")
    }

    var subject by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "📝 New User Signup",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Select User Type",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            RoleButton(
                role = "Student",
                selectedRole = signupRole,
                onRoleSelected = {
                    signupRole = it
                    message = ""
                }
            )
        }

        if (signupRole.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    message = ""
                },
                label = {
                    Text(
                        if (signupRole == "Student")
                            "Student Name"
                        else
                            "Full Name"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.DarkGray
                )
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    if (
                        it.length <= 10 &&
                        it.all { char -> char.isDigit() }
                    ) {
                        mobile = it
                        message = ""
                    }
                },
                label = {
                    Text("Mobile Number")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.DarkGray
                )
            )

            if (signupRole == "Student") {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = className,
                    onValueChange = {
                        className = it
                        message = ""
                    },
                    label = {
                        Text("Class")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.DarkGray
                    )
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = division,
                    onValueChange = {
                        division = it
                        message = ""
                    },
                    label = {
                        Text("Division")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.DarkGray
                    )
                )
            }

            if (signupRole == "Teacher") {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = {
                        subject = it
                        message = ""
                    },
                    label = {
                        Text("Subject")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.DarkGray
                    )
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        message = ""
                    },
                    label = {
                        Text("Email")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.DarkGray
                    )
                )
            }

            if (signupRole == "Admin") {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        message = ""
                    },
                    label = {
                        Text("Email")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.DarkGray
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    message = ""
                },
                label = {
                    Text("Username")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.DarkGray
                )
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = ""
                },
                label = {
                    Text("Initial Password / PIN")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.DarkGray
                )
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = {

                    if (
                        signupRole.isBlank() ||
                        fullName.isBlank() ||
                        mobile.isBlank() ||
                        userName.isBlank() ||
                        password.isBlank()
                    ) {

                        message =
                            "❌ Please fill all required fields."

                        return@Button
                    }

                    if (
                        signupRole == "Student" &&
                        (className.isBlank() || division.isBlank())
                    ) {

                        message =
                            "❌ Please enter Class and Division."

                        return@Button
                    }

                    if (
                        signupRole == "Teacher" &&
                        subject.isBlank()
                    ) {

                        message =
                            "❌ Please enter Subject."

                        return@Button
                    }

                    if (
                        mobile.length != 10 ||
                        mobile.firstOrNull() !in '6'..'9'
                    ) {

                        message =
                            "❌ Invalid mobile number."

                        return@Button
                    }
                    if (mobile.length != 10 ||
                        mobile.firstOrNull() !in '6'..'9'
                    ) {

                        message =
                            "❌ Invalid mobile number."

                        return@Button
                    }

                    isSaving = true

                    message =
                        "⏳ Creating account..."

                    val userId =
                        userName.trim()

                    firestore
                        .collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { document ->

                            if (document.exists()) {

                                isSaving = false

                                message =
                                    "❌ Username already exists."

                            } else {

                                val userData =
                                    hashMapOf<String, Any>(
                                        "name" to fullName,
                                        "mobile" to mobile,
                                        "email" to email,
                                        "role" to signupRole,
                                        "username" to userName,
                                        "password" to password
                                    )

                                if (signupRole == "Student") {

                                    userData["class"] =
                                        className

                                    userData["division"] =
                                        division
                                }

                                if (signupRole == "Teacher") {

                                    userData["subject"] =
                                        subject
                                }

                                firestore
                                    .collection("users")
                                    .document(userId)
                                    .set(userData)
                                    .addOnSuccessListener {

                                        isSaving = false

                                        message =
                                            "✅ Account created successfully!"
                                    }
                                    .addOnFailureListener { e ->

                                        isSaving = false

                                        message =
                                            "❌ Firebase error: ${e.message}"
                                    }
                            }
                        }
                        .addOnFailureListener { e ->

                            isSaving = false

                            message =
                                "❌ Firebase error: ${e.message}"
                        }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                enabled = !isSaving
            ) {

                Text(
                    text =
                        if (isSaving)
                            "⏳ CREATING..."
                        else
                            "CREATE ACCOUNT",

                    fontSize = 17.sp
                )
            }

            if (message.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {

            Text("← Back to Login")
        }
    }
}
@Composable
fun RoleButton(
    role: String,
    selectedRole: String,
    onRoleSelected: (String) -> Unit
)
{
    Button(
        onClick = {
            onRoleSelected(role)
        },
        modifier = Modifier
            .width(150.dp)
            .height(52.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7E57C2),
            contentColor = Color.White
        )
    ) {
        Text(
            text = if (selectedRole == role) {
                "✓  $role"
            } else {
                role
            },
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// ======================================================
// DASHBOARD
// ======================================================

@Composable
fun DashboardScreen(
    role: String,
    onLogout: () -> Unit
) {

    var selectedScreen by remember {
        mutableStateOf<String?>(null)
    }
    var showAddTeacher by remember {
        mutableStateOf(false)
    }
    var showAddStudent by remember {
        mutableStateOf(false)
    }
    var showTeacherTodayLectures by remember {
        mutableStateOf(false)
    }
    var showTeacherLeave by remember {
        mutableStateOf(false)
    }

    var selectedProfileStudent by remember { mutableStateOf<SheetStudent?>(null) }
    var selectedProfileTeacher by remember { mutableStateOf<TeacherRecord?>(null) }

    var dashboardSearchQuery by remember { mutableStateOf("") }
    var globalStudents by remember { mutableStateOf<List<SheetStudent>>(emptyList()) }
    var globalTeachers by remember { mutableStateOf<List<TeacherRecord>>(emptyList()) }

    LaunchedEffect(role) {
        if (role.equals("Admin", ignoreCase = true)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val scriptUrl = "https://script.google.com/macros/s/AKfycbwbBEeUDm0gY_mCuPUJC04sw-O1aWlTTGbyu-x4yhl-BOLbUIoHD4cqWuuS_pNKRSCi/exec"
                    val connection = URL(scriptUrl).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 20000
                    connection.readTimeout = 20000
                    if (connection.responseCode == 200) {
                        val result = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(result)
                        if (json.optBoolean("success")) {
                            val studentsArray = json.getJSONArray("students")
                            val loadedStudents = mutableListOf<SheetStudent>()
                            for (i in 0 until studentsArray.length()) {
                                val student = studentsArray.getJSONObject(i)
                                loadedStudents.add(SheetStudent(
                                    rollNo = student.optString("rollNo").trim(),
                                    studentName = student.optString("studentName").trim(),
                                    mobileNo = student.optString("mobileNo").trim(),
                                    standard = student.optString("standard").trim(),
                                    board = student.optString("board").trim()
                                ))
                            }
                            withContext(Dispatchers.Main) {
                                globalStudents = loadedStudents
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("role", "Teacher")
                .get()
                .addOnSuccessListener { documents ->
                    val loaded = documents.map { doc ->
                        TeacherRecord(
                            name = doc.getString("name") ?: "Unknown",
                            mobile = doc.getString("mobile") ?: "",
                            subject = doc.getString("subject") ?: ""
                        )
                    }
                    globalTeachers = loaded
                }
        }
    }

    if (showTeacherTodayLectures) {
        TeacherTodayLecturesScreen(
            onBack = {
                showTeacherTodayLectures = false
            }
        )
        return
    }

    if (showTeacherLeave) {
        TeacherLeaveScreen(
            onBack = {
                showTeacherLeave = false
            }
        )
        return
    }

    if (showAddTeacher) {
        AddTeacherScreen(
            onBack = {
                showAddTeacher = false
            }
        )
        return
    }

    if (showAddStudent) {
        AddStudentScreen(
            onBack = {
                showAddStudent = false
            }
        )
        return
    }

    if (selectedProfileStudent != null) {
        StudentProfileScreen(
            student = selectedProfileStudent!!,
            onBack = { selectedProfileStudent = null }
        )
        return
    }

    if (selectedProfileTeacher != null) {
        AdminTeacherProfileScreen(
            teacher = selectedProfileTeacher!!,
            onBack = { selectedProfileTeacher = null }
        )
        return
    }

    // --------------------------------------------------
    // SUB SCREEN
    // --------------------------------------------------

    if (selectedScreen != null) {
        val screenName = selectedScreen!!
            .replace(Regex("^[^A-Za-z]*"), "")
            .trim()

        Scaffold(
            topBar = {
                IndiumTopBar(
                    title = screenName,
                    onBack = { selectedScreen = null }
                )
            },
            containerColor = Color(0xFFF7F4FF)
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (screenName) {
                    "My Profile" -> ProfileScreen(onBack = { selectedScreen = null })
                    "Timetable" -> TimetableScreen(onBack = { selectedScreen = null })
                    "Homework" -> {
                        if (role == "Teacher") {
                            TeacherHomeworkScreen(onBack = { selectedScreen = null })
                        } else {
                            HomeworkScreen(onBack = { selectedScreen = null })
                        }
                    }
                    "Study Material" -> StudyMaterialScreen(onBack = { selectedScreen = null })
                    "Attendance" -> AttendanceScreen(onBack = { selectedScreen = null })
                    "My Attendance" -> TeacherAttendanceScreen(onBack = { selectedScreen = null })
                    "Student Attendance" -> StudentAttendanceForTeacherScreen(onBack = { selectedScreen = null })
                    "Exams & Results" -> ResultsScreen(onBack = { selectedScreen = null })
                    "Exams & Marks" -> ResultsScreen(onBack = { selectedScreen = null })
                    "Fees" -> FeesScreen(onBack = { selectedScreen = null })
                    "Weekly Timetable" -> AdminWeeklyTimetableScreen(onBack = { selectedScreen = null })
                    "Today's All Lectures" -> AdminTodayLecturesScreen(onBack = { selectedScreen = null })
                    "Fees Structure" -> FeesScreen(onBack = { selectedScreen = null })
                    "Notices" -> NoticeBoardScreen(userRole = CurrentUser.role, userName = CurrentUser.name, onBack = { selectedScreen = null })
                    "Notice Board" -> NoticeBoardScreen(userRole = CurrentUser.role, userName = CurrentUser.name, onBack = { selectedScreen = null })
                    "Students" -> TeacherStudentsScreen(userRole = role, onBack = { selectedScreen = null })
                    "Teachers" -> {
                        if (role == "Admin") {
                            AdminTeachersScreen(
                                onTeacherClick = { selectedProfileTeacher = it },
                                onBack = { selectedScreen = null }
                            )
                        } else {
                            AddTeacherScreen(onBack = { selectedScreen = null })
                        }
                    }
                    else -> MenuScreen(title = screenName, onBack = { selectedScreen = null })
                }
            }
        }
        return
    }


    // ======================================================
    // MENU ITEMS WITH ICONS
    // ======================================================

    data class MenuItem(val title: String, val icon: ImageVector)

    val menuItems = when (role) {
        "Student" -> listOf(
            MenuItem("My Profile", Icons.Default.Person),
            MenuItem("Timetable", Icons.Default.CalendarToday),
            MenuItem("Homework", Icons.Default.Book),
            MenuItem("Study Material", Icons.Default.MenuBook),
            MenuItem("Attendance", Icons.Default.CheckCircle),
            MenuItem("Exams & Results", Icons.Default.Assignment),
            MenuItem("Fees", Icons.Default.Payments),
            MenuItem("Notices", Icons.Default.Notifications)
        )
        "Teacher" -> listOf(
            MenuItem("Students", Icons.Default.Group),
            MenuItem("My Attendance", Icons.Default.CheckCircle),
            MenuItem("Student Attendance", Icons.Default.BarChart),
            MenuItem("Homework", Icons.Default.EditNote),
            MenuItem("Exams & Marks", Icons.Default.Assignment),
            MenuItem("Study Material", Icons.Default.MenuBook),
            MenuItem("Notices", Icons.Default.Notifications)
        ).filter { it.title != "Students" && it.title != "Study Material" }
        "Admin" -> listOf(
            MenuItem("Weekly Timetable", Icons.Default.CalendarToday),
            MenuItem("Today's All Lectures", Icons.Default.Today),
            MenuItem("Fees Structure", Icons.Default.Payments),
            MenuItem("Notice Board", Icons.Default.Notifications)
        )
        else -> emptyList()
    }

    // ======================================================
    // DASHBOARD UI
    // ======================================================

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .systemBarsPadding(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            IndiumDashboardHeader(
                userName = CurrentUser.name,
                role = role,
                onLogout = onLogout
            )
        }

        if (role.equals("Admin", ignoreCase = true)) {
            item(span = { GridItemSpan(2) }) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IndiumButton(
                        text = "Add Student",
                        onClick = { showAddStudent = true },
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF7C4DFF)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IndiumButton(
                        text = "Add Teacher",
                        onClick = { showAddTeacher = true },
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF673AB7)
                    )
                }
            }
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = dashboardSearchQuery,
                    onValueChange = { dashboardSearchQuery = it },
                    placeholder = { Text("Search students or teachers...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF252238),
                        unfocusedTextColor = Color(0xFF252238)
                    )
                )
            }
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (role.equals("Teacher", ignoreCase = true)) {
            item(span = { GridItemSpan(2) }) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IndiumButton(
                        text = "Today's Lectures",
                        onClick = { showTeacherTodayLectures = true },
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF7C4DFF)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IndiumButton(
                        text = "Apply Leave",
                        onClick = { showTeacherLeave = true },
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF673AB7)
                    )
                }
            }
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (role.equals("Admin", ignoreCase = true) && dashboardSearchQuery.trim().isNotEmpty()) {
            val query = dashboardSearchQuery.trim()
            val matchesStudents = globalStudents.filter { it.studentName.contains(query, ignoreCase = true) }
            val matchesTeachers = globalTeachers.filter { it.name.contains(query, ignoreCase = true) }

            if (matchesStudents.isEmpty() && matchesTeachers.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No records match.", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            } else {
                matchesStudents.forEach { student ->
                    item(span = { GridItemSpan(2) }) {
                        val context = LocalContext.current
                        IndiumCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedProfileStudent = student },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = student.studentName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF252238)
                                    )
                                    Surface(
                                        color = Color(0xFF7C4DFF).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "STUDENT",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF7C4DFF),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${student.standard} • ${if(student.board.isBlank()) "General" else student.board}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                if (student.mobileNo.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobileNo}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Call",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF4CAF50)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = student.mobileNo,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = TextDecoration.Underline
                                            ),
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                matchesTeachers.forEach { teacher ->
                    item(span = { GridItemSpan(2) }) {
                        val context = LocalContext.current
                        IndiumCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedProfileTeacher = teacher },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = teacher.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF252238)
                                    )
                                    Surface(
                                        color = Color(0xFF673AB7).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "TEACHER",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF673AB7),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (teacher.subject.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = teacher.subject,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                if (teacher.mobile.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${teacher.mobile}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Call",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF4CAF50)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = teacher.mobile,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = TextDecoration.Underline
                                            ),
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF252238)
                    )
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(menuItems) { item ->
                IndiumFeatureCard(
                    title = item.title,
                    icon = item.icon,
                    onClick = { selectedScreen = item.title },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!role.equals("Admin", ignoreCase = true)) {
                item(span = { GridItemSpan(2) }) {
                    IndiumCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFF7C4DFF).copy(alpha = 0.1f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Campaign,
                                                contentDescription = null,
                                                tint = Color(0xFF7C4DFF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Latest Notice",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF252238)
                                        )
                                    )
                                }
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF7C4DFF),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.clickable { selectedScreen = "Notices" }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Please check the Notices section regularly for important school announcements.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF686477),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


// ======================================================
// MENU SCREEN
// ======================================================

@Composable
fun MenuScreen(
    title: String,
    onBack: () -> Unit
) {

    BackHandler {
        onBack()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        Text(
            text = "This section will be available soon.",
            fontSize = 18.sp
        )


        Spacer(
            modifier = Modifier.height(30.dp)
        )


        Button(
            onClick = onBack
        ) {

            Text("← Back")
        }
    }
}


// ======================================================
// TIMETABLE
// ======================================================

@Composable
fun TimetableScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    var timetableData by remember { 
        mutableStateOf<Map<String, List<TodayLecture>>>(emptyMap()) 
    }

    val studentClass = CurrentUser.className
    val studentBoard = CurrentUser.board
    
    val scriptUrl = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec"

    LaunchedEffect(Unit) {
        if (studentClass.isBlank()) {
            errorMessage = "Class information not found in profile."
            return@LaunchedEffect
        }
        
        isLoading = true
        try {
            val encodedClass = URLEncoder.encode(studentClass, "UTF-8")
            val encodedBoard = URLEncoder.encode(studentBoard, "UTF-8")
            val urlString = "$scriptUrl?action=getTimetable&standard=$encodedClass&board=$encodedBoard"
            
            val result = withContext(Dispatchers.IO) {
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 20000
                connection.readTimeout = 20000
                connection.instanceFollowRedirects = true
                
                try {
                    val code = connection.responseCode
                    if (code == 200) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        null
                    }
                } finally {
                    connection.disconnect()
                }
            }

            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success", false)) {
                    val daysJson = json.optJSONObject("timetable") ?: JSONObject()
                    val loadedData = mutableMapOf<String, List<TodayLecture>>()
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                    
                    days.forEach { day ->
                        val dayArray = daysJson.optJSONArray(day)
                        if (dayArray != null) {
                            val lecturesList = mutableListOf<TodayLecture>()
                            for (i in 0 until dayArray.length()) {
                                val item = dayArray.getJSONObject(i)
                                lecturesList.add(
                                    TodayLecture(
                                        time = item.optString("time"),
                                        className = item.optString("className"),
                                        subject = item.optString("subject"),
                                        teacher = item.optString("teacher"),
                                        adjusted = item.optBoolean("adjusted", false),
                                        originalTeacher = item.optString("originalTeacher", "")
                                    )
                                )
                            }
                            loadedData[day] = lecturesList
                        }
                    }
                    timetableData = loadedData
                    if (loadedData.isEmpty()) {
                        errorMessage = "No timetable found for your class."
                    }
                } else {
                    errorMessage = json.optString("error", "Failed to fetch timetable.")
                }
            } else {
                errorMessage = "Timetable API not reachable."
            }
        } catch (e: Exception) {
            errorMessage = "Connection error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                IndiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Weekly Timetable",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$studentClass - $studentBoard",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    IndiumCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                    days.forEach { day ->
                        val lectures = timetableData[day] ?: emptyList()
                        if (lectures.isNotEmpty()) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF673AB7)
                                ),
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                            )
                            
                            lectures.forEach { lecture ->
                                IndiumCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    containerColor = if (lecture.adjusted) Color(0xFFFFF3E0) else Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = lecture.subject,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF252238)
                                                )
                                            )
                                            Text(
                                                text = lecture.teacher,
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                            )
                                            if (lecture.adjusted) {
                                                Text(
                                                    text = "Adjusted from: ${lecture.originalTeacher}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFFE65100),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                        Text(
                                            text = lecture.time,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF7C4DFF)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                IndiumButton(text = "BACK", onClick = onBack)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


// ======================================================
// HOMEWORK SCREEN
// ======================================================

@Composable
fun HomeworkScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFF7C4DFF),
            contentColor = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Homework", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = "Complete your tasks on time!", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HomeworkItem("Mathematics", "Complete Exercise 5.1 and 5.2 from textbooks.", "Due: Tomorrow")
        HomeworkItem("Science", "Draw and label the human respiratory system.", "Due: 18 Sep")
        HomeworkItem("English", "Write a letter to your friend about your holidays.", "Due: Friday")

        Spacer(modifier = Modifier.height(32.dp))
        IndiumButton(text = "BACK", onClick = onBack)
    }
}

@Composable
fun HomeworkItem(subject: String, task: String, dueDate: String) {
    IndiumCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = subject, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))
                Text(text = dueDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = task, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF252238))
        }
    }
}


@Composable
fun TeacherHomeworkScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var subject by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Assign Homework",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Homework Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                IndiumButton(text = "PUBLISH HOMEWORK", onClick = { /* Publish Logic */ }, containerColor = Color(0xFF7C4DFF))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        IndiumOutlinedButton(text = "BACK", onClick = onBack)
    }
}



// ======================================================
// FEES
// ======================================================

@Composable
fun FeesScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFF7C4DFF),
            contentColor = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Fee Summary", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AttendanceStatItem("Total", "₹50k")
                    AttendanceStatItem("Paid", "₹30k")
                    AttendanceStatItem("Balance", "₹20k")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF252238))
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeeTransactionItem("Installment 2", "₹10,000", "Paid on 15 Aug 2026", true)
        FeeTransactionItem("Installment 1", "₹20,000", "Paid on 10 June 2026", true)
        FeeTransactionItem("Bus Fees", "₹5,000", "Due on 01 Oct 2026", false)

        Spacer(modifier = Modifier.height(32.dp))
        IndiumButton(text = "PAY NOW", onClick = { /* Payment Integration */ }, containerColor = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(12.dp))
        IndiumOutlinedButton(text = "BACK", onClick = onBack)
    }
}

@Composable
fun FeeTransactionItem(title: String, amount: String, date: String, isPaid: Boolean) {
    IndiumCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        containerColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF252238))
                Text(text = date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = amount,
                fontWeight = FontWeight.ExtraBold,
                color = if (isPaid) Color(0xFF4CAF50) else Color.Red
            )
        }
    }
}


// ======================================================
// ATTENDANCE
// ======================================================

@Composable
fun AttendanceScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    var workingDays by remember { mutableStateOf(0) }
    var presentDays by remember { mutableStateOf(0) }
    var attendancePercentage by remember { mutableStateOf(0f) }
    var history by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val studentRollNo = CurrentUser.rollNo
    val studentClass = CurrentUser.className
    val studentBoard = CurrentUser.board
    
    val scriptUrl = "https://script.google.com/macros/s/AKfycbwbBEeUDm0gY_mCuPUJC04sw-O1aWlTTGbyu-x4yhl-BOLbUIoHD4cqWuuS_pNKRSCi/exec"

    LaunchedEffect(Unit) {
        if (studentRollNo.isBlank() || studentClass.isBlank()) {
            errorMessage = "Student information incomplete."
            return@LaunchedEffect
        }
        
        isLoading = true
        try {
            val encodedClass = URLEncoder.encode(studentClass, "UTF-8")
            val encodedBoard = URLEncoder.encode(studentBoard, "UTF-8")
            val urlString = "$scriptUrl?action=getStudentAttendance&rollNo=$studentRollNo&standard=$encodedClass&board=$encodedBoard"
            
            val result = withContext(Dispatchers.IO) {
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                try {
                    val code = connection.responseCode
                    if (code == 200) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        null
                    }
                } finally {
                    connection.disconnect()
                }
            }

            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success", false)) {
                    workingDays = json.optInt("totalDays", 0)
                    presentDays = json.optInt("presentDays", 0)
                    attendancePercentage = json.optDouble("percentage", 0.0).toFloat()
                    
                    val historyArray = json.optJSONArray("history") ?: JSONArray()
                    val loadedHistory = mutableListOf<Pair<String, String>>()
                    for (i in 0 until historyArray.length()) {
                        val item = historyArray.getJSONObject(i)
                        loadedHistory.add(item.optString("date") to item.optString("status"))
                    }
                    history = loadedHistory
                } else {
                    errorMessage = json.optString("error", "Attendance data currently unavailable.")
                }
            } else {
                errorMessage = "Unable to fetch attendance record."
            }
        } catch (e: Exception) {
            errorMessage = "Connection error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                IndiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Attendance Summary",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttendanceStatItem("Total", "$workingDays")
                            AttendanceStatItem("Present", "$presentDays")
                            AttendanceStatItem("%", "${attendancePercentage.toInt()}%")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = if (workingDays > 0) presentDays.toFloat() / workingDays else 0f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Attendance History",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF252238)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage.isNotEmpty()) {
                    IndiumCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                } else if (history.isEmpty()) {
                    IndiumCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No attendance records found.",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                } else {
                    history.forEach { entry ->
                        IndiumCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            containerColor = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = entry.first, fontWeight = FontWeight.Medium, color = Color(0xFF252238))
                                Text(
                                    text = entry.second,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (entry.second.equals("Present", true)) Color(0xFF4CAF50) else Color.Red
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                IndiumButton(text = "BACK", onClick = onBack)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AttendanceStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
    }
}


// ======================================================
// RESULTS
// ======================================================

@Composable
fun ResultsScreen(
    onBack: () -> Unit
) {
    if (CurrentUser.role.equals("Teacher", ignoreCase = true)) {
        TeacherResultsScreen(onBack = onBack)
    } else {
        StudentResultsScreen(onBack = onBack)
    }
}

@Composable
fun StudentResultsScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var resultsList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    val studentName = CurrentUser.name
    val studentRollNo = CurrentUser.rollNo
    val scriptUrl = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec"

    LaunchedEffect(Unit) {
        if (studentName.isBlank()) {
            errorMessage = "Student name not found in profile."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val encodedName = URLEncoder.encode(studentName.trim(), "UTF-8")
            val encodedRoll = URLEncoder.encode(studentRollNo.trim(), "UTF-8")
            val urlString = "$scriptUrl?action=getStudentResults&studentName=$encodedName&rollNo=$encodedRoll"
            val response = withContext(Dispatchers.IO) {
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true
                if (connection.responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else null
            }

            if (response != null) {
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    val arr = json.optJSONArray("results") ?: JSONArray()
                    val list = mutableListOf<Map<String, Any>>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val map = mutableMapOf<String, Any>()
                        map["examName"] = obj.optString("examName")
                        map["subject"] = obj.optString("subject")
                        map["obtainedMarks"] = obj.optDouble("obtainedMarks", 0.0)
                        map["maxMarks"] = obj.optDouble("maxMarks", 100.0)
                        map["timestamp"] = obj.optString("timestamp")
                        list.add(map)
                    }
                    resultsList = list
                } else {
                    errorMessage = json.optString("error", "Failed to load results.")
                }
            } else {
                errorMessage = "Server not reachable."
            }
        } catch (e: Exception) {
            errorMessage = "Error loading results: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFF7C4DFF),
            contentColor = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Examination Results", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = "Student: ${CurrentUser.name} (Roll #${CurrentUser.rollNo})", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else if (errorMessage.isNotEmpty()) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = errorMessage, modifier = Modifier.padding(16.dp), color = Color.Red, fontWeight = FontWeight.Bold)
            }
        } else if (resultsList.isEmpty()) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No examination results published yet.", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                }
            }
        } else {
            val groupedByExam = resultsList.groupBy { it["examName"]?.toString() ?: "Examination" }
            groupedByExam.forEach { (examName, results) ->
                Text(
                    text = examName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF673AB7)),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                IndiumCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        var totalObtained = 0.0
                        var totalMax = 0.0
                        results.forEach { res ->
                            val subject = res["subject"]?.toString() ?: "Subject"
                            val obtained = res["obtainedMarks"]?.toString() ?: "0"
                            val max = res["maxMarks"]?.toString() ?: "100"
                            totalObtained += obtained.toDoubleOrNull() ?: 0.0
                            totalMax += max.toDoubleOrNull() ?: 100.0

                            ResultSubjectItem(subject, obtained, max)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE6DDFB))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Marks", fontWeight = FontWeight.Bold, color = Color(0xFF252238))
                            Text(text = "${totalObtained.toInt()} / ${totalMax.toInt()}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C4DFF))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        IndiumButton(text = "BACK", onClick = onBack)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TeacherResultsScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var currentView by remember { mutableStateOf("MENU") } // "MENU", "ENTRY", "HISTORY"

    when (currentView) {
        "ENTRY" -> TeacherResultsEntryScreen(
            onBack = { currentView = "MENU" },
            onSavedSuccess = { currentView = "MENU" }
        )
        "HISTORY" -> TeacherResultsHistoryScreen(
            onBack = { currentView = "MENU" }
        )
        else -> TeacherResultsMenuScreen(
            onEnterMarks = { currentView = "ENTRY" },
            onViewHistory = { currentView = "HISTORY" },
            onBack = onBack
        )
    }
}

@Composable
fun TeacherResultsMenuScreen(
    onEnterMarks: () -> Unit,
    onViewHistory: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📊 Examination Results",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF252238)
        )

        Spacer(modifier = Modifier.height(30.dp))

        IndiumCard(
            modifier = Modifier.fillMaxWidth().clickable { onEnterMarks() },
            containerColor = Color(0xFF7C4DFF),
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Enter / Edit Marks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = "Record student scores for exams", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        IndiumCard(
            modifier = Modifier.fillMaxWidth().clickable { onViewHistory() },
            containerColor = Color.White,
            contentColor = Color(0xFF252238)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color(0xFF673AB7))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Results History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = "View and review previously saved exam records", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        IndiumOutlinedButton(text = "BACK TO DASHBOARD", onClick = onBack)
    }
}

@Composable
fun TeacherResultsEntryScreen(
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scriptUrl = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec"
    val classList = listOf("1st Standard", "2nd Standard", "3rd Standard", "4th Standard", "5th Standard", "6th Standard", "7th Standard", "8th Standard", "9th Standard", "10th Standard")

    var selectedClass by remember { mutableStateOf("10th Standard") }
    var showClassMenu by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("") }
    var examName by remember { mutableStateOf("Weekly Class Test") }
    var maxMarksInput by remember { mutableStateOf("100") }

    var sheetStudents by remember { mutableStateOf<List<SheetStudent>>(emptyList()) }
    var isLoadingStudents by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableStateOf(0) }

    val marksMap = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(selectedClass, retryTrigger) {
        isLoadingStudents = true
        loadError = ""
        try {
            val studentScriptUrl = "https://script.google.com/macros/s/AKfycbwbBEeUDm0gY_mCuPUJC04sw-O1aWlTTGbyu-x4yhl-BOLbUIoHD4cqWuuS_pNKRSCi/exec"
            val standardNumber = selectedClass.filter { it.isDigit() }
            val result = withContext(Dispatchers.IO) {
                val connection = URL(studentScriptUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 35000
                connection.readTimeout = 35000
                connection.instanceFollowRedirects = true
                if (connection.responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
            }
            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success")) {
                    val studentsArray = json.optJSONArray("students") ?: JSONArray()
                    val loadedStudents = mutableListOf<SheetStudent>()
                    for (i in 0 until studentsArray.length()) {
                        val student = studentsArray.getJSONObject(i)
                        val standard = student.optString("standard").trim()
                        if (standard.filter { it.isDigit() } == standardNumber) {
                            loadedStudents.add(SheetStudent(
                                rollNo = student.optString("rollNo").trim(),
                                studentName = student.optString("studentName").trim(),
                                mobileNo = "",
                                standard = standard,
                                board = student.optString("board").trim()
                            ))
                        }
                    }
                    sheetStudents = loadedStudents.sortedBy { it.rollNo.filter { c -> c.isDigit() }.toIntOrNull() ?: 99 }
                    marksMap.clear()
                    sheetStudents.forEach { s ->
                        marksMap[s.rollNo] = ""
                    }
                    if (sheetStudents.isEmpty()) {
                        loadError = "No students found for $selectedClass."
                    }
                } else {
                    loadError = json.optString("error", "Failed to fetch student list.")
                }
            } else {
                loadError = "Unable to reach student server (Timeout/Network error)."
            }
        } catch (e: Exception) {
            loadError = "Error loading students: ${e.localizedMessage}"
        } finally {
            isLoadingStudents = false
        }
    }

    LaunchedEffect(selectedClass, subject, examName) {
        if (subject.isNotBlank() && examName.isNotBlank()) {
            FirebaseFirestore.getInstance().collection("exam_results")
                .whereEqualTo("standard", selectedClass)
                .whereEqualTo("subject", subject.trim())
                .whereEqualTo("examName", examName.trim())
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { doc ->
                        val rollNo = doc.getString("rollNo") ?: ""
                        val obtained = doc.get("obtainedMarks")?.toString() ?: ""
                        val max = doc.get("maxMarks")?.toString() ?: ""
                        if (rollNo.isNotBlank()) {
                            marksMap[rollNo] = obtained
                            if (max.isNotBlank()) {
                                maxMarksInput = max
                            }
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Enter Examination Results", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF673AB7)))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Standard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    IndiumOutlinedButton(text = selectedClass, onClick = { showClassMenu = true })
                    DropdownMenu(expanded = showClassMenu, onDismissRequest = { showClassMenu = false }) {
                        classList.forEach { cls ->
                            DropdownMenuItem(text = { Text(cls) }, onClick = { selectedClass = cls; showClassMenu = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g., Mathematics)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Examination Name (e.g., Final Term - 2026)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = maxMarksInput,
                    onValueChange = { newVal ->
                        if (newVal.all { c -> c.isDigit() || c == '.' } || newVal.isEmpty()) {
                            maxMarksInput = newVal
                        }
                    },
                    label = { Text("Maximum Marks (Common for all students)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Students List & Marks Entry", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF252238)))
            IconButton(
                onClick = { if (!isLoadingStudents) retryTrigger++ },
                enabled = !isLoadingStudents
            ) {
                if (isLoadingStudents) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF7C4DFF), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Students", tint = Color(0xFF7C4DFF))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoadingStudents) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else if (loadError.isNotEmpty()) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Unable to refresh. Please try again.", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = loadError, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    IndiumButton(
                        text = "RETRY",
                        onClick = { retryTrigger++ },
                        containerColor = Color(0xFF7C4DFF)
                    )
                }
            }
        } else if (sheetStudents.isEmpty()) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "No students found for $selectedClass.", modifier = Modifier.padding(16.dp), color = Color.Gray)
            }
        } else {
            sheetStudents.forEach { student ->
                val obtainedVal = marksMap[student.rollNo] ?: ""
                IndiumCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    containerColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${student.rollNo}. ${student.studentName}", fontWeight = FontWeight.Bold, color = Color(0xFF252238))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = obtainedVal,
                            onValueChange = { newVal ->
                                if (newVal.all { c -> c.isDigit() || c == '.' } || newVal.isEmpty()) {
                                    marksMap[student.rollNo] = newVal
                                }
                            },
                            label = { Text("Obtained Marks") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(160.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        IndiumButton(
            text = if (isSaving) "SAVING..." else "SAVE RESULTS",
            onClick = {
                if (subject.isBlank()) {
                    saveMessage = "❌ Please enter a subject name."
                    return@IndiumButton
                }
                if (examName.isBlank()) {
                    saveMessage = "❌ Please enter an examination name."
                    return@IndiumButton
                }

                val maxMarksVal = maxMarksInput.trim().toDoubleOrNull()
                if (maxMarksVal == null || maxMarksVal <= 0) {
                    saveMessage = "❌ Please enter a valid positive Maximum Marks value."
                    return@IndiumButton
                }

                var hasError = false
                var errorMsg = ""
                var enteredCount = 0
                sheetStudents.forEach { student ->
                    val obtainedStr = marksMap[student.rollNo]?.trim() ?: ""
                    if (obtainedStr.isNotEmpty()) {
                        enteredCount++
                        val obtained = obtainedStr.toDoubleOrNull()
                        if (obtained == null || obtained < 0) {
                            hasError = true
                            errorMsg = "❌ Obtained marks for ${student.studentName} cannot be negative."
                        } else if (obtained > maxMarksVal) {
                            hasError = true
                            errorMsg = "❌ Obtained marks for ${student.studentName} ($obtained) cannot exceed Maximum Marks ($maxMarksVal)."
                        }
                    }
                }

                if (enteredCount == 0) {
                    saveMessage = "❌ Please enter marks for at least one student before saving records."
                    return@IndiumButton
                }

                if (hasError) {
                    saveMessage = errorMsg
                    return@IndiumButton
                }

                isSaving = true
                saveMessage = "⏳ Saving results to Google Sheets..."

                scope.launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            val connection = URL(scriptUrl).openConnection() as HttpURLConnection
                            connection.requestMethod = "POST"
                            connection.doOutput = true
                            connection.setRequestProperty("Content-Type", "application/json")
                            connection.instanceFollowRedirects = true

                            val requestJson = JSONObject().apply {
                                put("action", "saveResults")
                                put("standard", selectedClass)
                                put("subject", subject.trim())
                                put("examName", examName.trim())
                                put("teacherName", CurrentUser.name)

                                val resultsArray = JSONArray()
                                sheetStudents.forEach { student ->
                                    val obtainedStr = marksMap[student.rollNo]?.trim() ?: ""
                                    if (obtainedStr.isNotEmpty()) {
                                        val obtained = obtainedStr.toDoubleOrNull() ?: 0.0

                                        val obj = JSONObject().apply {
                                            put("rollNo", student.rollNo)
                                            put("studentName", student.studentName)
                                            put("board", student.board)
                                            put("obtainedMarks", obtained)
                                            put("maxMarks", maxMarksVal)
                                        }
                                        resultsArray.put(obj)
                                    }
                                }
                                put("results", resultsArray)
                            }

                            connection.outputStream.bufferedWriter().use { it.write(requestJson.toString()) }

                            val code = connection.responseCode
                            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                            stream?.bufferedReader()?.use { it.readText() }
                        }

                        withContext(Dispatchers.Main) {
                            if (response != null) {
                                val json = JSONObject(response)
                                if (json.optBoolean("success")) {
                                    val count = json.optInt("savedCount", 0)
                                    saveMessage = "✅ Successfully saved results for $count students!"
                                    delay(1200)
                                    onSavedSuccess()
                                } else {
                                    saveMessage = "❌ Server error: ${json.optString("error", "Failed to save")}"
                                }
                            } else {
                                saveMessage = "❌ Server error: No response received."
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            saveMessage = "❌ Error: ${e.message}"
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            isSaving = false
                        }
                    }
                }
            },
            enabled = !isSaving && sheetStudents.isNotEmpty(),
            containerColor = Color(0xFF673AB7)
        )

        if (saveMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = saveMessage, fontWeight = FontWeight.Bold, color = if (saveMessage.startsWith("✅")) Color(0xFF4CAF50) else Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))
        IndiumOutlinedButton(text = "BACK TO MENU", onClick = onBack)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ResultSubjectItem(subject: String, obtained: String, total: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = subject, fontWeight = FontWeight.Medium, color = Color(0xFF252238))
        Text(text = "$obtained / $total", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
    }
}

@Composable
fun TeacherResultsHistoryScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val scriptUrl = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec"

    var selectedSubject by remember { mutableStateOf("All Subjects") }
    var showSubjectMenu by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var allResults by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var availableSubjects by remember { mutableStateOf<List<String>>(listOf("All Subjects")) }

    fun loadHistory(isRefresh: Boolean = false) {
        if (isRefresh) refreshing = true else isLoading = true
        errorMessage = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "$scriptUrl?action=getTeacherResults"
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.instanceFollowRedirects = true

                if (connection.responseCode == 200) {
                    val res = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(res)
                    if (json.optBoolean("success")) {
                        val arr = json.optJSONArray("results") ?: JSONArray()
                        val list = mutableListOf<Map<String, Any>>()
                        val subjectsSet = mutableSetOf<String>()
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONObject(i)
                            val map = mutableMapOf<String, Any>()
                            val sub = item.optString("subject").trim()
                            if (sub.isNotBlank()) subjectsSet.add(sub)

                            map["timestamp"] = item.optString("timestamp")
                            map["standard"] = item.optString("standard")
                            map["studentName"] = item.optString("studentName")
                            map["rollNo"] = item.optString("rollNo")
                            map["subject"] = sub
                            map["examName"] = "Weekly Class Test"
                            map["obtainedMarks"] = item.optDouble("obtainedMarks", 0.0)
                            map["maxMarks"] = item.optDouble("maxMarks", 100.0)
                            list.add(map)
                        }
                        withContext(Dispatchers.Main) {
                            allResults = list
                            availableSubjects = listOf("All Subjects") + subjectsSet.sorted()
                            isLoading = false
                            refreshing = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Unable to refresh. Please try again."
                            isLoading = false
                            refreshing = false
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Unable to refresh. Please try again."
                        isLoading = false
                        refreshing = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Unable to refresh. Please try again."
                    isLoading = false
                    refreshing = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadHistory()
    }

    val filteredResults = if (selectedSubject == "All Subjects") {
        allResults
    } else {
        allResults.filter { it["subject"]?.toString().equals(selectedSubject, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFF7C4DFF),
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Results History", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(text = "Review saved examination records", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(
                    onClick = { if (!refreshing) loadHistory(true) },
                    enabled = !refreshing
                ) {
                    if (refreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh History", tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Name Dropdown Filter Only
        IndiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Filter by Subject", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF673AB7))
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    IndiumOutlinedButton(text = selectedSubject, onClick = { showSubjectMenu = true })
                    DropdownMenu(expanded = showSubjectMenu, onDismissRequest = { showSubjectMenu = false }) {
                        availableSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    selectedSubject = sub
                                    showSubjectMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else if (errorMessage.isNotEmpty()) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Unable to refresh. Please try again.", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    IndiumButton(
                        text = "RETRY",
                        onClick = { loadHistory(true) },
                        containerColor = Color(0xFF7C4DFF)
                    )
                }
            }
        } else if (filteredResults.isEmpty()) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "No saved examination results found for $selectedSubject.", modifier = Modifier.padding(16.dp), color = Color.Gray)
            }
        } else {
            filteredResults.forEach { record ->
                val obtained = record["obtainedMarks"]?.toString()?.toDoubleOrNull() ?: 0.0
                val max = record["maxMarks"]?.toString()?.toDoubleOrNull() ?: 100.0
                val percentage = if (max > 0) (obtained / max * 100).toInt() else 0

                IndiumCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), containerColor = Color.White) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Roll No: ${record["rollNo"]} • ${record["studentName"]}", fontWeight = FontWeight.Bold, color = Color(0xFF252238))
                            Surface(color = Color(0xFF7C4DFF).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = record["standard"]?.toString() ?: "",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF7C4DFF),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Exam: Weekly Class Test • Subject: ${record["subject"]}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = "Obtained Marks: ${obtained.toInt()} / ${max.toInt()}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF252238))
                            }
                            Text(
                                text = "$percentage%",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF673AB7),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        IndiumOutlinedButton(text = "BACK TO MENU", onClick = onBack)
        Spacer(modifier = Modifier.height(24.dp))
    }
}


// ======================================================
// NOTICES
// ======================================================

@Composable
fun NoticesScreen(
    onBack: () -> Unit
) {
    NoticeBoardScreen(
        userRole = CurrentUser.role,
        userName = CurrentUser.name,
        onBack = onBack
    )
}


// ======================================================
// STUDY MATERIAL
// ======================================================

@Composable
fun StudyMaterialScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFF7C4DFF),
            contentColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Study Material", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = "Access all your resources here", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        StudyMaterialItem("Mathematics", "Algebra, Geometry, Calculus", Icons.Default.SquareFoot)
        StudyMaterialItem("Science", "Physics, Chemistry, Biology", Icons.Default.Science)
        StudyMaterialItem("English", "Grammar, Literature, Writing", Icons.Default.MenuBook)

        Spacer(modifier = Modifier.height(32.dp))
        IndiumButton(text = "BACK", onClick = onBack)
    }
}

@Composable
fun StudyMaterialItem(subject: String, description: String, icon: ImageVector) {
    IndiumCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        containerColor = Color.White
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0E6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF7C4DFF))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = subject, fontWeight = FontWeight.Bold, color = Color(0xFF252238))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color(0xFFE6DDFB), modifier = Modifier.size(16.dp))
        }
    }
}


// ======================================================
// PROFILE
// ======================================================

@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Student Information",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C4DFF)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileInfoItem("Name", CurrentUser.name)
                ProfileInfoItem("Class", CurrentUser.className)
                ProfileInfoItem("Board", CurrentUser.board)
                ProfileInfoItem("Roll Number", CurrentUser.rollNo)
                ProfileInfoItem("Academic Year", "2026–27")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        IndiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Personal Details",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C4DFF)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileInfoItem("Mobile", CurrentUser.mobile)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        IndiumButton(
            text = "BACK TO DASHBOARD",
            onClick = onBack,
            containerColor = Color(0xFF673AB7)
        )
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF686477)
            )
        )
        Text(
            text = value.ifBlank { "Not provided" },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = Color(0xFF252238)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFFE6DDFB), thickness = 0.5.dp)
    }
}


// ======================================================
// TEACHER STUDENTS
// ======================================================
@Composable
fun TeacherAttendanceScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val teacherName = CurrentUser.name
    val mobile = CurrentUser.mobile
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var inTime by remember { mutableStateOf("") }
    var outTime by remember { mutableStateOf("") }
    var attendanceStatus by remember { mutableStateOf("") }
    
    var showHistory by remember { mutableStateOf(false) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf("") }
    var historyList by remember { mutableStateOf<List<TeacherAttendanceRecord>>(emptyList()) }

    val attendanceUrl = "https://script.google.com/macros/s/AKfycbx3vXqB5Vs6DToJp5ArnnbuIGIvBzGwcLJFFUWtDrlBrD7dqLcRj7u89xNrskwPjrgu/exec"

    suspend fun loadAttendance() {
        if (mobile.isBlank()) {
            message = "Teacher mobile number not found"
            return
        }
        loading = true
        try {
            val encodedMobile = URLEncoder.encode(mobile, "UTF-8")
            val response = withContext(Dispatchers.IO) {
                val connection = URL("$attendanceUrl?action=check&mobile=$encodedMobile").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                try {
                    val code = connection.responseCode
                    if (code == 200) connection.inputStream.bufferedReader().use { it.readText() } else null
                } finally {
                    connection.disconnect()
                }
            }
            if (response != null) {
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    attendanceStatus = json.optString("status", "")
                    inTime = json.optString("inTime", "")
                    outTime = json.optString("outTime", "")
                }
            }
        } catch (e: Exception) {
            message = "Update failed: ${e.message}"
        } finally {
            loading = false
        }
    }

    suspend fun loadHistory() {
        if (mobile.isBlank()) {
            historyError = "Teacher identification missing"
            return
        }
        historyLoading = true
        historyError = ""
        try {
            val encodedMobile = URLEncoder.encode(mobile, "UTF-8")
            val urlString = "$attendanceUrl?action=history&mobile=$encodedMobile"
            
            val response = withContext(Dispatchers.IO) {
                // Return to a very basic HttpURLConnection logic, similar to working loadAttendance()
                // GAS history is currently failing with 404/Server unreachable.
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true
                
                try {
                    val code = connection.responseCode
                    if (code == 200) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else if (code == 302 || code == 307) {
                        // GAS redirect manual fallback
                        val loc = connection.getHeaderField("Location")
                        if (loc != null) {
                            val conn2 = URL(loc).openConnection() as HttpURLConnection
                            conn2.requestMethod = "GET"
                            if (conn2.responseCode == 200) {
                                conn2.inputStream.bufferedReader().use { it.readText() }
                            } else {
                                "HTTP_${conn2.responseCode}"
                            }
                        } else {
                            "HTTP_$code"
                        }
                    } else {
                        "HTTP_$code"
                    }
                } catch (e: Exception) {
                    "ERR_${e.message}"
                } finally {
                    connection.disconnect()
                }
            }
            
            if (response != null && !response.startsWith("HTTP_") && !response.startsWith("ERR_")) {
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    val arr = json.optJSONArray("records") ?: json.optJSONArray("history") ?: JSONArray()
                    val result = mutableListOf<TeacherAttendanceRecord>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        result.add(TeacherAttendanceRecord(
                            date = obj.optString("date"),
                            teacherName = obj.optString("teacherName"),
                            inTime = obj.optString("inTime"),
                            outTime = obj.optString("outTime"),
                            status = obj.optString("status")
                        ))
                    }
                    historyList = result.reversed()
                } else {
                    historyError = json.optString("error", "The backend script does not support the 'history' action yet.")
                }
            } else {
                historyError = if (response?.startsWith("HTTP_404") == true) {
                    "Error 404: Attendance history endpoint not found. Please verify the 'history' action is deployed in your Apps Script."
                } else {
                    "Server Error: ${response ?: "Unknown error occurred"}"
                }
            }
        } catch (e: Exception) {
            historyError = "Connection error: ${e.message}"
        } finally {
            historyLoading = false
        }
    }

    suspend fun markAttendance(action: String) {
        loading = true
        try {
            val response = withContext(Dispatchers.IO) {
                val connection = URL(attendanceUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                val requestJson = JSONObject().apply {
                    put("action", action)
                    put("mobile", mobile)
                }
                connection.outputStream.bufferedWriter().use { it.write(requestJson.toString()) }
                try {
                    val code = connection.responseCode
                    if (code == 200) connection.inputStream.bufferedReader().use { it.readText() } else null
                } finally {
                    connection.disconnect()
                }
            }
            if (response != null) {
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    message = json.optString("message", "Success")
                    loadAttendance()
                } else {
                    message = json.optString("error", "Failed")
                }
            }
        } catch (e: Exception) {
            message = "Error: ${e.message}"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadAttendance() }

    if (showHistory) {
        Scaffold(
            topBar = {
                IndiumTopBar(title = "My Attendance History", onBack = { showHistory = false })
            },
            containerColor = Color(0xFFF7F4FF)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (historyLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    }
                } else if (historyError.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = historyError, color = Color.Red, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            IndiumButton(text = "RETRY", onClick = { scope.launch { loadHistory() } })
                        }
                    }
                } else if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No previous attendance records found.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(historyList) { record ->
                            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = record.date,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF252238)
                                        )
                                        Surface(
                                            color = if (record.status.contains("PRESENT", true)) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = record.status,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (record.status.contains("PRESENT", true)) Color(0xFF4CAF50) else Color.Red,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "IN Time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text(text = record.inTime.ifBlank { "--:--" }, fontWeight = FontWeight.Medium)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "OUT Time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text(text = record.outTime.ifBlank { "--:--" }, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Load history once when showing
        LaunchedEffect(showHistory) {
            if (showHistory && historyList.isEmpty()) {
                loadHistory()
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFF7C4DFF),
            contentColor = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Mark Attendance", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = "Teacher: $teacherName", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AttendanceStatItem("Status", attendanceStatus.ifBlank { "ABSENT" })
                    AttendanceStatItem("IN", inTime.ifBlank { "--:--" })
                    AttendanceStatItem("OUT", outTime.ifBlank { "--:--" })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        IndiumButton(
            text = "MARK IN / PRESENT",
            onClick = { scope.launch { markAttendance("IN") } },
            enabled = !loading && attendanceStatus.isBlank(),
            containerColor = Color(0xFF7C4DFF)
        )
        Spacer(modifier = Modifier.height(12.dp))
        IndiumButton(
            text = "MARK OUT",
            onClick = { scope.launch { markAttendance("OUT") } },
            enabled = !loading && attendanceStatus == "PRESENT" && outTime.isBlank(),
            containerColor = Color(0xFF673AB7)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        IndiumOutlinedButton(
            text = "My Attendance History",
            onClick = { showHistory = true }
        )

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = message, modifier = Modifier.padding(16.dp), color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        IndiumOutlinedButton(text = "BACK", onClick = onBack)
    }
}

// ======================================================
// STUDENT ATTENDANCE FOR TEACHER
// ======================================================

// ======================================================
// STUDENT ATTENDANCE FOR TEACHER
// ======================================================

data class SheetStudent(
    val rollNo: String,
    val studentName: String,
    val mobileNo: String,
    val standard: String,
    val board: String
)


@Composable
fun AttendanceHistoryScreen(
    selectedClass: String,
    students: List<SheetStudent>,
    preferences: SharedPreferences,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    var selectedHistoryDate by remember {
        mutableStateOf("")
    }

    var selectedStudentName by remember {
        mutableStateOf("")
    }

    var showStudentMenu by remember {
        mutableStateOf(false)
    }

    var historyMode by remember {
        mutableStateOf("DATE")
    }


    // ---------------------------------------------
    // SAVED DATES
    // ---------------------------------------------

    val savedDates =
        preferences
            .getStringSet(
                "attendance_dates_$selectedClass",
                emptySet()
            )
            ?.toList()
            ?: emptyList()


    val sortedDates =
        savedDates.sortedByDescending { dateString ->

            try {

                SimpleDateFormat(
                    "d MMMM yyyy",
                    Locale.ENGLISH
                ).parse(dateString)?.time ?: 0L

            } catch (e: Exception) {

                0L
            }
        }


    // ---------------------------------------------
    // SELECT FIRST DATE
    // ---------------------------------------------

    LaunchedEffect(sortedDates) {

        if (
            selectedHistoryDate.isEmpty() &&
            sortedDates.isNotEmpty()
        ) {

            selectedHistoryDate =
                sortedDates.first()
        }
    }


    // ---------------------------------------------
    // CURRENT MONTH DATES ONLY
    // ---------------------------------------------

    val currentMonthDates =
        sortedDates.filter { dateString ->

            try {

                val date =
                    SimpleDateFormat(
                        "d MMMM yyyy",
                        Locale.ENGLISH
                    ).parse(dateString)

                val calendar =
                    Calendar.getInstance()

                calendar.time = date!!

                val today =
                    Calendar.getInstance()

                calendar.get(
                    Calendar.MONTH
                ) ==
                        today.get(
                            Calendar.MONTH
                        ) &&

                        calendar.get(
                            Calendar.YEAR
                        ) ==
                        today.get(
                            Calendar.YEAR
                        )

            } catch (e: Exception) {

                false
            }
        }


    // ---------------------------------------------
    // DATE-WISE CALCULATION
    // ---------------------------------------------

    val isDateAttendanceAvailable =
        savedDates.contains(selectedHistoryDate)

    val dateAttendance =
        if (isDateAttendanceAvailable) {

            students.map { student ->

                val key =
                    "$selectedClass-$selectedHistoryDate-${student.rollNo}-${student.studentName}"

                val present =
                    preferences.getBoolean(
                        key,
                        true
                    )
            student to present
        }
        } else {

            emptyList()
        }

    val datePresentCount =
        dateAttendance.count {
            it.second
        }


    val dateAbsentCount =
        dateAttendance.size -
                datePresentCount


    val datePercentage =
        if (dateAttendance.isNotEmpty()) {

            (datePresentCount * 100) /
                    dateAttendance.size

        } else {

            0
        }


    // ---------------------------------------------
    // STUDENT-WISE CALCULATION
    // ---------------------------------------------

    val selectedStudent =
        students.find {
            it.studentName ==
                    selectedStudentName
        }


    val studentHistory =
        if (selectedStudent != null) {

            currentMonthDates.map { dateString ->

                val key =
                    "$selectedClass-$dateString-${selectedStudent.rollNo}-${selectedStudent.studentName}"

                val present =
                    preferences.getBoolean(
                        key,
                        true
                    )

                dateString to present
            }

        } else {

            emptyList()
        }


    val studentPresentCount =
        studentHistory.count {
            it.second
        }


    val studentAbsentCount =
        studentHistory.size -
                studentPresentCount


    val studentPercentage =
        if (studentHistory.isNotEmpty()) {

            (studentPresentCount * 100) /
                    studentHistory.size

        } else {

            0
        }


    // =============================================
    // UI
    // =============================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
    ) {


        // -----------------------------------------
        // TITLE
        // -----------------------------------------

        Text(
            text = "📅 Attendance History",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(15.dp)
        )


        // -----------------------------------------
        // MODE BUTTONS
        // -----------------------------------------

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    historyMode = "DATE"
                },

                modifier =
                    Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndiumDeepViolet,
                    contentColor = IndiumWhite
                )
            ) {

                Text("📅 DATE")
            }


            Button(
                onClick = {
                    historyMode = "STUDENT"
                },

                modifier =
                    Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndiumDeepViolet,
                    contentColor = IndiumWhite
                )
            ) {

                Text("👤 STUDENT")
            }
        }


        Spacer(
            modifier =
                Modifier.height(15.dp)
        )


        // =========================================
        // DATE-WISE HISTORY
        // =========================================

        if (historyMode == "DATE") {

            Text(
                text = "Select Date",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedButton(
                onClick = {

                    val calendar = Calendar.getInstance()

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->

                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.set(year, month, dayOfMonth)

                            selectedHistoryDate = SimpleDateFormat(
                                "d MMMM yyyy",
                                Locale.ENGLISH
                            ).format(selectedCalendar.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    if (selectedHistoryDate.isNotEmpty())
                        "📅 $selectedHistoryDate"
                    else
                        "📅 Select Date"
                )
            }

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            // -------------------------------------
            // SUMMARY
            // -------------------------------------

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = IndiumLavender
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${dateAttendance.size}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndiumWhite
                        )
                        Text("Total", color = IndiumWhite)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$datePresentCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndiumWhite
                        )
                        Text("Present", color = IndiumWhite)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$dateAbsentCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndiumWhite
                        )
                        Text("Absent", color = IndiumWhite)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$datePercentage%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndiumWhite
                        )
                        Text("Attendance", color = IndiumWhite)
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            // -------------------------------------
            // STUDENT LIST FOR SELECTED DATE
            // -------------------------------------

            if (!isDateAttendanceAvailable) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "⚠ Attendance Not Found",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {

                    items(dateAttendance) { item ->

                        val student = item.first
                        val present = item.second

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text =
                                        "${student.rollNo}. ${student.studentName}",

                                    fontSize = 16.sp,

                                    fontWeight =
                                        FontWeight.Medium
                                )

                                Text(
                                    text =
                                        if (present)
                                            "✓ PRESENT"
                                        else
                                            "✗ ABSENT",

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }


        // =========================================
        // STUDENT-WISE HISTORY
        // =========================================

        else {

            Text(
                text = "Select Student",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(

                    onClick = {
                        showStudentMenu = true
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        if (
                            selectedStudentName
                                .isNotEmpty()
                        )
                            selectedStudentName
                        else
                            "Select Student"
                    )
                }


                DropdownMenu(

                    expanded =
                        showStudentMenu,

                    onDismissRequest = {
                        showStudentMenu = false
                    }
                ) {

                    students.forEach {
                            student ->

                        DropdownMenuItem(

                            text = {
                                Text(
                                    "${student.rollNo}. ${student.studentName}"
                                )
                            },

                            onClick = {

                                selectedStudentName =
                                    student.studentName

                                showStudentMenu =
                                    false
                            }
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )


            // -------------------------------------
            // STUDENT SUMMARY
            // -------------------------------------

            if (
                selectedStudentName.isNotEmpty()
            ) {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = IndiumLavender
                    )
                ) {

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),

                        horizontalArrangement =
                            Arrangement.SpaceEvenly
                    ) {

                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                "${studentHistory.size}",
                                fontSize = 20.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color = IndiumWhite
                            )

                            Text("Days", color = IndiumWhite)
                        }


                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                "$studentPresentCount",
                                fontSize = 20.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color = IndiumWhite
                            )

                            Text("Present", color = IndiumWhite)
                        }


                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                "$studentAbsentCount",
                                fontSize = 20.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color = IndiumWhite
                            )

                            Text("Absent", color = IndiumWhite)
                        }


                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                "$studentPercentage%",
                                fontSize = 20.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color = IndiumWhite
                            )

                            Text("Attendance", color = IndiumWhite)
                        }
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )


                // ---------------------------------
                // MONTHLY STUDENT LIST
                // ---------------------------------

                LazyColumn(

                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                ) {

                    items(studentHistory) {
                            item ->

                        val dateString =
                            item.first

                        val present =
                            item.second


                        Card(

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 4.dp
                                    )
                        ) {

                            Row(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    dateString,
                                    fontSize = 16.sp
                                )


                                Text(
                                    if (present)
                                        "✓ PRESENT"
                                    else
                                        "✗ ABSENT",

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
                }

            } else {

                Text(
                    text =
                        "Please select a student",

                    fontSize = 17.sp
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        // -----------------------------------------
        // BACK BUTTON
        // -----------------------------------------

        OutlinedButton(

            onClick = onBack,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text("← Back")
        }
    }
}


@Composable
fun StudentAttendanceForTeacherScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scriptUrl = "https://script.google.com/macros/s/AKfycbwbBEeUDm0gY_mCuPUJC04sw-O1aWlTTGbyu-x4yhl-BOLbUIoHD4cqWuuS_pNKRSCi/exec"
    val classList = listOf("1st Standard", "2nd Standard", "3rd Standard", "4th Standard", "5th Standard", "6th Standard", "7th Standard", "8th Standard", "9th Standard", "10th Standard")

    var selectedClass by remember { mutableStateOf("10th Standard") }
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date())) }
    var showClassMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var sheetStudents by remember { mutableStateOf<List<SheetStudent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }

    val preferences = remember { context.getSharedPreferences("student_attendance", Context.MODE_PRIVATE) }
    val attendance = remember { mutableStateMapOf<String, Boolean>() }

    if (showHistory) {
        AttendanceHistoryScreen(
            selectedClass = selectedClass,
            students = sheetStudents,
            preferences = preferences,
            onBack = { showHistory = false }
        )
        return
    }

    LaunchedEffect(selectedClass) {
        isLoading = true
        loadError = ""
        try {
            val standardNumber = selectedClass.filter { it.isDigit() }
            val result = withContext(Dispatchers.IO) {
                val connection = URL(scriptUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 20000
                connection.readTimeout = 20000
                if (connection.responseCode == 200) connection.inputStream.bufferedReader().use { it.readText() } else null
            }
            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success")) {
                    val studentsArray = json.getJSONArray("students")
                    val loadedStudents = mutableListOf<SheetStudent>()
                    for (i in 0 until studentsArray.length()) {
                        val student = studentsArray.getJSONObject(i)
                        val standard = student.optString("standard").trim()
                        if (standard.filter { it.isDigit() } == standardNumber) {
                            loadedStudents.add(SheetStudent(
                                rollNo = student.optString("rollNo").trim(),
                                studentName = student.optString("studentName").trim(),
                                mobileNo = "", // Privacy: Do not fetch student mobile numbers for teachers
                                standard = standard,
                                board = student.optString("board").trim()
                            ))
                        }
                    }
                    sheetStudents = loadedStudents.sortedBy { it.rollNo.filter { c -> c.isDigit() }.toIntOrNull() ?: 99 }
                    attendance.clear()
                    sheetStudents.forEach { student ->
                        val key = "$selectedClass-$selectedDate-${student.rollNo}-${student.studentName}"
                        attendance[student.studentName] = preferences.getBoolean(key, true)
                    }
                }
            }
        } catch (e: Exception) {
            loadError = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            IndiumTopBar(title = "Student Attendance", onBack = onBack)
        },
        containerColor = Color(0xFFF7F4FF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            IndiumOutlinedButton(text = selectedClass, onClick = { showClassMenu = true })
                            DropdownMenu(expanded = showClassMenu, onDismissRequest = { showClassMenu = false }) {
                                classList.forEach { className ->
                                    DropdownMenuItem(text = { Text(className) }, onClick = { selectedClass = className; showClassMenu = false })
                                }
                            }
                        }
                        IndiumOutlinedButton(text = selectedDate, onClick = { showDatePicker = true }, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search Student") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IndiumButton(text = "ALL PRESENT", onClick = { sheetStudents.forEach { attendance[it.studentName] = true } }, modifier = Modifier.weight(1f), containerColor = Color(0xFF4CAF50))
                IndiumButton(text = "ALL ABSENT", onClick = { sheetStudents.forEach { attendance[it.studentName] = false } }, modifier = Modifier.weight(1f), containerColor = Color(0xFFF44336))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                val filtered = sheetStudents.filter { it.studentName.contains(searchText, ignoreCase = true) }
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { student ->
                        val isPresent = attendance[student.studentName] ?: true
                        IndiumCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${student.rollNo}. ${student.studentName}", fontWeight = FontWeight.Bold)
                                }
                                Switch(checked = isPresent, onCheckedChange = { attendance[student.studentName] = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF4CAF50)))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            IndiumButton(text = "SAVE ATTENDANCE", onClick = {
                val editor = preferences.edit()
                sheetStudents.forEach { student ->
                    val key = "$selectedClass-$selectedDate-${student.rollNo}-${student.studentName}"
                    editor.putBoolean(key, attendance[student.studentName] ?: true)
                }
                val savedDates = preferences.getStringSet("attendance_dates_$selectedClass", emptySet())?.toMutableSet() ?: mutableSetOf()
                savedDates.add(selectedDate)
                editor.putStringSet("attendance_dates_$selectedClass", savedDates).apply()
                saveMessage = "✅ Saved Successfully!"
            }, containerColor = Color(0xFF673AB7))
            
            TextButton(onClick = { showHistory = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("VIEW HISTORY", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold) }
            
            if (saveMessage.isNotEmpty()) {
                Text(text = saveMessage, modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d)
            selectedDate = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(cal.time)
            showDatePicker = false
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}

fun addTeacherToFirebase(
    name: String,
    mobile: String,
    temporaryPassword: String,
    onResult: (Boolean, String) -> Unit
) {

    val db = FirebaseFirestore.getInstance()

    if (name.isBlank() ||
        mobile.isBlank() ||
        temporaryPassword.isBlank()
    ) {
        onResult(false, "Please fill all fields")
        return
    }

    db.collection("users")
        .whereEqualTo("mobile", mobile)
        .get()
        .addOnSuccessListener { documents ->

            if (!documents.isEmpty) {

                onResult(
                    false,
                    "A user with this mobile number already exists"
                )

                return@addOnSuccessListener
            }

            val teacher = hashMapOf(
                "name" to name.trim(),
                "mobile" to mobile.trim(),
                "password" to temporaryPassword,
                "role" to "Teacher",
                "firstLogin" to true
            )

            db.collection("users")
                .add(teacher)
                .addOnSuccessListener {

                    onResult(
                        true,
                        "Teacher account created successfully"
                    )
                }
                .addOnFailureListener { error ->

                    onResult(
                        false,
                        error.message
                            ?: "Failed to create teacher"
                    )
                }
        }
        .addOnFailureListener { error ->

            onResult(
                false,
                error.message
                    ?: "Unable to check mobile number"
            )
        }
}
@Composable
fun AddTeacherScreen(
    onBack: () -> Unit
) {
    var teacherName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var temporaryPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IndiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add New Teacher",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Teacher Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = temporaryPassword,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) temporaryPassword = it },
                    label = { Text("Temporary Password (4 Digits)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                IndiumButton(
                    text = if (isSaving) "CREATING..." else "CREATE TEACHER ACCOUNT",
                    onClick = {
                        if (temporaryPassword.length != 4) { message = "Password must be 4 digits"; return@IndiumButton }
                        isSaving = true
                        addTeacherToFirebase(teacherName, mobile, temporaryPassword) { success, res ->
                            isSaving = false
                            message = res
                            if (success) { teacherName = ""; mobile = ""; temporaryPassword = "" }
                        }
                    },
                    enabled = !isSaving,
                    containerColor = Color(0xFF7C4DFF)
                )
            }
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = message, modifier = Modifier.padding(16.dp), color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        IndiumOutlinedButton(text = "BACK", onClick = onBack)
    }
}

fun addStudentToFirebase(
    name: String,
    mobile: String,
    className: String,
    division: String,
    rollNo: String,
    board: String,
    username: String,
    temporaryPassword: String,
    onResult: (Boolean, String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    if (name.isBlank() || mobile.isBlank() || className.isBlank() || division.isBlank() || temporaryPassword.isBlank() || username.isBlank()) {
        onResult(false, "Please fill all required fields")
        return
    }

    // Check if username exists (following SignupScreen logic)
    db.collection("users").document(username.trim()).get()
        .addOnSuccessListener { doc ->
            if (doc.exists()) {
                onResult(false, "Username already exists")
            } else {
                val student = hashMapOf(
                    "name" to name.trim(),
                    "mobile" to mobile.trim(),
                    "class" to className.trim(),
                    "division" to division.trim(),
                    "rollNo" to rollNo.trim(),
                    "board" to board.trim(),
                    "username" to username.trim(),
                    "password" to temporaryPassword,
                    "role" to "Student"
                )

                db.collection("users").document(username.trim()).set(student)
                    .addOnSuccessListener {
                        onResult(true, "Student account created successfully")
                    }
                    .addOnFailureListener { error ->
                        onResult(false, error.message ?: "Failed to create student")
                    }
            }
        }
        .addOnFailureListener { error ->
            onResult(false, error.message ?: "Unable to check username")
        }
}

@Composable
fun AddStudentScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedOption by remember { mutableStateOf("EXISTING") } // "EXISTING" or "NEW"
    
    // Shared state
    var studentName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var temporaryPassword by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    
    // Option 1: Existing Student state
    var selectedClassForExisting by remember { mutableStateOf("10th Standard") }
    var showClassMenu by remember { mutableStateOf(false) }
    var sheetStudents by remember { mutableStateOf<List<SheetStudent>>(emptyList()) }
    var isLoadingStudents by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val scriptUrl = "https://script.google.com/macros/s/AKfycbwbBEeUDm0gY_mCuPUJC04sw-O1aWlTTGbyu-x4yhl-BOLbUIoHD4cqWuuS_pNKRSCi/exec"

    val classList = listOf(
        "1st Standard", "2nd Standard", "3rd Standard", "4th Standard", "5th Standard",
        "6th Standard", "7th Standard", "8th Standard", "9th Standard", "10th Standard"
    )

    // Load students from sheet when class changes
    LaunchedEffect(selectedClassForExisting, selectedOption) {
        if (selectedOption == "EXISTING") {
            isLoadingStudents = true
            try {
                val standardNumber = selectedClassForExisting.filter { it.isDigit() }
                val result = withContext(Dispatchers.IO) {
                    val connection = URL(scriptUrl).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 15000
                    connection.instanceFollowRedirects = true
                    connection.inputStream.bufferedReader().use { it.readText() }
                }
                val json = JSONObject(result)
                if (json.optBoolean("success", false)) {
                    val array = json.getJSONArray("students")
                    val loaded = mutableListOf<SheetStudent>()
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val std = item.optString("standard")
                        if (std.filter { it.isDigit() } == standardNumber) {
                            loaded.add(SheetStudent(
                                rollNo = item.optString("rollNo"),
                                studentName = item.optString("studentName"),
                                mobileNo = item.optString("mobileNo"),
                                standard = std,
                                board = item.optString("board")
                            ))
                        }
                    }
                    sheetStudents = loaded.sortedBy { it.rollNo.filter { c -> c.isDigit() }.toIntOrNull() ?: 999 }
                }
            } catch (e: Exception) {
                message = "Error loading students: ${e.message}"
            }
            isLoadingStudents = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(text = "Add Student Login", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        // Options Toggle
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { selectedOption = "EXISTING"; message = "" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedOption == "EXISTING") MaterialTheme.colorScheme.primary else Color.LightGray
                )
            ) { Text("EXISTING STUDENT") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { selectedOption = "NEW"; message = ""; studentName = ""; mobile = ""; className = ""; division = ""; rollNo = ""; board = ""; username = "" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedOption == "NEW") MaterialTheme.colorScheme.primary else Color.LightGray
                )
            ) { Text("NEW STUDENT") }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedOption == "EXISTING") {
            // Existing Student selection UI
            Text("1. Select Class", fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { showClassMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedClassForExisting)
                }
                DropdownMenu(expanded = showClassMenu, onDismissRequest = { showClassMenu = false }) {
                    classList.forEach { cls ->
                        DropdownMenuItem(text = { Text(cls) }, onClick = { selectedClassForExisting = cls; showClassMenu = false })
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            
            Text("2. Search & Select Student", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search by name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (isLoadingStudents) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
            } else {
                Box(modifier = Modifier.heightIn(max = 250.dp).fillMaxWidth().padding(vertical = 8.dp)) {
                    val filtered = sheetStudents.filter { it.studentName.contains(searchText, ignoreCase = true) }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filtered) { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                    studentName = student.studentName
                                    mobile = student.mobileNo
                                    className = student.standard
                                    rollNo = student.rollNo
                                    board = student.board
                                    username = student.mobileNo // Default username to mobile
                                    message = "Selected: ${student.studentName}"
                                }
                            ) {
                                Text("${student.rollNo}. ${student.studentName}", modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            }
        }

        if (studentName.isNotEmpty() || selectedOption == "NEW") {
            Spacer(Modifier.height(10.dp))
            Text("Student Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            OutlinedTextField(
                value = studentName,
                onValueChange = { if (selectedOption == "NEW") studentName = it },
                label = { Text("Student Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedOption == "NEW",
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = mobile,
                onValueChange = { if (selectedOption == "NEW" && it.length <= 10) mobile = it },
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedOption == "NEW",
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                OutlinedTextField(
                    value = className,
                    onValueChange = { if (selectedOption == "NEW") className = it },
                    label = { Text("Class") },
                    modifier = Modifier.weight(1f),
                    enabled = selectedOption == "NEW"
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = division,
                    onValueChange = { division = it },
                    label = { Text("Division") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                OutlinedTextField(
                    value = rollNo,
                    onValueChange = { if (selectedOption == "NEW") rollNo = it },
                    label = { Text("Roll No") },
                    modifier = Modifier.weight(1f),
                    enabled = selectedOption == "NEW"
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = board,
                    onValueChange = { if (selectedOption == "NEW") board = it },
                    label = { Text("Board") },
                    modifier = Modifier.weight(1f),
                    enabled = selectedOption == "NEW"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Login Credentials", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = temporaryPassword,
                onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) temporaryPassword = it },
                label = { Text("Password (4 digits)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (temporaryPassword.length != 4) { message = "Password must be 4 digits"; return@Button }
                    if (username.isBlank()) { message = "Username is required"; return@Button }
                    
                    isSaving = true
                    message = "⏳ Saving..."
                    
                    scope.launch {
                        var sheetSuccess = true
                        var sheetMessage = ""
                        
                        if (selectedOption == "NEW") {
                            // Add to Google Sheets first
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    val connection = URL(scriptUrl).openConnection() as HttpURLConnection
                                    connection.requestMethod = "POST"
                                    connection.doOutput = true
                                    connection.setRequestProperty("Content-Type", "application/json")
                                    val payload = JSONObject().apply {
                                        put("action", "addStudent")
                                        put("studentName", studentName)
                                        put("mobileNo", mobile)
                                        put("rollNo", rollNo)
                                        put("standard", className)
                                        put("board", board)
                                    }
                                    connection.outputStream.use { it.write(payload.toString().toByteArray()) }
                                    val res = if (connection.responseCode == 200) {
                                        connection.inputStream.bufferedReader().use { it.readText() }
                                    } else {
                                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                                    }
                                    JSONObject(res)
                                }
                                sheetSuccess = response.optBoolean("success", false)
                                sheetMessage = response.optString("error", response.optString("message", "Sheet error"))
                            } catch (e: Exception) {
                                sheetSuccess = false
                                sheetMessage = e.message ?: "Connection error"
                            }
                        }
                        
                        if (sheetSuccess) {
                            addStudentToFirebase(studentName, mobile, className, division, rollNo, board, username, temporaryPassword) { success, result ->
                                isSaving = false
                                message = if (selectedOption == "NEW") "✅ Student added to Sheet & Firebase!" else "✅ $result"
                                if (success) {
                                    studentName = ""; mobile = ""; className = ""; division = ""; rollNo = ""; board = ""; username = ""; temporaryPassword = ""
                                }
                            }
                        } else {
                            isSaving = false
                            message = "❌ Apps Script Error: $sheetMessage"
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "SAVING..." else if (selectedOption == "EXISTING") "CREATE LOGIN" else "CREATE STUDENT & ADD TO ROLL")
            }
        }

        Spacer(modifier = Modifier.height(15.dp))
        if (message.isNotEmpty()) { Text(text = message, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("BACK") }
    }
}


@Composable
fun ChangeTeacherPasswordScreen(
    mobile: String,
    onPasswordChanged: () -> Unit
) {

    var newPassword by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    var saving by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            text = "🔐 Create New Password",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
            },
            label = {
                Text("New Password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.DarkGray
            )
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            label = {
                Text("Confirm Password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.DarkGray
            )
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {

                if (newPassword.isBlank()) {
                    message = "Enter a new password"
                    return@Button
                }

                if (newPassword != confirmPassword) {
                    message = "Passwords do not match"
                    return@Button
                }

                saving = true
                message = ""

                FirebaseFirestore
                    .getInstance()
                    .collection("users")
                    .whereEqualTo("mobile", mobile)
                    .whereEqualTo("role", "Teacher")
                    .get()
                    .addOnSuccessListener { documents ->

                        if (documents.isEmpty) {

                            saving = false
                            message =
                                "Teacher account not found"

                        } else {

                            val document =
                                documents.documents[0]

                            document.reference
                                .update(
                                    mapOf(
                                        "password" to newPassword,
                                        "firstLogin" to false
                                    )
                                )
                                .addOnSuccessListener {

                                    saving = false
                                    onPasswordChanged()
                                }
                                .addOnFailureListener {

                                    saving = false
                                    message =
                                        "Unable to save password"
                                }
                        }
                    }
                    .addOnFailureListener {

                        saving = false
                        message =
                            "Connection error"
                    }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (saving)
                    "Saving..."
                else
                    "SAVE PASSWORD"
            )
        }

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        if (message.isNotEmpty()) {

            Text(
                text = message,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ChangePasswordScreen(
    documentId: String,
    onPasswordChanged: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F4FF)).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        IndiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Change Password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Create your new 4-digit security PIN.", color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPassword = it },
                    label = { Text("New PIN") },
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton(onClick = { showNewPassword = !showNewPassword }) { Icon(imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPassword = it },
                    label = { Text("Confirm PIN") },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) { Icon(imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                IndiumButton(
                    text = if (saving) "SAVING..." else "UPDATE PIN",
                    onClick = {
                        if (newPassword.length != 4 || newPassword != confirmPassword) { message = "Invalid PIN or mismatch"; return@IndiumButton }
                        saving = true
                        FirebaseFirestore.getInstance().collection("users").document(documentId).update(mapOf("password" to newPassword, "firstLogin" to false))
                            .addOnSuccessListener { saving = false; onPasswordChanged() }
                            .addOnFailureListener { e -> saving = false; message = e.message ?: "Error" }
                    },
                    enabled = !saving,
                    containerColor = Color(0xFF7C4DFF)
                )
            }
        }
        if (message.isNotEmpty()) Text(text = message, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTodayLecturesScreen(
    onBack: () -> Unit
) {
    val teacherName = CurrentUser.name
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var lectures by remember { mutableStateOf<List<TodayLecture>>(emptyList()) }

    fun loadLectures(isRefresh: Boolean = false) {
        if (isRefresh) refreshing = true else loading = true
        errorMessage = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedTeacher = URLEncoder.encode(teacherName, "UTF-8")
                val urlString = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec?action=todayLectures&teacherName=$encodedTeacher"
                
                val response = withContext(Dispatchers.IO) {
                    val connection = URL(urlString).openConnection() as HttpURLConnection
                    connection.connectTimeout = 30000
                    connection.readTimeout = 30000
                    connection.instanceFollowRedirects = true
                    try {
                        if (connection.responseCode == 200) connection.inputStream.bufferedReader().use { it.readText() } else null
                    } finally {
                        connection.disconnect()
                    }
                }

                withContext(Dispatchers.Main) {
                    if (response != null) {
                        val json = JSONObject(response)
                        if (json.optBoolean("success")) {
                            day = json.optString("day")
                            date = json.optString("date")
                            val jsonLectures = json.optJSONArray("lectures") ?: JSONArray()
                            val resultList = mutableListOf<TodayLecture>()
                            for (i in 0 until jsonLectures.length()) {
                                val item = jsonLectures.getJSONObject(i)
                                resultList.add(TodayLecture(
                                    time = item.optString("time"),
                                    className = item.optString("className"),
                                    subject = item.optString("subject"),
                                    teacher = item.optString("teacher"),
                                    adjusted = item.optBoolean("adjusted", false),
                                    originalTeacher = item.optString("originalTeacher", "")
                                ))
                            }
                            lectures = resultList
                        } else {
                            errorMessage = json.optString("error", "No lectures found.")
                        }
                    } else {
                        errorMessage = "Unable to refresh. Please try again."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Unable to refresh. Please try again."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    loading = false
                    refreshing = false
                }
            }
        }
    }

    LaunchedEffect(teacherName) {
        loadLectures()
    }

    Scaffold(
        topBar = { IndiumTopBar(title = "Today's Lectures", onBack = onBack) },
        containerColor = Color(0xFFF7F4FF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (errorMessage.isNotBlank() && lectures.isEmpty()) {
                IndiumCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Unable to refresh. Please try again.", color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = errorMessage, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        IndiumButton(
                            text = "RETRY",
                            onClick = { loadLectures(true) },
                            containerColor = Color(0xFF7C4DFF)
                        )
                    }
                }
            } else {
                IndiumCard(modifier = Modifier.fillMaxWidth(), containerColor = Color(0xFF7C4DFF), contentColor = Color.White) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = day, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text(text = date, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(
                            onClick = { if (!refreshing) loadLectures(true) },
                            enabled = !refreshing
                        ) {
                            if (refreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Lectures", tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                lectures.forEach { lecture ->
                    IndiumCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        containerColor = if (lecture.adjusted) Color(0xFFFFF3E0) else Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = lecture.time, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C4DFF))
                                if (lecture.adjusted) Text("ADJUSTED", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "${lecture.className} • ${lecture.subject}", fontWeight = FontWeight.Bold, color = Color(0xFF252238))
                            Text(text = "Teacher: ${lecture.teacher}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            if (lecture.adjusted) Text("Adjusted from: ${lecture.originalTeacher}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
                        }
                    }
                }
            }
        }
    }
}

data class TodayLecture(
    val time: String,
    val className: String,
    val subject: String,
    val teacher: String,
    val adjusted: Boolean,
    val originalTeacher: String
)
data class TeacherAttendanceRecord(
    val date: String,
    val teacherName: String,
    val inTime: String,
    val outTime: String,
    val status: String
)
data class LeaveLecture(
    val time: String,
    val className: String,
    val subject: String,
    val originalTeacher: String
)

data class LeaveHistoryRecord(
    val date: String,
    val submissionDate: String,
    val status: String,
    val details: String
)

@Composable
fun TeacherLeaveScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kolkataTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
    val todayKolkata = Calendar.getInstance(kolkataTimeZone)
    
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(todayKolkata.time)) }
    var lectures by remember { mutableStateOf<List<LeaveLecture>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var selectedLectureIndex by remember { mutableStateOf(-1) }
    var showTeacherDialog by remember { mutableStateOf(false) }
    var adjustments by remember { mutableStateOf<List<String>>(emptyList()) }
    val teachers = remember { mutableStateListOf<String>() }
    
    var showHistory by remember { mutableStateOf(false) }
    var historyList by remember { mutableStateOf<List<LeaveHistoryRecord>>(emptyList()) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf("") }

    fun loadLeaveHistory() {
        if (CurrentUser.name.isBlank()) {
            historyError = "Teacher name not found"
            return
        }
        historyLoading = true
        historyError = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec?action=leaveHistory&teacherName=${URLEncoder.encode(CurrentUser.name, "UTF-8")}"
                val response = withContext(Dispatchers.IO) {
                    val connection = URL(urlString).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    connection.instanceFollowRedirects = true
                    try {
                        if (connection.responseCode == 200) {
                            connection.inputStream.bufferedReader().use { it.readText() }
                        } else {
                            null
                        }
                    } finally {
                        connection.disconnect()
                    }
                }
                if (response == null) {
                    withContext(Dispatchers.Main) {
                        historyError = "Failed to load leave records."
                        historyLoading = false
                    }
                    return@launch
                }
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    val arr = json.optJSONArray("records") ?: JSONArray()
                    val result = mutableListOf<LeaveHistoryRecord>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        result.add(LeaveHistoryRecord(
                            date = obj.optString("date"),
                            submissionDate = obj.optString("appliedTime").ifBlank { obj.optString("submissionDate") },
                            status = obj.optString("status"),
                            details = obj.optString("teacher").ifBlank { obj.optString("details") }
                        ))
                    }
                    withContext(Dispatchers.Main) {
                        historyList = result.reversed()
                        historyLoading = false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val error = json.optString("error", "Failed to load history")
                        historyError = error
                        historyLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    historyError = "Error: ${e.message}"
                    historyLoading = false
                }
            }
        }
    }

    fun loadTeachers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://script.google.com/macros/s/AKfycbx3vXqB5Vs6DToJp5ArnnbuIGIvBzGwcLJFFUWtDrlBrD7dqLcRj7u89xNrskwPjrgu/exec?action=teachers")
                val response = url.openConnection().inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    val array = json.getJSONArray("teachers")
                    withContext(Dispatchers.Main) {
                        teachers.clear()
                        for (i in 0 until array.length()) { teachers.add(array.getJSONObject(i).getString("teacherName")) }
                    }
                }
            } catch (e: Exception) { withContext(Dispatchers.Main) { message = "Error loading teachers" } }
        }
    }

    fun loadLectures() {
        loading = true
        message = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString =
                    "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec" +
                            "?action=leaveLectures" +
                            "&teacherName=${URLEncoder.encode(CurrentUser.name, "UTF-8")}" +
                            "&date=${URLEncoder.encode(selectedDate, "UTF-8")}"

                val response = URL(urlString)
                    .openConnection()
                    .getInputStream()
                    .bufferedReader()
                    .use { it.readText() }

                val json = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (json.optBoolean("success")) {

                        val arr = json.optJSONArray("lectures") ?: JSONArray()
                        val result = mutableListOf<LeaveLecture>()

                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)

                            result.add(
                                LeaveLecture(
                                    obj.optString("time"),
                                    obj.optString("className"),
                                    obj.optString("subject"),
                                    obj.optString("originalTeacher")
                                )
                            )
                        }

                        lectures = result
                        adjustments = List(result.size) { "" }
                        message = if (result.isEmpty()) {
                            "No lectures on this date"
                        } else {
                            ""
                        }

                    } else {
                        message = "Server error: ${
                            json.optString("error", "Unknown response from server")
                        }"
                    }

                    loading = false
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loading = false
                    message = "Error: ${e.message ?: e.javaClass.simpleName}"
                }
            }
        }
    }

    fun applyLeave() {

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.timeZone = kolkataTimeZone

        val today = Calendar.getInstance(kolkataTimeZone).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Validate selected date
        try {
            val selectedDateObj = sdf.parse(selectedDate)

            if (selectedDateObj == null) {
                message = "❌ Invalid date format"
                return
            }

            if (selectedDateObj.before(today.time)) {
                message =
                    "❌ Backdated leave applications are not allowed. Please select today or a future date."
                return
            }

        } catch (e: Exception) {
            message = "❌ Invalid date format"
            return
        }

        loading = true
        message = "Applying leave..."

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val scriptUrl =
                    "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec"

                val connection =
                    URL(scriptUrl).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                val requestJson = JSONObject().apply {

                    put("action", "applyLeave")
                    put("teacherName", CurrentUser.name)
                    put("date", selectedDate)

                    val adjArray = JSONArray()

                    lectures.forEachIndexed { index, lecture ->

                        val obj = JSONObject().apply {
                            put("time", lecture.time)
                            put("className", lecture.className)
                            put("subject", lecture.subject)
                            put("adjustedTeacher", adjustments[index])
                        }

                        adjArray.put(obj)
                    }

                    put("adjustments", adjArray)
                }

                connection.outputStream.bufferedWriter().use {
                    it.write(requestJson.toString())
                }

                val response = try {

                    val responseCode = connection.responseCode

                    val responseStream =
                        if (responseCode in 200..299) {
                            connection.inputStream
                        } else {
                            connection.errorStream
                        }

                    responseStream?.bufferedReader()?.use {
                        it.readText()
                    }

                } finally {
                    connection.disconnect()
                }

                withContext(Dispatchers.Main) {

                    if (response != null) {

                        val json = JSONObject(response)

                        if (json.optBoolean("success")) {

                            message = "✅ Leave applied successfully!"

                        } else {

                            val errorMessage =
                                json.optString("error", "Failed")

                            message = if (
                                errorMessage.contains(
                                    "leave already applied",
                                    ignoreCase = true
                                )
                            ) {
                                "You have already applied for leave on this date."
                            } else {
                                "❌ Error: $errorMessage"
                            }
                        }

                    } else {

                        message = "❌ Server error. Please try again."
                    }

                    loading = false
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    loading = false
                    message = "❌ Error: ${e.message}"
                }
            }
        }
    }

    LaunchedEffect(Unit) { loadTeachers(); loadLectures() }

    Scaffold(topBar = { IndiumTopBar("Apply Leave", onBack) }, containerColor = Color(0xFFF7F4FF)) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    IndiumOutlinedButton(text = "SELECT DATE: $selectedDate", onClick = {
                        val c = Calendar.getInstance(kolkataTimeZone)
                        val dialog = DatePickerDialog(context, { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d)
                            selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
                            loadLectures()
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                        
                        // Prevent selecting past dates
                        c.set(Calendar.HOUR_OF_DAY, 0)
                        c.set(Calendar.MINUTE, 0)
                        c.set(Calendar.SECOND, 0)
                        c.set(Calendar.MILLISECOND, 0)
                        dialog.datePicker.minDate = c.timeInMillis
                        
                        dialog.show()
                    })
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    IndiumButton(
                        text = "VIEW LEAVE HISTORY",
                        onClick = { 
                            showHistory = true
                            loadLeaveHistory()
                        },
                        containerColor = Color(0xFF673AB7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (message.isNotEmpty()) Text(message, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)

            lectures.forEachIndexed { index, lecture ->
                IndiumCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(lecture.time, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))
                        Text("${lecture.className} • ${lecture.subject}")
                        Spacer(modifier = Modifier.height(8.dp))
                        IndiumOutlinedButton(
                            text = adjustments.getOrNull(index).let { if (it.isNullOrBlank()) "SELECT SUBSTITUTE" else it },
                            onClick = { selectedLectureIndex = index; showTeacherDialog = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            IndiumButton(text = "APPLY LEAVE", onClick = { applyLeave() }, enabled = lectures.isNotEmpty() && adjustments.all { it.isNotBlank() }, containerColor = Color(0xFFF44336))
            Spacer(modifier = Modifier.height(12.dp))
            IndiumOutlinedButton(text = "CANCEL", onClick = onBack)
        }
    }

    if (showTeacherDialog) {
        AlertDialog(onDismissRequest = { showTeacherDialog = false }, title = { Text("Select Substitute") }, text = {
            Column { teachers.filter { it != CurrentUser.name }.forEach { t ->
                TextButton(onClick = { adjustments = adjustments.toMutableList().apply { this[selectedLectureIndex] = t }; showTeacherDialog = false }, modifier = Modifier.fillMaxWidth()) { Text(t) }
            }}
        }, confirmButton = { TextButton(onClick = { showTeacherDialog = false }) { Text("CLOSE") }})
    }
}

@Composable
fun TeacherLeaveHistoryScreen(
    historyList: List<LeaveHistoryRecord>,
    isLoading: Boolean,
    errorMessage: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = { IndiumTopBar(title = "My Leave History", onBack = onBack) },
        containerColor = Color(0xFFF7F4FF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C4DFF))
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = errorMessage, color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        IndiumButton(text = "RETRY", onClick = onRefresh)
                    }
                }
            } else if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No previous leave applications found.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(historyList) { record ->
                        IndiumCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = record.date,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF252238)
                                    )
                                    Surface(
                                        color = when(record.status.uppercase()) {
                                            "APPROVED" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                            "REJECTED" -> Color.Red.copy(alpha = 0.1f)
                                            else -> Color(0xFF7C4DFF).copy(alpha = 0.1f)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = record.status,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when(record.status.uppercase()) {
                                                "APPROVED" -> Color(0xFF4CAF50)
                                                "REJECTED" -> Color.Red
                                                else -> Color(0xFF7C4DFF)
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Submitted on: ${record.submissionDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                if (record.details.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = record.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF252238)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    selectedRole: String,
    onBack: () -> Unit
) {
    var mobile by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F4FF)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IndiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Reset PIN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Enter your registered mobile number.", color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) mobile = it },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                IndiumButton(
                    text = if (isLoading) "PROCESSING..." else "REQUEST RESET",
                    onClick = {
                        if (mobile.length != 10) { message = "Invalid mobile number"; return@IndiumButton }
                        isLoading = true
                        FirebaseFirestore.getInstance().collection("users").whereEqualTo("mobile", mobile).whereEqualTo("role", selectedRole).get()
                            .addOnSuccessListener { res ->
                                isLoading = false
                                message = if (res.isEmpty) "No account found." else "Contact Admin for temporary PIN."
                            }
                    },
                    enabled = !isLoading,
                    containerColor = Color(0xFF7C4DFF)
                )
            }
        }
        if (message.isNotBlank()) Text(text = message, modifier = Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) { Text("BACK TO LOGIN", color = Color(0xFF7C4DFF)) }
    }
}

@Composable
fun TeacherStudentsScreen(userRole: String = "Teacher", onBack: () -> Unit) {
    val context = LocalContext.current
    val scriptUrl = "https://script.google.com/macros/s/AKfycbwbBEeUDm0gY_mCuPUJC04sw-O1aWlTTGbyu-x4yhl-BOLbUIoHD4cqWuuS_pNKRSCi/exec"
    
    val classList = listOf(
        "1st Standard", "2nd Standard", "3rd Standard", "4th Standard", 
        "5th Standard", "6th Standard", "7th Standard", "8th Standard", "9th Standard", "10th Standard"
    )
    
    val boardOptions = listOf("SSC", "CBSE", "General")

    var selectedClass by remember { mutableStateOf("10th Standard") }
    var selectedBoard by remember { mutableStateOf("SSC") }
    var searchText by remember { mutableStateOf("") }
    
    var allStudents by remember { mutableStateOf<List<SheetStudent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf("") }
    
    var showClassMenu by remember { mutableStateOf(false) }
    var showBoardMenu by remember { mutableStateOf(false) }

    // Fetch students
    LaunchedEffect(Unit) {
        isLoading = true
        loadError = ""
        try {
            val result = withContext(Dispatchers.IO) {
                val connection = URL(scriptUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 20000
                connection.readTimeout = 20000
                if (connection.responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else null
            }
            
            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success")) {
                    val studentsArray = json.getJSONArray("students")
                    val loadedStudents = mutableListOf<SheetStudent>()
                    for (i in 0 until studentsArray.length()) {
                        val student = studentsArray.getJSONObject(i)
                        loadedStudents.add(SheetStudent(
                            rollNo = student.optString("rollNo").trim(),
                            studentName = student.optString("studentName").trim(),
                            mobileNo = student.optString("mobileNo").trim(),
                            standard = student.optString("standard").trim(),
                            board = student.optString("board").trim()
                        ))
                    }
                    allStudents = loadedStudents.sortedBy { it.studentName }
                } else {
                    loadError = json.optString("error", "Failed to load data")
                }
            } else {
                loadError = "Unable to connect to server"
            }
        } catch (e: Exception) {
            loadError = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val filteredStudents = allStudents.filter { student ->
        val classDigits = selectedClass.filter { it.isDigit() }
        val matchesClass = student.standard.filter { it.isDigit() } == classDigits
        
        val matchesBoard = when (selectedBoard) {
            "General" -> !student.board.equals("SSC", true) && !student.board.equals("CBSE", true)
            else -> student.board.equals(selectedBoard, ignoreCase = true)
        }
        
        val matchesSearch = if (searchText.isBlank()) true 
                           else student.studentName.contains(searchText, ignoreCase = true)
        
        matchesClass && matchesBoard && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (userRole == "Admin") {
            IndiumCard(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                containerColor = Color(0xFF673AB7).copy(alpha = 0.05f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Total Students: ${allStudents.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF673AB7)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Standard-wise counts summary (Admin only)
                    val countsByStandard = allStudents.groupBy { it.standard }
                    Text(
                        text = countsByStandard.entries
                            .sortedBy { entry -> entry.key.filter { it.isDigit() }.toIntOrNull() ?: 0 }
                            .joinToString(" | ") { "${it.key.split(" ")[0]}: ${it.value.size}" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search by Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF252238),
                unfocusedTextColor = Color(0xFF252238)
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Standard Filter
            Box(modifier = Modifier.weight(1.3f)) {
                IndiumOutlinedButton(
                    text = selectedClass,
                    onClick = { showClassMenu = true }
                )
                DropdownMenu(
                    expanded = showClassMenu,
                    onDismissRequest = { showClassMenu = false }
                ) {
                    classList.forEach { className ->
                        DropdownMenuItem(
                            text = { Text(className) },
                            onClick = {
                                selectedClass = className
                                showClassMenu = false
                            }
                        )
                    }
                }
            }
            
            // Board Filter
            Box(modifier = Modifier.weight(0.7f)) {
                IndiumOutlinedButton(
                    text = selectedBoard,
                    onClick = { showBoardMenu = true }
                )
                DropdownMenu(
                    expanded = showBoardMenu,
                    onDismissRequest = { showBoardMenu = false }
                ) {
                    boardOptions.forEach { board ->
                        DropdownMenuItem(
                            text = { Text(board) },
                            onClick = {
                                selectedBoard = board
                                showBoardMenu = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Student Count - CURRENT GROUP ONLY
        Text(
            text = "Group Count: ${filteredStudents.size}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else if (loadError.isNotEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = loadError, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        } else if (filteredStudents.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.GroupOff,
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (allStudents.isEmpty()) "Loading students..." else "No students match these filters",
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredStudents) { student ->
                    IndiumCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = student.studentName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF252238)
                                )
                                if (student.rollNo.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFF7C4DFF).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "#${student.rollNo}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF7C4DFF),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${student.standard} • ${if(student.board.isBlank()) "General" else student.board}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            if (userRole == "Admin" && student.mobileNo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobileNo}"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Handle case where no dialer is available
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = student.mobileNo,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = TextDecoration.Underline
                                        ),
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileScreen(student: SheetStudent, onBack: () -> Unit) {
    val context = LocalContext.current
    val scriptUrl = "https://script.google.com/macros/s/AKfycbwbBEeUDm0gY_mCuPUJC04sw-O1aWlTTGbyu-x4yhl-BOLbUIoHD4cqWuuS_pNKRSCi/exec"

    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) } // 1-based index
    var attendanceList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoadingAttendance by remember { mutableStateOf(false) }
    var attendanceError by remember { mutableStateOf("") }

    val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    LaunchedEffect(selectedMonth, student.rollNo, student.standard, student.board) {
        if (student.rollNo.isBlank() || student.standard.isBlank()) {
            attendanceError = "Student profile mapping incomplete."
            return@LaunchedEffect
        }
        isLoadingAttendance = true
        attendanceError = ""
        try {
            val encodedClass = URLEncoder.encode(student.standard, "UTF-8")
            val encodedBoard = URLEncoder.encode(student.board, "UTF-8")
            val urlString = "$scriptUrl?action=getStudentAttendance&rollNo=${student.rollNo}&standard=$encodedClass&board=$encodedBoard"
            
            val result = withContext(Dispatchers.IO) {
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                try {
                    if (connection.responseCode == 200) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else null
                } finally {
                    connection.disconnect()
                }
            }

            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success", false)) {
                    val historyArray = json.optJSONArray("history") ?: JSONArray()
                    val loadedHistory = mutableListOf<Pair<String, String>>()
                    for (i in 0 until historyArray.length()) {
                        val item = historyArray.getJSONObject(i)
                        val dateStr = item.optString("date") // expected format "dd/MM/yyyy" or similar
                        val status = item.optString("status")
                        
                        val parts = dateStr.split("/")
                        if (parts.size >= 2) {
                            val recordMonth = parts[1].toIntOrNull()
                            if (recordMonth == selectedMonth) {
                                loadedHistory.add(dateStr to status)
                            }
                        } else {
                            loadedHistory.add(dateStr to status)
                        }
                    }
                    attendanceList = loadedHistory
                } else {
                    attendanceError = json.optString("error", "No attendance records available.")
                }
            } else {
                attendanceError = "Unable to load attendance server records."
            }
        } catch (e: Exception) {
            attendanceError = "Error loading attendance history: ${e.message}"
        } finally {
            isLoadingAttendance = false
        }
    }

    Scaffold(
        topBar = { IndiumTopBar(title = "Student Profile", onBack = onBack) },
        containerColor = Color(0xFFF7F4FF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // A. BASIC INFORMATION
            Text(text = "Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF252238))
            Spacer(modifier = Modifier.height(8.dp))
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Name: ${student.studentName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Class: ${student.standard}", color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Board: ${if(student.board.isBlank()) "General" else student.board}", color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Roll No: #${student.rollNo}", color = Color.Gray)
                    
                    if (student.mobileNo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobileNo}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = student.mobileNo, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline), color = Color(0xFF4CAF50))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // B. FEES INFORMATION
            Text(text = "Fees Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF252238))
            Spacer(modifier = Modifier.height(8.dp))
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Fee information is not available.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // C. MONTHLY ATTENDANCE
            Text(text = "Monthly Attendance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF252238))
            Spacer(modifier = Modifier.height(8.dp))
            
            var showMonthMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                IndiumOutlinedButton(text = "Month: ${monthsList[selectedMonth - 1]}", onClick = { showMonthMenu = true })
                DropdownMenu(expanded = showMonthMenu, onDismissRequest = { showMonthMenu = false }) {
                    monthsList.forEachIndexed { idx, name ->
                        DropdownMenuItem(text = { Text(name) }, onClick = { selectedMonth = idx + 1; showMonthMenu = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoadingAttendance) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C4DFF))
                }
            } else if (attendanceError.isNotEmpty()) {
                Text(text = attendanceError, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            } else {
                val totalPresent = attendanceList.count { it.second.equals("Present", ignoreCase = true) }
                val totalAbsent = attendanceList.count { it.second.equals("Absent", ignoreCase = true) }
                
                IndiumCard(modifier = Modifier.fillMaxWidth(), containerColor = Color(0xFF7C4DFF).copy(alpha = 0.05f)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Present Days", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("$totalPresent", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF4CAF50))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Absent Days", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("$totalAbsent", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (attendanceList.isEmpty()) {
                    Text(text = "No logs found for this month.", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                } else {
                    attendanceList.forEach { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardColors(containerColor = Color.White, contentColor = Color.Unspecified, disabledContainerColor = Color.Unspecified, disabledContentColor = Color.Unspecified)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = record.first, fontWeight = FontWeight.Medium)
                                Surface(
                                    color = if(record.second.equals("Present", ignoreCase = true)) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = record.second,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = if(record.second.equals("Present", ignoreCase = true)) Color(0xFF4CAF50) else Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            IndiumOutlinedButton(text = "BACK TO DASHBOARD", onClick = onBack)
        }
    }
}

@Composable
fun AdminTeacherProfileScreen(teacher: TeacherRecord, onBack: () -> Unit) {
    val context = LocalContext.current
    var attendanceList by remember { mutableStateOf<List<TeacherAttendanceRecord>>(emptyList()) }
    var leaveList by remember { mutableStateOf<List<LeaveHistoryRecord>>(emptyList()) }
    var isLoadingAttendance by remember { mutableStateOf(false) }
    var isLoadingLeave by remember { mutableStateOf(false) }
    var attendanceError by remember { mutableStateOf("") }
    var leaveError by remember { mutableStateOf("") }

    val attendanceUrl = "https://script.google.com/macros/s/AKfycbx3vXqB5Vs6DToJp5ArnnbuIGIvBzGwcLJFFUWtDrlBrD7dqLcRj7u89xNrskwPjrgu/exec"
    val leaveUrl = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec"

    LaunchedEffect(teacher.mobile) {
        val rawMobile = teacher.mobile.filter { it.isDigit() }
        val normalizedMobile = if (rawMobile.length > 10) rawMobile.takeLast(10) else rawMobile
        
        if (normalizedMobile.isBlank()) return@LaunchedEffect
        
        // 1. Fetch Teacher Attendance History
        isLoadingAttendance = true
        attendanceError = ""
        
        try {
            val encodedMobile = URLEncoder.encode(normalizedMobile, "UTF-8")
            val urlString = "$attendanceUrl?action=history&mobile=$encodedMobile"
            
            val response = withContext(Dispatchers.IO) {
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true
                
                try {
                    val code = connection.responseCode
                    if (code == 200) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else if (code == 302 || code == 307) {
                        val loc = connection.getHeaderField("Location")
                        if (loc != null) {
                            val conn2 = URL(loc).openConnection() as HttpURLConnection
                            conn2.requestMethod = "GET"
                            if (conn2.responseCode == 200) conn2.inputStream.bufferedReader().use { it.readText() } else null
                        } else null
                    } else null
                } finally {
                    connection.disconnect()
                }
            }

            if (response != null) {
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    val arr = json.optJSONArray("records") 
                           ?: json.optJSONArray("history") 
                           ?: json.optJSONArray("attendance")
                           ?: json.optJSONArray("data")
                           ?: JSONArray()
                           
                    val recordsList = mutableListOf<TeacherAttendanceRecord>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        recordsList.add(TeacherAttendanceRecord(
                            date = obj.optString("date"),
                            teacherName = obj.optString("teacherName", teacher.name),
                            inTime = obj.optString("inTime"),
                            outTime = obj.optString("outTime"),
                            status = obj.optString("status")
                        ))
                    }
                    attendanceList = recordsList.reversed()
                } else {
                    attendanceError = json.optString("error", "No records found.")
                }
            } else {
                attendanceError = "Unable to reach attendance logs."
            }
        } catch (e: Exception) {
            attendanceError = "Error: ${e.message}"
        } finally {
            isLoadingAttendance = false
        }

        // 2. Fetch Teacher Leave History
        isLoadingLeave = true
        leaveError = ""
        try {
            val encodedTeacherName = URLEncoder.encode(teacher.name, "UTF-8")
            val urlString = "$leaveUrl?action=leaveHistory&teacherName=$encodedTeacherName"
            
            val response = withContext(Dispatchers.IO) {
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true
                
                try {
                    if (connection.responseCode == 200) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        null
                    }
                } finally {
                    connection.disconnect()
                }
            }
            
            if (response != null) {
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    val arr = json.optJSONArray("records") ?: json.optJSONArray("history") ?: JSONArray()
                    val recordsList = mutableListOf<LeaveHistoryRecord>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        recordsList.add(LeaveHistoryRecord(
                            date = obj.optString("date"),
                            submissionDate = obj.optString("appliedTime").ifBlank { obj.optString("submissionDate") },
                            status = obj.optString("status"),
                            details = obj.optString("teacher").ifBlank { obj.optString("details") }
                        ))
                    }
                    leaveList = recordsList.reversed()
                } else {
                    leaveError = json.optString("error", "No leave requests found.")
                }
            } else {
                leaveError = "Unable to load leave records."
            }
        } catch (e: Exception) {
            leaveError = "Error: ${e.message}"
        } finally {
            isLoadingLeave = false
        }
    }

    Scaffold(
        topBar = { IndiumTopBar(title = "Teacher Profile", onBack = onBack) },
        containerColor = Color(0xFFF7F4FF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Basic Information
            Text(text = "Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF252238))
            Spacer(modifier = Modifier.height(8.dp))
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Name: ${teacher.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    if (teacher.subject.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Subject/Specialization: ${teacher.subject}", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Role: Registered Teacher", color = Color.Gray)
                    
                    if (teacher.mobile.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${teacher.mobile}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = teacher.mobile, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline), color = Color(0xFF4CAF50))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Attendance History
            Text(text = "Attendance History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF252238))
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoadingAttendance) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C4DFF))
                }
            } else if (attendanceError.isNotEmpty()) {
                Text(text = attendanceError, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            } else if (attendanceList.isEmpty()) {
                Text(text = "No logs found.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            } else {
                attendanceList.forEach { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardColors(containerColor = Color.White, contentColor = Color.Unspecified, disabledContainerColor = Color.Unspecified, disabledContentColor = Color.Unspecified)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = log.date, fontWeight = FontWeight.Medium)
                                Text(text = "IN: ${log.inTime.ifBlank { "--:--" }} • OUT: ${log.outTime.ifBlank { "--:--" }}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Surface(
                                color = if(log.status.equals("Present", ignoreCase = true)) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                              ) {
                                Text(
                                    text = log.status.ifBlank { "Logged" },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if(log.status.equals("Present", ignoreCase = true)) Color(0xFF4CAF50) else Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Leave History
            Text(text = "Leave History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF252238))
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoadingLeave) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C4DFF))
                }
            } else if (leaveError.isNotEmpty()) {
                Text(text = leaveError, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            } else if (leaveList.isEmpty()) {
                Text(text = "No leave requests found.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            } else {
                leaveList.forEach { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardColors(containerColor = Color.White, contentColor = Color.Unspecified, disabledContainerColor = Color.Unspecified, disabledContentColor = Color.Unspecified)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = record.date, fontWeight = FontWeight.Medium)
                                Surface(
                                    color = when(record.status.uppercase()) {
                                        "APPROVED" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                        "REJECTED" -> Color.Red.copy(alpha = 0.1f)
                                        else -> Color(0xFF7C4DFF).copy(alpha = 0.1f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = record.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = when(record.status.uppercase()) {
                                            "APPROVED" -> Color(0xFF4CAF50)
                                            "REJECTED" -> Color.Red
                                            else -> Color(0xFF7C4DFF)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            if (record.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = record.details, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Applied on: ${record.submissionDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            IndiumOutlinedButton(text = "BACK TO DASHBOARD", onClick = onBack)
        }
    }
}

data class TeacherRecord(
    val name: String,
    val mobile: String,
    val subject: String = ""
)

@Composable
fun AdminTeachersScreen(onTeacherClick: (TeacherRecord) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var teachersList by remember { mutableStateOf<List<TeacherRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isLoading = true
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("role", "Teacher")
            .get()
            .addOnSuccessListener { documents ->
                val loaded = documents.map { doc ->
                    TeacherRecord(
                        name = doc.getString("name") ?: "Unknown",
                        mobile = doc.getString("mobile") ?: "",
                        subject = doc.getString("subject") ?: ""
                    )
                }.sortedBy { it.name }
                teachersList = loaded
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else if (errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        } else {
            IndiumCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                containerColor = Color(0xFF673AB7).copy(alpha = 0.05f)
            ) {
                Text(
                    text = "Total Teachers: ${teachersList.size}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF673AB7)
                    )
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(teachersList) { teacher ->
                    IndiumCard(
                        modifier = Modifier.fillMaxWidth().clickable { onTeacherClick(teacher) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = teacher.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF252238)
                            )
                            
                            if (teacher.subject.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = teacher.subject,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            if (teacher.mobile.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${teacher.mobile}"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {}
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = teacher.mobile,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = TextDecoration.Underline
                                        ),
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTodayLecturesScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var lectures by remember { mutableStateOf<List<TodayLecture>>(emptyList()) }

    val scriptUrl = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec"

    LaunchedEffect(Unit) {
        try {
            val urlString = "$scriptUrl?action=todayAllLectures"
            val response = withContext(Dispatchers.IO) {
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true
                try {
                    if (connection.responseCode == 200) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else null
                } finally {
                    connection.disconnect()
                }
            }

            if (response != null) {
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    day = json.optString("day")
                    date = json.optString("date")
                    val jsonLectures = json.optJSONArray("lectures") ?: JSONArray()
                    val resultList = mutableListOf<TodayLecture>()
                    for (i in 0 until jsonLectures.length()) {
                        val item = jsonLectures.getJSONObject(i)
                        resultList.add(TodayLecture(
                            time = item.optString("time"),
                            className = item.optString("className"),
                            subject = item.optString("subject"),
                            teacher = item.optString("teacher"),
                            adjusted = item.optBoolean("adjusted", false),
                            originalTeacher = item.optString("originalTeacher", "")
                        ))
                    }
                    lectures = resultList
                } else {
                    errorMessage = json.optString("error", "No lectures found for today.")
                }
            } else {
                errorMessage = "Unable to reach server."
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
    ) {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                IndiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Today's All Lectures", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        if (day.isNotBlank()) {
                            Text(text = "$day • $date", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f)))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    IndiumCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = errorMessage, modifier = Modifier.padding(16.dp), color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                } else if (lectures.isEmpty()) {
                    IndiumCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "No lectures scheduled for today across the academy.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    }
                } else {
                    lectures.forEach { lecture ->
                        IndiumCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            containerColor = if (lecture.adjusted) Color(0xFFFFF3E0) else Color.White
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = lecture.time, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C4DFF))
                                    if (lecture.adjusted) {
                                        Surface(
                                            color = Color(0xFFE65100).copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ADJUSTED",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "${lecture.className} • ${lecture.subject}", fontWeight = FontWeight.Bold, color = Color(0xFF252238))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Teacher: ${lecture.teacher}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                if (lecture.adjusted && lecture.originalTeacher.isNotBlank()) {
                                    Text(text = "Original Teacher: ${lecture.originalTeacher}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                IndiumButton(text = "BACK", onClick = onBack)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AdminWeeklyTimetableScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var timetableData by remember { mutableStateOf<Map<String, List<TodayLecture>>>(emptyMap()) }
    var selectedDay by remember { mutableStateOf("All") }

    val days = listOf("All", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val scriptUrl = "https://script.google.com/macros/s/AKfycbzlSejr1rTZ4yEDXVSskAyQAy5ZljPjyRfqbHmF9BsVkO0ZS770z0b39pNYFZwT3vLTCw/exec"

    fun loadTimetable(isRefresh: Boolean = false) {
        if (isRefresh) refreshing = true else loading = true
        errorMessage = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "$scriptUrl?action=academyTimetable"
                val response = withContext(Dispatchers.IO) {
                    val connection = URL(urlString).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 30000
                    connection.readTimeout = 30000
                    connection.instanceFollowRedirects = true
                    try {
                        if (connection.responseCode == 200) {
                            connection.inputStream.bufferedReader().use { it.readText() }
                        } else null
                    } finally {
                        connection.disconnect()
                    }
                }

                withContext(Dispatchers.Main) {
                    if (response != null) {
                        val json = JSONObject(response)
                        if (json.optBoolean("success", false)) {
                            val daysJson = json.optJSONObject("timetable") ?: JSONObject()
                            val loadedData = mutableMapOf<String, List<TodayLecture>>()
                            val weekdayList = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                            
                            weekdayList.forEach { day ->
                                val dayArray = daysJson.optJSONArray(day)
                                if (dayArray != null) {
                                    val lecturesList = mutableListOf<TodayLecture>()
                                    for (i in 0 until dayArray.length()) {
                                        val item = dayArray.getJSONObject(i)
                                        lecturesList.add(
                                            TodayLecture(
                                                time = item.optString("time"),
                                                className = item.optString("className"),
                                                subject = item.optString("subject"),
                                                teacher = item.optString("teacher"),
                                                adjusted = item.optBoolean("adjusted", false),
                                                originalTeacher = item.optString("originalTeacher", "")
                                            )
                                        )
                                    }
                                    loadedData[day] = lecturesList
                                }
                            }
                            timetableData = loadedData
                            if (loadedData.isEmpty()) {
                                errorMessage = "No weekly timetable found."
                            }
                        } else {
                            errorMessage = json.optString("error", "Failed to fetch timetable.")
                        }
                    } else {
                        errorMessage = "Unable to refresh. Please try again."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Unable to refresh. Please try again."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    loading = false
                    refreshing = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadTimetable()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FF))
            .padding(16.dp)
    ) {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else if (errorMessage.isNotBlank() && timetableData.isEmpty()) {
            IndiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Unable to refresh. Please try again.", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = errorMessage, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    IndiumButton(
                        text = "RETRY",
                        onClick = { loadTimetable(true) },
                        containerColor = Color(0xFF7C4DFF)
                    )
                }
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                IndiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Weekly Timetable (Academy)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text(text = "All classes & teachers schedule", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f)))
                        }
                        IconButton(
                            onClick = { if (!refreshing) loadTimetable(true) },
                            enabled = !refreshing
                        ) {
                            if (refreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Timetable", tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    days.forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            label = { Text(day) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF673AB7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    IndiumCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = errorMessage, modifier = Modifier.padding(16.dp), color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                } else {
                    val displayDays = if (selectedDay == "All") {
                        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                    } else {
                        listOf(selectedDay)
                    }

                    displayDays.forEach { day ->
                        val lectures = timetableData[day] ?: emptyList()
                        if (lectures.isNotEmpty()) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF673AB7)
                                ),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                            
                            lectures.forEach { lecture ->
                                IndiumCard(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    containerColor = if (lecture.adjusted) Color(0xFFFFF3E0) else Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${lecture.className} • ${lecture.subject}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF252238)
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Teacher: ${lecture.teacher}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                            )
                                            if (lecture.adjusted && lecture.originalTeacher.isNotBlank()) {
                                                Text(
                                                    text = "Original: ${lecture.originalTeacher}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFFE65100),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = lecture.time,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF7C4DFF)
                                                )
                                            )
                                            if (lecture.adjusted) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Surface(
                                                    color = Color(0xFFE65100).copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "ADJUSTED",
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFFE65100),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                IndiumButton(text = "BACK", onClick = onBack)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

