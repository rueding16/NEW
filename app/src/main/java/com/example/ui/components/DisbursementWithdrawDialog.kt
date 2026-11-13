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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LoanApplicationEntity
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisbursementWithdrawDialog(
    loan: LoanApplicationEntity,
    defaultRecipientName: String,
    onConfirmDisbursement: (amount: Double, channel: String, destinationType: String, targetAccount: String, targetName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var destinationType by remember { mutableStateOf("E_WALLET") } // "E_WALLET" or "BANK_TRANSFER"

    val eWalletProviders = listOf("GCash", "Maya (PayMaya)", "GrabPay", "PayPal Philippines")
    val bankProviders = listOf("BDO Unibank", "BPI (Bank of the Philippine Islands)", "UnionBank", "Metrobank", "Landbank")

    var selectedChannel by remember { mutableStateOf(eWalletProviders[0]) }
    var channelDropdownExpanded by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf(loan.availableBalance.toInt().toString()) }
    var accountIdentifier by remember { mutableStateOf("") }
    var accountHolderName by remember { mutableStateOf(defaultRecipientName) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val currentProviders = if (destinationType == "E_WALLET") eWalletProviders else bankProviders

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("disbursement_withdraw_dialog"),
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
                            text = "Withdraw / Send Funds",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Disburse available loan balance in real time",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Available Balance Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuccessGreen.copy(alpha = 0.12f))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Available Balance for Withdrawal",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₱${"%,.2f".format(loan.availableBalance)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = SuccessGreen
                            )
                        }

                        OutlinedButton(
                            onClick = { amountText = loan.availableBalance.toInt().toString() },
                            modifier = Modifier.testTag("withdraw_all_btn")
                        ) {
                            Text("Max", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Channel Type Segmented selector (E-Wallet vs Bank Transfer)
                Text(
                    text = "Select Payout Method",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = destinationType == "E_WALLET",
                        onClick = {
                            destinationType = "E_WALLET"
                            selectedChannel = eWalletProviders[0]
                        },
                        label = { Text("E-Wallet (GCash/Maya)") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_e_wallet_method")
                    )

                    FilterChip(
                        selected = destinationType == "BANK_TRANSFER",
                        onClick = {
                            destinationType = "BANK_TRANSFER"
                            selectedChannel = bankProviders[0]
                        },
                        label = { Text("Bank Transfer") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_bank_transfer_method")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Provider Dropdown
                ExposedDropdownMenuBox(
                    expanded = channelDropdownExpanded,
                    onExpandedChange = { channelDropdownExpanded = !channelDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedChannel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (destinationType == "E_WALLET") "E-Wallet Provider" else "Receiving Bank") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = channelDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("payout_provider_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = channelDropdownExpanded,
                        onDismissRequest = { channelDropdownExpanded = false }
                    ) {
                        currentProviders.forEach { prov ->
                            DropdownMenuItem(
                                text = { Text(prov) },
                                onClick = {
                                    selectedChannel = prov
                                    channelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount text field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { ch -> ch.isDigit() }
                    },
                    label = { Text("Amount to Disburse (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("disbursement_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Account Number / Phone
                OutlinedTextField(
                    value = accountIdentifier,
                    onValueChange = { accountIdentifier = it },
                    label = { Text(if (destinationType == "E_WALLET") "Mobile / Wallet Number (e.g. 0917xxxxxxx)" else "Bank Account Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("disbursement_account_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Account Holder Name
                OutlinedTextField(
                    value = accountHolderName,
                    onValueChange = { accountHolderName = it },
                    label = { Text("Account Holder Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("disbursement_name_input")
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg!!,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                EncryptedBadge(label = "Encrypted Financial Gateway • Zero Transfer Fee")

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Disbursement Button
                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        when {
                            parsedAmount <= 0 -> errorMsg = "Please enter an amount greater than 0"
                            parsedAmount > loan.availableBalance -> errorMsg = "Amount exceeds available balance"
                            accountIdentifier.length < 8 -> errorMsg = "Please enter a valid account or mobile number"
                            accountHolderName.isBlank() -> errorMsg = "Please enter the account holder name"
                            else -> {
                                errorMsg = null
                                onConfirmDisbursement(
                                    parsedAmount,
                                    selectedChannel,
                                    destinationType,
                                    accountIdentifier,
                                    accountHolderName
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_disbursement_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transfer ₱${"%,.0f".format(amountText.toDoubleOrNull() ?: 0.0)} Now",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
