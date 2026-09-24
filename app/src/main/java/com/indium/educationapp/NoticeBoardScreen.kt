package com.indium.educationapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlinx.coroutines.launch
import com.indium.educationapp.ui.theme.IndiumLavender
import com.indium.educationapp.ui.theme.IndiumDeepViolet
import com.indium.educationapp.ui.theme.IndiumWhite

private const val NOTICE_API = "https://script.google.com/macros/s/AKfycbwUl2MQJGc8NEhIHN7i1epOB6TkzBfcIxAVxeH_hrt4AwFCTisA-SHjwR3MIHUeUEyheQ/exec"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeBoardScreen(
    userRole: String,
    userName: String,
    onBack: () -> Unit
) {
    var notices by remember { mutableStateOf<List<Notice>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val isAdmin = userRole.equals("Admin", ignoreCase = true)
    val isTeacher = userRole.equals("Teacher", ignoreCase = true)

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf(if (isTeacher) "Students" else "All") }
    var audienceExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    suspend fun publishNotice() {
        if (title.isBlank() || message.isBlank()) {
            statusMessage = "Please fill all fields"
            return
        }
        loading = true
        statusMessage = ""
        try {
            val newNotice = hashMapOf(
                "title" to title.trim(),
                "content" to message.trim(),
                "author" to userName,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("notices").add(newNotice)
            statusMessage = "Notice published successfully!"
            title = ""
            message = ""
        } catch (e: Exception) {
            statusMessage = "Error: ${e.localizedMessage}"
        } finally {
            loading = false
        }
    }

    suspend fun submitNotice() {

        if (title.isBlank() || message.isBlank()) {
            statusMessage = "Please enter both title and message."
            return
        }

        loading = true
        statusMessage = ""

        try {

            val payload = JSONObject().apply {
                put("action", "submit")
                put("title", title.trim())
                put("message", message.trim())
                put("audience", audience)
                put("postedBy", userName)
            }

            val result = withContext(Dispatchers.IO) {

                val connection =
                    URL(NOTICE_API).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8"
                )

                try {

                    connection.outputStream.use {
                        it.write(
                            payload.toString().toByteArray(Charsets.UTF_8)
                        )
                    }

                    val responseCode = connection.responseCode

                    val stream = if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                    stream?.bufferedReader()?.use {
                        it.readText()
                    } ?: ""

                } finally {
                    connection.disconnect()
                }
            }

            val json = JSONObject(result)

            if (json.optBoolean("success")) {

                statusMessage = "Notice submitted for Admin approval."

                title = ""
                message = ""

            } else {

                statusMessage = json.optString(
                    "message",
                    "Notice submission failed."
                )
            }

        } catch (e: Exception) {

            statusMessage =
                "Submission error: ${e.localizedMessage ?: "Please try again."}"

        } finally {

            loading = false
        }
    }

    LaunchedEffect(Unit) {
        db.collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    notices = snapshot.documents.mapNotNull { doc ->
                        val n = doc.toObject(Notice::class.java)
                        n?.copy(id = doc.id)
                    }
                }
            }
    }

    Scaffold(
        containerColor = Color(0xFFF7F4FF)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isAdmin || isTeacher) {
                item {
                    IndiumCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isAdmin) "Publish a Notice" else "Submit a Notice for Approval",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = IndiumDeepViolet
                                )
                            )
                            Spacer(Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                label = { Text("Message") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            // Audience selection for Admin and Teacher

                            Text(
                                text = "Select Notice Audience",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = IndiumDeepViolet
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { audienceExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Audience: $audience",
                                        color = IndiumDeepViolet
                                    )
                                }

                                DropdownMenu(
                                    expanded = audienceExpanded,
                                    onDismissRequest = {
                                        audienceExpanded = false
                                    }
                                ) {
                                    (if (isTeacher) listOf("Students") else listOf("All", "Teachers", "Students")).forEach { option ->


                                        DropdownMenuItem(
                                            text = {
                                                Text(option)
                                            },
                                            onClick = {
                                                audience = option
                                                audienceExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    scope.launch {
                                        if (isAdmin) {
                                            publishNotice()
                                        } else if (isTeacher) {
                                            submitNotice()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = IndiumLavender
                                )
                            ) {
                                Text(
                                    text = if (isAdmin) "PUBLISH NOTICE" else "SUBMIT FOR APPROVAL",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            if (statusMessage.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = statusMessage,
                                    color = if (statusMessage.contains("success", true) || statusMessage.contains("submitted", true)) Color(0xFF4CAF50) else Color.Red,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            if (notices.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFE6DDFB))
                            Spacer(Modifier.height(16.dp))
                            Text("No notices yet", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(notices) { notice ->
                    NoticeCard(notice, userRole) {
                        db.collection("notices").document(notice.id).delete()
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeCard(notice: Notice, userRole: String, onDelete: () -> Unit) {
    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notice.timestamp))
    
    IndiumCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                )
                if (userRole == "Admin") {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = notice.content, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF252238))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "By ${notice.author}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AddNoticeDialog(userName: String, onDismiss: () -> Unit, onPost: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post New Notice", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            IndiumButton(text = "POST", onClick = { if (title.isNotBlank() && content.isNotBlank()) onPost(title, content) }, containerColor = Color(0xFF7C4DFF))
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        }
    )
}

data class Notice(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "",
    val timestamp: Long = 0L
)
