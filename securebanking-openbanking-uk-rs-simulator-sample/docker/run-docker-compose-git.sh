#!/bin/sh
#
# Copyright © 2020 ForgeRock AS (obst@forgerock.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

BASEDIR=$(dirname $0)
echo "Script location: ${BASEDIR}"

export GIT_SSH_KEY="$(cat ~/.ssh/id_rsa)"

echo "Shutting down previous containers (if applicable)"
if [ ! -z "$1" ]
then
  docker-compose -f ${BASEDIR}/docker-compose-git.yml down "$1"
else
  docker-compose -f ${BASEDIR}/docker-compose-git.yml down
fi
echo ""

echo "Starting up containers...$1".
if [ ! -z "$1" ]
then
  docker-compose -f ${BASEDIR}/docker-compose-git.yml up "$1"
else
  docker-compose -f ${BASEDIR}/docker-compose-git.yml up
fi
