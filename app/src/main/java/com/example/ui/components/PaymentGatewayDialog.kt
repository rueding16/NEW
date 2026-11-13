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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.RepaymentScheduleEntity
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SuccessGreen

@Composable
fun PaymentGatewayDialog(
    schedule: RepaymentScheduleEntity,
    defaultCustomerName: String,
    onConfirmPayment: (scheduleId: Long, method: String, channelName: String, accountInfo: String, amount: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("CARD") } // "CARD", "E_WALLET", "BANK"

    // Card state
    var cardNumber by remember { mutableStateOf("4532 8812 9012 3456") }
    var cardExpiry by remember { mutableStateOf("09/28") }
    var cardCvv by remember { mutableStateOf("888") }
    var cardHolder by remember { mutableStateOf(defaultCustomerName) }

    // E-Wallet state
    var eWalletChannel by remember { mutableStateOf("GCash") }
    var eWalletPhone by remember { mutableStateOf("09171234567") }

    // Bank state
    var bankName by remember { mutableStateOf("BDO Unibank") }
    var bankAccountNum by remember { mutableStateOf("10982345091") }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val totalToPay = schedule.amountDue + schedule.penaltyAmount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("payment_gateway_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Gateway Header
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Secure Payment Gateway",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "PCI-DSS Level 1 & 256-Bit SSL Encrypted",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Installment Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Installment #${schedule.installmentNumber}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Loan ID: #${schedule.loanId}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Amount Due",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₱${"%,.2f".format(totalToPay)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryRed
                            )
                        }

                        if (schedule.penaltyAmount > 0) {
                            Text(
                                text = "Includes ₱${"%,.2f".format(schedule.penaltyAmount)} late surcharge",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selector Chips
                Text(
                    text = "Select Payment Channel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedMethod == "CARD",
                        onClick = { selectedMethod = "CARD" },
                        label = { Text("Debit/Credit", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pay_method_card")
                    )

                    FilterChip(
                        selected = selectedMethod == "E_WALLET",
                        onClick = { selectedMethod = "E_WALLET" },
                        label = { Text("E-Wallet", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pay_method_ewallet")
                    )

                    FilterChip(
                        selected = selectedMethod == "BANK",
                        onClick = { selectedMethod = "BANK" },
                        label = { Text("Online Bank", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pay_method_bank")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Form based on method
                when (selectedMethod) {
                    "CARD" -> {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Card Number (Visa / Mastercard)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_number_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it },
                                label = { Text("Exp (MM/YY)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("card_expiry_input")
                            )

                            OutlinedTextField(
                                value = cardCvv,
                                onValueChange = { cardCvv = it },
                                label = { Text("CVV") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("card_cvv_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { cardHolder = it },
                            label = { Text("Cardholder Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_holder_input")
                        )
                    }

                    "E_WALLET" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("GCash", "Maya").forEach { w ->
                                FilterChip(
                                    selected = eWalletChannel == w,
                                    onClick = { eWalletChannel = w },
                                    label = { Text(w, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = eWalletPhone,
                            onValueChange = { eWalletPhone = it },
                            label = { Text("$eWalletChannel Registered Mobile (09xxxxxxxxx)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ewallet_phone_input")
                        )
                    }

                    "BANK" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("BDO", "BPI", "UnionBank").forEach { b ->
                                FilterChip(
                                    selected = bankName.contains(b),
                                    onClick = { bankName = b },
                                    label = { Text(b) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = bankAccountNum,
                            onValueChange = { bankAccountNum = it },
                            label = { Text("$bankName Account / Reference Number") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bank_account_input")
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                EncryptedBadge(label = "End-to-End Encrypted Repayment Token")

                Spacer(modifier = Modifier.height(16.dp))

                // Pay Button
                Button(
                    onClick = {
                        val channelName = when (selectedMethod) {
                            "CARD" -> "Visa/Mastercard"
                            "E_WALLET" -> eWalletChannel
                            else -> bankName
                        }
                        val accountInfo = when (selectedMethod) {
                            "CARD" -> cardNumber
                            "E_WALLET" -> eWalletPhone
                            else -> bankAccountNum
                        }

                        if (accountInfo.length < 5) {
                            errorMessage = "Please enter valid payment details"
                            return@Button
                        }

                        errorMessage = null
                        isProcessing = true
                        onConfirmPayment(
                            schedule.id,
                            selectedMethod,
                            channelName,
                            accountInfo,
                            totalToPay
                        )
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_repayment_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authorizing Payment...")
                    } else {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Authorize & Pay ₱${"%,.2f".format(totalToPay)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
