package com.indium.educationapp

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Refresh
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
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlinx.coroutines.launch
import com.indium.educationapp.ui.theme.IndiumLavender
import com.indium.educationapp.ui.theme.IndiumDeepViolet
import com.indium.educationapp.ui.theme.IndiumWhite
import org.json.JSONArray

private const val NOTICE_API = "https://script.google.com/macros/s/AKfycbwUl2MQJGc8NEhIHN7i1epOB6TkzBfcIxAVxeH_hrt4AwFCTisA-SHjwR3MIHUeUEyheQ/exec"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeBoardScreen(
    userRole: String,
    userName: String,
    onBack: () -> Unit,
    onNoticeHistoryStateChange: (Boolean) -> Unit = {}
) {    BackHandler {
    onBack()
}
    var pendingNotices by remember { mutableStateOf<List<PendingNotice>>(emptyList()) }
    var loadingPending by remember { mutableStateOf(false) }
    var pendingError by remember { mutableStateOf("") }

    var showNoticeHistoryScreen by remember { mutableStateOf(false) }
    var historyRecords by remember { mutableStateOf<List<NoticeHistoryRecord>>(emptyList()) }
    var loadingHistory by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf("") }

    LaunchedEffect(showNoticeHistoryScreen) {
        onNoticeHistoryStateChange(showNoticeHistoryScreen)
    }

    var selectedRejectNoticeId by remember { mutableStateOf<String?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var rejectionValidationError by remember { mutableStateOf("") }

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

    suspend fun fetchPendingNotices() {
        if (!isAdmin) return
        loadingPending = true
        pendingError = ""
        try {
            val result = withContext(Dispatchers.IO) {
                val urlString = "$NOTICE_API?action=pending"
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
            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success")) {
                    val array = json.optJSONArray("notices") ?: JSONArray()
                    val list = mutableListOf<PendingNotice>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            PendingNotice(
                                noticeId = obj.optString("noticeId"),
                                title = obj.optString("title"),
                                message = obj.optString("message"),
                                audience = obj.optString("audience"),
                                postedBy = obj.optString("postedBy"),
                                date = obj.optString("date"),
                                status = obj.optString("status")
                            )
                        )
                    }
                    pendingNotices = list
                } else {
                    pendingError = json.optString("message", "Failed to load pending notices.")
                }
            } else {
                pendingError = "Unable to connect to pending notices server."
            }
        } catch (e: Exception) {
            pendingError = "Error: ${e.localizedMessage}"
        } finally {
            loadingPending = false
        }
    }

    suspend fun fetchNoticeHistory() {
        loadingHistory = true
        historyError = ""
        try {
            val urlString = if (isAdmin) {
                "$NOTICE_API?action=history"
            } else {
                val encodedName = URLEncoder.encode(userName, "UTF-8")
                "$NOTICE_API?action=history&postedBy=$encodedName"
            }

            val result = withContext(Dispatchers.IO) {
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

            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success")) {
                    val array = json.optJSONArray("notices") ?: JSONArray()
                    val list = mutableListOf<NoticeHistoryRecord>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            NoticeHistoryRecord(
                                noticeId = obj.optString("noticeId"),
                                date = obj.optString("date"),
                                title = obj.optString("title"),
                                message = obj.optString("message"),
                                audience = obj.optString("audience"),
                                postedBy = obj.optString("postedBy"),
                                active = obj.optBoolean("active", false),
                                status = obj.optString("status"),
                                adminRemark = obj.optString("adminRemark"),
                                decisionDate = obj.optString("decisionDate")
                            )
                        )
                    }
                    historyRecords = list.reversed()
                } else {
                    historyError = json.optString("message", "Failed to load history.")
                }
            } else {
                historyError = "Unable to connect to history server."
            }
        } catch (e: Exception) {
            historyError = "Error: ${e.localizedMessage}"
        } finally {
            loadingHistory = false
        }
    }

    suspend fun processPendingNotice(noticeId: String, actionName: String, adminRemark: String = "") {
        loadingPending = true
        try {
            val payload = JSONObject().apply {
                put("action", actionName)
                put("adminKey", "7887799174")
                put("noticeId", noticeId)
                if (actionName == "reject" && adminRemark.isNotBlank()) {
                    put("adminRemark", adminRemark.trim())
                }
            }
            withContext(Dispatchers.IO) {
                val connection = URL(NOTICE_API).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                try {
                    connection.outputStream.use {
                        it.write(payload.toString().toByteArray(Charsets.UTF_8))
                    }
                    val responseCode = connection.responseCode
                    val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                    stream?.bufferedReader()?.use { it.readText() } ?: ""
                } finally {
                    connection.disconnect()
                }
            }
            fetchPendingNotices()
        } catch (e: Exception) {
            pendingError = "Error: ${e.localizedMessage}"
            loadingPending = false
        }
    }

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
                "timestamp" to System.currentTimeMillis(),
                "audience" to audience
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

    var firestoreNotices by remember { mutableStateOf<List<Notice>>(emptyList()) }
    var apiNotices by remember { mutableStateOf<List<Notice>>(emptyList()) }

    val isStudent = !isAdmin && !isTeacher

    suspend fun fetchApiNotices() {
        try {
            val result = withContext(Dispatchers.IO) {
                val urlString = "$NOTICE_API?action=list"
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

            if (result != null) {
                val json = JSONObject(result)
                if (json.optBoolean("success")) {
                    val array = json.optJSONArray("notices") ?: JSONArray()
                    val list = mutableListOf<Notice>()
                    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)

                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val noticeId = obj.optString("noticeId")
                        val noticeTitle = obj.optString("title")
                        val noticeMessage = obj.optString("message")
                        val noticeAudience = obj.optString("audience")
                        val noticePostedBy = obj.optString("postedBy")
                        val noticeDateStr = obj.optString("date")
                        val status = obj.optString("status")

                        val matchesAudience = if (isStudent) {
                            noticeAudience.equals("Students", ignoreCase = true) ||
                                    noticeAudience.equals("All", ignoreCase = true)
                        } else true

                        val isApproved = status.contains("APPROVED", ignoreCase = true) || obj.optBoolean("active", false)

                        if (matchesAudience && isApproved && noticeTitle.isNotBlank()) {
                            val parsedTimestamp = try {
                                sdf.parse(noticeDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }

                            list.add(
                                Notice(
                                    id = noticeId,
                                    title = noticeTitle,
                                    content = noticeMessage,
                                    author = noticePostedBy,
                                    timestamp = parsedTimestamp
                                )
                            )
                        }
                    }
                    apiNotices = list
                }
            }
        } catch (e: Exception) {
            // Keep existing firestoreNotices on network failure
        }
    }

    LaunchedEffect(Unit) {
        fetchApiNotices()
        db.collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    firestoreNotices = snapshot.documents.mapNotNull { doc ->
                        val n = doc.toObject(Notice::class.java)
                        n?.copy(id = doc.id)
                    }
                }
            }
    }

    val notices = remember(firestoreNotices, apiNotices) {
        val combined = (apiNotices + firestoreNotices).distinctBy {
            if (it.id.isNotBlank()) it.id else "${it.title}_${it.content}"
        }
        combined.sortedByDescending { it.timestamp }
    }

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            fetchPendingNotices()
        }
    }

    if (showNoticeHistoryScreen) {
        NoticeHistoryScreen(
            isAdmin = isAdmin,
            historyRecords = historyRecords,
            isLoading = loadingHistory,
            errorMessage = historyError,
            onBack = { showNoticeHistoryScreen = false },
            onRefresh = { scope.launch { fetchNoticeHistory() } }
        )
        return
    }

    if (selectedRejectNoticeId != null) {
        AlertDialog(
            onDismissRequest = {
                selectedRejectNoticeId = null
            },
            title = {
                Text("Reject Notice", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Reason for rejection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF252238)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = {
                            rejectionReasonInput = it
                            if (it.isNotBlank()) {
                                rejectionValidationError = ""
                            }
                        },
                        label = { Text("Reason for rejection") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (rejectionValidationError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = rejectionValidationError,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReasonInput.isBlank()) {
                            rejectionValidationError = "Please enter a rejection reason."
                        } else {
                            val targetId = selectedRejectNoticeId!!
                            val reason = rejectionReasonInput.trim()
                            selectedRejectNoticeId = null
                            scope.launch {
                                processPendingNotice(targetId, "reject", reason)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("CONFIRM REJECT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedRejectNoticeId = null
                    }
                ) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
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

                            Spacer(Modifier.height(12.dp))

                            IndiumOutlinedButton(
                                text = "NOTICE HISTORY",
                                onClick = {
                                    showNoticeHistoryScreen = true
                                    scope.launch { fetchNoticeHistory() }
                                }
                            )
                            
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

            if (isAdmin) {
                item {
                    IndiumCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notices Awaiting Approval",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = IndiumDeepViolet
                                    )
                                )
                                if (loadingPending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = IndiumLavender,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    IconButton(
                                        onClick = { scope.launch { fetchPendingNotices() } },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            tint = IndiumDeepViolet,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (pendingError.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = pendingError,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (pendingNotices.isEmpty() && !loadingPending) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "No pending notices for approval.",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                items(pendingNotices) { pendingNotice ->
                    PendingNoticeCard(
                        notice = pendingNotice,
                        onApprove = {
                            scope.launch { processPendingNotice(pendingNotice.noticeId, "approve") }
                        },
                        onReject = {
                            selectedRejectNoticeId = pendingNotice.noticeId
                            rejectionReasonInput = ""
                            rejectionValidationError = ""
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (notices.isEmpty() && (!isAdmin || pendingNotices.isEmpty())) {
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
fun NoticeHistoryScreen(
    isAdmin: Boolean,
    historyRecords: List<NoticeHistoryRecord>,
    isLoading: Boolean,
    errorMessage: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            IndiumTopBar(title = "Notice History", onBack = onBack)
        },
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
                        IndiumButton(text = "RETRY", onClick = onRefresh, containerColor = Color(0xFF7C4DFF))
                    }
                }
            } else if (historyRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isAdmin) "No notice history found." else "No notices submitted yet.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(historyRecords) { record ->
                        NoticeHistoryCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeHistoryCard(record: NoticeHistoryRecord) {
    val statusUpper = record.status.uppercase()
    val (displayStatus, badgeColor) = when {
        statusUpper.contains("APPROVED") || record.active -> "Approved" to Color(0xFF4CAF50)
        statusUpper.contains("REJECTED") -> "Rejected" to Color(0xFFF44336)
        else -> "Pending" to Color(0xFFFF9800)
    }

    IndiumCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = badgeColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = displayStatus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = record.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF252238)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By ${record.postedBy.ifBlank { "Unknown" }} • Audience: ${record.audience.ifBlank { "All" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                if (record.date.isNotBlank()) {
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            if (record.decisionDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Decision Date: ${record.decisionDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray
                )
            }

            if (record.adminRemark.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.Red.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rejection Remark: ${record.adminRemark}",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
fun PendingNoticeCard(
    notice: PendingNotice,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    IndiumCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color(0xFFFFF8E1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                )
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "PENDING",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notice.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF252238)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By ${notice.postedBy} • Audience: ${notice.audience}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                if (notice.date.isNotBlank()) {
                    Text(
                        text = notice.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("APPROVE", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("REJECT", fontWeight = FontWeight.Bold)
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

data class PendingNotice(
    val noticeId: String = "",
    val title: String = "",
    val message: String = "",
    val audience: String = "",
    val postedBy: String = "",
    val date: String = "",
    val status: String = ""
)

data class NoticeHistoryRecord(
    val noticeId: String = "",
    val date: String = "",
    val title: String = "",
    val message: String = "",
    val audience: String = "",
    val postedBy: String = "",
    val active: Boolean = false,
    val status: String = "",
    val adminRemark: String = "",
    val decisionDate: String = ""
)
