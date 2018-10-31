# pipedev

This is the public workflow repository for the Pipeline Development and Evaluation (PDE) Working Group at [Ontario Institute for Cancer Research](http://www.oicr.on.ca) (OICR).

The PDE has two primary mandates:

* Develop [SeqWare](http://seqware.io) workflows and pipelines for the Sequencing Production Group
* Evaluate and verify software and pipelines that will be used by Sequencing Production Bioinformatics

This repository houses the open-source workflows, deciders and utilities that are written and maintained by our group.

## Binaries

Our utilities are available from our Maven repository.

* Releases: [gsi-release](https://artifacts.oicr.on.ca/artifactory/simple/gsi-release)
* Nightly snapshots: [gsi-snapshots](https://artifacts.oicr.on.ca/artifactory/simple/gsi-snapshots)

## Build

To build our utilities, use Maven.

```bash
mvn clean install
```

Build with integration tests (Postgres DB 9.1+ required, user should be able to create and drop its own DBs, and password should be specified in ~/.pgpass):
```bash
mvn clean install -DskipITs=false \
-DdbHost=<postgres db host> \
-DdbPort=<postgres db port> \
-DdbUser=<postgres user name> \
-DdbPassword=<postgres password>
```

## Usage

### pipedev-workflow-utils
Simplifies building of SeqWare workflows by providing:

- helper methods to create jobs, provision input or output files, and safely get ini properties
- unwrapping "ReturnValue" error states into an exception

Add pipedev-workflow-utils as a maven dependency:
```html
<dependency>
    <groupId>ca.on.oicr.gsi</groupId>
    <artifactId>pipedev-workflow-utils</artifactId>
    <version>2.3</version>
</dependency>
```

Modify your workflow class to extend ```OicrWorkflow``` (instead of ```AbstractWorkflowDataModel```).

Review [OicrWorkflow javadoc]() for documentation.

### pipedev-decider-utils
Simplifies building of SeqWare java deciders by providing:

- helper methods to simplify command line execution and get file attributes
- wrapping "ReturnValue" attributes and FileMetadata into a single FileAttribute object

Add pipedev-decider-utils as a maven dependency:
```html
<dependency>
    <groupId>ca.on.oicr.gsi</groupId>
    <artifactId>pipedev-decider-utils</artifactId>
    <version>2.3</version>
</dependency>
```

Modify your workflow class to extend ```OicrDecider``` (instead of ```BasicDecider```).

Review [OicrDecider javadoc]() for documentation.

### pipedev-test-utils
Provides unit and integration testing utilities such as:

- a common procedure to test run workflow and decider projects
- mocking of SeqWare infrastructure for debugging or unit testing
- helper methods to interact with SeqWare

Add pipedev-test-utils as a maven dependency:
```html
<dependency>
    <groupId>ca.on.oicr.gsi</groupId>
    <artifactId>pipedev-test-utils</artifactId>
    <version>2.3</version>
    <scope>test</scope>
</dependency>
```

Review the [pipedev wiki](https://github.com/oicr-gsi/pipedev/wiki) for documentation.


### Common maven configuration (parent pom)

Workflow projects or decider projects share a very similar maven configuration. To reduce configuration duplication and maintenance, common configuration can be moved to a parent pom.

PDE's [workflow parent pom](pipedev-configs/pipedev-workflow-parent/pom.xml) can be used as a template or can be used directly by adding the following to your workflow's pom.xml:

```html
<parent>
    <groupId>ca.on.oicr.gsi</groupId>
    <artifactId>pipedev-workflow-parent</artifactId>
    <version>2.3</version>
    <relativePath/>
</parent>
```

PDE's [decider parent pom](configs/decider-parent/pom.xml) can be used as a template or can be used directly by adding the following to your decider's pom.xml:

```html
<parent>
    <groupId>ca.on.oicr.gsi</groupId>
    <artifactId>pipedev-decider-parent</artifactId>
    <version>2.3</version>
    <relativePath/>
</parent>
```

To review your project's pom, execute: ```mvn help:effective-pom```
