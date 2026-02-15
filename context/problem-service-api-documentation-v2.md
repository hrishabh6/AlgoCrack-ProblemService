# Problem Service API Documentation v2.0

> **Version:** 2.0.0  
> **Last Updated:** 2026-02-03  
> **Base URL:** `/api/v1`  
> **Content-Type:** `application/json`

---

## Table of Contents

1. [Breaking Changes](#breaking-changes)
2. [Data Types & Enums](#data-types--enums)
3. [Questions API](#questions-api)
4. [Testcases API](#testcases-api)
5. [Solutions API](#solutions-api)
6. [Tags API](#tags-api)
7. [Reference Solution API](#reference-solution-api)
8. [Error Responses](#error-responses)

---

## Breaking Changes

> [!CAUTION]
> **Migration Required**: The following fields have been removed from TestCase:
> - `expectedOutput` - No longer stored; computed by oracle at runtime
> - `orderIndex` - Removed; presentation concern handled by frontend
> - `isHidden` - Replaced by `type` enum (`DEFAULT` | `HIDDEN`)

> [!WARNING]
> **Question Request/Response Changes:**
> - `testCases` field renamed to `defaultTestcases`
> - New `hiddenTestcases` field added
> - New `nodeType` field added
> - New `referenceSolution` field added
> - Response now includes `metadataList`

---

## Data Types & Enums

### TestCaseType
```typescript
type TestCaseType = "DEFAULT" | "HIDDEN";
```
| Value | Description |
|-------|-------------|
| `DEFAULT` | Visible to users, shown in testcase panel, editable, used for "Run" |
| `HIDDEN` | Judge-only, never exposed to users, used for "Submit" |

### NodeType
```typescript
type NodeType = "TREE_NODE" | "GRAPH_NODE" | "LIST_NODE" | null;
```
| Value | Description | Frontend Behavior |
|-------|-------------|-------------------|
| `TREE_NODE` | Binary tree or N-ary tree | Render tree visualization |
| `GRAPH_NODE` | Graph (directed/undirected) | Render graph visualization |
| `LIST_NODE` | Linked list | Render linked list visualization |
| `null` | Not applicable | No special visualization |

### Language
```typescript
type Language = "JAVA" | "PYTHON" | "CPP" | "JAVASCRIPT";
```

### Difficulty
```typescript
type Difficulty = "Easy" | "Medium" | "Hard";
```

---

## Questions API

### List Questions (Paginated)

```http
GET /api/v1/questions
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | `0` | Page number (0-indexed) |
| `size` | integer | No | `20` | Page size |
| `difficulty` | string | No | - | Filter by difficulty: `Easy`, `Medium`, `Hard` |
| `tag` | string | No | - | Filter by tag name |
| `search` | string | No | - | Search in question title |
| `company` | string | No | - | Filter by company name |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "questionTitle": "Two Sum",
      "difficultyLevel": "Easy",
      "tags": ["Array", "Hash Table"],
      "company": "Google",
      "acceptanceRate": 49.5,
      "totalSubmissions": 12500
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "first": true
}
```

---

### Get Question by ID

```http
GET /api/v1/questions/{id}
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | long | Yes | Question ID |

**Response:** `200 OK`
```json
{
  "id": 1,
  "questionTitle": "Two Sum",
  "questionDescription": "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
  "isOutputOrderMatters": false,
  "tags": ["Array", "Hash Table"],
  "difficultyLevel": "Easy",
  "company": "Google",
  "constraints": "2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9",
  "timeoutLimit": 5,
  "nodeType": null,
  "defaultTestcases": [
    {
      "id": 101,
      "questionId": 1,
      "input": "[[2,7,11,15], 9]",
      "type": "DEFAULT"
    },
    {
      "id": 102,
      "questionId": 1,
      "input": "[[3,2,4], 6]",
      "type": "DEFAULT"
    }
  ],
  "metadataList": [
    {
      "functionName": "twoSum",
      "returnType": "int[]",
      "paramTypes": ["int[]", "int"],
      "paramNames": ["nums", "target"],
      "language": "JAVA",
      "codeTemplate": "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Your code here\n    }\n}",
      "executionStrategy": "STANDARD",
      "customInputEnabled": true
    }
  ]
}
```

> [!IMPORTANT]
> **Hidden testcases are NEVER returned** in this response. Only `DEFAULT` testcases are exposed.

---

### Create Question

```http
POST /api/v1/questions
```

**Request Body:**
```json
{
  "questionTitle": "Two Sum",
  "questionDescription": "Given an array of integers nums and an integer target...",
  "defaultTestcases": [
    { "input": "[[2,7,11,15], 9]" },
    { "input": "[[3,2,4], 6]" }
  ],
  "hiddenTestcases": [
    { "input": "[[1,2,3,4,5], 9]" },
    { "input": "[[-1,-2,-3,-4,-5], -8]" }
  ],
  "metadataList": [
    {
      "functionName": "twoSum",
      "returnType": "int[]",
      "paramTypes": ["int[]", "int"],
      "paramNames": ["nums", "target"],
      "language": "JAVA",
      "codeTemplate": "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Your code here\n    }\n}",
      "executionStrategy": "STANDARD",
      "customInputEnabled": true
    }
  ],
  "referenceSolution": {
    "language": "JAVA",
    "sourceCode": "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < nums.length; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                return new int[] { map.get(complement), i };\n            }\n            map.put(nums[i], i);\n        }\n        return new int[] {};\n    }\n}"
  },
  "nodeType": null,
  "isOutputOrderMatters": false,
  "tags": [{ "name": "Array" }, { "name": "Hash Table" }],
  "difficultyLevel": "Easy",
  "company": "Google",
  "constraints": "2 <= nums.length <= 10^4",
  "timeoutLimit": 5,
  "solution": [
    {
      "code": "// Editorial solution code",
      "language": "JAVA"
    }
  ]
}
```

**Field Reference:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `questionTitle` | string | **Yes** | Max 200 characters |
| `questionDescription` | string | **Yes** | Markdown supported |
| `defaultTestcases` | array | **Yes** | At least 1 required. Visible to users. |
| `hiddenTestcases` | array | No | Judge-only testcases |
| `metadataList` | array | **Yes** | At least 1 required. Per-language config. |
| `referenceSolution` | object | No | Oracle for computing expected output |
| `nodeType` | string | No | `TREE_NODE`, `GRAPH_NODE`, `LIST_NODE`, or `null` |
| `isOutputOrderMatters` | boolean | No | Default: `false` |
| `tags` | array | No | Must reference existing tags |
| `difficultyLevel` | string | No | `Easy`, `Medium`, `Hard` |
| `company` | string | No | Company name |
| `constraints` | string | No | Problem constraints |
| `timeoutLimit` | integer | No | 1-30 seconds |
| `solution` | array | No | Editorial solutions |

**Response:** `200 OK`
```json
{
  "questionId": 1,
  "message": "Question created successfully"
}
```

**Error Response:** `400 Bad Request`
```json
{
  "questionId": null,
  "message": "Tag does not exist: InvalidTag"
}
```

---

### Update Question

```http
PUT /api/v1/questions/{id}
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | long | Yes | Question ID |

**Request Body:** Same as Create (all fields optional for partial update)

**Response:** `200 OK` - Returns updated `QuestionResponseDto`

---

### Delete Question

```http
DELETE /api/v1/questions/{id}
```

**Response:** `204 No Content`

---

## Testcases API

### Add Testcase

```http
POST /api/v1/testcases
```

**Request Body:**
```json
{
  "questionId": 1,
  "input": "[[1,2,3], 5]",
  "type": "DEFAULT"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `questionId` | long | **Yes** | Parent question ID |
| `input` | string | **Yes** | JSON-formatted input |
| `type` | string | **Yes** | `DEFAULT` or `HIDDEN` |

**Response:** `201 Created`

---

### Get Testcase by ID

```http
GET /api/v1/testcases/{id}
```

**Response:** `200 OK`
```json
{
  "id": 101,
  "questionId": 1,
  "input": "[[2,7,11,15], 9]",
  "type": "DEFAULT"
}
```

---

### Get Testcases by Question

```http
GET /api/v1/testcases/question/{questionId}
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `type` | string | No | Filter by `DEFAULT` or `HIDDEN` |

**Examples:**
```http
GET /api/v1/testcases/question/1              # All testcases
GET /api/v1/testcases/question/1?type=DEFAULT # Only DEFAULT
GET /api/v1/testcases/question/1?type=HIDDEN  # Only HIDDEN (admin use)
```

**Response:** `200 OK`
```json
[
  {
    "id": 101,
    "questionId": 1,
    "input": "[[2,7,11,15], 9]",
    "type": "DEFAULT"
  },
  {
    "id": 102,
    "questionId": 1,
    "input": "[[3,2,4], 6]",
    "type": "DEFAULT"
  }
]
```

---

### Update Testcase

```http
PUT /api/v1/testcases/{id}
```

**Request Body:**
```json
{
  "questionId": 1,
  "input": "[[1,2,3,4], 7]",
  "type": "DEFAULT"
}
```

**Response:** `200 OK` - Returns updated testcase

---

### Delete Testcase

```http
DELETE /api/v1/testcases/{id}
```

**Response:** `204 No Content`

---

## Solutions API

Editorial solutions (user-visible explanations, not the oracle).

### Get Solutions by Question

```http
GET /api/v1/solutions/question/{questionId}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "code": "class Solution { ... }",
    "language": "JAVA",
    "explanation": "We use a hash map to store...",
    "questionId": 1
  }
]
```

---

### Add Solution

```http
POST /api/v1/solutions
```

**Request Body:**
```json
{
  "questionId": 1,
  "code": "class Solution { ... }",
  "language": "JAVA",
  "explanation": "Optional explanation"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `questionId` | long | **Yes** | Parent question ID |
| `code` | string | **Yes** | Solution code |
| `language` | string | **Yes** | `JAVA`, `PYTHON`, `CPP`, `JAVASCRIPT` |
| `explanation` | string | No | Editorial explanation |

**Response:** `201 Created`
```json
{
  "id": 1,
  "code": "class Solution { ... }",
  "language": "JAVA",
  "explanation": "Optional explanation",
  "questionId": 1
}
```

---

### Update Solution

```http
PUT /api/v1/solutions/{solutionId}
```

**Request Body:**
```json
{
  "code": "updated code",
  "language": "PYTHON",
  "explanation": "Updated explanation"
}
```

**Response:** `200 OK` - Returns updated solution

---

### Delete Solution

```http
DELETE /api/v1/solutions/{solutionId}
```

**Response:** `204 No Content`

---

## Tags API

### List All Tags

```http
GET /api/v1/tags
```

**Response:** `200 OK`
```json
[
  { "id": 1, "name": "Array", "description": "Array manipulation problems" },
  { "id": 2, "name": "Hash Table", "description": "Hash map/set problems" },
  { "id": 3, "name": "Dynamic Programming", "description": "DP problems" }
]
```

---

### Get Tag by ID

```http
GET /api/v1/tags/{id}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Array",
  "description": "Array manipulation problems"
}
```

---

### Create Tag

```http
POST /api/v1/tags
```

**Request Body:**
```json
{
  "name": "Two Pointers",
  "description": "Problems using two pointer technique"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | **Yes** | Unique tag name |
| `description` | string | No | Tag description |

**Response:** `201 Created`

---

### Delete Tag

```http
DELETE /api/v1/tags/{id}
```

**Response:** `204 No Content`

---

## Reference Solution API

> [!CAUTION]
> **Admin-Only Endpoints**: Reference solutions (oracles) should NEVER be exposed to regular users. These endpoints are for problem management only.

### Get Reference Solution

```http
GET /api/v1/questions/{questionId}/reference-solution
```

**Response:** `200 OK`
```json
{
  "language": "JAVA",
  "sourceCode": "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Oracle implementation\n    }\n}"
}
```

---

### Create/Update Reference Solution

```http
PUT /api/v1/questions/{questionId}/reference-solution
```

**Request Body:**
```json
{
  "language": "JAVA",
  "sourceCode": "class Solution { ... }"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `language` | string | **Yes** | `JAVA`, `PYTHON`, `CPP`, `JAVASCRIPT` |
| `sourceCode` | string | **Yes** | Oracle implementation code |

**Response:** `200 OK` - Returns saved reference solution

---

### Delete Reference Solution

```http
DELETE /api/v1/questions/{questionId}/reference-solution
```

**Response:** `204 No Content`

---

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2026-02-03T12:00:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message"
}
```

### HTTP Status Codes

| Status | Meaning |
|--------|---------|
| `200` | Success |
| `201` | Created |
| `204` | No Content (successful delete) |
| `400` | Bad Request (validation error) |
| `404` | Not Found |
| `500` | Internal Server Error |

### Common Error Messages

| Error | Cause |
|-------|-------|
| `Question not found with ID: {id}` | Invalid question ID |
| `TestCase not found with id {id}` | Invalid testcase ID |
| `Tag does not exist: {name}` | Referenced tag doesn't exist |
| `At least one default test case is required` | Missing defaultTestcases |
| `At least one metadata entry is required` | Missing metadataList |
| `Question title is required` | Missing required field |

---

## Migration Guide for Frontend

### TestCase Changes

**Before (v1):**
```typescript
interface TestCase {
  id: number;
  questionId: number;
  input: string;
  expectedOutput: string;  // ❌ REMOVED
  orderIndex: number;      // ❌ REMOVED
  isHidden: boolean;       // ❌ REMOVED
}
```

**After (v2):**
```typescript
interface TestCase {
  id: number;
  questionId: number;
  input: string;
  type: "DEFAULT" | "HIDDEN";  // ✅ NEW
}
```

### Question Request Changes

**Before (v1):**
```typescript
interface QuestionRequest {
  testCases: TestCaseDto[];  // ❌ REMOVED
}
```

**After (v2):**
```typescript
interface QuestionRequest {
  defaultTestcases: TestCaseDto[];   // ✅ NEW - visible to users
  hiddenTestcases?: TestCaseDto[];   // ✅ NEW - judge only
  referenceSolution?: ReferenceSolutionDto;  // ✅ NEW - oracle
  nodeType?: NodeType;                       // ✅ NEW - visualization hint
}
```

### Question Response Changes

**Added Fields:**
- `nodeType` - Use for tree/graph/list visualization
- `defaultTestcases` - Full testcase objects (only DEFAULT type)
- `metadataList` - Per-language configuration including code templates

---

## TypeScript Interfaces

```typescript
// Core Types
type TestCaseType = "DEFAULT" | "HIDDEN";
type NodeType = "TREE_NODE" | "GRAPH_NODE" | "LIST_NODE" | null;
type Language = "JAVA" | "PYTHON" | "CPP" | "JAVASCRIPT";
type Difficulty = "Easy" | "Medium" | "Hard";

// DTOs
interface TestCaseDto {
  input: string;
  type?: TestCaseType;  // Required for standalone creation
}

interface TestCaseResponseDto {
  id: number;
  questionId: number;
  input: string;
  type: TestCaseType;
}

interface ReferenceSolutionDto {
  language: Language;
  sourceCode: string;
}

interface QuestionMetadataDto {
  functionName: string;
  returnType: string;
  paramTypes: string[];
  paramNames: string[];
  language: Language;
  codeTemplate: string;
  executionStrategy: string;
  customInputEnabled: boolean;
}

interface QuestionSummaryDto {
  id: number;
  questionTitle: string;
  difficultyLevel: Difficulty;
  tags: string[];
  company: string;
  acceptanceRate?: number;
  totalSubmissions?: number;
}

interface QuestionResponseDto {
  id: number;
  questionTitle: string;
  questionDescription: string;
  isOutputOrderMatters: boolean;
  tags: string[];
  difficultyLevel: Difficulty;
  company: string;
  constraints: string;
  timeoutLimit: number;
  nodeType: NodeType;
  defaultTestcases: TestCaseResponseDto[];
  metadataList: QuestionMetadataDto[];
}

interface QuestionRequestDto {
  questionTitle: string;
  questionDescription: string;
  defaultTestcases: TestCaseDto[];
  hiddenTestcases?: TestCaseDto[];
  metadataList: QuestionMetadataDto[];
  referenceSolution?: ReferenceSolutionDto;
  nodeType?: NodeType;
  isOutputOrderMatters?: boolean;
  tags?: { name: string }[];
  difficultyLevel?: Difficulty;
  company?: string;
  constraints?: string;
  timeoutLimit?: number;
  solution?: { code: string; language: string }[];
}

interface TagResponseDto {
  id: number;
  name: string;
  description: string;
}

interface SolutionResponseDto {
  id: number;
  code: string;
  language: string;
  explanation?: string;
  questionId: number;
}
```

---

**Document Version:** 2.0.0  
**Service Contact:** Problem Service Team
