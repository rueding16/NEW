package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SuccessGreen

@Composable
fun BaaSSystemArchitectureDialog(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("1. Real-Time BaaS", "2. Multi-Channel Gateways", "3. Per-Account Storage", "4. Backend Sample Code")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(680.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("baas_architecture_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkAccentGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = DarkAccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "BaaS & Gateway Architecture",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Interface Control • Real-Time WebSockets • Multi-Channel Messaging",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_arch_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Section 1: Real-Time Data & User Dashboards
                            ArchitectureCard(
                                title = "1. Real-Time Data & User Dashboards (Interface Control)",
                                icon = Icons.Default.Speed,
                                iconTint = SuccessGreen,
                                description = "Ensures each user only controls their own interface while data updates in real-time for customers and admins without reloading.",
                                bulletPoints = listOf(
                                    "Role Separation: Customer dashboards observe only documents tagged with their own userId. Admins stream the centralized approval & verification queue.",
                                    "WebSocket Stream Listeners: Room Flow & Firestore snapshot listeners push instant updates whenever a record changes on device or cloud.",
                                    "Session Persistence: Built-in SessionManager securely saves the login session so users remain logged in across app restarts."
                                ),
                                codeSnippet = """// Android Jetpack Compose Real-Time User Isolation
val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
val userLoans by viewModel.userLoans.collectAsStateWithLifecycle()

// Customer view only binds user's own loans
if (currentUser?.role == "CUSTOMER") {
    CustomerDashboardScreen(loans = userLoans)
} else {
    AdminDashboardScreen(allLoans = allLoans)
}"""
                            )
                        }

                        1 -> {
                            // Section 2: Multi-Channel Messaging & Notifications
                            ArchitectureCard(
                                title = "2. Multi-Channel Messaging Gateways",
                                icon = Icons.Default.Message,
                                iconTint = DarkAccentGold,
                                description = "Connects the financial platform to external communication API gateways for SMS, Email, and Push notifications.",
                                bulletPoints = listOf(
                                    "Twilio SMS API: Dedicated phone number (+1 888-555-LOAN) dispatches instant SMS alerts for loan approvals, OTP codes, and disbursement notices.",
                                    "SendGrid / Postmark: Transactional email engine automatically dispatches official loan contracts, repayment receipts, and compliance logs.",
                                    "Firebase Cloud Messaging (FCM): Direct push alerts on the user's device screen immediately when status or balance changes."
                                ),
                                codeSnippet = """// Gateway Dispatch Engine Example
fun dispatchApprovalNotice(loan: Loan, customer: User) {
    // 1. Twilio SMS
    twilioService.sendSms(to = customer.phone, body = "Loan approved! Funds ready.")
    // 2. SendGrid Email
    sendGridService.sendReceipt(to = customer.email, subject = "Official Approval")
    // 3. FCM Push Alert
    fcmService.sendPush(token = customer.fcmToken, title = "Approved! 🎉")
}"""
                            )
                        }

                        2 -> {
                            // Section 3: File Storage Per Account
                            ArchitectureCard(
                                title = "3. File Storage Per Account (Document Vault)",
                                icon = Icons.Default.FolderSpecial,
                                iconTint = PrimaryRed,
                                description = "Per-account document vault isolating government IDs, payslips, and proof documents per user ID.",
                                bulletPoints = listOf(
                                    "Strict Security Rules: Enforces 'allow read, write: if request.auth.uid == userId;'. Customer A cannot access Customer B's files.",
                                    "Cryptographic Integrity: Every uploaded document generates a SHA-256 integrity hash to prevent tampering.",
                                    "Storage Cloud Integration: Pre-configured for Firebase Cloud Storage and AWS S3 object stores."
                                ),
                                codeSnippet = """// Firestore Security Rule for Per-Account Isolation
match /user_documents/{documentId} {
  allow read: if isAuthenticated() && (resource.data.userId == request.auth.uid || isAdmin());
  allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
  allow update, delete: if isAuthenticated() && (resource.data.userId == request.auth.uid || isAdmin());
}"""
                            )
                        }

                        3 -> {
                            // Section 4: Sample Code Setup
                            ArchitectureCard(
                                title = "4. Production Setup Script (Node.js & Kotlin)",
                                icon = Icons.Default.Code,
                                iconTint = SuccessGreen,
                                description = "Backend setup script initializing Firebase, Twilio SMS, and SendGrid Email transactional services.",
                                bulletPoints = listOf(
                                    "Initialize Firebase Admin SDK with Service Account credentials.",
                                    "Attach Twilio client using Account SID and Auth Token.",
                                    "Configure SendGrid mail provider with transactional API key."
                                ),
                                codeSnippet = """// backend-gateway-init.js (Node.js / Express / Cloud Functions)
const admin = require('firebase-admin');
const twilio = require('twilio');
const sgMail = require('@sendgrid/mail');

// 1. Initialize Firebase BaaS
admin.initializeApp({
  credential: admin.credential.applicationDefault(),
  databaseURL: "https://credipulse-loan-default-rtdb.firebaseio.com"
});
const db = admin.firestore();

// 2. Initialize Twilio SMS Client
const twilioClient = twilio(process.env.TWILIO_SID, process.env.TWILIO_AUTH_TOKEN);

// 3. Initialize SendGrid Transactional Mail
sgMail.setApiKey(process.env.SENDGRID_API_KEY);

// Realtime Firestore listener triggering multi-channel messages
db.collection('loans').where('status', '==', 'APPROVED').onSnapshot(snapshot => {
  snapshot.docChanges().forEach(async change => {
    if (change.type === 'added' || change.type === 'modified') {
      const loan = change.doc.data();
      // Send Twilio SMS
      await twilioClient.messages.create({
        body: `CrediPulse: Loan #${'$'}{loan.id} approved for ₱${'$'}{loan.principalAmount}!`,
        from: '+18885555626',
        to: loan.customerPhone
      });
      // Send SendGrid Email
      await sgMail.send({
        to: loan.customerEmail,
        from: 'receipts@credipulse.com',
        subject: `Approval Notice - Loan #${'$'}{loan.id}`,
        text: `Your loan of ₱${'$'}{loan.principalAmount} is now approved.`
      });
    }
  });
});"""
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchitectureCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    description: String,
    bulletPoints: List<String>,
    codeSnippet: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            bulletPoints.forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("•", fontSize = 14.sp, color = iconTint, fontWeight = FontWeight.Black)
                    Text(point, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Code Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = codeSnippet,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF81C784)
                )
            }
        }
    }
}
