Feature: Questions API Performance and Functional Testing

  Background:
    * url 'http://localhost:8080'
    * def basePath = '/api/v1/questions'

  Scenario: Create a Question
    Given path basePath
    And request
      """
      {
        "questionTitle": "Two Sum",
        "questionDescription": "Find two numbers that add up to target.",
        "isOutputOrderMatters": false,
        "difficultyLevel": "EASY",
        "company": "Google",
        "constraints": "1 <= nums.length <= 10^4",
        "tags": [ { "name": "array" }, { "name": "hashmap" } ],
        "timeoutLimit": 2
      }
      """
    When method POST
    Then status 201
    And match $.questionId != null
    And match $.message == "Question created successfully"

  Scenario: Get Question by ID
    Given path basePath + '/1'
    When method GET
    Then status 200
    And match $.id == 1

  Scenario: Update Question
    Given path basePath + '/1'
    And request
      """
      {
        "questionTitle": "Two Sum Updated",
        "questionDescription": "Updated description",
        "difficultyLevel": "MEDIUM"
      }
      """
    When method PUT
    Then status 200
    And match $.questionTitle == "Two Sum Updated"

  Scenario: Delete Question
    Given path basePath + '/1'
    When method DELETE
    Then status 200
    And match $ == 'Question deleted successfully.'
