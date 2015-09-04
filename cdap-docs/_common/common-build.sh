# Copyright © 2014-2015 Cask Data, Inc.
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
  
# Common code for Build script for docs
# Not called directly; included in either the main build.sh or the individual manual's build.sh

# Optional Parameters (passed via Bamboo env variable or exported in shell):
# BELL: Set it to for the bell function to make a sound when called
# COLOR_LOGS: Set it for color output by Sphinx and these scripts

API="cdap-api"
APIDOCS="apidocs"
APIS="apis"
BUILD_PDF="build-pdf"
CDAP_DOCS="cdap-docs"
HTML="html"
INCLUDES="_includes"
JAVADOCS="javadocs"
LICENSES="licenses"
LICENSES_PDF="licenses-pdf"
PROJECT="cdap"
PROJECT_CAPS="CDAP"
REFERENCE="reference-manual"
SOURCE="source"
SPHINX_MESSAGES="warnings.txt"
TARGET="target"

FALSE="false"
TRUE="true"

# Redirect placed in top to redirect to 'en' directory
REDIRECT_EN_HTML=`cat <<EOF
<!DOCTYPE HTML>
<html lang="en-US">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="refresh" content="0;url=en/index.html">
        <script type="text/javascript">
            window.location.href = "en/index.html"
        </script>
        <title></title>
    </head>
    <body>
    </body>
</html>
EOF`

SCRIPT=`basename ${0}`
SCRIPT_PATH=`pwd`
MANUAL=`basename ${SCRIPT_PATH}`

DOC_GEN_PY="${SCRIPT_PATH}/../tools/doc-gen.py"
TARGET_PATH="${SCRIPT_PATH}/${TARGET}"
SOURCE_PATH="${SCRIPT_PATH}/${SOURCE}"

if [ "x${2}" == "x" ]; then
  PROJECT_PATH="${SCRIPT_PATH}/../../"
else
  PROJECT_PATH="${SCRIPT_PATH}/../../../${2}"
fi

API_JAVADOCS="${PROJECT_PATH}/target/site/${APIDOCS}"

CHECK_INCLUDES="false"

if [[ "x${COLOR_LOGS}" != "x" ]]; then
  SPHINX_COLOR=""
  RED="$(tput setaf 1)"
  BOLD="$(tput bold)"
  RED_BOLD="$(tput setaf 1; tput bold)"
  NO_COLOR="$(tput setaf 0; tput sgr0)"
else
  SPHINX_COLOR="-N"
  RED_BOLD=''
  RED=''
  BOLD=''
  NO_COLOR=''
fi
WARNING="${RED_BOLD}WARNING:${NO_COLOR}"
SPHINX_BUILD="sphinx-build ${SPHINX_COLOR} -b html -d ${TARGET}/doctrees"

# Hash of file with "Not Found"; returned by GitHub
NOT_FOUND_HASH="9d1ead73e678fa2f51a70a933b0bf017"

ZIP_FILE_NAME=$HTML
ZIP="${ZIP_FILE_NAME}.zip"

# Set Google Analytics Codes

# Corporate Docs Code
GOOGLE_ANALYTICS_WEB="UA-55077523-3"
WEB="web"

# CDAP Project Code
GOOGLE_ANALYTICS_GITHUB="UA-55081520-2"
GITHUB="github"

# BUILD.rst
BUILD_RST="BUILD.rst"
BUILD_RST_HASH="f54ae74bb72f9ad894766b6c0bd2d2df"


function usage() {
  echo "Build script for '${PROJECT_CAPS}' docs"
  echo "Usage: ${SCRIPT} <action> [source]"
  echo
  echo "  Action (select one)"
  echo "    build                Clean build of javadocs and HTML docs, copy javadocs and PDFs into place, zip results"
  echo "    build-github         Clean build and zip for placing on GitHub"
  echo "    build-web            Clean build and zip for placing on docs.cask.co webserver"
  echo "    build-docs           Clean build of docs"
  echo
  echo "    license-pdfs         Clean build of License Dependency PDFs"
  echo "    check-includes       Check if included files have changed from source"
  echo "    display-version      Print the version information"
  echo "  with"
  echo "    source               Path to $PROJECT source for javadocs, if not '$PROJECT_PATH'"
  echo "                         Path is relative to '${SCRIPT_PATH}/../..'"
  echo
}

function echo_red_bold() {
  echo "${RED_BOLD}${1}${NO_COLOR}${2}"
}

function echo_clean_colors() {
  local c="${1//${RED}/}"
  c="${c//${BOLD}/}"
  c="${c//${RED_BOLD}/}"
  c="${c//${NO_COLOR}/}"
  echo "${c}"
}

function clean() {
  cd ${SCRIPT_PATH}
  rm -rf ${SCRIPT_PATH}/${TARGET}
  mkdir -p ${SCRIPT_PATH}/${TARGET}
  echo "Cleaned ${SCRIPT_PATH}/${TARGET} directory"
  echo
}

function build_docs() {
  clean
  cd ${SCRIPT_PATH}
  check_includes
  ${SPHINX_BUILD} -w ${TARGET}/${SPHINX_MESSAGES} ${SOURCE} ${TARGET}/html
  consolidate_messages
}

function build_docs_google() {
  clean
  cd ${SCRIPT_PATH}
  check_includes
  ${SPHINX_BUILD} -w ${TARGET}/${SPHINX_MESSAGES} -D googleanalytics_id=$1 -D googleanalytics_enabled=1 ${SOURCE} ${TARGET}/html
  consolidate_messages
}

function build_license_pdfs() {
  set_version
  cd ${SCRIPT_PATH}
  PROJECT_VERSION_TRIMMED=${PROJECT_VERSION%%-SNAPSHOT*}
  rm -rf ${SCRIPT_PATH}/${LICENSES_PDF}
  mkdir ${SCRIPT_PATH}/${LICENSES_PDF}
  # paths are relative to location of $DOC_GEN_PY script
  LIC_PDF="../../../${REFERENCE}/${LICENSES_PDF}"
  LIC_RST="../${REFERENCE}/source/${LICENSES}"
  PDFS="cdap-enterprise-dependencies cdap-level-1-dependencies cdap-standalone-dependencies cdap-ui-dependencies"
  for PDF in ${PDFS}; do
    echo
    echo "Building ${PDF}"
    python ${DOC_GEN_PY} -g pdf -o ${LIC_PDF}/${PDF}.pdf -b ${PROJECT_VERSION_TRIMMED} ${LIC_RST}/${PDF}.rst
  done
}

function copy_license_pdfs() {
  cp ${SCRIPT_PATH}/${LICENSES_PDF}/* ${TARGET_PATH}/${HTML}/${LICENSES}
}

function make_zip() {
  set_version
  if [ "x${1}" == "x" ]; then
    ZIP_DIR_NAME="${PROJECT}-docs-${PROJECT_VERSION}"
  else
    ZIP_DIR_NAME="${PROJECT}-docs-${PROJECT_VERSION}-$1"
  fi
  cd ${TARGET_PATH}
  mkdir ${PROJECT_VERSION}
  mv ${HTML} ${PROJECT_VERSION}/en
  # Add a redirect index.html file
  echo "${REDIRECT_EN_HTML}" > ${PROJECT_VERSION}/index.html
  # Zip everything
  zip -qr ${ZIP_DIR_NAME}.zip ${PROJECT_VERSION}/* --exclude *.DS_Store* *.buildinfo*
}

function build() {
  build_docs
  build_extras
}

function build_github() {
  build_docs_google ${GOOGLE_ANALYTICS_GITHUB}
  build_extras
}

function build_web() {
  build_docs_google ${GOOGLE_ANALYTICS_WEB}
  build_extras
}

function build_extras() {
  # Over-ride this function in guides where Javadocs or licenses are being built or copied.
  # Currently performed in reference-manual
  echo "No extras being built or copied."
}

function set_mvn_environment() {
  cd ${PROJECT_PATH}
  if [[ "${OSTYPE}" == "darwin"* ]]; then
    # TODO: hard-coded Java version 1.7
    export JAVA_HOME=$(/usr/libexec/java_home -v 1.7)
  fi
  # check BUILD.rst for changes
  BUILD_RST_PATH="${PROJECT_PATH}/${BUILD_RST}"
  test_an_include ${BUILD_RST_HASH} ${BUILD_RST_PATH}
}

function check_includes() {
  if [ !"${CHECK_INCLUDES}" ]; then
    echo_red_bold "Downloading and checking files to be included."
    # Build includes
    local target_includes_dir=${SCRIPT_PATH}/${TARGET}/${INCLUDES}
    rm -rf ${target_includes_dir}
    mkdir ${target_includes_dir}
    download_includes ${target_includes_dir}
    test_includes
  else
    echo "No includes to be checked."
  fi
}

function download_includes() {
  # $1 is passed as the directory to which the downloaded files are to be written.
  # For an example of over-riding this function, see developer/build.sh
  echo "No includes to be downloaded."
}

function test_includes() {
  # For an example of over-riding this function, see developer/build.sh
  echo "No includes to be tested."
}

function test_an_include() {
  # Tests a file and checks that it hasn't changed.
  # Uses md5 hashes to monitor if any files have changed.
  local md5_hash=${1}
  local target=${2}
  local new_md5_hash
  
  local file_name=`basename ${target}`
  
  if [[ "${OSTYPE}" == "darwin"* ]]; then
    new_md5_hash=`md5 -q ${target}`
  else
    new_md5_hash=`md5sum ${target} | awk '{print $1}'`
  fi
  
  local m
  local m_display
  
  if [[ "${new_md5_hash}" == "${NOT_FOUND_HASH}" ]]; then
    m="${WARNING} ${RED_BOLD}${file_name} not found!${NO_COLOR} "  
    m="${m}\nfile: ${target}"  
  elif [[ "${new_md5_hash}" != "${md5_hash}" ]]; then
    m="${WARNING} ${RED_BOLD}${file_name} has changed! Compare files and update hash!${NO_COLOR} "   
    m="${m}\nfile: ${target}"   
    m="${m}\nOld MD5 Hash: ${md5_hash} New MD5 Hash: ${new_md5_hash}"   
  fi
  if [ "x${m}" != "x" ]; then
    set_message "${m}"
  else
    m="MD5 Hash for ${file_name} matches"
  fi
  echo "${m}"
}

function set_version() {
  OIFS="${IFS}"
  local current_directory=`pwd`
  cd ${PROJECT_PATH}
  PROJECT_VERSION=`grep "<version>" pom.xml`
  PROJECT_VERSION=${PROJECT_VERSION#*<version>}
  PROJECT_VERSION=${PROJECT_VERSION%%</version>*}
  PROJECT_LONG_VERSION=`expr "${PROJECT_VERSION}" : '\([0-9]*\.[0-9]*\.[0-9]*\)'`
  PROJECT_SHORT_VERSION=`expr "${PROJECT_VERSION}" : '\([0-9]*\.[0-9]*\)'`
  local full_branch=`git rev-parse --abbrev-ref HEAD`
  IFS=/ read -a branch <<< "${full_branch}"
  GIT_BRANCH="${branch[1]}"
  GIT_BRANCH_PARENT="develop"
  # Determine branch and branch type: one of develop, master, release, develop-feature, release-feature
  # If unable to determine type, uses develop-feature
  if [ "${full_branch}" == "develop" -o  "${full_branch}" == "master" ]; then
    GIT_BRANCH="${full_branch}"
    GIT_BRANCH_TYPE=${GIT_BRANCH}
  elif [ "${GIT_BRANCH:0:7}" == "release" ]; then
    GIT_BRANCH_TYPE="release"
  else
    # We are on a feature branch: but from develop or release?
    # This is not easy to determine. This can fail very easily.
    local git_branch_listing=`git show-branch | grep '*' | grep -v "$(git rev-parse --abbrev-ref HEAD)" | head -n1`
    if [ "x${git_branch_listing}" == "x" ]; then 
      echo_red_bold "Unable to determine parent branch as git_branch_listing empty; perhaps in a new branch with no commits"
      echo_red_bold "Using default GIT_BRANCH_PARENT: ${GIT_BRANCH_PARENT}"
    else
      GIT_BRANCH_PARENT=`echo ${git_branch_listing} | sed 's/.*\[\(.*\)\].*/\1/' | sed 's/[\^~].*//'`
    fi
    if [ "${GIT_BRANCH_PARENT:0:7}" == "release" ]; then
      GIT_BRANCH_TYPE="release-feature"
    else
      GIT_BRANCH_TYPE="develop-feature"
    fi
  fi
  cd ${current_directory}
  IFS="${OIFS}"
}

function display_version() {
  set_version
  echo "PROJECT_PATH: ${PROJECT_PATH}"
  echo "PROJECT_VERSION: ${PROJECT_VERSION}"
  echo "PROJECT_LONG_VERSION: ${PROJECT_LONG_VERSION}"
  echo "PROJECT_SHORT_VERSION: ${PROJECT_SHORT_VERSION}"
  echo "GIT_BRANCH: ${GIT_BRANCH}"
  echo "GIT_BRANCH_TYPE: ${GIT_BRANCH_TYPE}"
  echo "GIT_BRANCH_PARENT: ${GIT_BRANCH_PARENT}"
}

function clear_messages_set_messages_file() {
  unset -v MESSAGES
  TMP_MESSAGES_FILE="${TARGET_PATH}/.$(basename $0).$$.messages"
  cat /dev/null > ${TMP_MESSAGES_FILE}
  export TMP_MESSAGES_FILE
  echo_red_bold "Cleared Messages and Messages file: " "${TMP_MESSAGES_FILE}"
  echo
}

function cleanup_messages_file() {
  rm -f ${TMP_MESSAGES_FILE}
  unset -v TMP_MESSAGES_FILE
}

function set_message() {
  if [ "x${MESSAGES}" == "x" ]; then
    MESSAGES=${*}
  else
    MESSAGES="${MESSAGES}\n\n${*}"
  fi
  if [ "x${TMP_MESSAGES_FILE}" != "x" ]; then
    local clean_m=`echo_clean_colors "${*}"`
    if [ -e ${TMP_MESSAGES_FILE} ]; then
      # As TMP_MESSAGES_FILE exists, add a blank line to start new message
      echo >> ${TMP_MESSAGES_FILE}
    fi
    echo "Warning Message for \"${MANUAL}\":" >> ${TMP_MESSAGES_FILE}
    echo "${clean_m}" >> ${TMP_MESSAGES_FILE}
  fi
}

function consolidate_messages() {
  local m="Warning Messages for \"${MANUAL}\":"
  if [ "x${MESSAGES}" != "x" ]; then
    echo_red_bold "Consolidating messages" 
    echo_red_bold "${m}" >> ${TMP_MESSAGES_FILE}
    echo "${MESSAGES}" >> ${TMP_MESSAGES_FILE}
#     while read line
#     do
#       echo ${line} >> ${TMP_MESSAGES_FILE}
#     done <<(echo "${MESSAGES}")
    unset -v MESSAGES
  fi
  if [ -s ${TARGET}/${SPHINX_MESSAGES} ]; then
    echo_red_bold "Consolidating Sphinx messages" 
    m="Sphinx ${m}"
    echo_red_bold "${m}" >> ${TMP_MESSAGES_FILE}
    cat ${TARGET}/${SPHINX_MESSAGES} | while read line
    do
      echo ${line} >> ${TMP_MESSAGES_FILE}
    done
  fi
}

function display_messages_file() {
  if [[ "x${TMP_MESSAGES_FILE}" != "x" && -a ${TMP_MESSAGES_FILE} ]]; then
    echo 
    echo "--------------------------------------------------------"
    echo_red_bold "Warning Messages: ${TMP_MESSAGES_FILE}"
    echo "--------------------------------------------------------"
    echo 
    echo >> ${TMP_MESSAGES_FILE}
    cat ${TMP_MESSAGES_FILE} | while read line
    do
      echo "${line}"
    done
    return 1 # Indicates warning messages present
  else
    return 0
  fi
}

function rewrite() {
  # Substitutes text in file $1 and outputting to file $2, replacing text $3 with text $4
  # or if $4=="", substitutes text in-place in file $1, replacing text $2 with text $3
  # or if $3 & $4=="", substitutes text in-place in file $1, using sed command $2
  cd ${SCRIPT_PATH}
  local rewrite_source=${1}
  echo "Re-writing"
  echo "    $rewrite_source"
  if [ "x${3}" == "x" ]; then
    local sub_string=${2}
    echo "    $sub_string"
    if [ "$(uname)" == "Darwin" ]; then
      sed -i ".bak" "${sub_string}" ${rewrite_source}
      rm ${rewrite_source}.bak
    else
      sed -i "${sub_string}" ${rewrite_source}
    fi
  elif [ "x${4}" == "x" ]; then
    local sub_string=${2}
    local new_sub_string=${3}
    echo "    ${sub_string} -> ${new_sub_string} "
    if [ "$(uname)" == "Darwin" ]; then
      sed -i ".bak" "s|${sub_string}|${new_sub_string}|g" ${rewrite_source}
      rm ${rewrite_source}.bak
    else
      sed -i "s|${sub_string}|${new_sub_string}|g" ${rewrite_source}
    fi
  else
    local rewrite_target=${2}
    local sub_string=${3}
    local new_sub_string=${4}
    echo "  to"
    echo "    ${rewrite_target}"
    echo "    ${sub_string} -> ${new_sub_string} "
    sed -e "s|${sub_string}|${new_sub_string}|g" ${rewrite_source} > ${rewrite_target}
  fi
}

function run_command() {
  case ${1} in
    build|build-github|build-web|build-docs)      "${1/-/_}";;
    check-includes|display-version|license-pdfs)  "${1/-/_}";;
    *)                                           usage;;
  esac
}
