import groovy.json.*

def utils

def init(){
  utils = fileLoader.fromGit('jenkins_pipeline_tools/utils',
    'https://github.hpe.com/ChatOps/tools.git', 'master',
    null, '')
}

def cleanup(){
  stage 'Clean workspace'
  deleteDir()
  sh 'ls -lah'
}

def co_source(){
  stage 'Checkout source'
  checkout scm
  def version = utils.get_version ('package.json')
  echo "version ${version}"
  def changeUrl =
    utils.check_pr(env) ? "\nChange URL: ${env.CHANGE_URL}" : "";
  return ["changeUrl": changeUrl, "version": version]
}

def build(){
  stage 'build'
  sh 'npm install'
  stage 'linter'
  sh 'npm run coffeelint'
  sh 'npm run jslint'
  step([$class: 'WarningsPublisher', canComputeNew: false,
    canResolveRelativePaths: false, defaultEncoding: '', excludePattern: '',
    healthy: '', includePattern: '', messagesPattern: '',
    parserConfigurations: [[parserName: 'JSLint', pattern: '*lint.xml']],
    unHealthy: ''])
}

def test(){
  stage 'test'
  sh 'npm test || true'
  step([$class: 'JUnitResultArchiver', testResults: 'test/xunit.xml'])
}

def package_and_release(version){
  stage 'package'
  def changes = utils.get_tags_diff()
  sh "git tag v${version}" // -F ${changes}"
  sshagent(['github-ssh']) { sh "git -c core.askpass=true push --tags" }
  def gconf = utils.guess_github_settings()
  utils.create_version_json (gconf.api, gconf.project, gconf.cred,
    "${version}", "v${version}", true, readFile(changes))
  echo 'done!'
  return readFile(changes)
}

return this;
