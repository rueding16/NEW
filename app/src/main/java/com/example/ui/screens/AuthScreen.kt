package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EncryptedBadge
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (username: String, pass: String) -> Unit,
    onRegister: (
        username: String,
        pass: String,
        fullName: String,
        email: String,
        phone: String,
        role: String,
        idType: String,
        idNumber: String,
        employmentType: String,
        companyName: String,
        monthlyIncome: Double,
        proofDoc: String
    ) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onGoogleSignIn: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Register

    // Sign In Fields
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Register Fields
    var regRole by remember { mutableStateOf("CUSTOMER") }
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }

    val idTypes = listOf("National ID (PhilSys)", "Driver's License", "Passport", "UMID", "Postal ID")
    var selectedIdType by remember { mutableStateOf(idTypes[0]) }
    var idDropdownExpanded by remember { mutableStateOf(false) }
    var regIdNumber by remember { mutableStateOf("") }

    val empTypes = listOf("Employed", "Business Owner", "Self-Employed", "Freelancer")
    var selectedEmpType by remember { mutableStateOf(empTypes[0]) }
    var empDropdownExpanded by remember { mutableStateOf(false) }

    var regCompany by remember { mutableStateOf("") }
    var regIncomeText by remember { mutableStateOf("45000") }
    var regProofDoc by remember { mutableStateOf("Latest Payslip & Government ID scan") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo & Header
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PrimaryRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CrediPulse",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Real-Time Loan & Financial Credit Platform",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            EncryptedBadge(label = "End-to-End Encrypted Identity & Underwriting")

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Row (Sign In / Register)
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_sign_in")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Register", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_register")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 0) {
                // SIGN IN VIEW
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Access Your Dashboard",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Google Sign-In Option
                        OutlinedButton(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_sign_in_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Sign in with Google",
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign in with Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                            Text(
                                text = "  OR WITH CREDENTIALS  ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }

                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            label = { Text("Username / Account Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input")
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                    Icon(
                                        imageVector = if (showLoginPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Button(
                            onClick = { onLogin(loginUsername, loginPassword) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_login"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRed
                            )
                        ) {
                            Text("Sign In Securely", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        // Quick Fill for convenience & testing admin and customer
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quick Fill Credentials for Testing:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    loginUsername = "admin"
                                    loginPassword = "@adminpass2026"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_fill_admin")
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Admin", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    loginUsername = "maria.santos"
                                    loginPassword = "maria123"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_fill_customer")
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Customer", fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                // REGISTER VIEW
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Create Encrypted Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Role Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = regRole == "CUSTOMER",
                                onClick = { regRole = "CUSTOMER" },
                                label = { Text("Customer Account") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = regRole == "ADMIN",
                                onClick = {
                                    regRole = "ADMIN"
                                    regUsername = "admin"
                                    regPassword = "@adminpass2026"
                                },
                                label = { Text("Admin Account") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (regRole == "ADMIN") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkAccentGold.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Admin registration requires exact username 'admin' and password '@adminpass2026'.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Legal Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_fullname_input")
                        )

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Username") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_username_input")
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_email_input")
                        )

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("Phone Number (+63 xxx xxx xxxx)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_phone_input")
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password") },
                            visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                    Icon(
                                        imageVector = if (showRegPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password_input")
                        )

                        // KYC Section
                        Text(
                            text = "Identity & KYC Information (For Admin Verification)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = idDropdownExpanded,
                            onExpandedChange = { idDropdownExpanded = !idDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedIdType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Government ID Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = idDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = idDropdownExpanded,
                                onDismissRequest = { idDropdownExpanded = false }
                            ) {
                                idTypes.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t) },
                                        onClick = {
                                            selectedIdType = t
                                            idDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = regIdNumber,
                            onValueChange = { regIdNumber = it },
                            label = { Text("ID Document Number") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_id_number_input")
                        )

                        ExposedDropdownMenuBox(
                            expanded = empDropdownExpanded,
                            onExpandedChange = { empDropdownExpanded = !empDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedEmpType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Employment Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = empDropdownExpanded,
                                onDismissRequest = { empDropdownExpanded = false }
                            ) {
                                empTypes.forEach { e ->
                                    DropdownMenuItem(
                                        text = { Text(e) },
                                        onClick = {
                                            selectedEmpType = e
                                            empDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = regCompany,
                            onValueChange = { regCompany = it },
                            label = { Text("Company / Employer Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regIncomeText,
                            onValueChange = { regIncomeText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Declared Monthly Income (₱)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_income_input")
                        )

                        OutlinedTextField(
                            value = regProofDoc,
                            onValueChange = { regProofDoc = it },
                            label = { Text("Proof of Income Document Reference") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val income = regIncomeText.toDoubleOrNull() ?: 30000.0
                                onRegister(
                                    regUsername,
                                    regPassword,
                                    regFullName,
                                    regEmail,
                                    regPhone,
                                    regRole,
                                    selectedIdType,
                                    regIdNumber.ifBlank { "DOC-2026-9021" },
                                    selectedEmpType,
                                    regCompany,
                                    income,
                                    regProofDoc
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_register"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRed
                            )
                        ) {
                            Text("Create & Verify Profile", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
