const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
});

test("User can create their own valid user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      username: "alice1",
      fullName: "Alice Smith",
      email: "alice@example.com",
      role: "CUSTOMER",
      kycStatus: "PENDING_VERIFICATION",
    })
  );
});

test("User cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      username: "bob123",
      fullName: "Bob Jones",
      email: "bob@example.com",
      role: "CUSTOMER",
      kycStatus: "VERIFIED",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Customer can create loan application for themselves", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("loans").doc("loan_1").set({
      userId: ALICE_UID,
      loanType: "Personal Loan",
      principalAmount: 5000,
      tenorMonths: 12,
      status: "PENDING_APPROVAL",
    })
  );
});

test("Customer cannot read Bob's loan application", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("loans").doc("bob_loan").set({
      userId: BOB_UID,
      loanType: "Business Loan",
      principalAmount: 10000,
      tenorMonths: 24,
      status: "PENDING_APPROVAL",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("loans").doc("bob_loan").get());
});

test("Customer can query their own loans", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("loans").doc("alice_loan").set({
      userId: ALICE_UID,
      loanType: "Personal Loan",
      principalAmount: 2000,
      tenorMonths: 6,
      status: "APPROVED",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("loans").where("userId", "==", ALICE_UID).get()
  );
});

test("Customer can create KYC user document", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("user_documents").doc("doc_1").set({
      userId: ALICE_UID,
      docType: "PASSPORT",
      fileName: "passport_front.jpg",
      status: "IN_REVIEW",
    })
  );
});

test("Customer cannot view other user documents", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("user_documents").doc("bob_doc").set({
      userId: BOB_UID,
      docType: "PAYSLIP",
      fileName: "payslip.pdf",
      status: "VERIFIED",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("user_documents").doc("bob_doc").get());
});
