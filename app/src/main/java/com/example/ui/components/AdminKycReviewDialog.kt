package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserEntity
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.DarkPrimaryRed
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SuccessGreen

@Composable
fun AdminKycReviewDialog(
    customer: UserEntity,
    onApprove: (userId: Long, remarks: String) -> Unit,
    onReject: (userId: Long, remarks: String) -> Unit,
    onDismiss: () -> Unit
) {
    var remarks by remember { mutableStateOf("Compliance check verified. Valid identity and income documents.") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("admin_kyc_review_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Customer KYC Verification",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Admin compliance & identity verification review",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Information Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(label = "Full Name", value = customer.fullName)
                        InfoRow(label = "Username", value = "@${customer.username}")
                        InfoRow(label = "Email Address", value = customer.email)
                        InfoRow(label = "Contact Phone", value = customer.phone)
                        InfoRow(label = "Government ID Type", value = customer.idType)
                        InfoRow(label = "Encrypted ID Number", value = customer.idNumberEncrypted)
                        InfoRow(label = "Employment Type", value = customer.employmentType)
                        InfoRow(label = "Company / Employer", value = customer.companyName.ifBlank { "N/A" })
                        InfoRow(label = "Declared Monthly Income", value = "₱${"%,.2f".format(customer.monthlyIncome)}")
                        InfoRow(label = "Proof Documents", value = customer.proofDocInfo.ifBlank { "Government ID scan submitted" })
                        InfoRow(
                            label = "Current Status",
                            value = customer.kycStatus,
                            isStatus = true,
                            statusColor = when (customer.kycStatus) {
                                "VERIFIED" -> SuccessGreen
                                "PENDING_VERIFICATION" -> DarkAccentGold
                                else -> DarkPrimaryRed
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                EncryptedBadge(label = "Identity records stored in 256-bit AES encrypted store")

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Admin Compliance Remarks") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_kyc_remarks_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReject(customer.id, remarks) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_reject_kyc_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkPrimaryRed
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject KYC", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onApprove(customer.id, remarks) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_approve_kyc_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify & Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isStatus: Boolean = false,
    statusColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isStatus) FontWeight.Bold else FontWeight.Medium,
            color = if (isStatus) statusColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
