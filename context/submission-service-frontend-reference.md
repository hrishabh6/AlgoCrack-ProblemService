# Frontend Integration Reference - Submission Service v2.0

> **Document Version:** 2.0  
> **Last Updated:** 2026-02-03  
> **Status:** Production Ready  
> **Author:** Submission Service Team  
> **Target Audience:** Frontend Development Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [What Changed and Why](#2-what-changed-and-why)
3. [New Frontend Responsibilities](#3-new-frontend-responsibilities)
4. [New Backend Behavior](#4-new-backend-behavior)
5. [Verdict Reference](#5-verdict-reference)
6. [API Reference](#6-api-reference)
7. [WebSocket Reference](#7-websocket-reference)
8. [Migration Guide](#8-migration-guide)
9. [FAQ](#9-faq)

---

## 1. Executive Summary

### What's New in v2.0

| Feature | Before (v1.0) | After (v2.0) |
|---------|---------------|--------------|
| "Run Code" | Called `/custom`, returned raw output only | Calls `/run`, returns **comparison with expected output** |
| Judging | Backend compared against stored answers | Backend computes expected output **dynamically using oracle** |
| RUN verdicts | Used same verdicts as SUBMIT | **New RUN-specific verdicts** (e.g., `PASSED_RUN` ≠ `ACCEPTED`) |
| Testcase counts | Stored in `submission.passedTestCases` | **Computed from `testResults` JSON** |
| `/custom` endpoint | Primary for running code | **DEPRECATED** - use `/run` instead |

### Key Principle

> **RUN ≠ SUBMIT.** Running code tests against visible samples. Submitting code judges against hidden tests. They have different verdicts, different behaviors, and different purposes.

---

## 2. What Changed and Why

### 2.1 Oracle-Based Judging

**Before:** Expected outputs were stored in the database alongside testcases.

**After:** Expected outputs are **computed dynamically** by executing a reference solution (called "oracle").

**Why this matters to frontend:**
- You no longer receive `expectedOutput` from Problem Service API
- The backend now tells you both `actualOutput` AND `expectedOutput` for every testcase
- Comparison is done server-side, not client-side

### 2.2 Testcase Types

**Before:** Testcases had `isHidden: boolean` flag.

**After:** Testcases have `type: "DEFAULT" | "HIDDEN"` enum.

| Type | Visibility | Use Case |
|------|------------|----------|
| `DEFAULT` | Visible in testcase panel | User clicks "Run Code" |
| `HIDDEN` | Never exposed to client | User clicks "Submit" for official judging |

**Frontend impact:** You only ever receive `DEFAULT` testcases from Problem Service.

### 2.3 Synchronous /run Endpoint

**Before:** `/custom` was somewhat inconsistent - sometimes async, sometimes returned partial data.

**After:** `/run` is **fully synchronous**. You send a request, the server blocks until execution completes, and you get the full result in one response.

**Typical latency:** 2-5 seconds (show a loading spinner).

### 2.4 RUN vs SUBMIT Semantics

| Aspect | RUN Mode | SUBMIT Mode |
|--------|----------|-------------|
| Purpose | Test code, iterate | Official submission |
| Testcases | DEFAULT (visible) or custom | HIDDEN (judge-only) |
| Verdict | `PASSED_RUN`, `FAILED_RUN`, etc. | `ACCEPTED`, `WRONG_ANSWER`, etc. |
| Persistence | **None** - not saved to DB | Saved to submission history |
| Endpoint | `POST /api/v1/submissions/run` | `POST /api/v1/submissions` |
| Response time | Synchronous (2-5s) | Async (poll or WebSocket) |

---

## 3. New Frontend Responsibilities

### 3.1 MUST Do ✅

| Responsibility | Details |
|----------------|---------|
| **Display both outputs** | Show `actualOutput` (user's) AND `expectedOutput` (oracle's) for each testcase |
| **Handle RUN verdicts** | Map `PASSED_RUN`, `FAILED_RUN`, etc. to appropriate UI states |
| **Migrate from /custom to /run** | `/custom` is deprecated, switch to `/run` |
| **Handle rate limiting (429)** | Show user-friendly message when rate limited |
| **Handle validation errors (400)** | Display validation errors (too many testcases, input too large) |
| **Compute counts from results** | `passedTestCases` and `totalTestCases` come from iterating `testCaseResults` |

### 3.2 SHOULD Do ⚠️

| Responsibility | Details |
|----------------|---------|
| Show loading state for /run | Synchronous call takes 2-5s, show spinner |
| Differentiate RUN vs SUBMIT UI | RUN is "testing", SUBMIT is "official" - make this clear |
| Cache DEFAULT testcases | Avoid re-fetching on every render |

### 3.3 MUST NOT Do ❌

| Anti-Pattern | Reason |
|--------------|--------|
| Compare outputs client-side | Backend handles comparison, frontend just displays |
| Expect `expectedOutput` from Problem Service | Testcases no longer have stored expected outputs |
| Poll for `/run` results | `/run` is synchronous, no polling needed |
| Use `/custom` for new features | Deprecated, will be removed |

---

## 4. New Backend Behavior

### 4.1 /run Endpoint Behavior

```
Frontend                           Backend
    │                                 │
    │  POST /run                      │
    │  {code, questionId, testcases} │
    ├────────────────────────────────►│
    │                                 │
    │         [Backend executes]      │
    │         ┌─────────────────┐     │
    │         │ 1. Validate     │     │
    │         │ 2. Rate limit   │     │
    │         │ 3. Run user code│     │
    │         │ 4. Run oracle   │     │
    │         │ 5. Compare      │     │
    │         └─────────────────┘     │
    │                                 │
    │  200 OK                         │
    │  {verdict, testCaseResults}    │
    │◄────────────────────────────────┤
    │                                 │
```

**Key behaviors:**
- **Synchronous:** Request blocks until complete
- **Rate limited:** 30 requests/minute per IP
- **Validated:** Max 10 testcases, 10KB per input, 100KB total
- **Oracle comparison:** Every result includes `expectedOutput`

### 4.2 /submit Endpoint Behavior (Unchanged)

```
Frontend                           Backend
    │                                 │
    │  POST /submissions              │
    ├────────────────────────────────►│
    │                                 │
    │  202 Accepted                   │
    │  {submissionId}                 │
    │◄────────────────────────────────┤
    │                                 │
    │  Subscribe WebSocket            │
    │  /topic/submission/{id}         │
    ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ►│
    │                                 │
    │         [Backend processes]     │
    │                                 │
    │  WebSocket: Complete            │
    │  {verdict, testCaseResults}    │
    │◄ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
```

### 4.3 How Testcase Counts are Computed

**Old way (v1.0):**
```javascript
// Direct fields on submission
const passed = submission.passedTestCases;  // 8
const total = submission.totalTestCases;    // 10
```

**New way (v2.0):**
```javascript
// Computed from testResults
const results = JSON.parse(submission.testResults);
const total = results.length;
const passed = results.filter(r => r.passed).length;
```

> [!NOTE]
> The backend ALSO returns computed `passedTestCases` and `totalTestCases` in DTOs for convenience. But internally, these are derived from `testResults`.

---

## 5. Verdict Reference

### 5.1 RUN Verdicts (NEW)

Used ONLY in `/run` responses. These are **soft verdicts** for testing purposes.

| Verdict | Meaning | UI Suggestion |
|---------|---------|---------------|
| `PASSED_RUN` | All visible testcases matched oracle output | ✅ Green "All tests passed" |
| `FAILED_RUN` | One or more testcases did not match | ❌ Red "Wrong Answer" with diff |
| `COMPILATION_ERROR_RUN` | Code failed to compile | ⚠️ Show compilation output |
| `RUNTIME_ERROR_RUN` | Exception during execution | ⚠️ Show error message |
| `TIMEOUT_RUN` | Execution exceeded time limit | ⏱️ "Time Limit Exceeded" |
| `MEMORY_LIMIT_RUN` | Execution exceeded memory limit | 💾 "Memory Limit Exceeded" |
| `INTERNAL_ERROR_RUN` | Server error | ⚠️ "Something went wrong" |

> [!IMPORTANT]
> **`PASSED_RUN` ≠ `ACCEPTED`**. Passing RUN tests does NOT mean the code will be accepted. HIDDEN tests may still fail.

### 5.2 SUBMIT Verdicts (Unchanged)

Used in `/submissions` and WebSocket responses. These are **authoritative verdicts**.

| Verdict | Meaning | UI Suggestion |
|---------|---------|---------------|
| `ACCEPTED` | All HIDDEN tests passed | ✅ Green "Accepted" with celebration |
| `WRONG_ANSWER` | Output mismatch on HIDDEN tests | ❌ Red "Wrong Answer" |
| `COMPILATION_ERROR` | Code failed to compile | ⚠️ Show compilation output |
| `RUNTIME_ERROR` | Exception during execution | ⚠️ Show error message |
| `TIME_LIMIT_EXCEEDED` | Exceeded time limit | ⏱️ "TLE" |
| `MEMORY_LIMIT_EXCEEDED` | Exceeded memory limit | 💾 "MLE" |

### 5.3 Submission Status Values

| Status | Meaning | UI Suggestion |
|--------|---------|---------------|
| `PENDING` | In queue | Loading spinner |
| `COMPILING` | Compilation in progress | "Compiling..." |
| `RUNNING` | Executing tests | "Running tests..." |
| `COMPLETED` | Finished, check verdict | Show verdict |
| `FAILED` | System error | "Error occurred" |

---

## 6. API Reference

### 6.1 Run Code (NEW) ⭐

Execute code against visible testcases. **Synchronous.**

**Endpoint:** `POST /api/v1/submissions/run`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "questionId": 1,
  "language": "JAVA",
  "code": "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // implementation\n    }\n}",
  "customTestCases": [
    { "input": "{\"nums\": [2,7,11,15], \"target\": 9}" },
    { "input": "{\"nums\": [3,2,4], \"target\": 6}" }
  ]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `questionId` | number | Yes | Question ID |
| `language` | string | Yes | `JAVA`, `PYTHON`, `CPP`, `JAVASCRIPT` |
| `code` | string | Yes | User's source code |
| `customTestCases` | array | No | If omitted, uses DEFAULT testcases from DB |
| `customTestCases[].input` | string | Yes | JSON-formatted input |

**Response (200 OK) - Success:**
```json
{
  "verdict": "PASSED_RUN",
  "success": true,
  "runtimeMs": 145,
  "memoryKb": 25600,
  "compilationOutput": null,
  "errorMessage": null,
  "testCaseResults": [
    {
      "index": 0,
      "passed": true,
      "actualOutput": "[0,1]",
      "expectedOutput": "[0,1]",
      "executionTimeMs": 12,
      "error": null
    },
    {
      "index": 1,
      "passed": true,
      "actualOutput": "[1,2]",
      "expectedOutput": "[1,2]",
      "executionTimeMs": 8,
      "error": null
    }
  ]
}
```

**Response (200 OK) - Wrong Answer:**
```json
{
  "verdict": "FAILED_RUN",
  "success": false,
  "runtimeMs": 145,
  "memoryKb": 25600,
  "testCaseResults": [
    {
      "index": 0,
      "passed": false,
      "actualOutput": "[1,0]",
      "expectedOutput": "[0,1]",
      "executionTimeMs": 12,
      "error": null
    }
  ]
}
```

**Response (200 OK) - Compilation Error:**
```json
{
  "verdict": "COMPILATION_ERROR_RUN",
  "success": false,
  "compilationOutput": "Solution.java:3: error: ';' expected\n        return nums\n                   ^",
  "errorMessage": null,
  "testCaseResults": null
}
```

**Response (200 OK) - Runtime Error:**
```json
{
  "verdict": "RUNTIME_ERROR_RUN",
  "success": false,
  "testCaseResults": [
    {
      "index": 0,
      "passed": false,
      "actualOutput": null,
      "expectedOutput": "[0,1]",
      "executionTimeMs": 5,
      "error": "java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 4"
    }
  ]
}
```

**Error Responses:**

| Status | Reason | Response Body |
|--------|--------|---------------|
| 400 | Validation failed | `"Maximum 10 testcases per RUN. Got: 15"` |
| 400 | Input too large | `"Testcase 0 input too large. Max: 10000 bytes, got: 15234"` |
| 429 | Rate limited | `"RUN rate limit exceeded. Please wait before trying again."` |
| 500 | Server error | `"Internal server error"` |

---

### 6.2 Submit Code (Async)

Submit code for official judging. **Asynchronous.**

**Endpoint:** `POST /api/v1/submissions`

**Request Body:**
```json
{
  "userId": 12345,
  "questionId": 1,
  "language": "JAVA",
  "code": "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // implementation\n    }\n}",
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `userId` | number | Yes | Authenticated user ID |
| `questionId` | number | Yes | Question ID |
| `language` | string | Yes | `JAVA`, `PYTHON`, `CPP`, `JAVASCRIPT` |
| `code` | string | Yes | User's source code |
| `ipAddress` | string | No | Client IP (for analytics) |
| `userAgent` | string | No | Browser UA (for analytics) |

**Response (202 Accepted):**
```json
{
  "submissionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "message": "Submission queued for processing"
}
```

**Next steps:** Subscribe to WebSocket or poll `/submissions/{id}` for results.

---

### 6.3 Get Submission Details

**Endpoint:** `GET /api/v1/submissions/{submissionId}`

**Response (200 OK):**
```json
{
  "submissionId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 12345,
  "questionId": 1,
  "language": "JAVA",
  "code": "class Solution { ... }",
  "status": "COMPLETED",
  "verdict": "ACCEPTED",
  "runtimeMs": 15,
  "memoryKb": 25600,
  "passedTestCases": 10,
  "totalTestCases": 10,
  "errorMessage": null,
  "queuedAt": "2026-02-03T10:00:00",
  "startedAt": "2026-02-03T10:00:01",
  "completedAt": "2026-02-03T10:00:05"
}
```

> [!NOTE]
> `passedTestCases` and `totalTestCases` are computed from internal `testResults` JSON. They are included for convenience.

---

### 6.4 Get User Submission History

**Endpoint:** `GET /api/v1/submissions/user/{userId}?page=0&size=20`

**Response (200 OK):**
```json
[
  {
    "submissionId": "550e8400-e29b-41d4-a716-446655440000",
    "questionId": 1,
    "language": "JAVA",
    "status": "COMPLETED",
    "verdict": "ACCEPTED",
    "runtimeMs": 15,
    "memoryKb": 25600,
    "passedTestCases": 10,
    "totalTestCases": 10,
    "completedAt": "2026-02-03T10:00:05"
  },
  {
    "submissionId": "660f9511-f30c-52e5-b827-557766551111",
    "questionId": 1,
    "language": "JAVA",
    "status": "COMPLETED",
    "verdict": "WRONG_ANSWER",
    "runtimeMs": 12,
    "memoryKb": 24000,
    "passedTestCases": 8,
    "totalTestCases": 10,
    "completedAt": "2026-02-03T09:55:00"
  }
]
```

---

### 6.5 Custom Execution (DEPRECATED ⚠️)

> [!WARNING]
> **DEPRECATED.** Use `/run` instead. This endpoint will be removed in v3.0.

**Endpoint:** `POST /api/v1/submissions/custom`

---

## 7. WebSocket Reference

### 7.1 Connection

**URL:** `/ws`  
**Protocol:** STOMP over SockJS

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected');
});
```

### 7.2 Subscription

**Topic:** `/topic/submission/{submissionId}`

```javascript
stompClient.subscribe('/topic/submission/' + submissionId, function(message) {
  const data = JSON.parse(message.body);
  handleUpdate(data);
});
```

### 7.3 Message Types

**Status Update:**
```json
{
  "submissionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "RUNNING"
}
```

**Final Result:**
```json
{
  "submissionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "verdict": "ACCEPTED",
  "runtimeMs": 15,
  "memoryKb": 25600,
  "passedTestCases": 10,
  "totalTestCases": 10
}
```

**Error:**
```json
{
  "submissionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "FAILED",
  "error": "Internal server error"
}
```

---

## 8. Migration Guide

### 8.1 Migrating from /custom to /run

**Old Code (v1.0):**
```javascript
const response = await fetch('/api/v1/submissions/custom', {
  method: 'POST',
  body: JSON.stringify({
    userId: 123,
    questionId: 1,
    language: 'JAVA',
    code: userCode,
    testCases: [
      { input: '{"nums": [2,7,11,15], "target": 9}' }
    ]
  })
});
const result = await response.json();

// Only had actualOutput, no comparison
displayOutput(result.testCaseResults[0].output);
```

**New Code (v2.0):**
```javascript
const response = await fetch('/api/v1/submissions/run', {
  method: 'POST',
  body: JSON.stringify({
    questionId: 1,
    language: 'JAVA',
    code: userCode,
    customTestCases: [
      { input: '{"nums": [2,7,11,15], "target": 9}' }
    ]
  })
});
const result = await response.json();

// Now have both actual AND expected
displayComparison(
  result.testCaseResults[0].actualOutput,
  result.testCaseResults[0].expectedOutput,
  result.testCaseResults[0].passed
);
```

### 8.2 Field Name Changes

| Old Field | New Field |
|-----------|-----------|
| `testCases` | `customTestCases` |
| `output` | `actualOutput` |
| (not available) | `expectedOutput` |
| (not available) | `passed` (per testcase) |
| (implicit) | `verdict` (overall) |

### 8.3 Handling New Verdicts

```javascript
function getVerdictDisplay(verdict) {
  const verdictMap = {
    // RUN verdicts
    'PASSED_RUN': { label: 'All Tests Passed', color: 'green', icon: '✓' },
    'FAILED_RUN': { label: 'Wrong Answer', color: 'red', icon: '✗' },
    'COMPILATION_ERROR_RUN': { label: 'Compilation Error', color: 'orange', icon: '⚠' },
    'RUNTIME_ERROR_RUN': { label: 'Runtime Error', color: 'orange', icon: '⚠' },
    'TIMEOUT_RUN': { label: 'Time Limit Exceeded', color: 'orange', icon: '⏱' },
    'MEMORY_LIMIT_RUN': { label: 'Memory Limit Exceeded', color: 'orange', icon: '💾' },
    'INTERNAL_ERROR_RUN': { label: 'Error', color: 'red', icon: '⚠' },
    
    // SUBMIT verdicts (unchanged)
    'ACCEPTED': { label: 'Accepted', color: 'green', icon: '✓' },
    'WRONG_ANSWER': { label: 'Wrong Answer', color: 'red', icon: '✗' },
    // ... etc
  };
  
  return verdictMap[verdict] || { label: verdict, color: 'gray', icon: '?' };
}
```

---

## 9. FAQ

### Q: Do I still need to call Problem Service for testcases?

**A:** Yes, but only to display them. When user clicks "Run", you send the testcases (possibly user-edited) to `/run`. The backend handles comparison.

### Q: What's the difference between `PASSED_RUN` and `ACCEPTED`?

**A:**
- `PASSED_RUN` = Passed visible DEFAULT testcases only (soft verdict)
- `ACCEPTED` = Passed ALL testcases including HIDDEN (authoritative verdict)

User can get `PASSED_RUN` but still get `WRONG_ANSWER` on submit.

### Q: Why is `/run` synchronous but `/submit` async?

**A:**
- `/run` is interactive - user is waiting, staring at the screen
- `/submit` can take longer (more testcases) and user can do other things

### Q: How long does `/run` take?

**A:** Typically 2-5 seconds. Show a loading spinner. If it takes >10 seconds, something is wrong.

### Q: What if user sends invalid testcase input?

**A:** You'll get a 400 Bad Request with a descriptive error message. Display it to the user.

### Q: Can users see HIDDEN testcases?

**A:** No. They only see `passedTestCases / totalTestCases` count, never the actual HIDDEN testcase content.

### Q: What happens if I hit the rate limit?

**A:** 429 response with message. Display to user. Limit is 30 runs/minute per IP.

---

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 2.0 | 2026-02-03 | Complete rewrite for v2.0 architecture |
| 1.0 | 2026-01-27 | Initial release |

---

**Questions?** Contact the Submission Service team.
