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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.CreditRiskAssessment
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanApplicationDialog(
    initialMaxLimit: Double = 75000.0,
    onCalculateRisk: (Double, Int) -> CreditRiskAssessment,
    onSubmit: (loanType: String, amount: Double, tenor: Int, purpose: String) -> Unit,
    onDismiss: () -> Unit
) {
    val loanTypes = listOf(
        "Personal Cash Loan",
        "Gadget & Phone Loan",
        "Home Appliance Loan",
        "Express Salary Advance"
    )

    var selectedType by remember { mutableStateOf(loanTypes[0]) }
    var expandedDropdown by remember { mutableStateOf(false) }

    var amount by remember { mutableDoubleStateOf(20000.0) }
    var tenorMonths by remember { mutableIntStateOf(6) }
    val tenorOptions = listOf(3, 6, 9, 12, 18, 24)

    var purpose by remember { mutableStateOf("Personal / Emergency needs") }
    var purposeError by remember { mutableStateOf(false) }

    // Realtime risk & interest calculation
    val currentRisk = onCalculateRisk(amount, tenorMonths)
    val interestRate = currentRisk.recommendedInterestRate
    val totalRepayment = amount * (1 + (interestRate * tenorMonths))
    val monthlyDue = totalRepayment / tenorMonths

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("loan_application_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Apply for a Loan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instant credit decision & real-time assessment",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_loan_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Loan Type Selector
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loan Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("loan_type_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        loanTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Requested Amount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "₱${"%,.0f".format(amount)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryRed
                        )
                    }

                    Slider(
                        value = amount.toFloat(),
                        onValueChange = { amount = (Math.round(it / 1000f) * 1000).toDouble() },
                        valueRange = 3000f..100000f,
                        steps = 96,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("loan_amount_slider")
                    )

                    // Quick chip selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(10000.0, 25000.0, 50000.0, 75000.0).forEach { quickVal ->
                            FilterChip(
                                selected = amount == quickVal,
                                onClick = { amount = quickVal },
                                label = { Text("₱${"%,.0f".format(quickVal)}", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tenor Selector
                Column {
                    Text(
                        text = "Repayment Tenor (Months)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tenorOptions.forEach { t ->
                            FilterChip(
                                selected = tenorMonths == t,
                                onClick = { tenorMonths = t },
                                label = { Text("${t}m", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tenor_chip_$t")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Risk & Financial Breakdown Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Installment",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₱${"%,.2f".format(monthlyDue)} /mo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryRed
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Interest Rate",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${"%.1f".format(interestRate * 100)}% / month",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Repayment",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₱${"%,.2f".format(totalRepayment)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Real-time automated underwriting score pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (currentRisk.tier) {
                                        "LOW" -> SuccessGreen.copy(alpha = 0.15f)
                                        "MODERATE" -> DarkAccentGold.copy(alpha = 0.15f)
                                        else -> Color(0xFFFF5252).copy(alpha = 0.15f)
                                    }
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = when (currentRisk.tier) {
                                    "LOW" -> SuccessGreen
                                    "MODERATE" -> DarkAccentGold
                                    else -> Color(0xFFFF5252)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "Estimated Risk Tier: ${currentRisk.tierLabel}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Calculated Score: ${currentRisk.score}/850 | DTI: ${(currentRisk.debtToIncomeRatio * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Purpose Field
                OutlinedTextField(
                    value = purpose,
                    onValueChange = {
                        purpose = it
                        purposeError = it.isBlank()
                    },
                    label = { Text("Purpose of Loan") },
                    isError = purposeError,
                    supportingText = {
                        if (purposeError) Text("Please enter a valid loan purpose")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_purpose_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                EncryptedBadge(label = "End-to-End Cryptographic Ledger Verification")

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (purpose.isBlank()) {
                            purposeError = true
                        } else {
                            onSubmit(selectedType, amount, tenorMonths, purpose)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_loan_application_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed
                    )
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Application (₱${"%,.0f".format(amount)})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
