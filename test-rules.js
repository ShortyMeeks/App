
const fs = require("fs");
const testing = require("@firebase/rules-unit-testing");
const rules = fs.readFileSync("firestore.rules", "utf8");

testing.initializeTestEnvironment({
  projectId: "demo-test",
  firestore: { rules }
}).then(env => {
  console.log("INITIALIZE_TEST_ENVIRONMENT_SUCCESS");
  const aliceDb = env.authenticatedContext("alice").firestore();
  return aliceDb.collection("users").doc("alice").set({
    fullName: "Alice",
    studentNumber: "1234",
    role: "student",
    isApproved: true,
    email: "alice@example.com"
  }).then(() => {
    console.log("RULES_COMPILED_AND_EXECUTED_OK");
    return env.cleanup();
  });
}).catch(err => {
  console.error("COMPILE_ERROR:", err);
  process.exit(1);
});
