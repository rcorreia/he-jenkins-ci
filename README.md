# he-jenkins-ci
Jenkins pipeline scripts for hubot enterprise CI/CD

# prerequisites
1. Jenkins 2.X
2. Pipeline plugin
3. Github Organization plugin
4. AnsiColor
5. JUnit Plugin
6. Timestamper
7. Warnings Plug-in

# Jenkinsfile in your project:
To use the full pipeline add `Jenkinsfile` to your project root:
```groovy
def pipeline = fileLoader.fromGit('integration-flow',
    'https://github.hpe.com/eedevops/he-jenkins-ci.git', 'master', null, '')

pipeline.runPipeline()
```

```groovy
pipeline.runPipeline(FailOnTest = true) // set to false for pipeline not fail in tests
```

# credentials
To use this pipeline you must have the following credentials id installed:

1. github-token: username+password token for github.com hosted projects
2. github-enterprise-token: username+password token for github enterprise hosted projects

# usage:
1. create jenkins Github Organization folder job and point to your organization
