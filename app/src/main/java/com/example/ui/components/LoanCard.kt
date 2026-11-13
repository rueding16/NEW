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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanApplicationEntity
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.DarkPrimaryRed
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoanCard(
    loan: LoanApplicationEntity,
    isAdminView: Boolean = false,
    onApproveLoan: ((Long) -> Unit)? = null,
    onRejectLoan: ((Long) -> Unit)? = null,
    onWithdrawBalance: ((LoanApplicationEntity) -> Unit)? = null,
    onViewSchedules: ((LoanApplicationEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusColor, statusBg) = when (loan.status) {
        "PENDING_APPROVAL" -> Triple("Under Review", DarkAccentGold, DarkAccentGold.copy(alpha = 0.15f))
        "APPROVED" -> Triple("Approved & Ready", SuccessGreen, SuccessGreen.copy(alpha = 0.15f))
        "ACTIVE" -> Triple("Active Loan", Color(0xFF38BDF8), Color(0xFF38BDF8).copy(alpha = 0.15f))
        "COMPLETED" -> Triple("Fully Paid 🎉", Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
        "REJECTED" -> Triple("Declined", DarkPrimaryRed, DarkPrimaryRed.copy(alpha = 0.15f))
        else -> Triple(loan.status, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
    }

    val icon: ImageVector = when {
        loan.loanType.contains("Gadget", ignoreCase = true) || loan.loanType.contains("Phone", ignoreCase = true) -> Icons.Default.PhoneAndroid
        loan.loanType.contains("Appliance", ignoreCase = true) -> Icons.Default.Tv
        loan.loanType.contains("Salary", ignoreCase = true) -> Icons.Default.Savings
        else -> Icons.Default.AccountBalanceWallet
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val appliedDateStr = dateFormat.format(Date(loan.appliedAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("loan_card_${loan.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
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
                            .background(PrimaryRed.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = loan.loanType,
                            tint = PrimaryRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = loan.loanType,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAdminView) "Borrower: ${loan.customerName}" else "Applied on $appliedDateStr",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Financial Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Loan Principal",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₱${"%,.2f".format(loan.principalAmount)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Monthly Installment",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₱${"%,.2f".format(loan.monthlyInstallment)} /mo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryRed
                    )
                    Text(
                        text = "${loan.tenorMonths} mos @ ${(loan.monthlyInterestRate * 100).toInt()}%/mo",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (loan.status == "APPROVED") "Available Balance" else "Total Due",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (loan.status == "APPROVED") "₱${"%,.2f".format(loan.availableBalance)}" else "₱${"%,.2f".format(loan.remainingBalance)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (loan.status == "APPROVED") SuccessGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Repayment Progress if Active
            if (loan.status == "ACTIVE" || loan.status == "COMPLETED") {
                Spacer(modifier = Modifier.height(12.dp))
                val paidSoFar = (loan.totalRepayment - loan.remainingBalance).coerceAtLeast(0.0)
                val progressFraction = if (loan.totalRepayment > 0) (paidSoFar / loan.totalRepayment).toFloat().coerceIn(0f, 1f) else 0f

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Repayment Progress",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}% Paid",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SuccessGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Security hash badge
            if (loan.e2eHash.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                TamperProofHashBadge(hash = loan.e2eHash)
            }

            // Remarks / Purpose note
            if (loan.purpose.isNotBlank() || loan.adminRemarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (loan.adminRemarks.isNotBlank()) "Admin: ${loan.adminRemarks}" else "Purpose: ${loan.purpose}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            // Actions for Admin or Customer
            Spacer(modifier = Modifier.height(12.dp))

            if (isAdminView) {
                // Admin Actions: Approve or Reject
                if (loan.status == "PENDING_APPROVAL") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRejectLoan?.invoke(loan.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_reject_loan_${loan.id}"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DarkPrimaryRed
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onApproveLoan?.invoke(loan.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_approve_loan_${loan.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve Loan", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Customer Actions: Withdraw balance or View Payment Schedules
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (loan.status == "APPROVED" && loan.availableBalance > 0) {
                        Button(
                            onClick = { onWithdrawBalance?.invoke(loan) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("withdraw_loan_balance_${loan.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen
                            )
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw / Transfer Funds", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (loan.status == "ACTIVE" || loan.status == "APPROVED" || loan.status == "COMPLETED") {
                        OutlinedButton(
                            onClick = { onViewSchedules?.invoke(loan) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_repayments_${loan.id}")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Repayments", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
