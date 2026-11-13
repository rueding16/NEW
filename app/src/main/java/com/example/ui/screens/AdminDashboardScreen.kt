package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntity
import com.example.data.model.LoanApplicationEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.ui.components.AdminKycReviewDialog
import com.example.ui.components.AdminLoanApprovalDialog
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
fun AdminDashboardScreen(
    adminUser: UserEntity,
    allLoans: List<LoanApplicationEntity>,
    allCustomers: List<UserEntity>,
    allTransactions: List<TransactionEntity>,
    allAuditLogs: List<AuditLogEntity>,
    syncState: RealtimeSyncState? = null,
    onSimulateIncomingLoan: (() -> Unit)? = null,
    onTriggerSyncPulse: (() -> Unit)? = null,
    onApproveLoan: (loanId: Long, remarks: String) -> Unit,
    onRejectLoan: (loanId: Long, remarks: String) -> Unit,
    onUpdateKycStatus: (userId: Long, status: String, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Loan Queue", "KYC Review", "Risk Analytics", "Transactions", "Audit Trail")

    // Active inspection dialogs
    var inspectingLoan by remember { mutableStateOf<LoanApplicationEntity?>(null) }
    var inspectingCustomer by remember { mutableStateOf<UserEntity?>(null) }

    // KPI stats calculations
    val pendingLoansCount = allLoans.count { it.status == "PENDING_APPROVAL" }
    val pendingKycCount = allCustomers.count { it.kycStatus == "PENDING_VERIFICATION" }
    val totalDisbursed = allLoans.sumOf { it.disbursedTotal }
    val totalLoansValue = allLoans.sumOf { it.principalAmount }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Realtime Stream Status Card for Admin
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("admin_realtime_status_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ONLINE REAL-TIME STREAM ACTIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                                Text(
                                    text = syncState?.latestActionMessage ?: "Streaming database events zero-latency",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (onSimulateIncomingLoan != null) {
                            Button(
                                onClick = onSimulateIncomingLoan,
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("simulate_incoming_loan_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryRed
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulate Loan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                // Admin Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_header_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
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
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(26.dp)
                                )
                                Column {
                                    Text(
                                        text = "Admin Control Center",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Centralized loan underwriting, approvals & KYC verification",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            EncryptedBadge(label = "Master Access")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // KPI 4-grid stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiTile(
                                label = "Pending Loans",
                                value = "$pendingLoansCount",
                                icon = Icons.Default.HourglassTop,
                                tint = if (pendingLoansCount > 0) DarkAccentGold else SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )

                            KpiTile(
                                label = "KYC Queue",
                                value = "$pendingKycCount",
                                icon = Icons.Default.VerifiedUser,
                                tint = if (pendingKycCount > 0) DarkAccentGold else SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiTile(
                                label = "Total Volume",
                                value = "₱${"%,.0f".format(totalLoansValue)}",
                                icon = Icons.Default.Payments,
                                tint = PrimaryRed,
                                modifier = Modifier.weight(1f)
                            )

                            KpiTile(
                                label = "Disbursed",
                                value = "₱${"%,.0f".format(totalDisbursed)}",
                                icon = Icons.Default.CheckCircle,
                                tint = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Scrollable Tab Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    tabTitles.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("admin_tab_$idx")
                        )
                    }
                }
            }

            // Content
            when (selectedTab) {
                0 -> {
                    // LOAN QUEUE TAB
                    if (allLoans.isEmpty()) {
                        item {
                            EmptyAdminCard("No loans have been applied for yet.")
                        }
                    } else {
                        items(allLoans, key = { it.id }) { loan ->
                            LoanCard(
                                loan = loan,
                                isAdminView = true,
                                onApproveLoan = { inspectingLoan = loan },
                                onRejectLoan = { inspectingLoan = loan }
                            )
                        }
                    }
                }

                1 -> {
                    // KYC CUSTOMER REVIEW TAB
                    if (allCustomers.isEmpty()) {
                        item {
                            EmptyAdminCard("No registered customer accounts yet.")
                        }
                    } else {
                        items(allCustomers, key = { it.id }) { customer ->
                            CustomerKycCard(
                                customer = customer,
                                onReviewKyc = { inspectingCustomer = customer }
                            )
                        }
                    }
                }

                2 -> {
                    // RISK & SCORING ANALYTICS TAB
                    item {
                        RiskAnalyticsOverviewCard(loans = allLoans, customers = allCustomers)
                    }
                }

                3 -> {
                    // FINANCIAL TRANSACTIONS TAB
                    if (allTransactions.isEmpty()) {
                        item {
                            EmptyAdminCard("No financial transactions processed yet.")
                        }
                    } else {
                        items(allTransactions, key = { it.id }) { txn ->
                            TransactionItemCard(txn = txn)
                        }
                    }
                }

                4 -> {
                    // SECURITY AUDIT TRAIL TAB
                    if (allAuditLogs.isEmpty()) {
                        item {
                            EmptyAdminCard("Audit ledger is empty.")
                        }
                    } else {
                        items(allAuditLogs, key = { it.id }) { log ->
                            AuditLogItemCard(log = log)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Dialogs
        inspectingLoan?.let { loan ->
            AdminLoanApprovalDialog(
                loan = loan,
                onApprove = { id, remarks ->
                    onApproveLoan(id, remarks)
                    inspectingLoan = null
                },
                onReject = { id, remarks ->
                    onRejectLoan(id, remarks)
                    inspectingLoan = null
                },
                onDismiss = { inspectingLoan = null }
            )
        }

        inspectingCustomer?.let { customer ->
            AdminKycReviewDialog(
                customer = customer,
                onApprove = { id, remarks ->
                    onUpdateKycStatus(id, "VERIFIED", remarks)
                    inspectingCustomer = null
                },
                onReject = { id, remarks ->
                    onUpdateKycStatus(id, "REJECTED", remarks)
                    inspectingCustomer = null
                },
                onDismiss = { inspectingCustomer = null }
            )
        }
    }
}

@Composable
fun CustomerKycCard(
    customer: UserEntity,
    onReviewKyc: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReviewKyc() }
            .testTag("admin_customer_kyc_card_${customer.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        .clip(CircleShape)
                        .background(
                            when (customer.kycStatus) {
                                "VERIFIED" -> SuccessGreen.copy(alpha = 0.15f)
                                "PENDING_VERIFICATION" -> DarkAccentGold.copy(alpha = 0.15f)
                                else -> DarkPrimaryRed.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = when (customer.kycStatus) {
                            "VERIFIED" -> SuccessGreen
                            "PENDING_VERIFICATION" -> DarkAccentGold
                            else -> DarkPrimaryRed
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = customer.fullName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${customer.idType} • Monthly: ₱${"%,.0f".format(customer.monthlyIncome)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (customer.kycStatus) {
                            "VERIFIED" -> SuccessGreen.copy(alpha = 0.15f)
                            "PENDING_VERIFICATION" -> DarkAccentGold.copy(alpha = 0.15f)
                            else -> DarkPrimaryRed.copy(alpha = 0.15f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = customer.kycStatus,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (customer.kycStatus) {
                        "VERIFIED" -> SuccessGreen
                        "PENDING_VERIFICATION" -> DarkAccentGold
                        else -> DarkPrimaryRed
                    }
                )
            }
        }
    }
}

@Composable
fun RiskAnalyticsOverviewCard(
    loans: List<LoanApplicationEntity>,
    customers: List<UserEntity>
) {
    val lowRiskCount = loans.count { it.riskTier == "LOW" }
    val modRiskCount = loans.count { it.riskTier == "MODERATE" }
    val highRiskCount = loans.count { it.riskTier == "HIGH" }
    val avgScore = if (loans.isNotEmpty()) loans.map { it.riskScore }.average().toInt() else 650

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("risk_analytics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = PrimaryRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Portfolio Credit Risk Metrics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Avg Score: $avgScore",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Text(
                text = "Automated credit risk assessment distribution across all borrower applications:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Risk tier indicators
            RiskTierRow(label = "Low Risk (Prime 720+)", count = lowRiskCount, color = SuccessGreen)
            RiskTierRow(label = "Moderate Risk (Standard 600-719)", count = modRiskCount, color = DarkAccentGold)
            RiskTierRow(label = "High Risk (<600 Underwriting Scrutiny)", count = highRiskCount, color = DarkPrimaryRed)

            Spacer(modifier = Modifier.height(4.dp))
            EncryptedBadge(label = "End-to-End Cryptographically Audited Underwriting")
        }
    }
}

@Composable
private fun RiskTierRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(text = "$count borrower(s)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AuditLogItemCard(log: AuditLogEntity) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_log_item_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.action,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Actor: ${log.actor}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = log.details,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            TamperProofHashBadge(hash = log.integrityHash)
        }
    }
}

@Composable
private fun KpiTile(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun EmptyAdminCard(msg: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = msg, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
