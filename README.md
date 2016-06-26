# he-jenkins-ci
Jenkins pipeline scripts for hubot enterprise CI/CD

## what it is?
groovy script for Jenkins 2.0 pipelines to use, purposed for hubot-enterprise plugins (but not just)

## what is does?
Running a pipeline and report back to github+ slack channel the status of a commit/pull request

## prerequisites
### Jenkins core and plugins:
1. Jenkins 2.X
2. Pipeline plugin
3. Github Organization plugin
4. AnsiColor
5. JUnit Plugin
6. Timestamper
7. Warnings Plug-in
8. Slack plugin
# configurations:
1. configure global slack plugin
2. Credentials- To use this pipeline you must have the following credentials id installed:
  1. github-token: username+password token for github.com hosted projects [token should have `repo` credentials]
  2. github-enterprise-token: username+password token for github enterprise hosted projects [token should have `repo` credentials]
  3. github ssh key

### Jenkins slave executor
1. nodejs
2. npm
3. coffee-script
4. jshint
5. coffeelint

## current pipelines:
1. hubot integration: CI for single hubot integration
  Flow:
    - Check out
    - Build
    - lint (coffee+ javascript)
    - unit test (mocha) (Pipeline *can* fail on unit tests failures)
    - if its on master branch and there is no such version (no tags name `v${version}`- checked `package.json` vs git tags)
      - create new tag named `v${version}`
      - use github api to create new `version` from this tag- make release notes from commit diff between current and previous tags

## usage:
1. Jopy Jenkinsfile.example to your project root folder edit `pipeline.runPipeline(true/false)`, based on your desire to fail the job on failed unit tests
2. create jenkins Github Organization folder job and point to your organization
