package uk.gov.justice.digital.hmpps.keyworker.integration.mockResponses

class CaseNoteUsageListStub {

    static getResponse() {
        def response = """
[
    {
        "staffId": -5,
        "caseNoteType": "KA",
        "caseNoteSubType": "KS",
        "latestCaseNote": "2018-07-01",
        "numCaseNotes": 3
    },
    {
        "staffId": -4,
        "caseNoteType": "KA",
        "caseNoteSubType": "KE",
        "latestCaseNote": "2018-06-30",
        "numCaseNotes": 2
    },
    {
        "staffId": -5,
        "caseNoteType": "KA",
        "caseNoteSubType": "KS",
        "latestCaseNote": "2018-07-02",
        "numCaseNotes": 1
    },
    {
        "staffId": -4,
        "caseNoteType": "KA",
        "caseNoteSubType": "KE",
        "latestCaseNote": "2018-07-01",
        "numCaseNotes": 6
    }
]
"""
        return response
    }
}
