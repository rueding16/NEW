package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.CommunicationMessageEntity
import com.example.data.model.LoanApplicationEntity
import com.example.data.model.RepaymentScheduleEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserDocumentEntity
import com.example.data.model.UserEntity
import com.example.domain.CreditRiskAssessment
import com.example.ui.components.CreditScoreCard
import com.example.ui.components.EncryptedBadge
import com.example.ui.components.LoanCard
import com.example.ui.components.TamperProofHashBadge
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.DarkPrimaryRed
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.RealtimeSyncState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    user: UserEntity,
    loans: List<LoanApplicationEntity>,
    schedules: List<RepaymentScheduleEntity>,
    transactions: List<TransactionEntity>,
    creditAssessment: CreditRiskAssessment,
    syncState: RealtimeSyncState? = null,
    onTriggerSyncPulse: (() -> Unit)? = null,
    onApplyLoan: () -> Unit,
    onWithdrawBalance: (LoanApplicationEntity) -> Unit,
    onPaySchedule: (RepaymentScheduleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Loans", "Repayments", "Transactions", "KYC Profile")

    // Calculate total available balance across all approved loans
    val totalAvailableBalance = loans
        .filter { it.status == "APPROVED" || it.status == "ACTIVE" }
        .sumOf { it.availableBalance }

    val pendingRepayments = schedules.filter { it.status != "PAID" }

    val approvedLoanForWithdrawal = loans.firstOrNull { it.status == "APPROVED" && it.availableBalance > 0 }
        ?: loans.firstOrNull { it.availableBalance > 0 }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Online Realtime Stream Status Bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTriggerSyncPulse?.invoke() }
                        .testTag("realtime_status_pill"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Text(
                                text = "ONLINE REAL-TIME SYNC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                            Text(
                                text = "•",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = syncState?.latestActionMessage ?: "Zero Latency Stream",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Tap to sync pulse",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                // Available Balance Hero Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_balance_hero_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available Loan Balance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            EncryptedBadge(label = "Instant Payout")
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "₱${"%,.2f".format(totalAvailableBalance)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = if (totalAvailableBalance > 0)
                                "Approved credit line ready for E-Wallet or Bank Transfer"
                            else
                                "No active balance available. Submit a loan application to get funded.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (approvedLoanForWithdrawal != null) {
                                        onWithdrawBalance(approvedLoanForWithdrawal)
                                    } else {
                                        onApplyLoan()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("hero_withdraw_funds_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (totalAvailableBalance > 0) SuccessGreen else PrimaryRed
                                )
                            ) {
                                Icon(
                                    imageVector = if (totalAvailableBalance > 0) Icons.Default.Send else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (totalAvailableBalance > 0) "Withdraw / Transfer" else "Apply for Loan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onApplyLoan,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("hero_apply_loan_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Loan", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Realtime Underwriting Credit Score Card
            item {
                CreditScoreCard(assessment = creditAssessment)
            }

            // Navigation Tabs (Loans, Repayments, Transactions, KYC)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("customer_tab_$index")
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // LOANS TAB
                    if (loans.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "No loan applications yet",
                                actionLabel = "Apply for a Loan Now",
                                onAction = onApplyLoan
                            )
                        }
                    } else {
                        items(loans, key = { it.id }) { loan ->
                            LoanCard(
                                loan = loan,
                                isAdminView = false,
                                onWithdrawBalance = onWithdrawBalance,
                                onViewSchedules = { selectedTab = 1 }
                            )
                        }
                    }
                }

                1 -> {
                    // REPAYMENTS & PAYMENT GATEWAY TAB
                    if (pendingRepayments.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "All Caught Up!",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "You have no outstanding loan installments due right now.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(pendingRepayments, key = { it.id }) { schedule ->
                            RepaymentScheduleItemCard(
                                schedule = schedule,
                                onPayNow = { onPaySchedule(schedule) }
                            )
                        }
                    }
                }

                2 -> {
                    // TRANSACTIONS TAB
                    if (transactions.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "No financial transactions recorded yet.",
                                actionLabel = "Apply for Loan",
                                onAction = onApplyLoan
                            )
                        }
                    } else {
                        items(transactions, key = { it.id }) { txn ->
                            TransactionItemCard(txn = txn)
                        }
                    }
                }

                3 -> {
                    // KYC & IDENTITY PROFILE TAB
                    item {
                        KycProfileCard(user = user)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to Apply for Loan
        ExtendedFloatingActionButton(
            onClick = onApplyLoan,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Apply for Loan", fontWeight = FontWeight.Bold) },
            containerColor = PrimaryRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("customer_apply_loan_fab")
        )
    }
}

@Composable
fun RepaymentScheduleItemCard(
    schedule: RepaymentScheduleEntity,
    onPayNow: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDateStr = dateFormat.format(Date(schedule.dueDateMillis))
    val isPaid = schedule.status == "PAID"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("repayment_schedule_item_${schedule.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPaid) SuccessGreen.copy(alpha = 0.15f) else PrimaryRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPaid) Icons.Default.Check else Icons.Default.Payment,
                        contentDescription = null,
                        tint = if (isPaid) SuccessGreen else PrimaryRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Installment #${schedule.installmentNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isPaid) "Paid on ${dateFormat.format(Date(schedule.paidDateMillis ?: 0L))}" else "Due on $dueDateStr",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₱${"%,.2f".format(schedule.amountDue + schedule.penaltyAmount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isPaid) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("PAID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                } else {
                    Button(
                        onClick = onPayNow,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("pay_schedule_btn_${schedule.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed
                        )
                    ) {
                        Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(txn: TransactionEntity) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val isWithdrawal = txn.transactionType == "DISBURSEMENT_WITHDRAWAL"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${txn.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isWithdrawal) SuccessGreen.copy(alpha = 0.15f) else PrimaryRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWithdrawal) Icons.Default.Send else Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (isWithdrawal) SuccessGreen else PrimaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isWithdrawal) "Withdrawal to ${txn.channel}" else "Repayment via ${txn.channel}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateFormat.format(Date(txn.timestamp)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${if (isWithdrawal) "-" else "+"}₱${"%,.2f".format(txn.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isWithdrawal) SuccessGreen else PrimaryRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${txn.referenceNumber}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TamperProofHashBadge(hash = txn.securityHash)
            }
        }
    }
}

@Composable
fun KycProfileCard(user: UserEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kyc_profile_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = when (user.kycStatus) {
                            "VERIFIED" -> SuccessGreen
                            "PENDING_VERIFICATION" -> DarkAccentGold
                            else -> DarkPrimaryRed
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "KYC Verification Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (user.kycStatus) {
                                "VERIFIED" -> SuccessGreen.copy(alpha = 0.15f)
                                "PENDING_VERIFICATION" -> DarkAccentGold.copy(alpha = 0.15f)
                                else -> DarkPrimaryRed.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = user.kycStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (user.kycStatus) {
                            "VERIFIED" -> SuccessGreen
                            "PENDING_VERIFICATION" -> DarkAccentGold
                            else -> DarkPrimaryRed
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            ProfileField(label = "Legal Name", value = user.fullName)
            ProfileField(label = "Contact Email", value = user.email)
            ProfileField(label = "Phone Number", value = user.phone)
            ProfileField(label = "Government ID Type", value = user.idType)
            ProfileField(label = "Encrypted ID Number", value = user.idNumberEncrypted)
            ProfileField(label = "Employment Sector", value = user.employmentType)
            ProfileField(label = "Company / Employer", value = user.companyName.ifBlank { "Not specified" })
            ProfileField(label = "Monthly Declared Income", value = "₱${"%,.2f".format(user.monthlyIncome)}")
            ProfileField(label = "Uploaded Proof Info", value = user.proofDocInfo.ifBlank { "National ID document" })

            if (user.kycRejectionReason.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkPrimaryRed.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Admin Note: ${user.kycRejectionReason}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            EncryptedBadge(label = "End-to-End Cryptographically Protected Identity")
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptyStateCard(message: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text(actionLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
