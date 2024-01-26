# Test Triage at Adoptium

## Triage Guidance

There are many different test jobs running at the adoptium project.  No matter which test jobs are being triaged, there is a straight-forward pattern to follow.

### Categorize the test failure (based on test output) into 1 of 3 general types

- infra problem (machine/network)
- test problem
- product problem

### Check for an existing issue

- if issue already exists for the failure, annotate with additional information if needed.  For example, if you are investigating failures in openjdk tests, look to see if its already reported in JBS, as per instructions in [Guidance for creating OpenJDK bugs](https://github.com/adoptium/aqa-tests/wiki/Guidance-for-Creating-OpenJDK-Test-Defects).

### Raise an issue if no issue exists

- infra issue - raise an issue in [infrastructure](https://github.com/adoptium/infrastructure/issues)
- test issue - ideally, there are enough details to determine which test repo to raise an issue in ond of the test repositories from which test material is pulled (OpenJDK, [aqa-systemtest](https://github.com/adoptium/aqa-systemtest/issues), [aqa-tests](https://github.com/adoptium/aqa-tests/issues) or any of the various 3rd party application suites).  If in doubt, ask some questions in the [#testing channel](https://adoptium.slack.com/archives/C5219G28G) and/or raise in [aqa-tests](https://github.com/adoptium/aqa-tests/issues) where it will get routed to proper repo
  - OpenJDK test issues - see [Guidance for creating OpenJDK bugs](https://github.com/adoptium/aqa-tests/wiki/Guidance-for-Creating-OpenJDK-Test-Defects)
  - Additional guidance for external tests - [Triage Rules for Application tests](https://github.com/adoptium/aqa-tests/tree/master/external#triage-rules)
- product issue - additional steps may be necessary, before raising an issue
  - rerun the test - locally or using a Grinder: see [How to Run a Grinder wiki](https://github.com/adoptium/aqa-tests/wiki/How-to-Run-a-Grinder-Build-on-Jenkins)
  - determine if the problem is occurs in other jdk versions, implementations and on other platforms
  - if only observed against 1 implementation, raise an issue against that implementation in the correct upstream repo (typically either OpenJDK or [Eclipse OpenJ9](https://github.com/eclipse-openj9/openj9/issues) projects).

### Exclude the test

- exclude in the most minimal way possible, if failing only on 1 platform, version or implementation, only exclude for that instance
- put the full link to the associated open issue into the exclude file
- exclude files vary depending on what test group you are triaging, refer to the README files in the aqa-tests subdirectories for more details
  - for openjdk tests, see [Exclude an openjdk test](https://github.com/adoptium/aqa-tests/tree/master/openjdk#exclude-a-testcase)
  - for other tests (like system, external and perf tests), tests are typically disabled via the associated playlist.xml (see [example playlist](https://github.com/adoptium/aqa-tests/blob/master/external/example-test/playlist.xml)) file either by using `<platformRequirements>^os.win</platformRequirements>` for permanent exclusion based on platform, or `<disable>` tag for temporary exclusion.

![Common Triage Paths](./diagrams/commonTriagePaths.png)

```mermaid
flowchart TD
    A(Categorize) --> B(Product)
    A(Categorize) --> C(Test)
    A(Categorize) --> D(Infra)
    A(Categorize) --> E(Unknown)
    B ----> F(Issue exists?)
    C ----> F(Issue exists?)
    D ----> F(Issue exists?)
    L --[suspect infra]--> G(Deep History / Machines)
    L --[suspect prod/test]--> H(Upstream Issue)
    L --[no clue]--> I(Other Vendor Behaviour)
    E ----> L(Apply Methods of Determination)
    F --[no]--> J(Raise Issue)
    F --[yes]--> K(Update Existing Issue)
    J --[release triage]--> M(Blocking or Non-blocking Determination by PMC)
    I ----> N(Determination Complete)
    G ----> N(Determination Complete)
    H ----> N(Determination Complete)
    N ----> A(Categorize)
```

## Generate a Release Summary Report

This is a useful tool when trying to triage a full release.  

Locate the pipeline you wish to triage in the ["By Pipeline" view](https://trss.adoptium.net/tests/Test) of TRSS.

![image](https://user-images.githubusercontent.com/93431609/216171285-c313c957-6c8e-49df-95dd-6a68a9897f90.png)

Navigate to the Grid view by clicking on the Grid link for the pipeline you wish to triage and click on the Release Summary Report button at the top of the Grid view page.

![image](https://user-images.githubusercontent.com/93431609/216171387-abebba1d-0ff7-46a9-b486-8aba8fc4bcbc.png)

Click on the 'Copy` icon and paste the .md formatted content into a new Github issue that you wish to use to track triage investigation.

![image](https://user-images.githubusercontent.com/93431609/216171428-705c1933-eea4-4598-a9f2-e488c229bab0.png)
