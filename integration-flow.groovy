/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * Software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License. 
 */

import groovy.json.*

def wraps(body) {
    wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm',
      'defaultFg': 1, 'defaultBg': 2]) {
        wrap([$class: 'TimestamperBuildWrapper']) {
            body()
        }
    }
}

def runPipeline(FailOnTest = true) {
  def stages, utils, changeUrl, version
  fileLoader.withGit('https://github.hpe.com/ChatOps/tools.git',
    'master', null, '') {
    stages = fileLoader.load('jenkins_pipeline_tools/stages');
    utils = fileLoader.load('jenkins_pipeline_tools/utils');
  }
  node {
      wraps {
          try{
              stages.init()
              stages.cleanup()
              def info = stages.co_source()
              changeUrl = info["changeUrl"]
              version = info["version"]
              stages.build()
              stages.test()
              if (currentBuild.result != 'UNSTABLE' || !FailOnTest)
              {
                  def tag_version = utils.get_tag_version()
                  echo "latest tag version: "+tag_version
                  echo 'versions: ['+version +'] ['+tag_version+']'
                  if (!utils.check_pr(env) && (version != tag_version) &&
                    (env.BRANCH_NAME == 'master'))
                  {
                      def release_notes = stages.package_and_release(version)
                      slackSend color: 'good', message: "New version "+
                        "Preprleased: ${env.JOB_NAME} ${env.BUILD_NUMBER}\n"+
                        "Version: ${version}\nRelease Notes:\n"+
                        "${release_notes}\n${env.BUILD_URL}${changeUrl}"
                  } else if (utils.check_pr(env)) {
                      echo 'it\'s a pull request, not tagging'
                  } else if (env.BRANCH_NAME != 'master') {
                      echo 'not creating release for non master branches'
                  } else {
                      echo "version is the same, not tagging"
                  }
                  slackSend color: 'good', message: "Build done: "+
                    "${env.JOB_NAME} ${env.BUILD_NUMBER}\n"+
                    "${env.BUILD_URL}${changeUrl}"
              } else {
                  slackSend color: 'warning', message: "Unit tests failed: "+
                    "${env.JOB_NAME} ${env.BUILD_NUMBER}\n"+
                    "${env.BUILD_URL}${changeUrl}"
              }
              step([$class: 'GitHubCommitStatusSetter', statusResultSource:
                [$class: 'ConditionalStatusResultSource', results: []]])
          } catch (e) {
              echo "Exception: ${e}"
              slackSend color: 'danger', message: "Job failed: ${env.JOB_NAME}"+
                " ${env.BUILD_NUMBER}\n${env.BUILD_URL}${changeUrl}"
              currentBuild.result='FAILED'
              step([$class: 'GitHubCommitStatusSetter', statusResultSource:
                [$class: 'ConditionalStatusResultSource', results: []]])
              error "${e}"
          }
  	echo "status: [${currentBuild.result}]"
      }
  }
}

return this;
