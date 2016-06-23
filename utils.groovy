import groovy.json.*

def get_version ( fname ) {
  def jsonText = readFile(fname)
  def json = new JsonSlurper().parseText(jsonText)
  return json.version
}

def get_tag_version (){
  sh "git describe --tags `git rev-list --tags "+
    "--max-count=1` | sed s/^v//g | "+
    "sed s/-.*//g > ver.txt"
  return readFile('ver.txt').trim()
}

def get_tags_diff () {
  try {
    sh "git describe --tags"
    sh 'git log --no-merges $(git describe --tags '+
    '`git rev-list --tags --max-count=1`)..HEAD '+
    '--pretty=\'tformat:- %s\' > changes.txt'
  } catch (e) {
    sh 'git log --no-merges '+
    '--pretty=\'tformat:- %s\' > changes.txt'
  }
  return 'changes.txt'
}

def check_pr ( env_vars ) {
  return env_vars.CHANGE_TARGET ? true : false
}

def guess_github_settings () {
  sh 'git remote -v | grep origin | head -1 | '+
    'awk \'{print $2}\' | '+
    'sed \'s/git@//g;s/\\.git//g\' > guess'
  def str = readFile('guess').trim().split(':')
  def ret = [api: '', project: str[1], cred: '']
  if (str[0] == 'github.com')
  {
    ret.api = 'https://api.'+str[0]
    ret.cred = 'github-token'
  } else {
    ret.api =  'https://'+str[0]+'/api/v3'
    ret.cred = 'github-enterprise-token'
  }
  println 'Project: '+ret.api+' '+ret.cred+' '+
    ret.project
  return ret
}

def create_version_json (url, path, auth, version_name,
  version_tag, prerelease, changes) {
  echo 'creating new github version'
  def obj = [tag_name: version_tag, name: version_name,
    body: changes, draft: false, prerelease: prerelease]
  def json = JsonOutput.toJson(obj)
  println "Sending to github:\n${json}"
  withCredentials([[$class:
    'UsernamePasswordMultiBinding', credentialsId: auth,
    passwordVariable: 'PW', usernameVariable: 'UN']]) {
      sh "curl -H \'Authorization: token "+env.PW+"\' "+
        "--request POST --data \'"+json+"\' "+
        url+"/repos/"+path+"/releases > out"
  }
  def resFile = readFile('out').trim()
  def res = new JsonSlurper().parseText(resFile)
  echo "result:\n"+resFile
}

return this;
